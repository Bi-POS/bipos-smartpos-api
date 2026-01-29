package br.com.bipos.smartposapi

import br.com.bipos.smartposapi.security.PosJwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableConfigurationProperties(PosJwtProperties::class)
class SmartPosApplication

fun main(args: Array<String>) {
    runApplication<SmartPosApplication>(*args)
}
