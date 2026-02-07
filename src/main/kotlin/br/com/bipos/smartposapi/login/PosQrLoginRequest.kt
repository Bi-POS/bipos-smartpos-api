package br.com.bipos.smartposapi.login

import jakarta.validation.constraints.NotBlank

data class PosQrLoginRequest(

    @field:NotBlank
    val qrToken: String,

    @field:NotBlank
    val serialNumber: String,

    @field:NotBlank
    val posVersion: String
)