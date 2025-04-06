// Localizado em: com.emmanuel.demo.application.services.ContaService.kt
package com.emmanuel.demo.application.services

import com.emmanuel.demo.domain.entity.Conta
import com.emmanuel.demo.domain.entity.Transacao
import com.emmanuel.demo.domain.exceptions.AccountNotFoundException
import com.emmanuel.demo.domain.ports.input.ContaServicePort
import com.emmanuel.demo.domain.ports.output.ContaRepository
import com.emmanuel.demo.domain.ports.output.MessagePublisher
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class ContaService(
    private val contaRepository: ContaRepository,
    private val messagePublisher: MessagePublisher
) : ContaServicePort {

    override fun criarConta(conta: Conta): String {
        if (contaRepository.buscarPorCpf(conta.cpf) != null) {
            throw IllegalArgumentException("Já existe uma conta cadastrada com o CPF: ${conta.cpf}")
        }
        contaRepository.salvar(conta)
        return "Conta criada com sucesso. Número da conta: ${conta.id}, Titular: ${conta.nome}"
    }

    override fun buscarContaPorId(id: String): Conta {
        return contaRepository.buscarPorId(id)
            ?: throw AccountNotFoundException("Conta não encontrada com o ID: $id")
    }

    override fun listarTodasContas(): List<Conta> {
        return contaRepository.listaTodas()
    }

    override fun depositar(id: String, valor: Double?): String {
        val conta = contaRepository.buscarPorId(id)
            ?: throw AccountNotFoundException("NEGADO:Conta não encontrada com o ID: $id")
        conta.depositar(valor)
        contaRepository.salvar(conta)

        // Cria a transação com um ID único e dados atuais
        val transacao = Transacao(
            id = UUID.randomUUID().toString(),
            idConta = conta.id,
            tipo = "DEPOSITO",
            valor = valor!!,
            dataHora = LocalDateTime.now()
        )

        // Converte a transação para uma string CSV (pode-se optar por JSON)
        val mensagem = "${transacao.id},${transacao.idConta},${transacao.tipo},${transacao.valor},${transacao.dataHora}"
        messagePublisher.enviarMensagem("transacoes", mensagem)

        return "EFETUADO:Depósito efetuado com sucesso."
    }

    override fun sacar(id: String, valor: Double?): String {
        val conta = contaRepository.buscarPorId(id)
            ?: throw AccountNotFoundException("NEGADO:Conta não encontrada com o ID: $id")
        conta.sacar(valor)
        contaRepository.salvar(conta)

        val transacao = Transacao(
            id = UUID.randomUUID().toString(),
            idConta = conta.id,
            tipo = "SAQUE",
            valor = valor!!,
            dataHora = LocalDateTime.now()
        )
        val mensagem = "${transacao.id},${transacao.idConta},${transacao.tipo},${transacao.valor},${transacao.dataHora}"
        messagePublisher.enviarMensagem("transacoes", mensagem)

        return "EFETUADO:Saque efetuado com sucesso."
    }
}