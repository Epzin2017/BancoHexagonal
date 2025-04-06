package com.emmanuel.demo.domain.ports.output

import com.emmanuel.demo.domain.entity.Transacao

interface TransacaoRepository {
    fun salvar(transacao: Transacao): Transacao
    fun buscarPorConta(idConta: String): List<Transacao>
    fun listarTodas(): List<Transacao>

}