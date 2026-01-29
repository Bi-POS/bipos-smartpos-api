package br.com.bipos.smartposapi.credential

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PosCredentialRepository : JpaRepository<PosCredential, UUID> {

    fun findByCnpjAndActiveTrue(cnpj: String): PosCredential?
    fun findByCompanyIdAndActiveTrue(companyId: UUID): PosCredential?
}
