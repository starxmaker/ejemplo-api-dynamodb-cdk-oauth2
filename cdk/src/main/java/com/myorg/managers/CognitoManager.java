package com.myorg.managers;

import java.util.ArrayList;
import java.util.List;

import software.amazon.awscdk.services.cognito.AutoVerifiedAttrs;
import software.amazon.awscdk.services.cognito.CognitoDomainOptions;
import software.amazon.awscdk.services.cognito.OAuthFlows;
import software.amazon.awscdk.services.cognito.OAuthScope;
import software.amazon.awscdk.services.cognito.OAuthSettings;
import software.amazon.awscdk.services.cognito.ResourceServerScope;
import software.amazon.awscdk.services.cognito.ResourceServerScopeProps;
import software.amazon.awscdk.services.cognito.StandardAttribute;
import software.amazon.awscdk.services.cognito.StandardAttributes;
import software.amazon.awscdk.services.cognito.UserPool;
import software.amazon.awscdk.services.cognito.UserPoolClient;
import software.amazon.awscdk.services.cognito.UserPoolClientIdentityProvider;
import software.amazon.awscdk.services.cognito.UserPoolClientOptions;
import software.amazon.awscdk.services.cognito.UserPoolDomain;
import software.amazon.awscdk.services.cognito.UserPoolDomainProps;
import software.amazon.awscdk.services.cognito.UserPoolProps;
import software.amazon.awscdk.services.cognito.UserPoolResourceServer;
import software.amazon.awscdk.services.cognito.UserPoolResourceServerProps;
import software.constructs.Construct;

public class CognitoManager {
    private Construct scope;
    public CognitoManager(Construct scope) {
        this.scope = scope;
    }
    public UserPool createUserPool() {
        UserPool pool = new UserPool(scope, "example-dynamo-user-pool", UserPoolProps.builder()
            .userPoolName("ExampleDynamoUserPool")
            .selfSignUpEnabled(true)
            .standardAttributes(StandardAttributes.builder()
                .email(StandardAttribute.builder()
                    .required(true)
                    .build())
                .build())
            .autoVerify(AutoVerifiedAttrs.builder()
                .email(true)
                .build())
        .build());
        createUserPoolDomain(pool);
        List<ResourceServerScope> scopes = createScopes();
        UserPoolResourceServer resourceServer = createUserPoolResourceServer(pool, "ExampleResourceServer", scopes);
        UserPoolClient userPoolClient = createUserPoolClient(pool, resourceServer, scopes);
        return pool;
    }
    private UserPoolDomain createUserPoolDomain(UserPool userPool) {
        return new UserPoolDomain(scope, "example-dynamo-user-pool-domain", UserPoolDomainProps.builder()
            .userPool(userPool)
            .cognitoDomain(CognitoDomainOptions.builder().domainPrefix("example-dynamo-user-pool").build())
        .build());
    }
    private UserPoolClient createUserPoolClient(UserPool userpool, UserPoolResourceServer resourceServer, List<ResourceServerScope> scopes) {
        List<OAuthScope> finalScopes = new ArrayList<OAuthScope>() {{
            add(OAuthScope.COGNITO_ADMIN);
            add(OAuthScope.EMAIL);
            add(OAuthScope.OPENID);
            add(OAuthScope.PROFILE);
            add(OAuthScope.PHONE);
        }};
        scopes.forEach(scope -> finalScopes.add(OAuthScope.resourceServer(resourceServer, scope)));
        return userpool.addClient("example-dynamo-user-pool-client", UserPoolClientOptions.builder()
            .userPoolClientName("ExampleDynamoUserPoolClient")
            .generateSecret(true)
            .supportedIdentityProviders(List.of(UserPoolClientIdentityProvider.COGNITO))
            .oAuth(OAuthSettings.builder()
                .callbackUrls(List.of("https://app.swaggerhub.com/oauth2_redirect"))
                .flows(OAuthFlows.builder().authorizationCodeGrant(true).implicitCodeGrant(true).build())
                .scopes(finalScopes)
                .build()
            )
        .build());
    }

    private List<ResourceServerScope> createScopes() {
        return List.of(
            createResourceServerScope("read_products", "Permission to read products"),
            createResourceServerScope("write_products", "Permission to write products")
        );
    }
    private ResourceServerScope createResourceServerScope(String scopeName, String scopeDescription) {
        return ResourceServerScope.Builder.create().scopeName(scopeName).scopeDescription(scopeDescription).build();
    }

    private UserPoolResourceServer createUserPoolResourceServer(UserPool userPool, String resourceServerName, List<ResourceServerScope> scopes) {
        return userPool.addResourceServer("example-dynamo-user-pool-resource-server", UserPoolResourceServerProps.builder()
        .identifier(resourceServerName)
        .scopes(scopes)
        .userPool(userPool)
        .build());
    }
    
}
