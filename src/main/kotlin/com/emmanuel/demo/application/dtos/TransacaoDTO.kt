package com.emmanuel.demo.application.dtos

import java.time.LocalDateTime

data class TransacaoDTO(
    val id: String? = null,
    val idConta: String,
    val tipo: String,
    val valor: Double,
    val dataHora: LocalDateTime? = null
)