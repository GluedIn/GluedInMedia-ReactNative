package com.test.shopify;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import java.util.Objects;@Keep
public class CartItem {
    private final String lineId;
    private final String title;
    private final double price;
    private final String imageUrl;
    private int quantity;
    private final String variantsId;
    private final String variantTitle;
    private final String currencyCode;
    private final String variantName;

    public CartItem(
            String lineId,
            String title,
            double price,
            String imageUrl,
            int quantity,
            String variantsId,
            String variantTitle,
            String currencyCode,
            String variantName
    ) {
        this.lineId = lineId;
        this.title = title;
        this.price = price;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.variantsId = variantsId;
        this.variantTitle = variantTitle;
        this.currencyCode = currencyCode;
        this.variantName = variantName;
    }

    public String getLineId() {
        return lineId;
    }

    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getVariantsId() {
        return variantsId;
    }

    public String getVariantTitle() {
        return variantTitle;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getVariantName() {
        return variantName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Double.compare(cartItem.price, price) == 0 &&
                quantity == cartItem.quantity &&
                Objects.equals(lineId, cartItem.lineId) &&
                Objects.equals(title, cartItem.title) &&
                Objects.equals(imageUrl, cartItem.imageUrl) &&
                Objects.equals(variantsId, cartItem.variantsId) &&
                Objects.equals(variantTitle, cartItem.variantTitle) &&
                Objects.equals(currencyCode, cartItem.currencyCode) &&
                Objects.equals(variantName, cartItem.variantName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineId, title, price, imageUrl, quantity, variantsId, variantTitle, currencyCode, variantName);
    }

    @NonNull
    @Override
    public String toString() {
        return "CartItem{" +
                "lineId='" + lineId + '\'' +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", imageUrl='" + imageUrl + '\'' +
                ", quantity=" + quantity +
                ", variantsId='" + variantsId + '\'' +
                ", variantTitle='" + variantTitle + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", variantName='" + variantName + '\'' +
                '}';
    }
}
