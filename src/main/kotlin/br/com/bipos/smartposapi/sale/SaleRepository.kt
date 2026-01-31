package br.com.bipos.smartposapi.sale


import br.com.bipos.smartposapi.domain.catalog.Sale
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.*

interface SaleRepository : JpaRepository<Sale, UUID> {

    fun findAllByCreatedAtBetween(
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Sale>

    fun findDistinctByItems_Product_Group_Company_Id(
        companyId: UUID
    ): List<Sale>

    fun findAllByOrderByCreatedAtDesc(
        pageable: Pageable
    ): List<Sale>
}
