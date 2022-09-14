# Ejemplo Api Dynamo Project

Ejemplo de API escrita en Quarkus, basada en DynamoDB, desplegable a través de CDK y autenticable a través de Cognito OAuth2.
## Levantar proyecto de manera local

Primero, debe levantarse un contenedor de DynamoDB de forma local

    docker run --publish 8000:8000 amazon/dynamodb-local:1.11.477 -jar DynamoDBLocal.jar -inMemory -sharedDb

Segundo, construimos una tabla en el contenedor

    aws --endpoint-url=http://localhost:8000 dynamodb create-table --cli-input-json file://./src/test/resources/ProductTableSchema.json

Tercero, creamos datos de prueba

    aws --endpoint-url=http://localhost:8000 dynamodb batch-write-item  --request-items file://./src/test/resources/ProductSampleRows.json

Por último, levantamos el proyecto apuntando a nuestro contenedor

    mvn quarkus:dev -Dquarkus.dynamodb.endpoint-override=http://localhost:8000
     
## Pruebas

Para realizar pruebas de integración, se debe ejecutar el siguiente comando (requiere tener instalado Docker):

    mvn test
## Construir

Con el siguiente comando es posible construir un binario nativo compatible con AWS Lambda:

    quarkus build --native

## Desplegar

Para desplegar el proyecto en una cuenta propia de AWS, ejecutar los siguientes comandos en la carpeta `cdk`:

    cdk bootstrap
    cdk deploy