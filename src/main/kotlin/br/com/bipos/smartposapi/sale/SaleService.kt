package br.com.bipos.smartposapi.sale

import br.com.bipos.smartposapi.company.CompanyService
import br.com.bipos.smartposapi.domain.catalog.Payment
import br.com.bipos.smartposapi.domain.catalog.Sale
import br.com.bipos.smartposapi.domain.catalog.SaleItem
import br.com.bipos.smartposapi.payment.PaymentRepository
import br.com.bipos.smartposapi.sale.dto.SaleRequest
import br.com.bipos.smartposapi.sale.product.PosSaleProductRepository
import br.com.bipos.smartposapi.stock.StockService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SaleService(
    private val productRepository: PosSaleProductRepository,
    private val saleRepository: SaleRepository,
    private val paymentRepository: PaymentRepository,
    companyService: CompanyService,
    private val stockService: StockService
) {
    val company = companyService.getCurrentCompany()

    @Transactional
    fun createSale(request: SaleRequest): Sale {

        val sale = Sale(
            company = company,
            totalAmount = BigDecimal.ZERO
        )

        var total = BigDecimal.ZERO

        request.items.forEach { item ->

            val product = productRepository.findById(item.productId)
                .orElseThrow { RuntimeException("Produto não encontrado") }

            val subtotal = product.price.multiply(item.quantity.toBigDecimal())

            val saleItem = SaleItem(
                sale = sale,
                product = product,
                quantity = item.quantity,
                unitPrice = product.price,
                subtotal = subtotal
            )

            sale.items.add(saleItem)
            total += subtotal
        }

        sale.totalAmount = total

        val finalSale = saleRepository.save(sale)

        finalSale.items.forEach {
            stockService.decreaseStock(
                product = it.product,
                quantity = it.quantity,
                referenceId = finalSale.id!!
            )
        }

        paymentRepository.save(
            Payment(
                sale = finalSale,
                amount = total,
                method = request.paymentMethod
            )
        )

        return finalSale
    }
}
