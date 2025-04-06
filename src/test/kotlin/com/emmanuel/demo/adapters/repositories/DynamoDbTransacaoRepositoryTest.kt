package com.emmanuel.demo.adapters.repositories

import com.emmanuel.demo.domain.entity.Transacao
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.QueryResponse
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import software.amazon.awssdk.services.dynamodb.model.ScanResponse
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class DynamoDbTransacaoRepositoryTest {

 @Mock
 lateinit var dynamoDbClient: DynamoDbClient

 @InjectMocks
 lateinit var repository: DynamoDbTransacaoRepository

 @Test
 fun `salvar transacao com sucesso`() {
  // Arrange
  val dataHoraStr = "2023-01-01T12:00:00"
  val dataHora = LocalDateTime.parse(dataHoraStr)
  val transacao = Transacao(
   id = "t1",
   idConta = "c1",
   tipo = "DEPOSITO",
   valor = 150.0,
   dataHora = dataHora
  )
  `when`(dynamoDbClient.putItem(any(PutItemRequest::class.java)))
   .thenReturn(PutItemResponse.builder().build())

  // Act
  val resultado = repository.salvar(transacao)

  // Assert
  assertEquals(transacao, resultado)
  verify(dynamoDbClient).putItem(any(PutItemRequest::class.java))
 }

 @Test
 fun `buscarPorConta retorna lista de transacoes`() {
  // Arrange
  val dataHoraStr = "2023-01-01T12:00:00"
  val itemMap = mapOf(
   "id" to AttributeValue.builder().s("t1").build(),
   "idConta" to AttributeValue.builder().s("c1").build(),
   "tipo" to AttributeValue.builder().s("SAQUE").build(),
   "valor" to AttributeValue.builder().n("50.0").build(),
   "dataHora" to AttributeValue.builder().s(dataHoraStr).build()
  )
  val queryResponse = QueryResponse.builder().items(listOf(itemMap)).build()
  `when`(dynamoDbClient.query(any(QueryRequest::class.java))).thenReturn(queryResponse)

  // Act
  val transacoes = repository.buscarPorConta("c1")

  // Assert
  assertNotNull(transacoes)
  assertTrue(transacoes.isNotEmpty())
  val transacao = transacoes[0]
  assertEquals("t1", transacao.id)
  assertEquals("c1", transacao.idConta)
  assertEquals("SAQUE", transacao.tipo)
  assertEquals(50.0, transacao.valor, 0.0001)
  assertEquals(LocalDateTime.parse(dataHoraStr), transacao.dataHora)
  verify(dynamoDbClient).query(any(QueryRequest::class.java))
 }

 @Test
 fun `listarTodas retorna lista de transacoes`() {
  // Arrange
  val dataHoraStr = "2023-01-01T12:00:00"
  val itemMap = mapOf(
   "id" to AttributeValue.builder().s("t1").build(),
   "idConta" to AttributeValue.builder().s("c1").build(),
   "tipo" to AttributeValue.builder().s("SAQUE").build(),
   "valor" to AttributeValue.builder().n("50.0").build(),
   "dataHora" to AttributeValue.builder().s(dataHoraStr).build()
  )
  val scanResponse = ScanResponse.builder().items(listOf(itemMap)).build()
  `when`(dynamoDbClient.scan(any(ScanRequest::class.java))).thenReturn(scanResponse)

  // Act
  val transacoes = repository.listarTodas()

  // Assert
  assertNotNull(transacoes)
  assertEquals(1, transacoes.size)
  val transacao = transacoes.first()
  assertEquals("t1", transacao.id)
  assertEquals("c1", transacao.idConta)
  assertEquals("SAQUE", transacao.tipo)
  assertEquals(50.0, transacao.valor, 0.0001)
  assertEquals(LocalDateTime.parse(dataHoraStr), transacao.dataHora)
  verify(dynamoDbClient).scan(any(ScanRequest::class.java))
 }
}