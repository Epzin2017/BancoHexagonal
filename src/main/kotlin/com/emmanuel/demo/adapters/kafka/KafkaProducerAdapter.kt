package com.emmanuel.demo.adapters.kafka

import com.emmanuel.demo.domain.ports.output.MessagePublisher
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducerAdapter(private val kafkaTemplate: KafkaTemplate<String, String>) : MessagePublisher {

    private val logger = LoggerFactory.getLogger(KafkaProducerAdapter::class.java)

    override fun enviarMensagem(topico: String, mensagem: String) {
        val future = kafkaTemplate.send(topico, mensagem)
        future.whenComplete { result, ex ->
            if (ex == null) {
                logger.info("Mensagem publicada com sucesso no tópico '$topico': $mensagem")
                result?.let {
                    logger.info("Detalhes do envio: partição=${it.recordMetadata.partition()}, offset=${it.recordMetadata.offset()}")
                }
            } else {
                logger.error("Erro ao publicar mensagem no tópico '$topico': ${ex.message}", ex)
            }
        }
    }
}