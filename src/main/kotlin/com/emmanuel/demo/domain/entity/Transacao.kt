package com.emmanuel.demo.domain.entity

import java.time.LocalDateTime

data class Transacao(
    val id: String,
    val idConta: String,
    val tipo: String,
    val valor: Double,
    val dataHora: LocalDateTime
) {
    init {
        require(valor > 0) { "O valor da transação deve ser maior que zero." }
        require(tipo in listOf("DEPOSITO", "SAQUE")) { "Tipo de transação inválido. Deve ser 'DEPOSITO' ou 'SAQUE'." }
    }

    fun descricao(): String {
        return "Transação [$id]: $tipo de R$$valor realizada na conta ID: $idConta em $dataHora"
    }

    fun aplicarNaConta(conta: Conta): Conta {
        when (tipo) {
            "DEPOSITO" -> conta.depositar(valor)
            "SAQUE" -> conta.sacar(valor)
            else -> throw IllegalArgumentException("Tipo de transação inválido para aplicação.")
        }
        return conta
    }
}