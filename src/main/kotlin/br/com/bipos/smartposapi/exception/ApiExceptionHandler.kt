package br.com.bipos.smartposapi.exception

import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.time.Instant

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class ApiExceptionHandler {
    companion object {
        private val logger = LoggerFactory.getLogger(ApiExceptionHandler::class.java)
    }

    @ExceptionHandler(
        InvalidPosCredentialsException::class,
        InvalidRefreshTokenException::class,
        InvalidQrTokenException::class,
        QrTokenExpiredException::class
    )
    fun handleUnauthorized(
        ex: RuntimeException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> =
        build(
            status = HttpStatus.UNAUTHORIZED,
            message = ex.message ?: "Não autorizado",
            request = request
        )

    @ExceptionHandler(InvalidTerminalException::class)
    fun handleInvalidTerminal(
        ex: InvalidTerminalException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> =
        build(
            status = HttpStatus.FORBIDDEN,
            message = ex.message ?: "Terminal inválido",
            request = request
        )

    @ExceptionHandler(
        IllegalArgumentException::class,
        ConstraintViolationException::class,
        MissingServletRequestParameterException::class,
        MethodArgumentTypeMismatchException::class
    )
    fun handleBadRequest(
        ex: Exception,
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

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> =
        build(
            status = HttpStatus.BAD_REQUEST,
            message = ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
                ?: ex.bindingResult.globalErrors.firstOrNull()?.defaultMessage
                ?: "Requisição inválida",
            request = request
        )

    @ExceptionHandler(BusinessException::class)
    fun handleBusiness(
        ex: BusinessException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> =
        build(
            status = HttpStatus.UNPROCESSABLE_ENTITY,
            message = ex.message ?: "Operação inválida",
            request = request
        )

    @ExceptionHandler(
        ResourceNotFoundException::class,
        EntityNotFoundException::class,
        NoResourceFoundException::class
    )
    fun handleNotFound(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> =
        build(
            status = HttpStatus.NOT_FOUND,
            message = if (ex is NoResourceFoundException) {
                "Recurso não encontrado"
            } else {
                ex.message ?: "Recurso não encontrado"
            },
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
        logger.error(
            "Unhandled exception while processing {} {}",
            request.method,
            request.requestURI,
            ex
        )
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
