package com.emmanuel.demo.presentation.controllers

import com.emmanuel.demo.application.dtos.TransacaoDTO
import com.emmanuel.demo.application.services.TransacaoService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/transacoes")
class TransacaoController(private val transacaoService: TransacaoService) {



    @GetMapping("/contas/{idConta}")
    fun buscarTransacoesPorConta(@PathVariable idConta: String): ResponseEntity<List<TransacaoDTO>> {
        val transacoes = transacaoService.buscarTransacoesPorConta(idConta)
        val transacoesDTO = transacoes.map {
            TransacaoDTO(
                id = it.id,
                idConta = it.idConta,
                tipo = it.tipo,
                valor = it.valor,
                dataHora = it.dataHora
            )
        }
        return if (transacoesDTO.isNotEmpty()) {
            ResponseEntity.ok(transacoesDTO)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(emptyList())
        }
    }

    @GetMapping
    fun listarTodasTransacoes(): ResponseEntity<List<TransacaoDTO>> {
        val transacoes = transacaoService.listarTodasTransacoes()
        val transacoesDTO = transacoes.map {
            TransacaoDTO(
                id = it.id,
                idConta = it.idConta,
                tipo = it.tipo,
                valor = it.valor,
                dataHora = it.dataHora
            )
        }
        return ResponseEntity.ok(transacoesDTO)
    }
}