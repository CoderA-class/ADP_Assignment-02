package za.ac.cput.adp_assignment02.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * ProductCategory.java
 * A stock category the admin manages (e.g. "Electronics"), with an icon and colour
 * used for the product pictures. Products refer to it by name.
 * Author: Isaac Tinotenda Ziyengwa (231269307)
 * Date: 11 November 2026
 */
@Entity
@Table(name = "product_category")
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 40)
    private String name;
    /* icon key used by the frontend, e.g. "mobile"; empty means a default box */
    private String icon;
    /* hex colour, e.g. "#2563b8"; empty means a colour is picked automatically */
    private String color;

    public ProductCategory() {
    }

    public ProductCategory(String name, String icon, String color) {
        this.name = name;
        this.icon = icon;
        this.color = color;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
