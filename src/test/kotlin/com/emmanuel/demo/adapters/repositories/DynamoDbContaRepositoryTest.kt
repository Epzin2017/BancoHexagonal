package com.emmanuel.demo.adapters.repositories

import com.emmanuel.demo.domain.entity.Conta
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
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import software.amazon.awssdk.services.dynamodb.model.ScanResponse
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.QueryResponse
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class DynamoDbContaRepositoryTest {

 @Mock
 lateinit var dynamoDbClient: DynamoDbClient

 @InjectMocks
 lateinit var repository: DynamoDbContaRepository

 @Test
 fun `salvar deve enviar conta e retornar a conta`() {
  // Arrange
  val conta = Conta(
   id = "1",
   nome = "Fulano",
   cpf = "12345678900",
   dataNascimento = LocalDate.parse("1990-01-01"),
   email = "fulano@example.com",
   telefone = "123456789",
   saldo = 100.0
  )
  // Como o método não usa o retorno do putItem, basta simular um PutItemResponse vazio.
  `when`(dynamoDbClient.putItem(any(PutItemRequest::class.java)))
   .thenReturn(PutItemResponse.builder().build())

  // Act
  val resultado = repository.salvar(conta)

  // Assert
  assertEquals(conta, resultado)
  verify(dynamoDbClient).putItem(any(PutItemRequest::class.java))
 }

 @Test
 fun `buscarPorId deve retornar conta quando encontrada`() {
  // Arrange
  val id = "1"
  val itemMap = mapOf(
   "id" to AttributeValue.builder().s("1").build(),
   "nome" to AttributeValue.builder().s("Fulano").build(),
   "cpf" to AttributeValue.builder().s("12345678900").build(),
   "dataNascimento" to AttributeValue.builder().s("1990-01-01").build(),
   "saldo" to AttributeValue.builder().n("100.0").build(),
   "email" to AttributeValue.builder().s("fulano@example.com").build(),
   "telefone" to AttributeValue.builder().s("123456789").build()
  )
  val response = GetItemResponse.builder().item(itemMap).build()
  `when`(dynamoDbClient.getItem(any(GetItemRequest::class.java))).thenReturn(response)

  // Act
  val conta = repository.buscarPorId(id)

  // Assert
  assertNotNull(conta)
  assertEquals("1", conta?.id)
  assertEquals("Fulano", conta?.nome)
  assertEquals("12345678900", conta?.cpf)
  assertEquals(LocalDate.parse("1990-01-01"), conta?.dataNascimento)
  assertEquals("fulano@example.com", conta?.email)
  assertEquals("123456789", conta?.telefone)
  assertEquals(100.0, conta?.obterSaldo())
  verify(dynamoDbClient).getItem(any(GetItemRequest::class.java))
 }

 @Test
 fun `buscarPorId deve retornar null quando conta nao encontrada`() {
  // Arrange
  // Simula resposta sem item (ou com mapa vazio)
  val response = GetItemResponse.builder().item(emptyMap()).build()
  `when`(dynamoDbClient.getItem(any(GetItemRequest::class.java))).thenReturn(response)

  // Act
  val conta = repository.buscarPorId("1")

  // Assert
  assertNull(conta)
  verify(dynamoDbClient).getItem(any(GetItemRequest::class.java))
 }

 @Test
 fun `listaTodas deve retornar lista de contas`() {
  // Arrange
  val itemMap = mapOf(
   "id" to AttributeValue.builder().s("1").build(),
   "nome" to AttributeValue.builder().s("Fulano").build(),
   "cpf" to AttributeValue.builder().s("12345678900").build(),
   "dataNascimento" to AttributeValue.builder().s("1990-01-01").build(),
   "saldo" to AttributeValue.builder().n("100.0").build(),
   "email" to AttributeValue.builder().s("fulano@example.com").build(),
   "telefone" to AttributeValue.builder().s("123456789").build()
  )
  val scanResponse = ScanResponse.builder().items(listOf(itemMap)).build()
  `when`(dynamoDbClient.scan(any(ScanRequest::class.java))).thenReturn(scanResponse)

  // Act
  val contas = repository.listaTodas()

  // Assert
  assertEquals(1, contas.size)
  val conta = contas[0]
  assertEquals("1", conta.id)
  assertEquals("Fulano", conta.nome)
  verify(dynamoDbClient).scan(any(ScanRequest::class.java))
 }

 @Test
 fun `buscarPorCpf deve retornar conta quando encontrada`() {
  // Arrange
  val cpf = "12345678900"
  val itemMap = mapOf(
   "id" to AttributeValue.builder().s("1").build(),
   "nome" to AttributeValue.builder().s("Fulano").build(),
   "cpf" to AttributeValue.builder().s(cpf).build(),
   "dataNascimento" to AttributeValue.builder().s("1990-01-01").build(),
   "saldo" to AttributeValue.builder().n("100.0").build(),
   "email" to AttributeValue.builder().s("fulano@example.com").build(),
   "telefone" to AttributeValue.builder().s("123456789").build()
  )
  val queryResponse = QueryResponse.builder().items(listOf(itemMap)).build()
  `when`(dynamoDbClient.query(any(QueryRequest::class.java))).thenReturn(queryResponse)

  // Act
  val conta = repository.buscarPorCpf(cpf)

  // Assert
  assertNotNull(conta)
  assertEquals(cpf, conta?.cpf)
  verify(dynamoDbClient).query(any(QueryRequest::class.java))
 }
}