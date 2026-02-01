package br.com.bipos.smartposapi.sale.mapper

import br.com.bipos.smartposapi.domain.catalog.Sale
import br.com.bipos.smartposapi.sale.dto.SaleResponse

fun Sale.toResponseDto(): SaleResponse =
    SaleResponse(
        id = id.toString(),
        totalAmount = totalAmount,
        status = status,
    )