package com.test.shopify;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.test.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.gluedin.base.presentation.customView.GluedInSemiboldTextView;
import com.gluedin.base.presentation.customView.PlusSAWBoldTextView;
import com.gluedin.presentation.utils.BaseBottomSheet;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.List;

public class ProductBottomSheetDialog extends BaseBottomSheet {

    private final Product product;
    private final OnAddToCartListener onAddToCart;

    // UI View References
    private SimpleDraweeView productImage;
    private PlusSAWBoldTextView productTitle;
    private GluedInSemiboldTextView productDesc;
    private GluedInSemiboldTextView variantTitle;
    private GluedInSemiboldTextView productPrice;
    private GluedInSemiboldTextView btnAddToCart;
    private RadioGroup optionsGroup;

    public interface OnAddToCartListener {
        void onAddToCart(Product product, String variantId);
    }

    public ProductBottomSheetDialog(Product product, OnAddToCartListener onAddToCart) {
        this.product = product;
        this.onAddToCart = onAddToCart;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.shopify_add_to_cart_bottom_sheet, container, false);

        // Bind Views
        productImage = view.findViewById(R.id.productImage);
        productTitle = view.findViewById(R.id.productTitle);
        productDesc = view.findViewById(R.id.productDesc);
        variantTitle = view.findViewById(R.id.variantTitle);
        productPrice = view.findViewById(R.id.productPrice);
        btnAddToCart = view.findViewById(R.id.btnAddToCart);
        optionsGroup = view.findViewById(R.id.optionsGroup);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog() != null ? getDialog().getWindow() : null;
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.dimAmount = 0.60f;
            params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(params);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI();
        initBehavior();
    }

    private void initBehavior() {
        View bottomSheet = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            behavior.setPeekHeight((int) (screenHeight * 0.8));
        }
    }

    private void setupUI() {
        // Basic Product Info
        productTitle.setText(product.getTitle());
        productDesc.setText(product.getDescription());
        productImage.setImageURI(product.getImageUrl());
        variantTitle.setText(String.format("%s :", product.getVariantName()));

        // Price formatting
        // Assuming ShopifyCartManagerJava singleton exists from previous conversion
        String currency = ShopifyCartManager.getInstance().currencySymbol(product.getCurrency());
        productPrice.setText(String.format("%s %s", currency, product.getPrice()));

        // Availability check
        if (!product.isAvailableForSale()) {
            btnAddToCart.setEnabled(false);
            btnAddToCart.setAlpha(0.5f);
            btnAddToCart.setText("Sold Out");
        } else {
            btnAddToCart.setEnabled(true);
            btnAddToCart.setAlpha(1.0f);
            btnAddToCart.setText("Add to Cart");
        }

        // Populate Variants (RadioButtons)
        optionsGroup.removeAllViews();
        List<String> options = product.getVariantsOptions();
        Typeface mTypeface = ResourcesCompat.getFont(getContext(), R.font.semibold);

        for (int i = 0; i < options.size(); i++) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(options.get(i));
            radioButton.setTextColor(Color.WHITE);
            radioButton.setTypeface(mTypeface);
            radioButton.setTextSize(16f);
            radioButton.setId(View.generateViewId());

            // Custom radio drawable from your project
            radioButton.setButtonDrawable(ContextCompat.getDrawable(getContext(), R.drawable.custom_radio));
            radioButton.setPadding(16, 8, 16, 8);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            // Add margin between items
            if (i != options.size() - 1) {
                params.setMarginEnd((int) (16 * getResources().getDisplayMetrics().density));
            }
            radioButton.setLayoutParams(params);

            optionsGroup.addView(radioButton);

            // Default select the first one
            if (i == 0) radioButton.setChecked(true);
        }

        // Click Listener for Add to Cart
        btnAddToCart.setOnClickListener(v -> {
            int checkedId = optionsGroup.getCheckedRadioButtonId();
            RadioButton selectedRadio = optionsGroup.findViewById(checkedId);

            if (selectedRadio != null) {
                String selectedOptionText = selectedRadio.getText().toString();
                int selectedIndex = options.indexOf(selectedOptionText);

                // Safety check for list bounds
                String selectedVariantId = (selectedIndex >= 0 && selectedIndex < product.getVariantsId().size())
                        ? product.getVariantsId().get(selectedIndex)
                        : "null";

                if (onAddToCart != null) {
                    onAddToCart.onAddToCart(product, selectedVariantId);
                }
                dismiss();
            }
        });
    }
}
