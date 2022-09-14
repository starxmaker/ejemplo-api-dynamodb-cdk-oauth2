# Ejemplo Api Dynamo Project

## Levantar proyecto de manera local

Primero, debe levantarse un contenedor de DynamoDB de forma local

    docker run --publish 8000:8000 amazon/dynamodb-local:1.11.477 -jar DynamoDBLocal.jar -inMemory -sharedDb

Segundo, construimos una tabla en el contenedor

    aws --endpoint-url=http://localhost:8000 dynamodb create-table --cli-input-json file://./src/test/resources/ProductTableSchema.json

Tercero, creamos datos de prueba

    aws --endpoint-url=http://localhost:8000 dynamodb batch-write-item  --request-items file://./src/test/resources/ProductSampleRows.json

Por último, levantamos el proyecto apuntando a nuestro contenedor

    mvn quarkus:dev -Dquarkus.dynamodb.endpoint-override=http://localhost:8000
     
## Construir

    quarkus build --native