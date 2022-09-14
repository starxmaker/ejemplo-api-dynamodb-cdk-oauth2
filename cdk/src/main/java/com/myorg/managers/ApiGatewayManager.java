package com.myorg.managers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import software.amazon.awscdk.services.apigateway.AuthorizationType;
import software.amazon.awscdk.services.apigateway.Authorizer;
import software.amazon.awscdk.services.apigateway.CognitoUserPoolsAuthorizer;
import software.amazon.awscdk.services.apigateway.CognitoUserPoolsAuthorizerProps;
import software.amazon.awscdk.services.apigateway.IntegrationOptions;
import software.amazon.awscdk.services.apigateway.IntegrationResponse;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.LambdaIntegrationOptions;
import software.amazon.awscdk.services.apigateway.MethodOptions;
import software.amazon.awscdk.services.apigateway.MethodResponse;
import software.amazon.awscdk.services.apigateway.MockIntegration;
import software.amazon.awscdk.services.apigateway.Model;
import software.amazon.awscdk.services.apigateway.PassthroughBehavior;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigateway.RestApiProps;
import software.amazon.awscdk.services.apigateway.StageOptions;
import software.amazon.awscdk.services.cognito.UserPool;
import software.amazon.awscdk.services.lambda.Function;
import software.constructs.Construct;

public class ApiGatewayManager {
    private Construct scope;

    public ApiGatewayManager(Construct scope) {
        this.scope = scope;
    }

    public CognitoUserPoolsAuthorizer createAuthorizer(UserPool userPool) {
        return new CognitoUserPoolsAuthorizer(this.scope, "user-pool-authorizer", CognitoUserPoolsAuthorizerProps.builder()
            .cognitoUserPools(List.of(userPool))
            .authorizerName("ExampleAPIDynamoAuthorizer")
            .identitySource("method.request.header.Authorization")
        .build());
    }
    public RestApi createApi(Function lambdaHandler, Authorizer authorizer) {
        RestApi restApi = new RestApi(this.scope, "main-api", RestApiProps.builder()
                .deploy(true)
                .description("API para el ejemplo de CRUD con DynamoDB")
                .restApiName("EjemploApiProductosDynamo")
                .deployOptions(StageOptions.builder()
                        .stageName("prod")
                        .build())
                .build());
        
        LambdaIntegration productIntegration = buildLambdaIntegration(lambdaHandler);
        // products
        Resource productResource = restApi.getRoot().addResource("products");
        enableCors(productResource);
        productResource.addMethod("GET", productIntegration, generateMethodOptions(authorizer, "read_products"));
        productResource.addMethod("POST", productIntegration, generateMethodOptions(authorizer, "write_products"));
        // products/{id}
        Resource productIdResource = productResource.addResource("{id}");
        enableCors(productIdResource);
        productIdResource.addMethod("GET", productIntegration, generateMethodOptions(authorizer, "read_products"));
        return restApi;
    }

    private MethodOptions generateMethodOptions(Authorizer authorizer, String scope) {
        MethodOptions options = MethodOptions.builder()
        .authorizationType(AuthorizationType.COGNITO)
        .authorizer(authorizer)
        .authorizationScopes(List.of(scope))
        .methodResponses(
            Arrays.asList(
                MethodResponse.builder()
                    .statusCode("200")
                    .responseParameters(
                        Map.of(
                            "method.response.header.Access-Control-Allow-Origin", Boolean.TRUE,
                            "method.response.header.Access-Control-Allow-Headers", Boolean.TRUE,
                            "method.response.header.Access-Control-Allow-Methods", Boolean.TRUE
                        )
                    )
                    .build()
            )
        )
        .build();
        return options;
    }
    private LambdaIntegration buildLambdaIntegration (Function handler) {
        return new LambdaIntegration(handler, LambdaIntegrationOptions.builder()
        .requestTemplates(Map.of("application/json", "{\"statusCode\": 200}"))
        .build());
    }

    private void enableCors(Resource resource) {
        MethodOptions options = MethodOptions.builder().methodResponses(
            Arrays.asList(
                MethodResponse.builder()
                    .statusCode("200")
                    .responseModels(Map.of("application/json", Model.EMPTY_MODEL))
                    .responseParameters(
                        Map.of(
                            "method.response.header.Access-Control-Allow-Origin", Boolean.TRUE,
                            "method.response.header.Access-Control-Allow-Headers", Boolean.TRUE,
                            "method.response.header.Access-Control-Allow-Methods", Boolean.TRUE
                        )
                    )
                    .build()
            )
        ).build();
        MockIntegration integration = new MockIntegration(IntegrationOptions.builder()
            .requestTemplates(new HashMap<String, String>() {{
                put("application/json", "{ \"statusCode\": 200 }");
            }})
            .integrationResponses(
                Arrays.asList(
                    IntegrationResponse.builder().statusCode("200")
                        .responseParameters(
                            Map.of(
                                "method.response.header.Access-Control-Allow-Origin", "'*'",
                                "method.response.header.Access-Control-Allow-Headers", "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,id-client'",
                                "method.response.header.Access-Control-Allow-Methods", "'GET,POST,PATCH,DELETE,OPTIONS'"
                            )
                        )
                        .responseTemplates(
                            Map.of("application/json", "{ \"statusCode\": 200 }")
                        )
                        .build()
                )
            ).passthroughBehavior(PassthroughBehavior.WHEN_NO_MATCH)
            .build()
        );

        resource.addMethod("OPTIONS", integration,options);

    }
}
