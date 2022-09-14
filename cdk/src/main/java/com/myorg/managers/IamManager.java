package com.myorg.managers;

import java.util.HashMap;
import java.util.List;

import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.PolicyDocument;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.constructs.Construct;

public class IamManager {
    private Construct scope;
    public IamManager(Construct scope) {
        this.scope = scope;
    }
    public Role createLambdaRole(Table productsTable) {
        return Role.Builder.create(scope, "LambdaRole")
            .roleName("EjemploCrudDynamoCDKRole")
            .managedPolicies(List.of(ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole")))
            .inlinePolicies(new HashMap<String, PolicyDocument>(){{
                put("DynamoPolicy", PolicyDocument.Builder.create().statements(
                    List.of(
                            PolicyStatement.Builder.create()
                                    .actions(
                                            List.of(
                                                    "dynamodb:GetItem",
                                                    "dynamodb:Scan",
                                                    "dynamodb:PutItem"))
                                    .effect(Effect.ALLOW)
                                    .resources(List.of(productsTable.getTableArn()))
                                    .build()
                            )
                    ).build());
            }})
            .assumedBy(new ServicePrincipal("lambda.amazonaws.com"))
            .build();
    }
}
