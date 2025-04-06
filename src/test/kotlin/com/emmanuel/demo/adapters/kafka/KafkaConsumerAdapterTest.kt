import com.emmanuel.demo.adapters.kafka.KafkaConsumerAdapter
import com.emmanuel.demo.domain.entity.Conta
import com.emmanuel.demo.domain.entity.Transacao
import com.emmanuel.demo.domain.ports.output.ContaRepository
import com.emmanuel.demo.domain.ports.output.TransacaoRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class KafkaConsumerAdapterTest {

 @Mock
 private lateinit var contaRepository: ContaRepository

 @Mock
 private lateinit var transacaoRepository: TransacaoRepository

 @InjectMocks
 private lateinit var kafkaConsumerAdapter: KafkaConsumerAdapter

 @Test
 fun `deve processar mensagem de deposito com sucesso`() {
  // Given
  val mensagem = "trans123,conta456,DEPOSITO,100.0,2025-04-05T12:00:00"
  val conta = Conta(
   id = "conta456",
   nome = "João",
   cpf = "12345678901",
   dataNascimento = LocalDate.of(1990, 1, 1),
   saldo = 200.0
  )
  val transacao = Transacao(
   id = "trans123",
   idConta = "conta456",
   tipo = "DEPOSITO",
   valor = 100.0,
   dataHora = LocalDateTime.parse("2025-04-05T12:00:00")
  )

  `when`(contaRepository.buscarPorId("conta456")).thenReturn(conta)
  `when`(transacaoRepository.salvar(transacao)).thenReturn(transacao)

  // When
  kafkaConsumerAdapter.processarMensagem(mensagem)

  // Then
  verify(contaRepository, times(1)).buscarPorId("conta456")
  verify(transacaoRepository, times(1)).salvar(transacao)
  assertEquals(300.0, conta.obterSaldo()) // Verifica saldo atualizado
 }

 @Test
 fun `deve processar mensagem de saque com sucesso`() {
  // Given
  val mensagem = "trans123,conta456,SAQUE,50.0,2025-04-05T12:00:00"
  val conta = Conta(
   id = "conta456",
   nome = "João",
   cpf = "12345678901",
   dataNascimento = LocalDate.of(1990, 1, 1),
   saldo = 200.0
  )
  val transacao = Transacao(
   id = "trans123",
   idConta = "conta456",
   tipo = "SAQUE",
   valor = 50.0,
   dataHora = LocalDateTime.parse("2025-04-05T12:00:00")
  )

  `when`(contaRepository.buscarPorId("conta456")).thenReturn(conta)
  `when`(transacaoRepository.salvar(transacao)).thenReturn(transacao)

  // When
  kafkaConsumerAdapter.processarMensagem(mensagem)

  // Then
  verify(contaRepository, times(1)).buscarPorId("conta456")
  verify(transacaoRepository, times(1)).salvar(transacao)
  assertEquals(150.0, conta.obterSaldo()) // Verifica saldo atualizado
 }

 @Test
 fun `deve registrar erro ao processar mensagem invalida`() {
  // Given
  val mensagem = "trans123,conta456,DEPOSITO,100.0" // Formato inválido (faltando campos)

  // When
  kafkaConsumerAdapter.processarMensagem(mensagem)

  // Then
  verifyNoInteractions(contaRepository)
  verifyNoInteractions(transacaoRepository)
 }

 @Test
 fun `deve registrar erro ao processar mensagem com conta inexistente`() {
  // Given
  val mensagem = "trans123,conta456,DEPOSITO,100.0,2025-04-05T12:00:00"

  `when`(contaRepository.buscarPorId("conta456")).thenReturn(null) // Conta não encontrada

  // When
  kafkaConsumerAdapter.processarMensagem(mensagem)

  // Then
  verify(contaRepository, times(1)).buscarPorId("conta456")
  verifyNoInteractions(transacaoRepository) // Nenhuma transação deve ser salva
 }

 @Test
 fun `deve registrar erro ao processar mensagem com tipo de transacao invalido`() {
  // Given
  val mensagem = "trans123,conta456,PAGAMENTO,100.0,2025-04-05T12:00:00" // Tipo inválido

  // When
  kafkaConsumerAdapter.processarMensagem(mensagem)

  // Then
  // Verifica que nenhum método dos mocks foi chamado
  verifyNoInteractions(contaRepository)
  verifyNoInteractions(transacaoRepository)
 }

}