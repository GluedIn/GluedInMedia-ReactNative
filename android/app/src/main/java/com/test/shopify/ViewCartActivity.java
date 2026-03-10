package com.test.shopify;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gluedin.presentation.activities.WebViewActivity;
import com.test.R;
import com.gluedin.base.presentation.customView.GluedInSemiboldTextView;
import com.gluedin.base.presentation.customView.PlusSAWBoldTextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ViewCartActivity extends AppCompatActivity implements CartAdapter.CartListener {

    private CartAdapter adapter;
    private RecyclerView recyclerViewCart;
    private View bottomLayout;
    private View errorMessage;
    private ProgressBar progress;
    private PlusSAWBoldTextView tvTitle;
    private GluedInSemiboldTextView tvProductPrice;
    private GluedInSemiboldTextView btnCheckout;
    private AppCompatImageView ivBack;
    private View mainRoot;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cart);

        initViews();
        setFullScreenChanges();
        initVariables();
    }

    private void initViews() {
        mainRoot = findViewById(R.id.main);
        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        bottomLayout = findViewById(R.id.bottomLayout);
        errorMessage = findViewById(R.id.errorMessage);
        progress = findViewById(R.id.progress);
        tvTitle = findViewById(R.id.tvTitle);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        btnCheckout = findViewById(R.id.btnCheckout);
        ivBack = findViewById(R.id.ivBack);
    }

    private void setFullScreenChanges() {
        ViewCompat.setOnApplyWindowInsetsListener(mainRoot, (v, insets) -> {
            WindowInsetsCompat systemBars = insets;
            v.setPadding(
                    systemBars.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    systemBars.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    systemBars.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    systemBars.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            );
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progress.setVisibility(View.VISIBLE);
        tvTitle.setText("Your Cart (0)");
        bottomLayout.setVisibility(View.GONE);
        if (adapter != null) {
            adapter.remove();
        }
        setAdapter();
    }

    private void initVariables() {
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);

        btnCheckout.setOnClickListener(v -> {
            ShopifyCartManager.getInstance().init(this);
            String cartId = ShopifyCartManager.getInstance().getCarId();
            if (cartId != null) {
                ShopifyCartManager.getInstance().getCheckoutUrl(cartId, checkoutUrl -> {
                    if (checkoutUrl != null) {
                        Intent intent = new Intent(ViewCartActivity.this, WebViewActivity.class);
                        intent.putExtra("url", checkoutUrl);
                        intent.putExtra("title", "Checkout");
                        startActivity(intent);
                    }
                    return null;
                });
            }
        });

        ivBack.setOnClickListener(v -> finish());
    }

    private void setAdapter() {
        ShopifyCartManager.getInstance().init(this);
        String cartId = ShopifyCartManager.getInstance().getCarId();

        ShopifyCartManager.getInstance().getCartDetails(cartId, (items, isSuccess) -> {
            runOnUiThread(() -> {
                if (isSuccess && items != null) {
                    if (items != null && !items.isEmpty()) {
                        errorMessage.setVisibility(View.GONE);
                        adapter = new CartAdapter(new ArrayList<>(items), this);
                        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
                        recyclerViewCart.setAdapter(adapter);
                        updateSubtotal(adapter.getItemList());
                        bottomLayout.setVisibility(View.VISIBLE);
                    } else {
                        errorMessage.setVisibility(View.VISIBLE);
                    }
                } else {
                    errorMessage.setVisibility(View.VISIBLE);
                    Timber.e("Failed to load cart");
                }
                progress.setVisibility(View.GONE);
            });
        });
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void updateSubtotal(List<CartItem> cartItems) {
        if (cartItems != null && !cartItems.isEmpty()) {
            double subtotal = 0;
            int totalQuantity = 0;
            for (CartItem item : cartItems) {
                subtotal += (item.getPrice() * item.getQuantity());
                totalQuantity += item.getQuantity();
            }

            String formattedSubtotal = new BigDecimal(subtotal)
                    .setScale(2, RoundingMode.HALF_UP)
                    .toPlainString();

            String currencyCode = cartItems.get(0).getCurrencyCode();
            String currency = ShopifyCartManager.getInstance().currencySymbol(currencyCode != null ? currencyCode : "SGD");

            tvProductPrice.setText(String.format("%s %s", currency, formattedSubtotal));
            tvTitle.setText(String.format("Your Cart (%02d)", totalQuantity));
        }
    }

    @Override
    public void onQuantityChanged(CartItem item) {
        updateSubtotal(adapter.getItemList());
        progress.setVisibility(View.VISIBLE);
        String cartId = ShopifyCartManager.getInstance().getCarId();

        ShopifyCartManager.getInstance().updateCartLineQuantity(cartId, item.getLineId(), item.getQuantity(), result -> {
            runOnUiThread(() -> {
                progress.setVisibility(View.GONE);
                if (!result) {
                    Toast.makeText(this, "Failed to update quantity", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onItemRemoved(CartItem item) {
        updateSubtotal(adapter.getItemList());
        if (adapter.getItemList().isEmpty()) {
            tvTitle.setText("Your Cart (0)");
            errorMessage.setVisibility(View.VISIBLE);
            bottomLayout.setVisibility(View.GONE);
        } else {
            errorMessage.setVisibility(View.GONE);
            bottomLayout.setVisibility(View.VISIBLE);
        }

        progress.setVisibility(View.VISIBLE);
        String cartId = ShopifyCartManager.getInstance().getCarId();
        ShopifyCartManager.getInstance().removeCartLine(cartId, item.getLineId(), result -> {
            runOnUiThread(() -> {
                progress.setVisibility(View.GONE);
                if (result == null || result.toString().contains("Failure")) {
                    Toast.makeText(this, "Item removed successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to remove item", Toast.LENGTH_SHORT).show();
                }
            });
            return null;
        });
    }

    public interface BooleanCallback {
        void onResult(boolean success);
    }
}
