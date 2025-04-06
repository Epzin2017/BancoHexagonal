package com.emmanuel.demo.domain.ports.input

import com.emmanuel.demo.domain.entity.Conta

interface ContaServicePort {
    fun criarConta(conta: Conta): String
    fun buscarContaPorId(id: String): Conta
    fun listarTodasContas(): List<Conta>
    fun depositar(id: String, valor: Double?): String
    fun sacar(id: String, valor: Double?): String
}