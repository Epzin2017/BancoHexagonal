package com.emmanuel.demo.infra.config

import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*

/**
 * Classe responsável por criar tabelas e índices no DynamoDB Local.
 * Ela usa o cliente DynamoDB configurado pela classe DynamoDbConfig.
 */
@Component
class DynamoDbTableInitializer(private val dynamoDbClient: DynamoDbClient) {

    /**
     * Método executado automaticamente após a inicialização do contexto Spring.
     * Ele chama os métodos para criar as tabelas e configurar os índices.
     */
    @PostConstruct
    fun initialize() {
        println("DynamoDbTableInitializer está sendo inicializado.")
        criarTabelaContas()      // Cria a tabela "Contas"
        criarTabelaTransacoes()  // Cria a tabela "Transacoes"
        configurarCpfIndex()     // Configura o índice "cpf-index" na tabela "Contas"
    }

    private fun criarTabelaContas() {
        val request = CreateTableRequest.builder()
            .tableName("Contas") // Nome da tabela
            .attributeDefinitions(
                AttributeDefinition.builder().attributeName("id").attributeType(ScalarAttributeType.S).build()
            ) // Define o atributo "id" como String
            .keySchema(
                KeySchemaElement.builder().attributeName("id").keyType(KeyType.HASH).build()
            ) // Define o esquema de chaves primária
            .provisionedThroughput(
                ProvisionedThroughput.builder().readCapacityUnits(1).writeCapacityUnits(1).build()
            ) // Capacidade de leitura e escrita
            .build()

        try {
            dynamoDbClient.createTable(request) // Tenta criar a tabela no DynamoDB
            println("Tabela 'Contas' criada com sucesso.")
        } catch (e: ResourceInUseException) { // Captura erro caso a tabela já exista
            println("Tabela 'Contas' já existe.")
        }
    }

    private fun criarTabelaTransacoes() {
        val request = CreateTableRequest.builder()
            .tableName("Transacoes") // Nome da tabela
            .attributeDefinitions(
                AttributeDefinition.builder().attributeName("id").attributeType(ScalarAttributeType.S).build(),
                AttributeDefinition.builder().attributeName("idConta").attributeType(ScalarAttributeType.S).build()
            ) // Define atributos "id" e "idConta"
            .keySchema(
                KeySchemaElement.builder().attributeName("id").keyType(KeyType.HASH).build()
            ) // Chave primária "id"
            .globalSecondaryIndexes(
                GlobalSecondaryIndex.builder()
                    .indexName("idConta-index") // Índice global secundário
                    .keySchema(
                        KeySchemaElement.builder().attributeName("idConta").keyType(KeyType.HASH).build()
                    ) // Esquema da chave do índice
                    .projection(
                        Projection.builder().projectionType(ProjectionType.ALL).build()
                    ) // Todos os atributos no índice
                    .provisionedThroughput(
                        ProvisionedThroughput.builder().readCapacityUnits(1).writeCapacityUnits(1).build()
                    ) // Capacidade do índice
                    .build()
            )
            .provisionedThroughput(
                ProvisionedThroughput.builder().readCapacityUnits(1).writeCapacityUnits(1).build()
            ) // Capacidade da tabela
            .build()

        try {
            dynamoDbClient.createTable(request) // Tenta criar a tabela
            println("Tabela 'Transacoes' criada com sucesso.")
        } catch (e: ResourceInUseException) { // Captura erro caso a tabela já exista
            println("Tabela 'Transacoes' já existe.")
        }
    }

    private fun configurarCpfIndex() {
        try {
            val describeTableRequest = DescribeTableRequest.builder().tableName("Contas").build()
            val describeTableResponse = dynamoDbClient.describeTable(describeTableRequest)
            val indices = describeTableResponse.table().globalSecondaryIndexes()

            if (indices.any { it.indexName() == "cpf-index" }) { // Verifica se o índice já existe
                println("Índice 'cpf-index' já está configurado na tabela 'Contas'.")
                return
            }

            val updateTableRequest = UpdateTableRequest.builder()
                .tableName("Contas")
                .attributeDefinitions(
                    AttributeDefinition.builder().attributeName("cpf").attributeType(ScalarAttributeType.S).build()
                ) // Define o atributo "cpf"
                .globalSecondaryIndexUpdates(
                    GlobalSecondaryIndexUpdate.builder()
                        .create(
                            CreateGlobalSecondaryIndexAction.builder()
                                .indexName("cpf-index") // Nome do índice
                                .keySchema(
                                    KeySchemaElement.builder().attributeName("cpf").keyType(KeyType.HASH).build()
                                ) // Esquema da chave do índice
                                .projection(
                                    Projection.builder().projectionType(ProjectionType.ALL).build()
                                ) // Projeção: Todos os atributos
                                .provisionedThroughput(
                                    ProvisionedThroughput.builder().readCapacityUnits(1).writeCapacityUnits(1).build()
                                ) // Capacidade de leitura e escrita
                                .build()
                        )
                        .build()
                )
                .build()

            dynamoDbClient.updateTable(updateTableRequest) // Adiciona o índice à tabela
            println("Índice 'cpf-index' adicionado com sucesso à tabela 'Contas'.")
        } catch (e: Exception) {
            println("Erro ao configurar o índice 'cpf-index': ${e.message}")
        }
    }
}