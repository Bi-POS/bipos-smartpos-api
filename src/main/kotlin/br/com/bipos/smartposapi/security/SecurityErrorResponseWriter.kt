package br.com.bipos.smartposapi.security

import br.com.bipos.smartposapi.exception.ApiErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant

@Component
class SecurityErrorResponseWriter(
    private val objectMapper: ObjectMapper
) {
    fun write(
        response: HttpServletResponse,
        status: HttpStatus,
        message: String,
        path: String
    ) {
        if (response.isCommitted) {
            return
        }

        response.status = status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = StandardCharsets.UTF_8.name()

        objectMapper.writeValue(
            response.writer,
            ApiErrorResponse(
                timestamp = Instant.now(),
                status = status.value(),
                error = status.reasonPhrase,
                message = message,
                path = path
            )
        )
    }
}
