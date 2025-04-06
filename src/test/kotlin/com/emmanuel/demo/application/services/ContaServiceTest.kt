package com.emmanuel.demo.application.services

import com.emmanuel.demo.domain.entity.Conta
import com.emmanuel.demo.domain.exceptions.AccountNotFoundException
import com.emmanuel.demo.domain.ports.output.ContaRepository
import com.emmanuel.demo.domain.ports.output.MessagePublisher
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class ContaServiceTest {

 @Mock
 lateinit var contaRepository: ContaRepository

 @Mock
 lateinit var messagePublisher: MessagePublisher

 @InjectMocks
 lateinit var contaService: ContaService

 @Test
 fun `criarConta deve criar conta quando nao existe nenhuma com o mesmo CPF`() {
  // Arrange
  val dummyConta = Conta(
   id = "1",
   nome = "Fulano",
   cpf = "12345678900",
   dataNascimento = LocalDate.parse("1990-01-01"),
   email = "fulano@example.com",
   telefone = "123456789",
   saldo = 0.0
  )
  `when`(contaRepository.buscarPorCpf(dummyConta.cpf)).thenReturn(null)
  `when`(contaRepository.salvar(dummyConta)).thenReturn(dummyConta)

  // Act
  val resposta = contaService.criarConta(dummyConta)

  // Assert
  val mensagemEsperada = "Conta criada com sucesso. Número da conta: ${dummyConta.id}, Titular: ${dummyConta.nome}"
  assertEquals(mensagemEsperada, resposta)
  verify(contaRepository).buscarPorCpf(dummyConta.cpf)
  verify(contaRepository).salvar(dummyConta)
 }

 @Test
 fun `criarConta deve lancar IllegalArgumentException se ja existir conta com o mesmo CPF`() {
  // Arrange
  val dummyConta = Conta(
   id = "1",
   nome = "Fulano",
   cpf = "12345678900",
   dataNascimento = LocalDate.parse("1990-01-01"),
   email = "fulano@example.com",
   telefone = "123456789",
   saldo = 0.0
  )
  `when`(contaRepository.buscarPorCpf(dummyConta.cpf)).thenReturn(dummyConta)

  // Act & Assert
  val exception = assertFailsWith<IllegalArgumentException> {
   contaService.criarConta(dummyConta)
  }
  assertEquals("Já existe uma conta cadastrada com o CPF: ${dummyConta.cpf}", exception.message)
  verify(contaRepository).buscarPorCpf(dummyConta.cpf)
  verify(contaRepository, never()).salvar(any(Conta::class.java))
 }

 @Test
 fun `buscarContaPorId deve retornar a conta encontrada`() {
  // Arrange
  val dummyConta = Conta(
   id = "1",
   nome = "Fulano",
   cpf = "12345678900",
   dataNascimento = LocalDate.parse("1990-01-01"),
   email = "fulano@example.com",
   telefone = "123456789",
   saldo = 0.0
  )
  `when`(contaRepository.buscarPorId("1")).thenReturn(dummyConta)

  // Act
  val contaRetornada = contaService.buscarContaPorId("1")

  // Assert
  assertNotNull(contaRetornada)
  assertEquals(dummyConta, contaRetornada)
  verify(contaRepository).buscarPorId("1")
 }

 @Test
 fun `buscarContaPorId deve lancar AccountNotFoundException quando conta nao for encontrada`() {
  // Arrange
  `when`(contaRepository.buscarPorId("1")).thenReturn(null)

  // Act & Assert
  val exception = assertFailsWith<AccountNotFoundException> {
   contaService.buscarContaPorId("1")
  }
  assertEquals("Conta não encontrada com o ID: 1", exception.message)
  verify(contaRepository).buscarPorId("1")
 }

 @Test
 fun `listarTodasContas deve retornar a lista de contas`() {
  // Arrange
  val conta1 = Conta(
   id = "1",
   nome = "Fulano",
   cpf = "12345678900",
   dataNascimento = LocalDate.parse("1990-01-01"),
   email = "fulano@example.com",
   telefone = "123456789",
   saldo = 0.0
  )
  val conta2 = Conta(
   id = "2",
   nome = "Beltrano",
   cpf = "98765432100",
   dataNascimento = LocalDate.parse("1985-05-05"),
   email = "beltrano@example.com",
   telefone = "987654321",
   saldo = 0.0
  )
  `when`(contaRepository.listaTodas()).thenReturn(listOf(conta1, conta2))

  // Act
  val lista = contaService.listarTodasContas()

  // Assert
  assertEquals(2, lista.size)
  assertEquals(conta1, lista[0])
  assertEquals(conta2, lista[1])
  verify(contaRepository).listaTodas()
 }

 @Test
 fun `depositar deve efetuar deposito e publicar transacao quando a conta existe `() {
  // Arrange
  val idConta = "1"
  val valorDeposito = 100.0
  val dummyConta = Conta(
   id = idConta,
   nome = "Fulano",
   cpf = "12345678900",
   dataNascimento = LocalDate.parse("1990-01-01"),
   email = "fulano@example.com",
   telefone = "123456789",
   saldo = 0.0
  )
  `when`(contaRepository.buscarPorId(idConta)).thenReturn(dummyConta)
  `when`(contaRepository.salvar(any(Conta::class.java))).thenAnswer { it.getArgument(0) }

  // Act
  val resposta = contaService.depositar(idConta, valorDeposito)

  // Assert
  assertEquals("EFETUADO:Depósito efetuado com sucesso.", resposta)
  val captor: ArgumentCaptor<Conta> = ArgumentCaptor.forClass(Conta::class.java)
  verify(contaRepository).salvar(captor.capture())
  val savedConta = captor.value
  assertEquals(100.0, savedConta.obterSaldo(), 0.0001)
  // Aqui usamos anyString() em vez de any(String::class.java)
  verify(messagePublisher).enviarMensagem(eq("transacoes"), anyString())
 }

 @Test
 fun `sacar deve efetuar saque e publicar transacao quando a conta existe `() {
  // Arrange
  val idConta = "1"
  val valorSaque = 50.0
  val dummyConta = Conta(
   id = idConta,
   nome = "Fulano",
   cpf = "12345678900",
   dataNascimento = LocalDate.parse("1990-01-01"),
   email = "fulano@example.com",
   telefone = "123456789",
   saldo = 100.0
  )
  `when`(contaRepository.buscarPorId(idConta)).thenReturn(dummyConta)
  `when`(contaRepository.salvar(any(Conta::class.java))).thenAnswer { it.getArgument(0) }

  // Act
  val resposta = contaService.sacar(idConta, valorSaque)

  // Assert
  assertEquals("EFETUADO:Saque efetuado com sucesso.", resposta)
  val captor: ArgumentCaptor<Conta> = ArgumentCaptor.forClass(Conta::class.java)
  verify(contaRepository).salvar(captor.capture())
  val savedConta = captor.value
  assertEquals(50.0, savedConta.obterSaldo(), 0.0001)
  verify(messagePublisher).enviarMensagem(eq("transacoes"), anyString())
 }
}