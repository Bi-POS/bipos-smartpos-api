package br.com.bipos.smartposapi.sale

import br.com.bipos.smartposapi.auth.PosAuthContext
import br.com.bipos.smartposapi.comanda.dto.CloseComandaRequest
import br.com.bipos.smartposapi.domain.catalog.Payment
import br.com.bipos.smartposapi.domain.catalog.Product
import br.com.bipos.smartposapi.domain.catalog.Sale
import br.com.bipos.smartposapi.domain.catalog.SaleItem
import br.com.bipos.smartposapi.domain.comanda.ComandaSession
import br.com.bipos.smartposapi.domain.company.Company
import br.com.bipos.smartposapi.payment.PaymentMethod
import br.com.bipos.smartposapi.payment.PaymentStatus
import br.com.bipos.smartposapi.sale.dto.DailySalesReportResponse
import br.com.bipos.smartposapi.sale.dto.PaymentMethodReportResponse
import br.com.bipos.smartposapi.sale.dto.RecentSaleReportResponse
import br.com.bipos.smartposapi.sale.dto.SaleRequest
import br.com.bipos.smartposapi.sale.dto.TopProductReportResponse
import br.com.bipos.smartposapi.sale.product.PosSaleProductRepository
import br.com.bipos.smartposapi.stock.StockService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

@Service
class SaleService(
    private val productRepository: PosSaleProductRepository,
    private val saleRepository: SaleRepository,
    private val stockService: StockService
) {

    @Transactional
    fun createSale(
        auth: PosAuthContext,
        company: Company,
        request: SaleRequest
    ): Sale {

        if (request.items.isEmpty()) {
            throw IllegalArgumentException("Venda sem itens")
        }

        val productIds = request.items.map { it.productId }
        val products = productRepository
            .findAllById(productIds)
            .associateBy { it.id }

        if (products.size != productIds.distinct().size) {
            throw IllegalArgumentException("Produto inválido na venda")
        }

        val sale = Sale(
            company = company,
            totalAmount = BigDecimal.ZERO
        )

        sale.status = SaleStatus.CREATED

        request.items.forEach { itemReq ->
            val product = products[itemReq.productId]
                ?: throw IllegalArgumentException("Produto não encontrado")

            val quantity = itemReq.quantity.takeIf { it > 0 }
                ?: throw IllegalArgumentException("Quantidade inválida")

            appendItem(
                sale = sale,
                product = product,
                quantity = quantity,
                unitPrice = product.price
            )
        }

        return finalizeSale(
            auth = auth,
            sale = sale,
            paymentMethod = request.paymentMethod,
            amount = request.amount,
            nsu = request.nsu,
            authorizationCode = request.authorizationCode,
            cardBrand = request.cardBrand,
            cardNumberMasked = request.cardNumberMasked,
            installments = request.installments,
            hostMessage = request.hostMessage,
            acquirerResponse = request.acquirerResponse
        )
    }

    @Transactional
    fun createSaleFromComanda(
        auth: PosAuthContext,
        company: Company,
        comanda: ComandaSession,
        request: CloseComandaRequest
    ): Sale {
        if (comanda.items.isEmpty()) {
            throw IllegalArgumentException("Comanda sem itens")
        }

        val sale = Sale(
            company = company,
            totalAmount = BigDecimal.ZERO
        )

        sale.status = SaleStatus.CREATED

        comanda.items.forEach { item ->
            sale.items.add(
                SaleItem(
                    sale = sale,
                    product = item.product,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    subtotal = item.subtotal
                )
            )
            sale.totalAmount += item.subtotal
        }

        return finalizeSale(
            auth = auth,
            sale = sale,
            paymentMethod = request.paymentMethod,
            amount = request.amount,
            nsu = request.nsu,
            authorizationCode = request.authorizationCode,
            cardBrand = request.cardBrand,
            cardNumberMasked = request.cardNumberMasked,
            installments = request.installments,
            hostMessage = request.hostMessage,
            acquirerResponse = request.acquirerResponse
        )
    }

    @Transactional
    fun getDailyReport(
        companyId: UUID,
        reportDate: LocalDate
    ): DailySalesReportResponse {
        val sales = saleRepository
            .findAllByCompany_IdAndStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
                companyId = companyId,
                status = SaleStatus.COMPLETED,
                start = reportDate.atStartOfDay(),
                end = reportDate.atTime(LocalTime.MAX)
            )

        val grossRevenue = sales.fold(BigDecimal.ZERO) { acc, sale ->
            acc + sale.totalAmount
        }
        val totalSales = sales.size
        val totalItems = sales.sumOf { sale ->
            sale.items.sumOf { item -> item.quantity }
        }
        val averageTicket = if (totalSales == 0) {
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        } else {
            grossRevenue.divide(BigDecimal(totalSales), 2, RoundingMode.HALF_UP)
        }

        val paymentMethods = sales
            .flatMap { it.payments }
            .groupBy { it.method }
            .map { (method, payments) ->
                PaymentMethodReportResponse(
                    method = method,
                    salesCount = payments.size,
                    totalAmount = payments.fold(BigDecimal.ZERO) { acc, payment ->
                        acc + payment.amount
                    }
                )
            }
            .sortedByDescending { it.totalAmount }

        val topProducts = sales
            .flatMap { it.items }
            .groupBy { item -> requireNotNull(item.product.id) to item.product.name }
            .map { (product, items) ->
                TopProductReportResponse(
                    productId = product.first,
                    name = product.second,
                    quantity = items.sumOf { it.quantity },
                    totalAmount = items.fold(BigDecimal.ZERO) { acc, item ->
                        acc + item.subtotal
                    }
                )
            }
            .sortedByDescending { it.quantity }
            .take(5)

        val recentSales = sales
            .take(10)
            .map { sale ->
                RecentSaleReportResponse(
                    saleId = requireNotNull(sale.id),
                    createdAt = sale.createdAt,
                    totalAmount = sale.totalAmount,
                    paymentMethod = sale.payments.firstOrNull()?.method ?: PaymentMethod.CASH
                )
            }

        return DailySalesReportResponse(
            reportDate = reportDate,
            totalSales = totalSales,
            totalItems = totalItems,
            grossRevenue = grossRevenue,
            averageTicket = averageTicket,
            paymentMethods = paymentMethods,
            topProducts = topProducts,
            recentSales = recentSales
        )
    }

    private fun appendItem(
        sale: Sale,
        product: Product,
        quantity: Int,
        unitPrice: BigDecimal
    ) {
        val subtotal = unitPrice.multiply(BigDecimal(quantity))

        sale.items.add(
            SaleItem(
                sale = sale,
                product = product,
                quantity = quantity,
                unitPrice = unitPrice,
                subtotal = subtotal
            )
        )

        sale.totalAmount += subtotal
    }

    private fun finalizeSale(
        auth: PosAuthContext,
        sale: Sale,
        paymentMethod: PaymentMethod,
        amount: BigDecimal,
        nsu: String?,
        authorizationCode: String?,
        cardBrand: String?,
        cardNumberMasked: String?,
        installments: Int,
        hostMessage: String?,
        acquirerResponse: String?
    ): Sale {
        sale.status = SaleStatus.PENDING_PAYMENT

        val payment = Payment(
            sale = sale,
            method = paymentMethod,
            amount = amount,
            status = PaymentStatus.PAID,
            posSerial = auth.serialNumber,
            user = auth.user,
            paidAt = LocalDateTime.now(),
            nsu = nsu,
            authorizationCode = authorizationCode,
            cardBrand = cardBrand,
            cardNumberMasked = cardNumberMasked,
            installments = installments,
            hostMessage = hostMessage,
            acquirerResponse = acquirerResponse
        )

        sale.payments.add(payment)
        sale.status = SaleStatus.COMPLETED

        val savedSale = saleRepository.save(sale)

        savedSale.items.forEach { item ->
            stockService.decreaseStockIfExists(
                product = item.product,
                quantity = item.quantity,
                referenceId = savedSale.id!!
            )
        }

        return savedSale
    }
}
