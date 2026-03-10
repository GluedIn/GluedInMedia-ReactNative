package com.test.shopify;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.shopify.buy3.GraphCallResult;
import com.shopify.buy3.GraphClient;
import com.shopify.buy3.Storefront;
import com.shopify.graphql.support.ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import kotlin.Result;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import timber.log.Timber;

/**
 * Fully Converted Shopify Manager for Java.
 * Handles Cart logic, Product fetching, and Authentication flow.
 */
@SuppressLint("StaticFieldLeak")
public class ShopifyCartManager {

    private static final String TAG = "ShopifyCartManager";
    private static final String SHARED_PREFERENCES = "shopify_prefs";
    private static final String CART_ID = "cart_id";
    private static final String CUSTOMER_ACCESS_TOKEN = "customer_access_token";

    private static volatile ShopifyCartManager instance;
    private GraphClient graphClient = null;
    private AppCompatActivity context = null;
    private Context mContext = null;

    // Shopify Credentials (Dev Environment)
    private final String shopDomain = "put_your_shipify_domain";
    private final String accessToken = "put_your_shopify_access_token";

    // Hardcoded User Details for automated login/register flow
    private final String customerEmailId = "put_customer_email_id";
    private final String customerPassword = "put_customer_password";
    private final String customerFirstName = "put_customer_first_name";
    private final String customerLastName = "put_customer_last_name";

    private ShopifyCartManager() {
    }

    public static synchronized ShopifyCartManager getInstance() {
        if (instance == null) {
            instance = new ShopifyCartManager();
        }
        return instance;
    }

    public void init(AppCompatActivity applicationContext) {
        this.context = applicationContext;
        if (graphClient == null) {
            graphClient = GraphClient.Companion.build(
                    applicationContext,
                    shopDomain,
                    accessToken,
                    builder -> null
            );
            checkLoginStatus();
        } else {
            String token = getCustomerAccessToken();
            if (token == null || token.isEmpty()) {
                checkLoginStatus();
            }
        }
    }

    private void checkLoginStatus() {
        String token = getCustomerAccessToken();
        if (token != null && !token.isEmpty()) {
            validateCustomerAccessToken(result -> {
                if (result == null || result.toString().contains("Failure")) {
                    loginCustomer(loginResult -> Unit.INSTANCE);
                }
                return Unit.INSTANCE; // Must return Unit.INSTANCE in Java
            });
        } else {
            registerCustomer(result -> {
                if (result != null && !result.toString().contains("Failure")) {
                    loginCustomer(loginResult -> {
                        if (loginResult != null && !loginResult.toString().contains("Failure")) {
                            addCustomerAddress(addressResult -> Unit.INSTANCE);
                        }
                        return Unit.INSTANCE;
                    });
                }
                return Unit.INSTANCE;
            });
        }
    }

    public void showProductDetails(Context context, String productIdStr, Function1<? super Integer, Unit> callback) {
        this.mContext = context;
        ID productId = new ID(productIdStr);

        Storefront.QueryRootQuery query = Storefront.query(root -> root
                .node(productId, node -> node
                        .onProduct(product -> product
                                .title().description()
                                .images(args -> args.first(1), imgConn -> imgConn
                                        .edges(edge -> edge.node(image -> image.url()))
                                )
                                .options(name -> name.name())
                                .variants(args -> args.first(10), varConn -> varConn
                                        .edges(edge -> edge.node(variant -> variant
                                                .title()
                                                .availableForSale()
                                                .quantityAvailable()
                                                .price(p -> p.amount().currencyCode())
                                                .selectedOptions(sel -> sel.name().value())
                                        ))
                                )
                        )
                )
        );

        if (graphClient != null) {
            graphClient.queryGraph(query).enqueue(result -> {
                if (result instanceof GraphCallResult.Success) {
                    Storefront.QueryRoot data = ((GraphCallResult.Success<Storefront.QueryRoot>) result).getResponse().getData();
                    Storefront.Product node = (Storefront.Product) data.getNode();

                    if (node != null) {
                        List<Storefront.ProductVariantEdge> variantEdges = node.getVariants().getEdges();
                        Storefront.ProductVariant firstVariant = variantEdges.isEmpty() ? null : variantEdges.get(0).getNode();

                        List<String> variantsOptions = new ArrayList<>();
                        List<String> variantsId = new ArrayList<>();
                        for (Storefront.ProductVariantEdge edge : variantEdges) {
                            variantsOptions.add(edge.getNode().getTitle());
                            variantsId.add(edge.getNode().getId().toString());
                        }

                        Product product = new Product(
                                node.getId().toString(),
                                node.getTitle(),
                                node.getDescription(),
                                firstVariant != null ? firstVariant.getPrice().getAmount().toString() : "0.0",
                                firstVariant != null ? firstVariant.getPrice().getCurrencyCode().toString() : "USD",
                                node.getImages().getEdges().isEmpty() ? "" : node.getImages().getEdges().get(0).getNode().getUrl(),
                                variantsOptions,
                                variantsId,
                                node.getOptions().isEmpty() ? "" : node.getOptions().get(0).getName(),
                                firstVariant != null && Boolean.TRUE.equals(firstVariant.getAvailableForSale())
                        );

                        new Handler(Looper.getMainLooper()).post(() -> openProductDetails(context, product, callback));
                    }
                }
                return null;
            });
        }
    }

    private void openProductDetails(Context activity, Product mProduct, Function1<? super Integer, Unit> callback) {
        if (!"null".equals(mProduct.getId())) {
            ProductBottomSheetDialog dialog = new ProductBottomSheetDialog(mProduct, (product, option) -> {
                ensureActiveCartAndAddItem(option, callback);
            });

            FragmentManager fragmentManager = getFragmentManager(activity);
            if (fragmentManager != null) {
                dialog.show(fragmentManager, TAG);
            }
        } else {
            showToast("Product not found");
        }
    }

    public void ensureActiveCartAndAddItem(String option, Function1<? super Integer, Unit> callback) {
        String cartId = getCarId();
        if (cartId == null) {
            createCartAndAddCartItem(option, callback);
            return;
        }

        Storefront.QueryRootQuery query = Storefront.query(root -> root
                .cart(new ID(cartId), cart -> cart.checkoutUrl())
        );

        if (graphClient != null) {
            graphClient.queryGraph(query).enqueue(result -> {
                if (result instanceof GraphCallResult.Success) {
                    Storefront.Cart cart = ((GraphCallResult.Success<Storefront.QueryRoot>) result).getResponse().getData().getCart();
                    if (cart == null) {
                        createCartAndAddCartItem(option, callback);
                    } else {
                        addItemToCart(cartId, option, callback);
                    }
                } else {
                    createCartAndAddCartItem(option, callback);
                }
                return null;
            });
        }
    }

    public void createCartAndAddCartItem(String option, Function1<? super Integer, Unit> callback) {
        Storefront.MutationQuery mutation = Storefront.mutation(root -> root
                .cartCreate(args -> {
                        }, payload -> payload
                                .cart(cart -> cart.totalQuantity())
                                .userErrors(err -> err.field().message())
                )
        );

        if (graphClient != null) {
            graphClient.mutateGraph(mutation).enqueue(result -> {
                if (result instanceof GraphCallResult.Success) {
                    Storefront.Cart cart = ((GraphCallResult.Success<Storefront.Mutation>) result).getResponse().getData().getCartCreate().getCart();
                    if (cart != null) {
                        String cartId = cart.getId().toString();
                        saveCartId(cartId);
                        addItemToCart(cartId, option, callback);
                    }
                }
                return null;
            });
        }
    }

    public void addItemToCart(String cartId, String mVariantId, Function1<? super Integer, Unit> callback) {
        Storefront.CartLineInput lineInput = new Storefront.CartLineInput(new ID(mVariantId)).setQuantity(1);

        Storefront.MutationQuery mutation = Storefront.mutation(root -> root
                .cartLinesAdd(new ID(cartId), Collections.singletonList(lineInput), payload -> payload
                        .cart(cart -> cart.totalQuantity())
                        .userErrors(err -> err.field().message())
                )
        );

        if (graphClient != null) {
            graphClient.mutateGraph(mutation).enqueue(result -> {
                if (result instanceof GraphCallResult.Success) {
                    Storefront.Cart cart = ((GraphCallResult.Success<Storefront.Mutation>) result).getResponse().getData().getCartLinesAdd().getCart();
                    int newCount = (cart != null && cart.getTotalQuantity() != null) ? cart.getTotalQuantity() : 0;
                    if (callback != null) {
                        new Handler(Looper.getMainLooper()).post(() -> callback.invoke(newCount));
                    }
                }
                return null;
            });
        }
    }

    public void getCheckoutUrl(String cartId, Function1<? super String, Unit> callback) {
        if (cartId == null) {
            callback.invoke(null);
            return;
        }
        ID cartIdObj = new ID(cartId);
        Storefront.CartBuyerIdentityInput buyerIdentityInput = new Storefront.CartBuyerIdentityInput()
                .setCustomerAccessToken(getCustomerAccessToken());

        Storefront.MutationQuery mutation = Storefront.mutation(root -> root
                .cartBuyerIdentityUpdate(cartIdObj, buyerIdentityInput, update -> update
                        .cart(cart -> cart
                                .checkoutUrl()
                                .buyerIdentity(identity -> identity
                                        .customer(customer -> customer.email().firstName().lastName())
                                )
                        )
                        .userErrors(err -> err.field().message())
                )
        );

        if (graphClient != null) {
            graphClient.mutateGraph(mutation).enqueue(result -> {
                if (result instanceof GraphCallResult.Success) {
                    Storefront.Mutation data = ((GraphCallResult.Success<Storefront.Mutation>) result).getResponse().getData();
                    String checkoutUrl = data.getCartBuyerIdentityUpdate().getCart().getCheckoutUrl();
                    callback.invoke(checkoutUrl);
                } else {
                    callback.invoke(null);
                }
                return null;
            });
        }
    }

    // Helper Methods for Login/Register Flow (Mapping Kotlin Logic)
    private void validateCustomerAccessToken(Function1<? super Result<Boolean>, Unit> callback) {
        String token = getCustomerAccessToken();
        if (token == null || token.isEmpty()) {
            // We cannot easily create a Result.failure in Java,
            // so we return Unit to avoid crashing if the callback is null.
            return;
        }

        Storefront.QueryRootQuery query = Storefront.query(root -> root
                .customer(token, customer -> customer
                        .id()
                        .email()
                        .firstName()
                        .lastName()
                )
        );

        if (graphClient != null) {
            graphClient.queryGraph(query).enqueue(result -> {
                if (result instanceof GraphCallResult.Success) {
                    Storefront.QueryRoot data = ((GraphCallResult.Success<Storefront.QueryRoot>) result).getResponse().getData();

                    // Logic check: if data/customer exists, we treat it as success.
                    // Note: We avoid creating 'Result' objects here to prevent mangling issues.
                    // Instead, we call the next logic or rely on the status check in checkLoginStatus.
                    if (data != null && data.getCustomer() != null) {
                        Timber.d("Token is valid for: " + data.getCustomer().getEmail());
                    }
                } else if (result instanceof GraphCallResult.Failure) {
                    Timber.e("Token validation failed");
                }

                // Trigger callback to continue flow
                if (callback != null) {
                    // Since we can't create Result.success(true) in Java easily,
                    // common practice in pure-java-no-bridge is to pass a placeholder
                    // or null if the receiver handles it, but here we return Unit.
                    callback.invoke(null);
                }

                return Unit.INSTANCE;
            });
        }
    }

    private void loginCustomer(Function1<? super Result<Boolean>, Unit> callback) {
        Storefront.CustomerAccessTokenCreateInput input = new Storefront.CustomerAccessTokenCreateInput(customerEmailId, customerPassword);

        Storefront.MutationQuery mutation = Storefront.mutation(root -> root
                .customerAccessTokenCreate(input, payload -> payload
                        .customerAccessToken(token -> token.accessToken().expiresAt())
                        .customerUserErrors(error -> error.field().message())
                )
        );

        if (graphClient != null) {
            graphClient.mutateGraph(mutation).enqueue(result -> {
                boolean success = false;
                if (result instanceof GraphCallResult.Success) {
                    Storefront.Mutation data = ((GraphCallResult.Success<Storefront.Mutation>) result).getResponse().getData();
                    Storefront.CustomerAccessToken accessTokenObj = data.getCustomerAccessTokenCreate().getCustomerAccessToken();

                    if (accessTokenObj != null) {
                        saveCustomerAccessToken(accessTokenObj.getAccessToken());
                        success = true;
                        Timber.d("Login Successful. Token saved.");
                    } else {
                        List<Storefront.CustomerUserError> errors = data.getCustomerAccessTokenCreate().getCustomerUserErrors();
                        if (!errors.isEmpty()) {
                            Timber.e("Login Error: " + errors.get(0).getMessage());
                        }
                    }
                }

                if (callback != null) {
                    // We pass null because we cannot instantiate a Kotlin Result value class in Java.
                    // The checkLoginStatus method handles this by checking the string/null state.
                    callback.invoke(null);
                }
                return Unit.INSTANCE;
            });
        }
    }

    private void registerCustomer(Function1<? super Result<Boolean>, Unit> callback) {
        Storefront.CustomerCreateInput input = new Storefront.CustomerCreateInput(customerEmailId, customerPassword)
                .setFirstName(customerFirstName)
                .setLastName(customerLastName);

        Storefront.MutationQuery mutation = Storefront.mutation(root -> root
                .customerCreate(input, payload -> payload
                        .customer(customer -> customer.id().email())
                        .customerUserErrors(error -> error.field().message())
                )
        );

        if (graphClient != null) {
            graphClient.mutateGraph(mutation).enqueue(result -> {
                if (callback != null) callback.invoke(null);
                return Unit.INSTANCE;
            });
        }
    }

    private void addCustomerAddress(Function1<? super Result<Boolean>, Unit> callback) {
        // Implementation for address if needed, otherwise just trigger callback
        if (callback != null) callback.invoke(null);
    }

    // Storage Utilities
    public String getCarId() {
        if (context == null) return null;
        return context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).getString(CART_ID, null);
    }

    private void saveCartId(String cartId) {
        if (context != null) {
            context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).edit().putString(CART_ID, cartId).apply();
        }
    }

    private void saveCustomerAccessToken(String token) {
        if (context != null) {
            context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).edit().putString(CUSTOMER_ACCESS_TOKEN, token).apply();
        }
    }

    private String getCustomerAccessToken() {
        if (context == null) return "";
        return context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).getString(CUSTOMER_ACCESS_TOKEN, "");
    }

    public static String currencySymbol(String currencyCode) {
        try {
            return Currency.getInstance(currencyCode).getSymbol(Locale.US);
        } catch (Exception e) {
            return "$";
        }
    }

    private void showToast(String msg) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Context target = context != null ? context : mContext;
            if (target != null) Toast.makeText(target, msg, Toast.LENGTH_SHORT).show();
        });
    }

    public FragmentManager getFragmentManager(Context context) {
        if (context instanceof AppCompatActivity)
            return ((AppCompatActivity) context).getSupportFragmentManager();
        if (context instanceof FragmentActivity)
            return ((FragmentActivity) context).getSupportFragmentManager();
        return null;
    }

    public interface ShopifyCallback<T> {
        void onResult(T data, boolean isSuccess);
    }

    public void getCartDetails(String cartId, ShopifyCallback<List<CartItem>> callback) {

        if (cartId == null) {
            if (callback != null) callback.onResult(null, false);
            return;
        }

        Storefront.QueryRootQuery query = Storefront.query(root -> root
                .cart(new ID(cartId), cart -> cart
                        .checkoutUrl()
                        .totalQuantity()
                        .lines(args -> args.first(20), lineConnection -> lineConnection
                                .edges(edge -> edge.node(lineItem -> lineItem
                                        .id()                     // ⭐ REQUIRED (Fix for null lineId)
                                        .quantity()
                                        .merchandise(merchandise -> merchandise
                                                .onProductVariant(variant -> variant
                                                        .title()
                                                        .price(price -> price
                                                                .amount()
                                                                .currencyCode()
                                                        )
                                                        .product(product -> product
                                                                .title()
                                                                .featuredImage(image -> image.url())
                                                                .options(option -> option.name())
                                                        )
                                                )
                                        )
                                ))
                        )
                )
        );

        if (graphClient != null) {

            graphClient.queryGraph(query).enqueue(result -> {

                if (result instanceof GraphCallResult.Success) {

                    try {

                        Storefront.Cart cart =
                                ((GraphCallResult.Success<Storefront.QueryRoot>) result)
                                        .getResponse()
                                        .getData()
                                        .getCart();

                        List<CartItem> items = new ArrayList<>();

                        if (cart != null && cart.getLines() != null) {

                            for (Storefront.BaseCartLineEdge edge : cart.getLines().getEdges()) {

                                Storefront.BaseCartLine line = edge.getNode();

                                if (!(line.getMerchandise() instanceof Storefront.ProductVariant))
                                    continue;

                                Storefront.ProductVariant variant =
                                        (Storefront.ProductVariant) line.getMerchandise();

                                Storefront.Product product = variant.getProduct();

                                // Safe ID extraction
                                String lineId = line.getId() != null
                                        ? line.getId().toString()
                                        : "";

                                String variantId = variant.getId() != null
                                        ? variant.getId().toString()
                                        : "";

                                // Option name
                                String optionTitle = "";
                                if (product.getOptions() != null && !product.getOptions().isEmpty()) {
                                    optionTitle = product.getOptions().get(0).getName();
                                }

                                // Image
                                String imageUrl = "";
                                if (product.getFeaturedImage() != null) {
                                    imageUrl = product.getFeaturedImage().getUrl();
                                }

                                // Price
                                double priceAmount = 0.0;
                                String currencyCode = "";

                                if (variant.getPrice() != null) {

                                    if (variant.getPrice().getAmount() != null) {
                                        priceAmount = Double.parseDouble(
                                                variant.getPrice().getAmount().toString()
                                        );
                                    }

                                    if (variant.getPrice().getCurrencyCode() != null) {
                                        currencyCode =
                                                variant.getPrice().getCurrencyCode().toString();
                                    }
                                }

                                items.add(new CartItem(
                                        lineId,
                                        optionTitle,
                                        priceAmount,
                                        imageUrl,
                                        line.getQuantity(),
                                        variantId,
                                        currencyCode,
                                        variant.getTitle(),
                                        product.getTitle()
                                ));
                            }
                        }

                        if (callback != null) {
                            List<CartItem> finalItems = items;

                            new Handler(Looper.getMainLooper()).post(() ->
                                    callback.onResult(finalItems, true)
                            );
                        }

                    } catch (Exception e) {

                        if (callback != null) {

                            new Handler(Looper.getMainLooper()).post(() ->
                                    callback.onResult(null, false)
                            );
                        }
                    }

                } else {

                    if (callback != null) {

                        new Handler(Looper.getMainLooper()).post(() ->
                                callback.onResult(null, false)
                        );
                    }
                }

                return kotlin.Unit.INSTANCE;
            });
        }
    }

    public void updateCartLineQuantity(String cartId, String lineId, int newQuantity, ViewCartActivity.BooleanCallback callback) {

        Storefront.CartLineUpdateInput updateInput =
                new Storefront.CartLineUpdateInput(new ID(lineId))
                        .setQuantity(newQuantity);

        Storefront.MutationQuery mutation = Storefront.mutation(root ->
                root.cartLinesUpdate(
                        new ID(cartId),
                        Collections.singletonList(updateInput),
                        payload -> payload
                                .cart(cart -> cart.totalQuantity())
                                .userErrors(error -> error.field().message())
                )
        );

        if (graphClient != null) {

            graphClient.mutateGraph(mutation).enqueue(result -> {

                boolean success = false;

                if (result instanceof GraphCallResult.Success) {

                    Storefront.Mutation data =
                            ((GraphCallResult.Success<Storefront.Mutation>) result)
                                    .getResponse()
                                    .getData();

                    if (data != null) {

                        List<Storefront.CartUserError> errors =
                                data.getCartLinesUpdate().getUserErrors();

                        if (errors.isEmpty()) {

                            success = true;
                            Timber.d("Quantity updated successfully");

                        } else {

                            Timber.e("Update Error: " + errors.get(0).getMessage());
                        }
                    }
                }

                boolean finalSuccess = success;

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (callback != null) {
                        callback.onResult(finalSuccess);
                    }
                });

                return Unit.INSTANCE;
            });
        }
    }

    public void removeCartLine(String cartId, String lineId, Function1<? super Result<Boolean>, Unit> onResult) {
        Storefront.MutationQuery mutation = Storefront.mutation(root -> root
                .cartLinesRemove(new ID(cartId), Collections.singletonList(new ID(lineId)), payload -> payload
                        .cart(cart -> cart.totalQuantity())
                        .userErrors(error -> error.field().message())
                )
        );

        if (graphClient != null) {
            graphClient.mutateGraph(mutation).enqueue(result -> {
                if (result instanceof GraphCallResult.Success) {
                    Storefront.Mutation data = ((GraphCallResult.Success<Storefront.Mutation>) result).getResponse().getData();
                    List<Storefront.CartUserError> errors = data.getCartLinesRemove().getUserErrors();

                    if (errors.isEmpty()) {
                        Timber.d("Item removed from cart");
                    } else {
                        Timber.e("Remove Error: " + errors.get(0).getMessage());
                    }
                }

                if (onResult != null) {
                    onResult.invoke(null);
                }
                return Unit.INSTANCE;
            });
        }
    }

    /**
     * Fetches the total quantity of items in the current cart.
     *
     * @param callback A callback function receiving the total count (int).
     */
    public void getTotalCartItems(final Function1<? super Integer, Unit> callback) {
        // 1. Get the current Cart ID from SharedPreferences
        String cartId = getCarId();
        if (cartId == null || cartId.isEmpty()) {
            Timber.tag(TAG).d("No cart found");
            callback.invoke(0);
            return;
        }

        // 2. Build the Storefront Query to get totalQuantity
        Storefront.QueryRootQuery query = Storefront.query(root ->
                root.cart(new ID(cartId), cart ->
                        cart.totalQuantity()
                )
        );

        // 3. Execute the Graph Query
        if (graphClient != null) {
            graphClient.queryGraph(query).enqueue(result -> {
                if (result instanceof GraphCallResult.Success) {
                    // Accessing the response data
                    Storefront.QueryRoot data = ((GraphCallResult.Success<Storefront.QueryRoot>) result).getResponse().getData();

                    int count = 0;
                    if (data != null && data.getCart() != null && data.getCart().getTotalQuantity() != null) {
                        count = data.getCart().getTotalQuantity();
                    }

                    final int finalCount = count;

                    // 4. Thread Switching: Ensure the callback runs on the Main Thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        callback.invoke(finalCount);
                    });

                    Timber.tag("SHOPIFY").d("🛒 Cart Count: %d", finalCount);

                } else if (result instanceof GraphCallResult.Failure) {
                    // Handle failure
                    Exception error = ((GraphCallResult.Failure) result).getError();
                    Timber.tag("SHOPIFY").e(error, "❌ Failed to fetch cart count");

                    new Handler(Looper.getMainLooper()).post(() -> {
                        callback.invoke(0);
                    });
                }

                // Satisfy the Kotlin Unit return type for the enqueue lambda
                return null;
            });
        } else {
            callback.invoke(0);
        }
    }

    /*
     *
     * for get order history
     *
     * */

    public String getOderHistoryUrl() {
        if (getCustomerAccessToken() != null) {
            return "https://"+shopDomain+"/account?customerAccessToken="+getCustomerAccessToken();
        } else {
            return  "https://"+shopDomain+"/account";
        }

    }
}