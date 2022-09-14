package com.myorg.managers;

import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.constructs.Construct;

public class DynamoManager {
    private Construct scope;
    public DynamoManager(Construct scope) {
        this.scope = scope;
    }
    public Table createProductsTable(){
        return Table.Builder.create(scope, "productsTable")
                .partitionKey(Attribute.builder()
                    .name("id")
                    .type(AttributeType.STRING)
                    .build())
                .tableName("products")
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();

    }
}
