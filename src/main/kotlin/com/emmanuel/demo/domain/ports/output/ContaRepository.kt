package com.emmanuel.demo.domain.ports.output

import com.emmanuel.demo.domain.entity.Conta

interface ContaRepository {
    fun salvar(conta: Conta): Conta
    fun buscarPorId(id: String): Conta?
    fun listaTodas(): List<Conta>
    fun buscarPorCpf(cpf: String): Conta? // Método para buscar conta pelo CPF
}