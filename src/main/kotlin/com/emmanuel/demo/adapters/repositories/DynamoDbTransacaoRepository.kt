package com.emmanuel.demo.adapters.repositories

import com.emmanuel.demo.domain.entity.Transacao
import com.emmanuel.demo.domain.ports.output.TransacaoRepository
import org.springframework.stereotype.Repository
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import java.time.LocalDateTime
import org.slf4j.LoggerFactory

@Repository
class DynamoDbTransacaoRepository(private val dynamoDbClient: DynamoDbClient) : TransacaoRepository {

    private val logger = LoggerFactory.getLogger(DynamoDbTransacaoRepository::class.java)

    override fun salvar(transacao: Transacao): Transacao {
        return try {
            val request = PutItemRequest.builder()
                .tableName("Transacoes")
                .item(
                    mapOf(
                        "id" to AttributeValue.builder().s(transacao.id).build(),
                        "idConta" to AttributeValue.builder().s(transacao.idConta).build(),
                        "tipo" to AttributeValue.builder().s(transacao.tipo).build(),
                        "valor" to AttributeValue.builder().n(transacao.valor.toString()).build(),
                        "dataHora" to AttributeValue.builder().s(transacao.dataHora.toString()).build()
                    )
                )
                .build()

            dynamoDbClient.putItem(request)
            logger.info("Transação salva com sucesso no DynamoDB: ${transacao.id}")
            transacao
        } catch (e: Exception) {
            logger.error("Erro ao salvar transação no DynamoDB: ${e.message}", e)
            throw e
        }
    }

    override fun buscarPorConta(idConta: String): List<Transacao> {
        return try {
            val request = QueryRequest.builder()
                .tableName("Transacoes")
                .indexName("idConta-index")
                .keyConditionExpression("idConta = :idConta")
                .expressionAttributeValues(
                    mapOf(":idConta" to AttributeValue.builder().s(idConta).build())
                )
                .build()

            val result = dynamoDbClient.query(request)
            result.items().map { mapItemToTransacao(it) }
        } catch (e: Exception) {
            logger.error("Erro ao buscar transações pelo idConta no DynamoDB: ${e.message}", e)
            throw e
        }
    }

    override fun listarTodas(): List<Transacao> {
        return try {
            val request = ScanRequest.builder()
                .tableName("Transacoes")
                .build()

            val result = dynamoDbClient.scan(request)
            result.items().map { mapItemToTransacao(it) }
        } catch (e: Exception) {
            logger.error("Erro ao listar todas as transações no DynamoDB: ${e.message}", e)
            throw e
        }
    }

    private fun mapItemToTransacao(item: Map<String, AttributeValue>): Transacao {
        return Transacao(
            id = item["id"]?.s() ?: "",
            idConta = item["idConta"]?.s() ?: "",
            tipo = item["tipo"]?.s() ?: "",
            valor = item["valor"]?.n()?.toDouble() ?: 0.0,
            dataHora = LocalDateTime.parse(item["dataHora"]?.s())
        )
    }
}