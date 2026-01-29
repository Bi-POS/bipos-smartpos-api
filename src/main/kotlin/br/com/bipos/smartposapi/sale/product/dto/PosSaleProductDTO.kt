package br.com.bipos.smartposapi.sale.product.dto

import br.com.bipos.smartposapi.domain.catalog.UnitType
import java.math.BigDecimal
import java.util.*

data class PosSaleProductDTO(
    val id: UUID?,
    val name: String,
    val price: BigDecimal,
    val unitType: UnitType,
    val imageUrl: String?,
    val groupId: UUID?,
    val groupName: String
)
