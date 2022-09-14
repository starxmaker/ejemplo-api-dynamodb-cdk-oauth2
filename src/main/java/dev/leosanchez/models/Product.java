package dev.leosanchez.models;

import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Product {
    @Schema(readOnly = true, example = "3187b908-62f8-4ce8-ad90-2fa4f475b899", description="Identificador del producto, generado automáticamente en su creación")
    private String id;
    @NotBlank
    @Schema(example = "Producto de ejemplo", description = "Nombre del producto")
    private String name;
    @NotBlank
    @Schema(example = "Descripción del producto de ejemplo", description = "Breve descripción del producto")
    private String description;
    @NotBlank
    @Schema(example = "Marca de ejemplo", description = "Marca del producto")
    private String brand;
    @NotBlank
    @Schema(example = "Modelo de ejemplo", description = "Modelo del producto")
    private String model;
    @NotBlank
    @Schema(example="Categoría de ejemplo", description = "Categoría a la que pertenece el producto")
    private String category;
    @PositiveOrZero
    @Max(1000000000)
    @Schema(example = "100", description = "Precio del producto")
    private Double price;
    @Schema(readOnly = true, example = "10", description = "Descuento del producto. Corresponde al 10% del precio original al momento de la creación")
    private Double discount;

    public Product() {
    }

    public Product(String id, String name, String description, String brand, String model, String category, Double price, Double discount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.brand = brand;
        this.model = model;
        this.category = category;
        this.price = price;
        this.discount = discount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    @Override
    public boolean equals (Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Product other = (Product) obj;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }
}
