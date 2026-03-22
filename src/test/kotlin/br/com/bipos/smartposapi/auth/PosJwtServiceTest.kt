package br.com.bipos.smartposapi.auth

import br.com.bipos.smartposapi.credential.PosDevice
import br.com.bipos.smartposapi.domain.company.Company
import br.com.bipos.smartposapi.domain.company.CompanyStatus
import br.com.bipos.smartposapi.domain.user.AppUser
import br.com.bipos.smartposapi.domain.user.UserRole
import br.com.bipos.smartposapi.domain.utils.DocumentType
import br.com.bipos.smartposapi.security.PosJwtProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class PosJwtServiceTest {
    private val props = PosJwtProperties(
        secret = "gx7ow6V8/zS3n53u540FcFsFGTGpVz85Y1dXDFNLMDyZsFEQMKvgL4pHMEV9is2zm++BXV/QYppVZ3DZLYBugw==",
        expiration = 90_000L
    )

    private val jwtService = PosJwtService(props)

    @Test
    fun `generate access token uses configured expiration`() {
        val token = jwtService.generateAccessToken(
            user = appUser(),
            pos = posDevice()
        )

        val claims = jwtService.extractAllClaims(token)

        assertThat(claims.expiration.time - claims.issuedAt.time)
            .isEqualTo(props.expiration)
    }

    @Test
    fun `extract pos principal returns token identity`() {
        val token = jwtService.generateAccessToken(
            user = appUser(),
            pos = posDevice()
        )

        val principal = jwtService.extractPosPrincipal(token)

        assertThat(principal.userId).isEqualTo(USER_ID)
        assertThat(principal.companyId).isEqualTo(COMPANY_ID)
        assertThat(principal.serialNumber).isEqualTo(SERIAL_NUMBER)
        assertThat(jwtService.extractSubject(token)).isEqualTo(USER_ID.toString())
        assertThat(jwtService.extractType(token)).isEqualTo("POS")
        assertThat(jwtService.extractPosVersion(token)).isEqualTo("1.0.0")
    }

    private fun appUser() = AppUser(
        id = USER_ID,
        name = "Operador POS",
        email = "contato@bipos.com.br",
        document = "06847947560",
        passwordHash = "\$2a\$10\$abcdefghijklmnopqrstuv",
        role = UserRole.OPERATOR,
        company = company(),
        active = true
    )

    private fun company() = Company(
        id = COMPANY_ID,
        name = "Bipos",
        email = "contato@bipos.com.br",
        document = "12345678000190",
        documentType = DocumentType.CNPJ,
        phone = "71999999999",
        status = CompanyStatus.ACTIVE
    )

    private fun posDevice() = PosDevice(
        id = POS_ID,
        serialNumber = SERIAL_NUMBER,
        company = company(),
        active = true,
        posVersion = "1.0.0"
    )

    companion object {
        private val COMPANY_ID: UUID = UUID.fromString("6be3bbb2-622b-4112-8613-92de732331fa")
        private val USER_ID: UUID = UUID.fromString("f22d4dba-30cf-4e82-a60d-b3e6f08b3910")
        private val POS_ID: UUID = UUID.fromString("fe2595e0-b5eb-4acd-bc3d-16a49d302bd3")
        private const val SERIAL_NUMBER = "POS001"
    }
}
