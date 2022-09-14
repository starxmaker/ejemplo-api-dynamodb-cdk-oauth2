package com.myorg.managers;

import java.util.Map;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.cognito.UserPoolClient;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

public class LambdaManager {
    private Construct scope;
    public LambdaManager(Construct scope) {
        this.scope = scope;
    }
   
    public Function configureLambdaFunction(Role role) {
        String binaryPath = "../target/function.zip";
        return new Function(this.scope, "ejemplo-crud-dynamo-cdk-lambda", FunctionProps.builder()
                .functionName("ejemplo-crud-dynamo-cdk-lambda")
                .runtime(Runtime.PROVIDED)
                .environment(Map.of("DISABLE_SIGNAL_HANDLERS", "true"))
                .handler("not.used.in.provided.runtimei")
                .code(Code.fromAsset(binaryPath))
                .role(role)
                .timeout(Duration.seconds(30))
                .memorySize(128)
                .build());
    }
}
