package com.emmanuel.demo.domain.entity

import java.time.LocalDate
import java.time.Period

data class Conta(
    val id: String,
    val nome: String,
    val cpf: String,
    val dataNascimento: LocalDate,
    private var saldo: Double = 0.0,
    var email: String? = null,
    var telefone: String? = null
) {
    init {
        require(nome.isNotBlank()) { "O nome do titular não pode estar em branco" }
        require(cpf.matches(Regex("\\d{11}"))) { "O CPF deve conter exatamente 11 dígitos" }
        val idade = Period.between(dataNascimento, LocalDate.now()).years
        require(idade >= 18) { "O titular deve ter pelo menos 18 anos. Idade atual: $idade anos" }
    }

    fun obterSaldo(): Double = saldo

    fun depositar(valor: Double?): Conta {
        requireNotNull(valor) { "O valor de depósito é obrigatório e não pode ser nulo." }
        require(valor > 0) { "NEGADO: O valor de depósito deve ser positivo" }
        saldo += valor
        return this
    }

    fun sacar(valor: Double?): Conta {
        requireNotNull(valor) { "O valor de saque é obrigatório e não pode ser nulo." }
        require(valor > 0) { "NEGADO: O valor de saque deve ser positivo" }
        require(valor <= saldo) { "NEGADO: Saldo insuficiente para realizar o saque" }
        saldo -= valor
        return this
    }
}