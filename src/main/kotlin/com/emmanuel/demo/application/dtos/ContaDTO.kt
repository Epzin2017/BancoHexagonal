package com.emmanuel.demo.application.dtos

import java.time.LocalDate

data class ContaDTO(
    val id: String? = null,
    val nome: String,
    val cpf: String,
    val dataNascimento: LocalDate,
    val email: String? = null,
    val telefone: String? = null,
    val saldo: Double? = null
)