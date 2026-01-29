package br.com.bipos.smartposapi.sale.product

import br.com.bipos.smartposapi.sale.product.dto.PosSaleProductDTO
import br.com.bipos.smartposapi.security.PosSecurityUtils
import org.springframework.stereotype.Service
import java.util.*

@Service
class PosSaleProductService(
    private val repository: PosSaleProductRepository
) {
    fun list(): List<PosSaleProductDTO> {
        val companyId = PosSecurityUtils.getCompanyId()

        return repository.findAllByGroup_Company_Id(companyId)
            .map {
                PosSaleProductDTO(
                    id = it.id,
                    name = it.name,
                    price = it.price,
                    imageUrl = it.imageUrl,
                    unitType = it.unitType,
                    groupId = it.group.id,
                    groupName = it.group.name
                )
            }
    }

    fun listByGroup(groupId: UUID): List<PosSaleProductDTO> {
        return repository
            .findAllByGroup_Id(groupId)
            .map {
                PosSaleProductDTO(
                    id = it.id,
                    name = it.name,
                    price = it.price,
                    imageUrl = it.imageUrl,
                    unitType = it.unitType,
                    groupId = it.group.id,
                    groupName = it.group.name
                )
            }
    }
}