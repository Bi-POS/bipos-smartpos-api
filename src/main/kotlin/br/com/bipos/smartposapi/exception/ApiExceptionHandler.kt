package br.com.bipos.smartposapi.exception

import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.time.Instant

@ControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(
        ex: IllegalArgumentException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> =
        build(
            status = HttpStatus.BAD_REQUEST,
            message = ex.message ?: "Requisição inválida",
            request = request
        )

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableMessage(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> =
        build(
            status = HttpStatus.BAD_REQUEST,
            message = "Corpo da requisição inválido",
            request = request
        )

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNotFound(
        ex: EntityNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> =
        build(
            status = HttpStatus.NOT_FOUND,
            message = ex.message ?: "Recurso não encontrado",
            request = request
        )

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(
        ex: NoResourceFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> =
        build(
            status = HttpStatus.NOT_FOUND,
            message = "Recurso não encontrado",
            request = request
        )

    @ExceptionHandler(AccessDeniedException::class)
    fun handleForbidden(
        ex: AccessDeniedException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> =
        build(
            status = HttpStatus.FORBIDDEN,
            message = "Acesso negado",
            request = request
        )

    @ExceptionHandler(Exception::class)
    fun handleGeneric(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        ex.printStackTrace() // 🔴 log interno apenas
        return build(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            message = "Erro interno inesperado",
            request = request
        )
    }

    private fun build(
        status: HttpStatus,
        message: String,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity
            .status(status)
            .body(
                ApiErrorResponse(
                    timestamp = Instant.now(),
                    status = status.value(),
                    error = status.reasonPhrase,
                    message = message,
                    path = request.requestURI
                )
            )
}
