package br.com.bipos.smartposapi.credential

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PosDeviceRepository : JpaRepository<PosDevice, UUID> {

    fun findBySerialNumber(serialNumber: String): PosDevice?

    fun findBySerialNumberAndActiveTrue(serialNumber: String): PosDevice?

    fun existsBySerialNumber(serialNumber: String): Boolean
}