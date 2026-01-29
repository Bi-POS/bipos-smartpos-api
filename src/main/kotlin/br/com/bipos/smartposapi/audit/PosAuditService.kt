package br.com.bipos.smartposapi.audit

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import java.util.*

@Service
class PosAuditService(
    private val repository: PosAuditRepository
) {

    fun log(
        companyId: UUID?,
        action: String,
        request: HttpServletRequest,
        serialNumber: String? = null,
        posVersion: String? = null
    ) {
        val ip = extractIp(request)

        val audit = PosAudit(
            companyId = companyId,
            action = action,
            ipAddress = ip,
            serialNumber = serialNumber,
            posVersion = posVersion
        )

        repository.save(audit)
    }

    private fun extractIp(request: HttpServletRequest): String {
        return request.getHeader("X-Forwarded-For")
            ?.split(",")
            ?.first()
            ?: request.remoteAddr
    }
}
