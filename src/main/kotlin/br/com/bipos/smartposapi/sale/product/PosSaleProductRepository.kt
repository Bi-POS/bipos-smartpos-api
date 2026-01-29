package br.com.bipos.smartposapi.sale.product

import br.com.bipos.smartposapi.domain.catalog.Product
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PosSaleProductRepository : JpaRepository<Product, UUID> {
    fun findAllByGroup_Company_Id(companyId: UUID): List<Product>
    fun findAllByGroup_Id(groupId: UUID): List<Product>
}