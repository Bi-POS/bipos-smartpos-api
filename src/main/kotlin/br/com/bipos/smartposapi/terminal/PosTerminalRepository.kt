package br.com.bipos.smartposapi.terminal

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PosTerminalRepository : JpaRepository<PosTerminal, UUID> {

    fun findBySerialNumber(serialNumber: String): PosTerminal?

    fun findBySerialNumberAndActiveTrue(serialNumber: String): PosTerminal?
}