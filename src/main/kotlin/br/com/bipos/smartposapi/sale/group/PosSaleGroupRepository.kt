package br.com.bipos.smartposapi.sale.group

import br.com.bipos.smartposapi.domain.catalog.Group
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PosSaleGroupRepository : JpaRepository<Group, UUID> {

    fun findAllByCompany_Id(companyId: UUID): List<Group>
}
