package com.emmanuel.demo.adapters.repositories

import com.emmanuel.demo.domain.entity.Conta
import com.emmanuel.demo.domain.ports.output.ContaRepository
import org.springframework.stereotype.Repository
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import java.time.LocalDate
import org.slf4j.LoggerFactory

@Repository
class DynamoDbContaRepository(private val dynamoDbClient: DynamoDbClient) : ContaRepository {

    private val logger = LoggerFactory.getLogger(DynamoDbContaRepository::class.java)

    override fun salvar(conta: Conta): Conta {
        // Cria um mapa mutável para os atributos obrigatórios
        val itemMap = mutableMapOf<String, AttributeValue>(
            "id" to AttributeValue.builder().s(conta.id).build(),
            "nome" to AttributeValue.builder().s(conta.nome).build(),
            "cpf" to AttributeValue.builder().s(conta.cpf).build(),
            "dataNascimento" to AttributeValue.builder().s(conta.dataNascimento.toString()).build(),
            "saldo" to AttributeValue.builder().n(conta.obterSaldo().toString()).build()
        )
        // Se o email estiver presente, adiciona o atributo
        conta.email?.let {
            itemMap["email"] = AttributeValue.builder().s(it).build()
        }
        // Se o telefone estiver presente, adiciona o atributo
        conta.telefone?.let {
            itemMap["telefone"] = AttributeValue.builder().s(it).build()
        }

        val request = PutItemRequest.builder()
            .tableName("Contas")
            .item(itemMap)
            .build()

        dynamoDbClient.putItem(request)
        logger.info("Conta ${conta.id} salva com sucesso no DynamoDB.")
        return conta
    }

    override fun buscarPorId(id: String): Conta? {
        logger.info("Buscando conta no DynamoDB com ID: $id")
        val request = GetItemRequest.builder()
            .tableName("Contas")
            .key(mapOf("id" to AttributeValue.builder().s(id).build()))
            .build()

        val result = dynamoDbClient.getItem(request)
        val item = result.item()  // Captura o item retornado

        logger.info("Resultado da busca: $item")

        // Se o item for nulo ou estiver vazio, retorna null para evitar erros de mapeamento
        if (item == null || item.isEmpty()) {
            return null
        }

        // Se há um item, converte-o para a entidade Conta
        return mapItemToConta(item)
    }

    override fun listaTodas(): List<Conta> {
        val scanRequest = ScanRequest.builder()
            .tableName("Contas")
            .build()

        val result = dynamoDbClient.scan(scanRequest)
        return result.items().map { mapItemToConta(it) }
    }

    override fun buscarPorCpf(cpf: String): Conta? {
        val request = QueryRequest.builder()
            .tableName("Contas")
            .indexName("cpf-index")
            .keyConditionExpression("cpf = :cpf")
            .expressionAttributeValues(
                mapOf(":cpf" to AttributeValue.builder().s(cpf).build())
            )
            .build()

        val result = dynamoDbClient.query(request)
        return result.items().firstOrNull()?.let { mapItemToConta(it) }
    }

    private fun mapItemToConta(item: Map<String, AttributeValue>): Conta {
        return Conta(
            id = item["id"]?.s() ?: "",
            nome = item["nome"]?.s() ?: "",
            cpf = item["cpf"]?.s() ?: "",
            dataNascimento = LocalDate.parse(item["dataNascimento"]?.s()),
            email = item["email"]?.s(),         // Se não existir, ficará null
            telefone = item["telefone"]?.s(),   // Se não existir, ficará null
            saldo = item["saldo"]?.n()?.toDouble() ?: 0.0
        )
    }
}