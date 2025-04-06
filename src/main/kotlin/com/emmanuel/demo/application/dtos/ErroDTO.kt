package com.emmanuel.demo.application.dtos

import java.time.LocalDateTime

data class ErroDTO(
    val timestamp: LocalDateTime = LocalDateTime.now(), // Hora do erro
    val status: Int, // Código HTTP do erro
    val error: String, // Nome do erro (ex.: "Not Found")
    val message: String, // Mensagem detalhada
    val path: String // Caminho da requisição (URI)
)