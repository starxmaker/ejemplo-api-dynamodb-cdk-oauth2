package dev.leosanchez.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;

import dev.leosanchez.models.Product;
import dev.leosanchez.repositories.ProductRepository;

@ApplicationScoped
public class ProductService {
    
    @Inject
    ProductRepository productRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(String id) {
        return productRepository.findById(id);
    }

    public void save(@Valid Product product) {
        product.setId(UUID.randomUUID().toString());
        product.setDiscount( product.getPrice() * 0.1);
        productRepository.save(product);
    }

}
