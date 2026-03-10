package com.test.shopify;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.gluedin.base.presentation.customView.GluedInSemiboldTextView;
import com.gluedin.base.presentation.customView.PlusSAWMediumTextView;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> items;
    private final CartListener listener;

    public interface CartListener {
        void onQuantityChanged(CartItem item);
        void onItemRemoved(CartItem item);
    }

    public CartAdapter(List<CartItem> items, CartListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<CartItem>  getItemList() {
        return items;
    }

    public void remove() {
        items.clear();
        notifyDataSetChanged();
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        private final SimpleDraweeView productImage;
        private final GluedInSemiboldTextView tvProductName;
        private final GluedInSemiboldTextView variantTitle;
        private final GluedInSemiboldTextView variantName;
        private final PlusSAWMediumTextView tvProductPrice;
        private final PlusSAWMediumTextView tvQuantity;
        private final View btnPlus;
        private final View btnMinus;
        private final View ivDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            variantTitle = itemView.findViewById(R.id.variantTitle);
            variantName = itemView.findViewById(R.id.variantName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnPlus = itemView.findViewById(R.id.btnPlus);     // Note: This is the ADD button in your XML
            btnMinus = itemView.findViewById(R.id.btnMinus);   // Note: This is the REMOVE button in your XML
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }

        public void bind(CartItem item) {
            // Set Image using Fresco SimpleDraweeView
            if (item.getImageUrl() != null) {
                productImage.setImageURI(item.getImageUrl());
            }

            tvProductName.setText(item.getTitle());
            variantTitle.setText(String.format("%s :", item.getVariantTitle()));
            variantName.setText(item.getVariantName());

            // Format Price
            String currency = item.getCurrencyCode() != null ? item.getCurrencyCode() : "$";
            tvProductPrice.setText(String.format("%s %s", currency, item.getPrice()));

            // Set Quantity
            tvQuantity.setText(String.valueOf(item.getQuantity()));

            // Increase quantity (Plus Button)
            btnPlus.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    item.setQuantity(item.getQuantity() + 1);
                    notifyItemChanged(pos);
                    listener.onQuantityChanged(createClone(item));
                }
            });

            // Decrease quantity (Minus Button)
            btnMinus.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                    notifyItemChanged(pos);
                    listener.onQuantityChanged(createClone(item));
                }
            });

            // Delete item
            ivDelete.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    CartItem removedItem = items.get(pos);
                    items.remove(pos);
                    notifyItemRemoved(pos);
                    listener.onItemRemoved(removedItem);
                }
            });
        }

        // Helper to create a deep copy for the listener to prevent reference issues
        private CartItem createClone(CartItem item) {
            return new CartItem(
                    item.getLineId(),
                    item.getTitle(),
                    item.getPrice(),
                    item.getImageUrl(),
                    item.getQuantity(),
                    item.getVariantsId(),
                    item.getVariantTitle(),
                    item.getCurrencyCode(),
                    item.getVariantName()
            );
        }
    }
}
