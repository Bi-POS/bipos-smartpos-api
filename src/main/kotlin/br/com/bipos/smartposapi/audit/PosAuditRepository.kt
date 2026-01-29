package br.com.bipos.smartposapi.audit

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PosAuditRepository : JpaRepository<PosAudit, UUID>
