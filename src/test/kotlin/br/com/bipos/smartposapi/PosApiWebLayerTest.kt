package br.com.bipos.smartposapi

import br.com.bipos.smartposapi.audit.PosAuditRepository
import br.com.bipos.smartposapi.auth.PosAuthController
import br.com.bipos.smartposapi.auth.PosAuthContext
import br.com.bipos.smartposapi.auth.PosAuthService
import br.com.bipos.smartposapi.auth.PosJwtService
import br.com.bipos.smartposapi.auth.dto.CompanySnapshot
import br.com.bipos.smartposapi.auth.dto.PosAuthRequest
import br.com.bipos.smartposapi.auth.dto.PosAuthResponse
import br.com.bipos.smartposapi.auth.dto.PosSnapshot
import br.com.bipos.smartposapi.auth.dto.UserSnapshot
import br.com.bipos.smartposapi.auth.refresh.PosRefreshController
import br.com.bipos.smartposapi.auth.refresh.PosRefreshToken
import br.com.bipos.smartposapi.auth.refresh.PosRefreshTokenService
import br.com.bipos.smartposapi.bootstrap.PosBootstrapController
import br.com.bipos.smartposapi.bootstrap.PosBootstrapService
import br.com.bipos.smartposapi.comanda.ComandaController
import br.com.bipos.smartposapi.comanda.ComandaService
import br.com.bipos.smartposapi.comanda.dto.AddComandaItemRequest
import br.com.bipos.smartposapi.comanda.dto.ComandaDetailResponse
import br.com.bipos.smartposapi.comanda.dto.ComandaItemResponse
import br.com.bipos.smartposapi.comanda.dto.ComandaOverviewResponse
import br.com.bipos.smartposapi.comanda.dto.ComandaSummaryResponse
import br.com.bipos.smartposapi.comanda.dto.ComandaTableResponse
import br.com.bipos.smartposapi.comanda.dto.OpenComandaRequest
import br.com.bipos.smartposapi.comanda.dto.CloseComandaRequest
import br.com.bipos.smartposapi.bootstrap.dto.PosBootstrapResponse
import br.com.bipos.smartposapi.bootstrap.dto.PosModuleDTO
import br.com.bipos.smartposapi.company.CompanyRepository
import br.com.bipos.smartposapi.company.CompanyService
import br.com.bipos.smartposapi.credential.PosDevice
import br.com.bipos.smartposapi.credential.PosDeviceRepository
import br.com.bipos.smartposapi.domain.catalog.Sale
import br.com.bipos.smartposapi.domain.company.Company
import br.com.bipos.smartposapi.domain.company.CompanyStatus
import br.com.bipos.smartposapi.domain.module.ModuleType
import br.com.bipos.smartposapi.domain.settings.SmartPosPrint
import br.com.bipos.smartposapi.domain.settings.SmartPosSaleOperationMode
import br.com.bipos.smartposapi.domain.settings.SmartPosSettings
import br.com.bipos.smartposapi.domain.user.AppUser
import br.com.bipos.smartposapi.domain.user.UserRole
import br.com.bipos.smartposapi.exception.ApiExceptionHandler
import br.com.bipos.smartposapi.exception.BusinessException
import br.com.bipos.smartposapi.exception.InvalidPosCredentialsException
import br.com.bipos.smartposapi.exception.InvalidRefreshTokenException
import br.com.bipos.smartposapi.exception.ResourceNotFoundException
import br.com.bipos.smartposapi.domain.utils.DocumentType
import br.com.bipos.smartposapi.payment.PaymentMethod
import br.com.bipos.smartposapi.payment.PaymentRepository
import br.com.bipos.smartposapi.sale.SaleController
import br.com.bipos.smartposapi.sale.SaleRepository
import br.com.bipos.smartposapi.sale.SaleService
import br.com.bipos.smartposapi.sale.SaleStatus
import br.com.bipos.smartposapi.sale.dto.DailySalesReportResponse
import br.com.bipos.smartposapi.sale.dto.PaymentMethodReportResponse
import br.com.bipos.smartposapi.sale.dto.RecentSaleReportResponse
import br.com.bipos.smartposapi.sale.dto.TopProductReportResponse
import br.com.bipos.smartposapi.sale.dto.SaleItemRequest
import br.com.bipos.smartposapi.sale.dto.SaleRequest
import br.com.bipos.smartposapi.sale.group.PosSaleGroupRepository
import br.com.bipos.smartposapi.sale.product.PosSaleProductRepository
import br.com.bipos.smartposapi.security.PosJwtAuthenticationFilter
import br.com.bipos.smartposapi.security.PosPrincipal
import br.com.bipos.smartposapi.security.SecurityErrorResponseWriter
import br.com.bipos.smartposapi.security.SecurityConfig
import br.com.bipos.smartposapi.settings.SmartPosSettingsController
import br.com.bipos.smartposapi.settings.SmartPosSettingsRepository
import br.com.bipos.smartposapi.settings.SmartPosSettingsService
import br.com.bipos.smartposapi.settings.dto.SmartPosAvailableModuleResponse
import br.com.bipos.smartposapi.settings.dto.SmartPosSettingsResponse
import br.com.bipos.smartposapi.settings.dto.UpdateSmartPosSettingsRequest
import br.com.bipos.smartposapi.stock.StockMovementRepository
import br.com.bipos.smartposapi.stock.StockRepository
import br.com.bipos.smartposapi.terminal.PosTerminalRepository
import br.com.bipos.smartposapi.user.AppUserRepository
import br.com.bipos.smartposapi.login.SmartPosQrTokenRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest(
    classes = [PosApiWebLayerTest.TestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = [
        "security.jwt.pos.secret=gx7ow6V8/zS3n53u540FcFsFGTGpVz85Y1dXDFNLMDyZsFEQMKvgL4pHMEV9is2zm++BXV/QYppVZ3DZLYBugw==",
        "security.jwt.pos.expiration=900000"
    ]
)
@AutoConfigureMockMvc(addFilters = true)
class PosApiWebLayerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var posAuthService: PosAuthService

    @MockBean
    private lateinit var jwtService: PosJwtService

    @MockBean
    private lateinit var refreshService: PosRefreshTokenService

    @MockBean
    private lateinit var posDeviceRepository: PosDeviceRepository

    @MockBean
    private lateinit var userRepository: AppUserRepository

    @MockBean
    private lateinit var saleService: SaleService

    @MockBean
    private lateinit var companyService: CompanyService

    @MockBean
    private lateinit var settingsService: SmartPosSettingsService

    @MockBean
    private lateinit var bootstrapService: PosBootstrapService

    @MockBean
    private lateinit var comandaService: ComandaService

    @MockBean
    private lateinit var companyRepository: CompanyRepository

    @MockBean
    private lateinit var posAuditRepository: PosAuditRepository

    @MockBean
    private lateinit var posRefreshTokenRepository: br.com.bipos.smartposapi.auth.refresh.PosRefreshTokenRepository

    @MockBean
    private lateinit var paymentRepository: PaymentRepository

    @MockBean
    private lateinit var saleRepository: SaleRepository

    @MockBean
    private lateinit var posSaleGroupRepository: PosSaleGroupRepository

    @MockBean
    private lateinit var posSaleProductRepository: PosSaleProductRepository

    @MockBean
    private lateinit var smartPosSettingsRepository: SmartPosSettingsRepository

    @MockBean
    private lateinit var stockMovementRepository: StockMovementRepository

    @MockBean
    private lateinit var stockRepository: StockRepository

    @MockBean
    private lateinit var posTerminalRepository: PosTerminalRepository

    @MockBean
    private lateinit var smartPosQrTokenRepository: SmartPosQrTokenRepository
    @Test
    fun `POST pos auth login returns token and snapshots`() {
        val response = authResponse()

        whenever(posAuthService.login(any(), any())).thenReturn(response)

        val request = PosAuthRequest(
            document = DOCUMENT,
            password = "123456",
            serialNumber = SERIAL_NUMBER,
            posVersion = "1.0.0"
        )

        mockMvc.perform(
            post("/pos/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value("access-token"))
            .andExpect(jsonPath("$.company.id").value(COMPANY_ID.toString()))
            .andExpect(jsonPath("$.user.id").value(USER_ID.toString()))
            .andExpect(jsonPath("$.pos.serialNumber").value(SERIAL_NUMBER))
    }

    @Test
    fun `POST pos auth login returns 401 when credentials are invalid`() {
        whenever(posAuthService.login(any(), any())).thenThrow(InvalidPosCredentialsException())

        val request = PosAuthRequest(
            document = DOCUMENT,
            password = "wrong-password",
            serialNumber = SERIAL_NUMBER,
            posVersion = "1.0.0"
        )

        mockMvc.perform(
            post("/pos/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.message").value("Credenciais POS inválidas"))
            .andExpect(jsonPath("$.path").value("/pos/auth/login"))
    }

    @Test
    fun `POST pos auth refresh returns a new access token`() {
        val user = appUser()
        val company = company()
        val pos = posDevice(company)

        given(refreshService.validate("refresh-token")).willReturn(
            PosRefreshToken(
                token = "refresh-token",
                userId = USER_ID,
                companyId = COMPANY_ID,
                serialNumber = SERIAL_NUMBER,
                expiresAt = Instant.now().plusSeconds(3600)
            )
        )
        given(userRepository.findByIdAndActiveTrue(USER_ID)).willReturn(user)
        given(posDeviceRepository.findBySerialNumberAndActiveTrue(SERIAL_NUMBER)).willReturn(pos)
        given(jwtService.generateAccessToken(user, pos)).willReturn("new-access-token")

        mockMvc.perform(
            post("/pos/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"refresh-token"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value("new-access-token"))
            .andExpect(jsonPath("$.company.id").value(COMPANY_ID.toString()))
            .andExpect(jsonPath("$.company.name").value(company.name))
            .andExpect(jsonPath("$.user.id").value(USER_ID.toString()))
            .andExpect(jsonPath("$.pos.serialNumber").value(SERIAL_NUMBER))
    }

    @Test
    fun `POST pos auth refresh returns 401 when refresh token is invalid`() {
        whenever(refreshService.validate("invalid-refresh-token")).thenThrow(InvalidRefreshTokenException())

        mockMvc.perform(
            post("/pos/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"invalid-refresh-token"}""")
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.message").value("Refresh token POS inválido"))
            .andExpect(jsonPath("$.path").value("/pos/auth/refresh"))
    }

    @Test
    fun `POST pos sales requires a valid POS token`() {
        mockMvc.perform(
            post("/pos/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(saleRequest()))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.message").value("Autenticação POS obrigatória"))
            .andExpect(jsonPath("$.path").value("/pos/sales"))
    }

    @Test
    fun `POST pos sales creates a sale using the authenticated POS context`() {
        val company = company()
        val user = appUser()
        val sale = Sale(
            id = UUID.randomUUID(),
            company = company,
            totalAmount = BigDecimal("25.00"),
            status = SaleStatus.COMPLETED
        )

        stubValidPosToken()
        whenever(userRepository.findByIdAndActiveTrue(USER_ID)).thenReturn(user)
        whenever(companyService.getCurrentCompany()).thenReturn(company)
        whenever(saleService.createSale(any(), any(), any())).thenReturn(sale)

        mockMvc.perform(
            post("/pos/sales")
                .header("Authorization", "Bearer $VALID_TOKEN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(saleRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(sale.id.toString()))
            .andExpect(jsonPath("$.totalAmount").value(25.00))
            .andExpect(jsonPath("$.status").value(SaleStatus.COMPLETED.name))

        val authCaptor = argumentCaptor<PosAuthContext>()
        verify(saleService).createSale(
            authCaptor.capture(),
            any(),
            any()
        )

        val capturedAuth = authCaptor.firstValue
        assertThat(capturedAuth.companyId).isEqualTo(COMPANY_ID)
        assertThat(capturedAuth.serialNumber).isEqualTo(SERIAL_NUMBER)
        assertThat(capturedAuth.user.id).isEqualTo(USER_ID)
    }

    @Test
    fun `GET pos sales daily report returns the authenticated company report`() {
        val company = company()
        val reportDate = LocalDate.of(2026, 3, 22)
        val saleId = UUID.fromString("c8d9f8fd-fec8-44fc-b730-f727ca3e9c4a")
        val productId = UUID.fromString("2da25790-74fa-4299-bf7c-23ec2ebd4e7d")

        stubValidPosToken()
        whenever(companyService.getCurrentCompany()).thenReturn(company)
        whenever(saleService.getDailyReport(COMPANY_ID, reportDate)).thenReturn(
            DailySalesReportResponse(
                reportDate = reportDate,
                totalSales = 3,
                totalItems = 7,
                grossRevenue = BigDecimal("125.50"),
                averageTicket = BigDecimal("41.83"),
                paymentMethods = listOf(
                    PaymentMethodReportResponse(
                        method = PaymentMethod.CREDIT,
                        salesCount = 2,
                        totalAmount = BigDecimal("100.50")
                    ),
                    PaymentMethodReportResponse(
                        method = PaymentMethod.PIX,
                        salesCount = 1,
                        totalAmount = BigDecimal("25.00")
                    )
                ),
                topProducts = listOf(
                    TopProductReportResponse(
                        productId = productId,
                        name = "Arroz branco",
                        quantity = 4,
                        totalAmount = BigDecimal("28.00")
                    )
                ),
                recentSales = listOf(
                    RecentSaleReportResponse(
                        saleId = saleId,
                        createdAt = LocalDateTime.of(2026, 3, 22, 14, 30, 0),
                        totalAmount = BigDecimal("25.00"),
                        paymentMethod = PaymentMethod.CREDIT
                    )
                )
            )
        )

        mockMvc.perform(
            get("/pos/sales/daily-report")
                .header("Authorization", "Bearer $VALID_TOKEN")
                .param("date", reportDate.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.reportDate").value(reportDate.toString()))
            .andExpect(jsonPath("$.totalSales").value(3))
            .andExpect(jsonPath("$.totalItems").value(7))
            .andExpect(jsonPath("$.grossRevenue").value(125.50))
            .andExpect(jsonPath("$.averageTicket").value(41.83))
            .andExpect(jsonPath("$.paymentMethods[0].method").value(PaymentMethod.CREDIT.name))
            .andExpect(jsonPath("$.paymentMethods[0].salesCount").value(2))
            .andExpect(jsonPath("$.paymentMethods[0].totalAmount").value(100.50))
            .andExpect(jsonPath("$.topProducts[0].name").value("Arroz branco"))
            .andExpect(jsonPath("$.topProducts[0].quantity").value(4))
            .andExpect(jsonPath("$.recentSales[0].saleId").value(saleId.toString()))
            .andExpect(jsonPath("$.recentSales[0].paymentMethod").value(PaymentMethod.CREDIT.name))
            .andExpect(jsonPath("$.recentSales[0].createdAt").value("2026-03-22T14:30:00"))

        verify(saleService).getDailyReport(COMPANY_ID, reportDate)
    }

    @Test
    fun `GET pos bootstrap returns structured bootstrap data`() {
        stubValidPosToken()
        given(
            bootstrapService.bootstrap(
                COMPANY_ID,
                SERIAL_NUMBER
            )
        ).willReturn(
            PosBootstrapResponse(
                companyId = COMPANY_ID,
                companyName = "Bipos",
                logoUrl = "https://cdn.bipos.com/logo.png",
                stockEnabled = true,
                serialNumber = SERIAL_NUMBER,
                saleOperationMode = SmartPosSaleOperationMode.DIRECT.name,
                modules = listOf(
                    PosModuleDTO(
                        code = "SALE",
                        name = "Vendas"
                    )
                )
            )
        )

        mockMvc.perform(
            get("/pos/bootstrap")
                .header("Authorization", "Bearer $VALID_TOKEN")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.companyId").value(COMPANY_ID.toString()))
            .andExpect(jsonPath("$.companyName").value("Bipos"))
            .andExpect(jsonPath("$.logoUrl").value("https://cdn.bipos.com/logo.png"))
            .andExpect(jsonPath("$.stockEnabled").value(true))
            .andExpect(jsonPath("$.serialNumber").value(SERIAL_NUMBER))
            .andExpect(jsonPath("$.saleOperationMode").value(SmartPosSaleOperationMode.DIRECT.name))
            .andExpect(jsonPath("$.modules[0].code").value("SALE"))
            .andExpect(jsonPath("$.modules[0].name").value("Vendas"))

        verify(bootstrapService).bootstrap(COMPANY_ID, SERIAL_NUMBER)
    }

    @Test
    fun `POST pos settings validate pin uses authenticated company id instead of URL`() {
        stubValidPosToken()
        given(settingsService.validatePin(COMPANY_ID, "1234")).willReturn(true)

        mockMvc.perform(
            post("/pos/settings/validate-pin")
                .header("Authorization", "Bearer $VALID_TOKEN")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"pin":"1234"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.valid").value(true))
            .andExpect(jsonPath("$.message").value("PIN válido"))

        verify(settingsService).validatePin(COMPANY_ID, "1234")
    }

    @Test
    fun `POST pos settings validate pin returns 422 when business rule blocks operation`() {
        stubValidPosToken()
        given(settingsService.validatePin(COMPANY_ID, "1234"))
            .willThrow(BusinessException("PIN bloqueado temporariamente por muitas tentativas"))

        mockMvc.perform(
            post("/pos/settings/validate-pin")
                .header("Authorization", "Bearer $VALID_TOKEN")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"pin":"1234"}""")
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.status").value(422))
            .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
            .andExpect(jsonPath("$.message").value("PIN bloqueado temporariamente por muitas tentativas"))
            .andExpect(jsonPath("$.path").value("/pos/settings/validate-pin"))
    }

    @Test
    fun `POST pos settings validate pin returns 400 for invalid request body`() {
        stubValidPosToken()

        mockMvc.perform(
            post("/pos/settings/validate-pin")
                .header("Authorization", "Bearer $VALID_TOKEN")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Corpo da requisição inválido"))
            .andExpect(jsonPath("$.path").value("/pos/settings/validate-pin"))
    }

    @Test
    fun `legacy companyId settings route is no longer exposed`() {
        stubValidPosToken()

        mockMvc.perform(
            post("/pos/settings/$COMPANY_ID/validate-pin")
                .header("Authorization", "Bearer $VALID_TOKEN")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"pin":"1234"}""")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Recurso não encontrado"))
            .andExpect(jsonPath("$.path").value("/pos/settings/$COMPANY_ID/validate-pin"))
    }

    @Test
    fun `GET pos settings print type resolves company from JWT`() {
        stubValidPosToken()
        given(settingsService.getPrintType(COMPANY_ID)).willReturn(SmartPosPrint.FULL.name)

        mockMvc.perform(
            get("/pos/settings/print-type")
                .header("Authorization", "Bearer $VALID_TOKEN")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.printType").value(SmartPosPrint.FULL.name))

        verify(settingsService).getPrintType(COMPANY_ID)
    }

    @Test
    fun `GET pos settings print type returns 404 when company settings are missing`() {
        stubValidPosToken()
        given(settingsService.getPrintType(COMPANY_ID))
            .willThrow(ResourceNotFoundException("Configurações não encontradas"))

        mockMvc.perform(
            get("/pos/settings/print-type")
                .header("Authorization", "Bearer $VALID_TOKEN")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Configurações não encontradas"))
            .andExpect(jsonPath("$.path").value("/pos/settings/print-type"))
    }

    @Test
    fun `GET pos settings returns consolidated settings for authenticated company`() {
        stubValidPosToken()
        given(settingsService.getSettings(COMPANY_ID)).willReturn(
            smartPosSettingsResponse(
                print = SmartPosPrint.SHORT,
                printLogo = true,
                logoUrl = "https://cdn.bipos.com/logo.png",
                securityEnabled = true,
                hasPin = true,
                autoLogoutMinutes = 15,
                darkMode = true,
                soundEnabled = false
            )
        )

        mockMvc.perform(
            get("/pos/settings")
                .header("Authorization", "Bearer $VALID_TOKEN")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.saleOperationMode").value(SmartPosSaleOperationMode.DIRECT.name))
            .andExpect(jsonPath("$.print").value(SmartPosPrint.SHORT.name))
            .andExpect(jsonPath("$.printType").value(SmartPosPrint.SHORT.name))
            .andExpect(jsonPath("$.printLogo").value(true))
            .andExpect(jsonPath("$.logoConfigured").value(true))
            .andExpect(jsonPath("$.securityEnabled").value(true))
            .andExpect(jsonPath("$.hasPin").value(true))
            .andExpect(jsonPath("$.pinAttempts").value(0))
            .andExpect(jsonPath("$.autoLogoutMinutes").value(15))
            .andExpect(jsonPath("$.darkMode").value(true))
            .andExpect(jsonPath("$.soundEnabled").value(false))
            .andExpect(jsonPath("$.availableModules[0].moduleType").value(ModuleType.SALE.name))
            .andExpect(jsonPath("$.version").value(1))

        verify(settingsService).getSettings(COMPANY_ID)
    }

    @Test
    fun `PATCH pos settings updates settings using authenticated company id`() {
        stubValidPosToken()

        val request = UpdateSmartPosSettingsRequest(
            saleOperationMode = SmartPosSaleOperationMode.HYBRID.name,
            print = SmartPosPrint.SHORT.name,
            printLogo = true,
            logoUrl = "https://cdn.bipos.com/logo.png",
            securityEnabled = true,
            autoLogoutMinutes = 20,
            darkMode = true,
            soundEnabled = false
        )

        given(settingsService.updateSettings(COMPANY_ID, request)).willReturn(
            smartPosSettingsResponse(
                saleOperationMode = SmartPosSaleOperationMode.HYBRID,
                print = SmartPosPrint.SHORT,
                printLogo = true,
                logoUrl = "https://cdn.bipos.com/logo.png",
                securityEnabled = true,
                hasPin = true,
                autoLogoutMinutes = 20,
                darkMode = true,
                soundEnabled = false
            )
        )

        mockMvc.perform(
            patch("/pos/settings")
                .header("Authorization", "Bearer $VALID_TOKEN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.saleOperationMode").value(SmartPosSaleOperationMode.HYBRID.name))
            .andExpect(jsonPath("$.print").value(SmartPosPrint.SHORT.name))
            .andExpect(jsonPath("$.printType").value(SmartPosPrint.SHORT.name))
            .andExpect(jsonPath("$.printLogo").value(true))
            .andExpect(jsonPath("$.securityEnabled").value(true))
            .andExpect(jsonPath("$.autoLogoutMinutes").value(20))
            .andExpect(jsonPath("$.darkMode").value(true))
            .andExpect(jsonPath("$.soundEnabled").value(false))

        verify(settingsService).updateSettings(COMPANY_ID, request)
    }

    @Test
    fun `PUT pos settings pin updates pin using authenticated company id`() {
        stubValidPosToken()
        given(settingsService.updatePin(COMPANY_ID, "1234")).willReturn(
            smartPosSettings(
                securityEnabled = true,
                pinHash = "encoded-pin"
            )
        )

        mockMvc.perform(
            put("/pos/settings/pin")
                .header("Authorization", "Bearer $VALID_TOKEN")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"pin":"1234"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.securityEnabled").value(true))
            .andExpect(jsonPath("$.hasPin").value(true))
            .andExpect(jsonPath("$.message").value("PIN atualizado com sucesso"))

        verify(settingsService).updatePin(COMPANY_ID, "1234")
    }

    @Test
    fun `GET pos commandas returns overview for authenticated company`() {
        stubValidPosToken()
        val comandaId = UUID.fromString("1d86697d-d115-4c8b-a596-d2fd3a3ca6d0")
        val tableId = UUID.fromString("d1f9ef45-452e-4e1b-9528-c36b9d8e09cf")

        given(comandaService.getOverview(any())).willReturn(
            ComandaOverviewResponse(
                tables = listOf(
                    ComandaTableResponse(
                        id = tableId,
                        name = "Mesa 1",
                        sortOrder = 1,
                        openComanda = ComandaSummaryResponse(
                            id = comandaId,
                            label = "Mesa 1",
                            tableId = tableId,
                            tableName = "Mesa 1",
                            customerName = null,
                            itemCount = 3,
                            totalAmount = BigDecimal("45.00"),
                            updatedAt = LocalDateTime.of(2026, 3, 22, 20, 10)
                        )
                    )
                ),
                openCommandas = listOf(
                    ComandaSummaryResponse(
                        id = comandaId,
                        label = "Mesa 1",
                        tableId = tableId,
                        tableName = "Mesa 1",
                        customerName = null,
                        itemCount = 3,
                        totalAmount = BigDecimal("45.00"),
                        updatedAt = LocalDateTime.of(2026, 3, 22, 20, 10)
                    )
                )
            )
        )
        given(companyService.getCurrentCompany()).willReturn(company())

        mockMvc.perform(
            get("/pos/commandas")
                .header("Authorization", "Bearer $VALID_TOKEN")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.tables[0].name").value("Mesa 1"))
            .andExpect(jsonPath("$.tables[0].openComanda.id").value(comandaId.toString()))
            .andExpect(jsonPath("$.openCommandas[0].itemCount").value(3))

        verify(comandaService).getOverview(any())
    }

    @Test
    fun `POST pos commandas opens a comanda for the authenticated company`() {
        stubValidPosToken()
        val comandaId = UUID.fromString("6a22f4a4-c29d-4d5d-af6a-8cf8c36ec1e3")
        val tableId = UUID.fromString("d1f9ef45-452e-4e1b-9528-c36b9d8e09cf")
        val request = OpenComandaRequest(tableId = tableId)

        given(companyService.getCurrentCompany()).willReturn(company())
        given(userRepository.findByIdAndActiveTrue(USER_ID)).willReturn(appUser())
        given(comandaService.openComanda(any(), any(), any())).willReturn(
            ComandaDetailResponse(
                id = comandaId,
                label = "Mesa 1",
                tableId = tableId,
                tableName = "Mesa 1",
                customerName = null,
                status = "OPEN",
                itemCount = 0,
                totalAmount = BigDecimal.ZERO,
                openedAt = LocalDateTime.of(2026, 3, 22, 20, 15),
                updatedAt = LocalDateTime.of(2026, 3, 22, 20, 15),
                items = emptyList()
            )
        )

        mockMvc.perform(
            post("/pos/commandas")
                .header("Authorization", "Bearer $VALID_TOKEN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(comandaId.toString()))
            .andExpect(jsonPath("$.tableName").value("Mesa 1"))
            .andExpect(jsonPath("$.status").value("OPEN"))

        verify(comandaService).openComanda(any(), any(), any())
    }

    @Test
    fun `POST pos commandas items adds product to the current comanda`() {
        stubValidPosToken()
        val comandaId = UUID.fromString("6a22f4a4-c29d-4d5d-af6a-8cf8c36ec1e3")
        val itemId = UUID.fromString("29460f8d-7dd8-43d0-8f6a-7d6f47d3083c")
        val productId = UUID.fromString("b5f8dcfe-8856-4e33-b1fc-27053fa9547f")
        val request = AddComandaItemRequest(productId = productId, quantity = 2)

        given(companyService.getCurrentCompany()).willReturn(company())
        given(comandaService.addItem(COMPANY_ID, comandaId, request)).willReturn(
            ComandaDetailResponse(
                id = comandaId,
                label = "Mesa 1",
                tableId = null,
                tableName = null,
                customerName = null,
                status = "OPEN",
                itemCount = 2,
                totalAmount = BigDecimal("30.00"),
                openedAt = LocalDateTime.of(2026, 3, 22, 20, 15),
                updatedAt = LocalDateTime.of(2026, 3, 22, 20, 20),
                items = listOf(
                    ComandaItemResponse(
                        id = itemId,
                        productId = productId,
                        productName = "Arroz",
                        imageUrl = null,
                        unitType = "UNIT",
                        quantity = 2,
                        unitPrice = BigDecimal("15.00"),
                        subtotal = BigDecimal("30.00")
                    )
                )
            )
        )

        mockMvc.perform(
            post("/pos/commandas/$comandaId/items")
                .header("Authorization", "Bearer $VALID_TOKEN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.itemCount").value(2))
            .andExpect(jsonPath("$.items[0].productName").value("Arroz"))
            .andExpect(jsonPath("$.items[0].subtotal").value(30.00))

        verify(comandaService).addItem(COMPANY_ID, comandaId, request)
    }
    @Test
    fun `DELETE pos commandas cancels current comanda`() {
        stubValidPosToken()
        val comandaId = UUID.fromString("6a22f4a4-c29d-4d5d-af6a-8cf8c36ec1e3")

        given(companyService.getCurrentCompany()).willReturn(company())

        mockMvc.perform(
            delete("/pos/commandas/$comandaId")
                .header("Authorization", "Bearer $VALID_TOKEN")
        )
            .andExpect(status().isOk)

        verify(comandaService).deleteComanda(COMPANY_ID, comandaId)
    }

    @Test
    fun `POST pos commandas close converts comanda into sale`() {
        stubValidPosToken()
        val comandaId = UUID.fromString("6a22f4a4-c29d-4d5d-af6a-8cf8c36ec1e3")
        val request = CloseComandaRequest(
            paymentMethod = PaymentMethod.PIX,
            amount = BigDecimal("30.00"),
            nsu = "123456",
            authorizationCode = "ABC123"
        )

        given(companyService.getCurrentCompany()).willReturn(company())
        given(userRepository.findByIdAndActiveTrue(USER_ID)).willReturn(appUser())
        given(comandaService.closeComanda(any(), any(), any(), any())).willReturn(
            br.com.bipos.smartposapi.sale.dto.SaleResponse(
                id = UUID.fromString("0fe17d76-574f-45bb-83f5-040658e5f2fe").toString(),
                totalAmount = BigDecimal("30.00"),
                status = SaleStatus.COMPLETED
            )
        )

        mockMvc.perform(
            post("/pos/commandas/$comandaId/close")
                .header("Authorization", "Bearer $VALID_TOKEN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("0fe17d76-574f-45bb-83f5-040658e5f2fe"))
            .andExpect(jsonPath("$.totalAmount").value(30.00))
            .andExpect(jsonPath("$.status").value(SaleStatus.COMPLETED.name))

        verify(comandaService).closeComanda(any(), any(), any(), any())
    }
    private fun stubValidPosToken() {
        given(jwtService.isTokenExpired(VALID_TOKEN)).willReturn(false)
        given(jwtService.extractType(VALID_TOKEN)).willReturn("POS")
        given(jwtService.extractPosPrincipal(VALID_TOKEN)).willReturn(
            PosPrincipal(
                userId = USER_ID,
                companyId = COMPANY_ID,
                serialNumber = SERIAL_NUMBER
            )
        )
    }

    private fun authResponse() = PosAuthResponse(
        token = "access-token",
        company = CompanySnapshot(
            id = COMPANY_ID.toString(),
            name = "Bipos",
            cnpj = "12345678000190",
            logoPath = null,
            email = "contato@bipos.com.br",
            phone = "71999999999"
        ),
        user = UserSnapshot(
            id = USER_ID.toString(),
            name = "Operador POS",
            photoPath = null,
            email = "contato@bipos.com.br",
            role = UserRole.OPERATOR.name
        ),
        pos = PosSnapshot(
            serialNumber = SERIAL_NUMBER,
            version = "1.0.0"
        )
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

    private fun appUser() = AppUser(
        id = USER_ID,
        name = "Operador POS",
        email = "contato@bipos.com.br",
        document = DOCUMENT,
        passwordHash = "\$2a\$10\$abcdefghijklmnopqrstuv",
        role = UserRole.OPERATOR,
        company = company(),
        active = true
    )

    private fun posDevice(company: Company) = PosDevice(
        id = POS_ID,
        serialNumber = SERIAL_NUMBER,
        company = company,
        active = true,
        posVersion = "1.0.0"
    )

    private fun smartPosSettings(
        saleOperationMode: SmartPosSaleOperationMode = SmartPosSaleOperationMode.DIRECT,
        print: SmartPosPrint = SmartPosPrint.FULL,
        printLogo: Boolean = false,
        logoUrl: String? = null,
        securityEnabled: Boolean = false,
        pinHash: String? = null,
        autoLogoutMinutes: Int = 5,
        darkMode: Boolean = false,
        soundEnabled: Boolean = true
    ) = SmartPosSettings(
        companyId = COMPANY_ID,
        saleOperationMode = saleOperationMode,
        print = print,
        printLogo = printLogo,
        logoUrl = logoUrl,
        securityEnabled = securityEnabled,
        pinHash = pinHash,
        pinAttempts = 0,
        autoLogoutMinutes = autoLogoutMinutes,
        darkMode = darkMode,
        soundEnabled = soundEnabled
    )

    private fun smartPosSettingsResponse(
        saleOperationMode: SmartPosSaleOperationMode = SmartPosSaleOperationMode.DIRECT,
        print: SmartPosPrint = SmartPosPrint.FULL,
        printLogo: Boolean = false,
        logoUrl: String? = null,
        securityEnabled: Boolean = false,
        hasPin: Boolean = false,
        autoLogoutMinutes: Int = 5,
        darkMode: Boolean = false,
        soundEnabled: Boolean = true,
        pinAttempts: Int = 0,
        version: Long = 1
    ) = SmartPosSettingsResponse(
        id = UUID.fromString("4d9a6330-8841-452b-bfc2-48179d58d7df"),
        saleOperationMode = saleOperationMode.name,
        print = print.name,
        printType = print.name,
        printLogo = printLogo,
        logoConfigured = !logoUrl.isNullOrBlank(),
        logoUrl = logoUrl,
        securityEnabled = securityEnabled,
        hasPin = hasPin,
        lastPinChange = null,
        pinAttempts = pinAttempts,
        autoLogoutMinutes = autoLogoutMinutes,
        darkMode = darkMode,
        soundEnabled = soundEnabled,
        availableModules = listOf(
            SmartPosAvailableModuleResponse(
                id = UUID.fromString("df9de251-94eb-46f7-87ff-fde1a5dcefe1"),
                companyId = COMPANY_ID,
                moduleId = UUID.fromString("54a8c28f-88cf-4b96-b7e6-385429c08e4a"),
                moduleName = ModuleType.SALE.name,
                moduleType = ModuleType.SALE.name,
                enabled = true,
                createdAt = null,
                updatedAt = null,
                activatedAt = null,
                deactivatedAt = null
            )
        ),
        version = version,
        updatedAt = LocalDateTime.of(2026, 3, 23, 10, 0, 0)
    )

    private fun saleRequest() = SaleRequest(
        items = listOf(
            SaleItemRequest(
                productId = UUID.randomUUID(),
                quantity = 1
            )
        ),
        paymentMethod = PaymentMethod.CREDIT,
        amount = BigDecimal("25.00")
    )

    companion object {
        private val COMPANY_ID: UUID = UUID.fromString("6be3bbb2-622b-4112-8613-92de732331fa")
        private val USER_ID: UUID = UUID.fromString("f22d4dba-30cf-4e82-a60d-b3e6f08b3910")
        private val POS_ID: UUID = UUID.fromString("fe2595e0-b5eb-4acd-bc3d-16a49d302bd3")
        private const val DOCUMENT = "06847947560"
        private const val SERIAL_NUMBER = "POS001"
        private const val VALID_TOKEN = "valid-pos-token"
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(
        exclude = [
            DataSourceAutoConfiguration::class,
            HibernateJpaAutoConfiguration::class,
            JpaRepositoriesAutoConfiguration::class
        ]
    )
    @Import(
        PosAuthController::class,
        PosRefreshController::class,
        PosBootstrapController::class,
        SaleController::class,
        SmartPosSettingsController::class,
        ComandaController::class,
        SecurityConfig::class,
        PosJwtAuthenticationFilter::class,
        SecurityErrorResponseWriter::class,
        ApiExceptionHandler::class,
    )
    open class TestApplication
}






















