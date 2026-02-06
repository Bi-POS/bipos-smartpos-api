package br.com.bipos.smartposapi.user

import br.com.bipos.smartposapi.domain.user.AppUser
import br.com.bipos.smartposapi.domain.user.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AppUserRepository : JpaRepository<AppUser, UUID> {

    fun findFirstByCompanyIdAndRoleAndActiveTrue(
        companyId: UUID,
        role: UserRole
    ): AppUser?

    fun findByEmailAndActiveTrue(email: String?): AppUser?

    fun findByIdAndActiveTrue(id: UUID): AppUser?

    fun findByDocumentAndActiveTrue(document: String): AppUser?

}