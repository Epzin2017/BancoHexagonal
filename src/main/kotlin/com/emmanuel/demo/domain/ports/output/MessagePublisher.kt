package com.emmanuel.demo.domain.ports.output

interface MessagePublisher {
    fun enviarMensagem(topico: String, mensagem: String)
}