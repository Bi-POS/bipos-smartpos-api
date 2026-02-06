package br.com.bipos.smartposapi.sale

import br.com.bipos.smartposapi.auth.PosAuthContext
import br.com.bipos.smartposapi.domain.catalog.Payment
import br.com.bipos.smartposapi.domain.catalog.Sale
import br.com.bipos.smartposapi.domain.catalog.SaleItem
import br.com.bipos.smartposapi.domain.company.Company
import br.com.bipos.smartposapi.payment.PaymentStatus
import br.com.bipos.smartposapi.sale.dto.SaleRequest
import br.com.bipos.smartposapi.sale.product.PosSaleProductRepository
import br.com.bipos.smartposapi.stock.StockService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime


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

        /* =========================
           Buscar produtos
           ========================= */
        val productIds = request.items.map { it.productId }

        val products = productRepository
            .findAllById(productIds)
            .associateBy { it.id }

        if (products.size != productIds.distinct().size) {
            throw IllegalArgumentException("Produto inválido na venda")
        }

        /* =========================
           Criar SALE
           ========================= */
        val sale = Sale(
            company = company,
            totalAmount = BigDecimal.ZERO
        )

        sale.status = SaleStatus.CREATED

        /* =========================
           Criar itens
           ========================= */
        request.items.forEach { itemReq ->
            val product = products[itemReq.productId]
                ?: throw IllegalArgumentException("Produto não encontrado")

            val quantity = itemReq.quantity.takeIf { it > 0 }
                ?: throw IllegalArgumentException("Quantidade inválida")

            val subtotal = product.price.multiply(BigDecimal(quantity))

            sale.items.add(
                SaleItem(
                    sale = sale,
                    product = product,
                    quantity = quantity,
                    unitPrice = product.price,
                    subtotal = subtotal
                )
            )

            sale.totalAmount += subtotal
        }

        /* =========================
           Pagamento
           ========================= */
        sale.status = SaleStatus.PENDING_PAYMENT

        val payment = Payment(
            sale = sale,
            method = request.paymentMethod,
            amount = sale.totalAmount,
            status = PaymentStatus.PAID,
            posSerial = auth.serialNumber,
            user = auth.user,
            paidAt = LocalDateTime.now()
        )

        sale.payments.add(payment)
        sale.status = SaleStatus.COMPLETED

        val savedSale = saleRepository.save(sale)

        /* =========================
           Estoque
           ========================= */
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
