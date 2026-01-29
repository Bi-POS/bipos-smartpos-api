package br.com.bipos.smartposapi.bootstrap

import br.com.bipos.smartposapi.bootstrap.dto.PosBootstrapResponse
import br.com.bipos.smartposapi.bootstrap.dto.PosModuleDTO
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PosBootstrapService {

    fun bootstrap(companyId: UUID): PosBootstrapResponse {

        return PosBootstrapResponse(
            companyId = companyId,
            companyName = "Bipos Demo",
            modules = listOf(
                PosModuleDTO(
                    code = "SALE",
                    name = "Vendas"
                )
            )
        )
    }
}
