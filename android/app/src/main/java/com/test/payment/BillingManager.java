package com.test.payment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.gluedin.callback.PaymentMethod;
import com.gluedin.callback.PaymentStatus;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import kotlin.jvm.functions.Function1;

public class BillingManager {

    private static final String TAG = "BillingManager";
    private static volatile BillingManager instance;

    // Interface for Callbacks
    public interface PaymentCallback {
        void onResult(PaymentStatus status, String packageId, String orderId, PaymentMethod method);
    }

    public interface PriceMapCallback {
        void onPriceResult(Map<String, Object> result);
    }

    private PaymentCallback notifyPaymentResult;

    private String mPaymentSeriesId;
    private String mSkuId;
    private String basePlanId;
    private String mPackageId;
    private String mPaymentUrl;
    private String userId;

    private String mProductType = BillingClient.ProductType.SUBS;
    private BillingClient mBillingClient;
    private PaymentMethod paymentMethod;

    private boolean isFinalCallbackSent = false;
    private final Set<String> handledPurchaseTokens = new HashSet<>();

    private BillingManager() {}

    public static BillingManager getInstance() {
        if (instance == null) {
            synchronized (BillingManager.class) {
                if (instance == null) {
                    instance = new BillingManager();
                }
            }
        }
        return instance;
    }

    public void init(
            @NonNull Activity activity,
            @NonNull String skuId,
            @NonNull String basePlanId,
            @Nullable String seriesId,
            @Nullable String paymentUrl,
            @Nullable String packageId,
            @NonNull String productType,
            @NonNull String userId,
            @NonNull PaymentMethod paymentMethod,
            @NonNull PaymentCallback callback
    ) {
        this.isFinalCallbackSent = false;
        this.mSkuId = skuId;
        this.basePlanId = basePlanId;
        this.mPaymentSeriesId = seriesId;
        this.mPaymentUrl = paymentUrl;
        this.mPackageId = packageId;
        this.notifyPaymentResult = callback;
        this.mProductType = productType;
        this.userId = userId;
        this.paymentMethod = paymentMethod;

        setupBillingClient(activity);
    }

    private void setupBillingClient(Activity activity) {
        WeakReference<Activity> weakActivity = new WeakReference<>(activity);

        mBillingClient = BillingClient.newBuilder(activity)
                .setListener(purchasesUpdatedListener(weakActivity))
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .build();

        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult br) {
                if (br.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Activity act = weakActivity.get();
                    if (act != null) {
                        checkExistingSubscriptionAndLaunch(act);
                    }
                } else {
                    paymentCallback(PaymentStatus.PaymentFailed, "");
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(TAG, "Billing service disconnected");
            }
        });
    }

    private PurchasesUpdatedListener purchasesUpdatedListener(WeakReference<Activity> activityRef) {
        return (billingResult, purchases) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                paymentCallback(PaymentStatus.PaymentCancelled, "");
            } else {
                paymentCallback(PaymentStatus.PaymentFailed, "");
            }
        };
    }

    private void checkExistingSubscriptionAndLaunch(Activity activity) {
        if (BillingClient.ProductType.INAPP.equals(mProductType)) {
            launchPurchaseFlow(activity);
        } else {
            // Logic for Subscription upgrade or purchase
            launchNewPurchase(activity);
        }
    }

    private void launchPurchaseFlow(Activity activity) {
        consumeExistingPurchases(() -> launchNewPurchase(activity));
    }

    private void consumeExistingPurchases(Runnable onComplete) {
        if (mBillingClient == null) return;

        QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build();

        mBillingClient.queryPurchasesAsync(params, (billingResult, purchases) -> {
            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK || purchases.isEmpty()) {
                onComplete.run();
                return;
            }

            final int[] pendingConsumes = {0};
            for (Purchase purchase : purchases) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    pendingConsumes[0]++;
                    ConsumeParams consumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();

                    mBillingClient.consumeAsync(consumeParams, (br, token) -> {
                        pendingConsumes[0]--;
                        if (pendingConsumes[0] == 0) {
                            onComplete.run();
                        }
                    });
                }
            }
            if (pendingConsumes[0] == 0) onComplete.run();
        });
    }

    private void launchNewPurchase(Activity activity) {
        if (mSkuId == null || mSkuId.isEmpty() || mBillingClient == null || !mBillingClient.isReady()) {
            paymentCallback(PaymentStatus.PaymentFailed, "");
            return;
        }

        QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(mSkuId)
                .setProductType(mProductType)
                .build();

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(Collections.singletonList(product))
                .build();

        mBillingClient.queryProductDetailsAsync(params, (billingResult, queryResult) -> {
            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK || queryResult.getProductDetailsList() == null || queryResult.getProductDetailsList().isEmpty()) {
                paymentCallback(PaymentStatus.PaymentFailed, "");
                return;
            }

            BillingFlowParams.ProductDetailsParams productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(queryResult.getProductDetailsList().get(0))
                    .build();

            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(Collections.singletonList(productParams))
                    .setObfuscatedAccountId(userId != null ? userId : "")
                    .build();

            mBillingClient.launchBillingFlow(activity, flowParams);
        });
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (handledPurchaseTokens.contains(purchase.getPurchaseToken())) return;
            handledPurchaseTokens.add(purchase.getPurchaseToken());

            String orderId = "";
            try {
                orderId = new JSONObject(purchase.getOriginalJson()).optString("orderId", "");
            } catch (Exception e) {
                Log.e(TAG, "Error parsing JSON", e);
            }

            final String finalOrderId = orderId;

            if (BillingClient.ProductType.INAPP.equals(mProductType)) {
                paymentCallback(PaymentStatus.PaymentSuccess, finalOrderId);
                ConsumeParams consumeParams = ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                mBillingClient.consumeAsync(consumeParams, (br, token) -> {
                    if (br.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                        Log.e(TAG, "Consume failed for INAPP");
                    }
                });
            } else {
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams ackParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();
                    mBillingClient.acknowledgePurchase(ackParams, br -> {
                        if (br.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            paymentCallback(PaymentStatus.PaymentSuccess, finalOrderId);
                        }
                    });
                } else {
                    paymentCallback(PaymentStatus.PaymentSuccess, finalOrderId);
                }
            }
        } else {
            paymentCallback(PaymentStatus.PaymentFailed, "");
        }
    }

    private void paymentCallback(PaymentStatus status, String orderId) {
        if (isFinalCallbackSent && status != PaymentStatus.PaymentStarted) return;

        if (status != PaymentStatus.PaymentStarted) {
            isFinalCallbackSent = true;
        }

        if (notifyPaymentResult != null) {
            notifyPaymentResult.onResult(
                    status,
                    mPackageId != null ? mPackageId : "",
                    orderId,
                    paymentMethod != null ? paymentMethod : PaymentMethod.IN_APP_PURCHASE
            );
        }
    }

    public void fetchPricePartsForSkus(
            @NonNull Activity activity,
            @NonNull List<String> skuIds,
            @NonNull String productType,
            @NonNull PriceMapCallback callback
    ) {
        // Filter and distinct SKUs
        final List<String> validSkus = skuIds.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (validSkus.isEmpty()) {
            callback.onPriceResult(new HashMap<>());
            return;
        }

        // Initialize BillingClient if null
        if (mBillingClient == null) {
            mBillingClient = BillingClient.newBuilder(activity)
                    .setListener((billingResult, purchases) -> { /* No-op */ })
                    .enablePendingPurchases(
                            PendingPurchasesParams.newBuilder()
                                    .enableOneTimeProducts()
                                    .build()
                    )
                    .build();
        }

        // Check if ready
        if (mBillingClient.isReady()) {
            queryPriceParts(validSkus, productType, callback);
            return;
        }

        // Start connection
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult br) {
                if (br.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    queryPriceParts(validSkus, productType, callback);
                } else {
                    callback.onPriceResult(createErrorMap(validSkus));
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                callback.onPriceResult(createErrorMap(validSkus));
            }
        });
    }

    private void queryPriceParts(List<String> skus, String productType, PriceMapCallback callback) {
        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        for (String sku : skus) {
            productList.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(sku)
                    .setProductType(productType)
                    .build());
        }

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        mBillingClient.queryProductDetailsAsync(params, (billingResult, productDetailsList) -> {
            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK || productDetailsList == null) {
                callback.onPriceResult(createErrorMap(skus));
                return;
            }

            // Logic to extract prices (assuming helper methods exist or are called here)
            // This is where you'd map the pd list to your custom objects like PriceParts
            Map<String, Object> finalMap = new HashMap<>();
            // ... implementation of pd extraction ...

            callback.onPriceResult(finalMap);
        });
    }

    private Map<String, Object> createErrorMap(List<String> skus) {
        Map<String, Object> errorMap = new HashMap<>();
        for (String sku : skus) {
            errorMap.put(sku, null);
        }
        return errorMap;
    }

    public void openPlayStoreSubscription(
            @NonNull Context context,
            @NonNull String inAppSkuId,
            @NonNull String packageName
    ) {
        String subscriptionUrl = String.format(
                "https://play.google.com/store/account/subscriptions?sku=%s&package=%s",
                inAppSkuId,
                packageName
        );

        String fallbackUrl = "https://play.google.com/store/account/subscriptions";

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(subscriptionUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            // Fallback to general subscriptions page in browser/store
            try {
                Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(fallbackIntent);
            } catch (Exception fatal) {
                // Handle case where no browser or Play Store is installed
                Log.e("BillingManager", "Unable to open subscription URL", fatal);
            }
        }
    }
}