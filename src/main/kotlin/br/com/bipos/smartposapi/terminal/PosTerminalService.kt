package br.com.bipos.smartposapi.terminal

import br.com.bipos.smartposapi.exception.InvalidTerminalException
import org.springframework.stereotype.Service
import java.util.*

@Service
class PosTerminalService(
    private val repository: PosTerminalRepository
) {

    /**
     * Registra um terminal físico (primeira vez)
     */
    fun register(
        companyId: UUID,
        serialNumber: String,
        model: String
    ): PosTerminal {

        // evita duplicidade de serial
        val existing = repository.findBySerialNumber(serialNumber)
        if (existing != null) {
            return existing
        }

        val terminal = PosTerminal(
            companyId = companyId,
            serialNumber = serialNumber,
            model = model,
            active = true
        )

        return repository.save(terminal)
    }

    /**
     * Valida se o terminal pode operar para a company
     */
    fun validate(
        companyId: UUID,
        serialNumber: String
    ): PosTerminal {

        val terminal = repository.findBySerialNumberAndActiveTrue(serialNumber)
            ?: throw InvalidTerminalException("Terminal não registrado ou inativo")

        if (terminal.companyId != companyId) {
            throw InvalidTerminalException("Terminal não pertence à company")
        }

        return terminal
    }

    /**
     * Bloqueia um terminal remotamente
     */
    fun block(serialNumber: String) {
        val terminal = repository.findBySerialNumber(serialNumber)
            ?: throw InvalidTerminalException("Terminal não encontrado")

        terminal.active = false
        repository.save(terminal)
    }

    /**
     * Libera um terminal bloqueado
     */
    fun unblock(serialNumber: String) {
        val terminal = repository.findBySerialNumber(serialNumber)
            ?: throw InvalidTerminalException("Terminal não encontrado")

        terminal.active = true
        repository.save(terminal)
    }
}
