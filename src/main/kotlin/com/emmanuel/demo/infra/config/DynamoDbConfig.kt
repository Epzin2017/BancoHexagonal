package com.emmanuel.demo.infra.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.beans.factory.annotation.Value
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.regions.Region
import java.net.URI

/**
 * Classe de configuração do cliente DynamoDB.
 * Ela registra o cliente como um bean no contexto Spring e configura o endpoint para o DynamoDB Local.
 */
@Configuration
class DynamoDbConfig {

    @Value("\${DYNAMODB_ENDPOINT:http://dynamodb:8000}") // Lê o endpoint do arquivo application.properties ou usa o padrão
    private lateinit var dynamoDbEndpoint: String

    /**
     * Cria e configura o cliente DynamoDB com o endpoint correto.
     * O bean será usado por outras classes como DynamoDbTableInitializer.
     */
    @Bean
    fun dynamoDbClient(): DynamoDbClient {
        return DynamoDbClient.builder()
            .endpointOverride(URI.create(dynamoDbEndpoint)) // Configura o endpoint para o DynamoDB Local
            .region(Region.US_EAST_1) // Define uma região arbitrária, pois o DynamoDB Local não verifica isso
            .build()
    }
}