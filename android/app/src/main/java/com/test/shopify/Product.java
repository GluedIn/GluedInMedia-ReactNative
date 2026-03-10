package com.test.shopify;

import androidx.annotation.NonNull;
import java.util.List;
import java.util.Objects;

public class Product {
    private final String id;
    private final String title;
    private final String description;
    private final String price;
    private final String currency;
    private final String imageUrl;
    private final List<String> variantsOptions;
    private final List<String> variantsId;
    private final String variantName;
    private final boolean availableForSale;

    public Product(
            String id,
            String title,
            String description,
            String price,
            String currency,
            String imageUrl,
            List<String> variantsOptions,
            List<String> variantsId,
            String variantName,
            boolean availableForSale
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.imageUrl = imageUrl;
        this.variantsOptions = variantsOptions;
        this.variantsId = variantsId;
        this.variantName = variantName;
        this.availableForSale = availableForSale;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<String> getVariantsOptions() {
        return variantsOptions;
    }

    public List<String> getVariantsId() {
        return variantsId;
    }

    public String getVariantName() {
        return variantName;
    }

    public boolean isAvailableForSale() {
        return availableForSale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return availableForSale == product.availableForSale &&
                Objects.equals(id, product.id) &&
                Objects.equals(title, product.title) &&
                Objects.equals(description, product.description) &&
                Objects.equals(price, product.price) &&
                Objects.equals(currency, product.currency) &&
                Objects.equals(imageUrl, product.imageUrl) &&
                Objects.equals(variantsOptions, product.variantsOptions) &&
                Objects.equals(variantsId, product.variantsId) &&
                Objects.equals(variantName, product.variantName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, price, currency, imageUrl, variantsOptions, variantsId, variantName, availableForSale);
    }

    @NonNull
    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", price='" + price + '\'' +
                ", currency='" + currency + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", variantsOptions=" + variantsOptions +
                ", variantsId=" + variantsId +
                ", variantName='" + variantName + '\'' +
                ", availableForSale=" + availableForSale +
                '}';
    }
}
