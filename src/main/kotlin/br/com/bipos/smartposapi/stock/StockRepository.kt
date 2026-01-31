package br.com.bipos.smartposapi.stock

import br.com.bipos.smartposapi.domain.stock.Stock
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface StockRepository : JpaRepository<Stock, UUID> {
    fun findByCompany_IdAndProduct_Id(
        companyId: UUID,
        productId: UUID
    ): Stock?
}
