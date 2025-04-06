package com.emmanuel.demo.application.services

import com.emmanuel.demo.domain.entity.Transacao
import com.emmanuel.demo.domain.ports.output.TransacaoRepository
import org.springframework.stereotype.Service

@Service
class TransacaoService(
    private val transacaoRepository: TransacaoRepository
) {

    fun registrarTransacao(transacao: Transacao): Transacao {
        return transacaoRepository.salvar(transacao)
    }

    fun buscarTransacoesPorConta(idConta: String): List<Transacao> {
        return transacaoRepository.buscarPorConta(idConta)
    }

    fun listarTodasTransacoes(): List<Transacao> {
        return transacaoRepository.listarTodas()
    }
}