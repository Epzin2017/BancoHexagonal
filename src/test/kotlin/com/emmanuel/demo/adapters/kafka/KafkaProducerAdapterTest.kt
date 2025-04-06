package com.emmanuel.demo.adapters.kafka

import org.apache.kafka.clients.producer.RecordMetadata
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.util.concurrent.CompletableFuture
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class KafkaProducerAdapterTest {

 @Mock
 lateinit var kafkaTemplate: KafkaTemplate<String, String>

 @InjectMocks
 lateinit var producerAdapter: KafkaProducerAdapter

 @Test
 fun `deve publicar mensagem com sucesso `() {
  // Arrange: define o tópico e mensagem
  val topico = "transacoes"
  val mensagem = "Teste de mensagem"

  // Cria um CompletableFuture para simular o retorno do send()
  val future = CompletableFuture<SendResult<String, String>>()

  // Cria mocks para SendResult e RecordMetadata, simulando os metadados
  val sendResult = mock(SendResult::class.java) as SendResult<String, String>
  val recordMetadata = mock(RecordMetadata::class.java)
  `when`(recordMetadata.partition()).thenReturn(1)
  `when`(recordMetadata.offset()).thenReturn(100L)
  `when`(sendResult.recordMetadata).thenReturn(recordMetadata)

  // Configura o KafkaTemplate para retornar o future simulado
  `when`(kafkaTemplate.send(topico, mensagem)).thenReturn(future)

  // Act: chama o método do adapter (que internamente usa future.whenComplete { ... })
  producerAdapter.enviarMensagem(topico, mensagem)

  // Simula o sucesso do envio: completa o future com o sendResult
  future.complete(sendResult)

  // Assert: verifica se o método send foi chamado com os parâmetros corretos
  verify(kafkaTemplate, org.mockito.Mockito.times(1)).send(topico, mensagem)
  // Opcionalmente, podemos confirmar que os metadados do sendResult não estão nulos
  assertNotNull(sendResult.recordMetadata)
 }

 @Test
 fun `deve registrar erro ao publicar mensagem `() {
  // Arrange: define um tópico e mensagem para o cenário de erro
  val topico = "transacoes"
  val mensagem = "Mensagem de erro"

  val future = CompletableFuture<SendResult<String, String>>()
  `when`(kafkaTemplate.send(topico, mensagem)).thenReturn(future)

  // Act: chama o método que envia a mensagem
  producerAdapter.enviarMensagem(topico, mensagem)

  // Simula o erro completando o future com uma exceção
  future.completeExceptionally(RuntimeException("Simulated Error"))

  // Assert: verifica se o método send foi chamado com os parâmetros corretos
  verify(kafkaTemplate, org.mockito.Mockito.times(1)).send(topico, mensagem)
 }
}