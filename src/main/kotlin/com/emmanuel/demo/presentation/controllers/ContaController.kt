package com.emmanuel.demo.presentation.controllers

import com.emmanuel.demo.application.dtos.ContaDTO
import com.emmanuel.demo.domain.entity.Conta
import com.emmanuel.demo.domain.ports.input.ContaServicePort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/contas")
class ContaController(private val contaServicePort: ContaServicePort) {

    @PostMapping
    fun criarConta(@RequestBody contaDTO: ContaDTO): ResponseEntity<String> {
        val conta = Conta(
            id = contaDTO.id ?: UUID.randomUUID().toString(),
            nome = contaDTO.nome,
            cpf = contaDTO.cpf,
            dataNascimento = contaDTO.dataNascimento,
            email = contaDTO.email,
            telefone = contaDTO.telefone
        )
        val resposta = contaServicePort.criarConta(conta)
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta)
    }

    @GetMapping("/{id}")
    fun buscarContaPorId(@PathVariable id: String): ResponseEntity<ContaDTO> {
        val conta = contaServicePort.buscarContaPorId(id)
        val contaDTO = ContaDTO(
            id = conta.id,
            nome = conta.nome,
            cpf = conta.cpf,
            dataNascimento = conta.dataNascimento,
            email = conta.email,
            telefone = conta.telefone,
            saldo = conta.obterSaldo()
        )
        return ResponseEntity.ok(contaDTO)
    }

    @GetMapping
    fun listarTodasContas(): ResponseEntity<List<ContaDTO>> {
        val contas = contaServicePort.listarTodasContas()
        val contasDTO = contas.map {
            ContaDTO(
                id = it.id,
                nome = it.nome,
                cpf = it.cpf,
                dataNascimento = it.dataNascimento,
                email = it.email,
                telefone = it.telefone,
                saldo = it.obterSaldo()
            )
        }
        return ResponseEntity.ok(contasDTO)
    }

    @PostMapping("/{id}/depositar")
    fun depositar(@PathVariable id: String, @RequestBody payload: Map<String, Double?>): ResponseEntity<String> {
        val valor = payload["valor"]
        val resposta = contaServicePort.depositar(id, valor)
        return ResponseEntity.ok(resposta)
    }

    @PostMapping("/{id}/sacar")
    fun sacar(@PathVariable id: String, @RequestBody payload: Map<String, Double?>): ResponseEntity<String> {
        val valor = payload["valor"]
        val resposta = contaServicePort.sacar(id, valor)
        return ResponseEntity.ok(resposta)
    }
}