package com.myorg;

import software.constructs.Construct;

import java.util.List;

import com.myorg.managers.ApiGatewayManager;
import com.myorg.managers.CognitoManager;
import com.myorg.managers.DynamoManager;
import com.myorg.managers.IamManager;
import com.myorg.managers.LambdaManager;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.Authorizer;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.cognito.ResourceServerScope;
import software.amazon.awscdk.services.cognito.UserPool;
import software.amazon.awscdk.services.cognito.UserPoolClient;
import software.amazon.awscdk.services.cognito.UserPoolResourceServer;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.lambda.Function;
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;

public class CdkStack extends Stack {
    public CdkStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public CdkStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
        CognitoManager cognitoManager = new CognitoManager(this);
        UserPool userPool = cognitoManager.createUserPool();
        DynamoManager dynamoManager = new DynamoManager(this);
        Table productsTable = dynamoManager.createProductsTable();
        IamManager iamManager = new IamManager(this);
        Role lambdaRole = iamManager.createLambdaRole(productsTable);
        LambdaManager lambdaManager = new LambdaManager(this);
        Function lambdaHandler = lambdaManager.configureLambdaFunction(lambdaRole);
        ApiGatewayManager apiGatewayManager = new ApiGatewayManager(this);
        Authorizer authorizer = apiGatewayManager.createAuthorizer(userPool);
        RestApi restApi = apiGatewayManager.createApi(lambdaHandler, authorizer);
        
    }
}
