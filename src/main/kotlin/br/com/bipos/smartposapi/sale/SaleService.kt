package br.com.bipos.smartposapi.sale

import br.com.bipos.smartposapi.company.CompanyService
import br.com.bipos.smartposapi.domain.catalog.Payment
import br.com.bipos.smartposapi.domain.catalog.Sale
import br.com.bipos.smartposapi.domain.catalog.SaleItem
import br.com.bipos.smartposapi.payment.PaymentStatus
import br.com.bipos.smartposapi.sale.dto.SaleRequest
import br.com.bipos.smartposapi.sale.product.PosSaleProductRepository
import br.com.bipos.smartposapi.stock.StockService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class SaleService(
    private val productRepository: PosSaleProductRepository,
    private val saleRepository: SaleRepository,
    private val companyService: CompanyService,
    private val stockService: StockService
) {

    @Transactional
    fun createSale(
        companyId: UUID?,
        request: SaleRequest
    ): Sale {

        if (request.items.isEmpty()) {
            throw IllegalArgumentException("Venda sem itens")
        }

        /* =========================
           Buscar Company
           ========================= */
        val company = companyService.findById(companyId)
            ?: throw IllegalArgumentException("Empresa não encontrada")

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
           Criar itens da venda
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
           Criar pagamento
           ========================= */
        sale.status = SaleStatus.PENDING_PAYMENT

        val payment = Payment(
            sale = sale,
            method = request.paymentMethod,
            amount = sale.totalAmount,
            status = PaymentStatus.PAID
        )

        sale.payments.add(payment)
        sale.status = SaleStatus.COMPLETED

        /* =========================
           Persistir venda
           ========================= */
        val savedSale = saleRepository.save(sale)

        /* =========================
           Baixa de estoque (NÃO bloqueia)
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