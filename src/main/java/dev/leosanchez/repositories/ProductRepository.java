package dev.leosanchez.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.leosanchez.models.Product;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

@ApplicationScoped
public class ProductRepository {
    @Inject
    DynamoDbClient dynamoDbClient;

    @ConfigProperty(name = "dynamodb.table-name.product")
    String tableName;

    public List<Product> findAll() {
        ScanRequest request = ScanRequest.builder()
            .tableName(tableName)
            .build();
        return dynamoDbClient.scanPaginator(request).items().stream()
            .map(item -> this.fromRecord(item))
            .collect(Collectors.toList());
    }

    public Optional<Product> findById(String id) {
        Map<String, AttributeValue> key = Map.of("id", AttributeValue.builder().s(id).build());
        GetItemRequest request = GetItemRequest.builder()
            .tableName(tableName)
            .key(key)
            .build();
        GetItemResponse response = dynamoDbClient.getItem(request);
        return response.item().isEmpty() ? Optional.empty() : Optional.of(this.fromRecord(response.item()));
    }

    public void save(Product product) {
        Map<String, AttributeValue> item = this.toRecord(product);
        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();
        dynamoDbClient.putItem(request);
    }


    private Product fromRecord(Map<String, AttributeValue> item) {
        return new Product(
            item.get("id").s(),
            item.get("name").s(),
            item.get("description").s(),
            item.get("brand").s(),
            item.get("model").s(),
            item.get("category").s(),
            Double.parseDouble(item.get("price").n()),
            Double.parseDouble(item.get("discount").n())
        );
    }

    private Map<String,AttributeValue> toRecord(Product product) {
        return Map.of(
            "id", AttributeValue.builder().s(product.getId()).build(),
            "name", AttributeValue.builder().s(product.getName()).build(),
            "description", AttributeValue.builder().s(product.getDescription()).build(),
            "brand", AttributeValue.builder().s(product.getBrand()).build(),
            "model", AttributeValue.builder().s(product.getModel()).build(),
            "category", AttributeValue.builder().s(product.getCategory()).build(),
            "price", AttributeValue.builder().n(String.valueOf(product.getPrice())).build(),
            "discount", AttributeValue.builder().n(String.valueOf(product.getDiscount())).build()
        );
    }


}
