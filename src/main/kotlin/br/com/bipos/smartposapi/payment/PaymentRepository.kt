package br.com.bipos.smartposapi.payment

import br.com.bipos.smartposapi.domain.catalog.Payment
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.*

interface PaymentRepository : JpaRepository<Payment, UUID> {

    fun findBySale_Id(saleId: UUID): Payment?

    fun findAllByPaidAtBetween(
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Payment>

    fun findAllByMethod(
        method: PaymentMethod
    ): List<Payment>

    fun findDistinctBySale_Items_Product_Group_Company_Id(
        companyId: UUID
    ): List<Payment>
}
