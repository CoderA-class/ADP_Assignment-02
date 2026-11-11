package za.ac.cput.adp_assignment02.factory;

/*
ProductCategoryFactory.java
ProductCategory Factory class
Author: Isaac Tinotenda Ziyengwa (231269307)
Date: 11 November 2026
*/


import za.ac.cput.adp_assignment02.domain.ProductCategory;

public class ProductCategoryFactory {
    public static ProductCategory createCategory(String name, String icon, String color) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String clean = name.trim().replaceAll("\\s+", " ");
        String hex = color != null && color.matches("^#[0-9a-fA-F]{6}$") ? color.toLowerCase() : null;
        return new ProductCategory(clean, icon, hex);
    }

    public static ProductCategory createCategory(String name) {
        return createCategory(name, null, null);
    }
}
