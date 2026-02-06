package br.com.bipos.smartposapi.sale.group

import br.com.bipos.smartposapi.sale.group.dto.PosSaleGroupDTO
import br.com.bipos.smartposapi.security.PosSecurityUtils
import org.springframework.stereotype.Service

@Service
class PosSaleGroupService(
    private val repository: PosSaleGroupRepository
) {

    fun list(): List<PosSaleGroupDTO> {
        val companyId = PosSecurityUtils.companyId()

        return repository.findAllByCompany_Id(companyId)
            .map {
                PosSaleGroupDTO(
                    id = it.id,
                    name = it.name,
                    imageUrl = it.imageUrl
                )
            }
    }
}

