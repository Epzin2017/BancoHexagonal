// Localizado em: com.emmanuel.demo.adapters.kafka.KafkaConsumerAdapter.kt
package com.emmanuel.demo.adapters.kafka

import com.emmanuel.demo.domain.entity.Transacao
import com.emmanuel.demo.domain.ports.output.ContaRepository
import com.emmanuel.demo.domain.ports.output.TransacaoRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class KafkaConsumerAdapter(
    private val contaRepository: ContaRepository,
    private val transacaoRepository: TransacaoRepository
) {

    private val logger = LoggerFactory.getLogger(KafkaConsumerAdapter::class.java)

    @KafkaListener(topics = ["transacoes"], groupId = "banco-grupo")
    fun processarMensagem(mensagem: String) {
        logger.info("Mensagem recebida do Kafka: $mensagem")
        try {
            val dados = mensagem.split(",")
            if (dados.size != 5) {
                throw IllegalArgumentException("Formato de mensagem inválido: $mensagem")
            }
            // Reconstrói a transação a partir da mensagem CSV
            val transacao = Transacao(
                id = dados[0],
                idConta = dados[1],
                tipo = dados[2],
                valor = dados[3].toDouble(),
                dataHora = LocalDateTime.parse(dados[4])
            )

            // Atualiza o saldo da conta com base no tipo de transação
            val conta = contaRepository.buscarPorId(transacao.idConta)
                ?: throw IllegalArgumentException("Conta não encontrada com ID: ${transacao.idConta}")

            when (transacao.tipo) {
                "DEPOSITO" -> conta.depositar(transacao.valor)
                "SAQUE" -> conta.sacar(transacao.valor)
                else -> throw IllegalArgumentException("Tipo de transação inválido: ${transacao.tipo}")
            }

            // Registra a transação
            transacaoRepository.salvar(transacao)

            logger.info("Transação processada com sucesso: $transacao")
        } catch (e: Exception) {
            logger.error("Erro ao processar a mensagem: ${e.message}", e)
        }
    }
}