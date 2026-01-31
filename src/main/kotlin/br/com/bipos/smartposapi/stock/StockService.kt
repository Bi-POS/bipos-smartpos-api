package br.com.bipos.smartposapi.stock

import br.com.bipos.smartposapi.company.CompanyService
import br.com.bipos.smartposapi.domain.catalog.Product
import br.com.bipos.smartposapi.domain.stock.Stock
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

@Service
class StockService(
    private val stockRepository: StockRepository,
    private val stockMovementRepository: StockMovementRepository,
    private val companyService: CompanyService
) {

    /** =========================
     *  Flag global da empresa
     *  ========================= */
    fun isStockEnabled(): Boolean {
        return companyService.getCurrentCompany().stockEnabled
    }

    /** =========================
     *  Baixa segura (não bloqueia venda)
     *  ========================= */
    @Transactional
    fun decreaseStockIfExists(
        product: Product,
        quantity: Int,
        referenceId: UUID
    ) {
        val company = companyService.getCurrentCompany()

        // 🔕 Empresa não controla estoque
        if (!company.stockEnabled) {
            return
        }

        val stock = stockRepository.findByCompany_IdAndProduct_Id(
            company.id!!,
            product.id!!
        ) ?: return // 🔥 produto sem estoque → ignora

        // 🔕 Estoque insuficiente → não bloqueia venda
        if (stock.quantity < quantity) {
            // opcional: log
            return
        }

        stock.quantity -= quantity
        stockRepository.save(stock)

        stockMovementRepository.save(
            StockMovement(
                company = company,
                product = product,
                type = StockMovementType.SALE,
                quantity = -quantity,
                balanceAfter = stock.quantity,
                referenceId = referenceId
            )
        )
    }

    /** =========================
     *  Entrada de estoque (WEB)
     *  ========================= */
    @Transactional
    fun increaseStock(
        product: Product,
        quantity: Int,
        type: StockMovementType
    ) {
        val company = companyService.getCurrentCompany()

        val stock = stockRepository.findByCompany_IdAndProduct_Id(
            company.id!!,
            product.id!!
        ) ?: Stock(
            company = company,
            product = product,
            quantity = 0
        )

        stock.quantity += quantity
        stockRepository.save(stock)

        stockMovementRepository.save(
            StockMovement(
                company = company,
                product = product,
                type = type,
                quantity = quantity,
                balanceAfter = stock.quantity
            )
        )
    }
}
