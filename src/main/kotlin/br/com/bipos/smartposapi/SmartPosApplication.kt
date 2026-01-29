package br.com.bipos.smartposapi

import br.com.bipos.smartposapi.security.PosJwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EntityScan(basePackages = ["br.com.bipos.smartposapi"])
@EnableConfigurationProperties(PosJwtProperties::class)
@EnableJpaRepositories(basePackages = ["br.com.bipos.smartposapi"])
class SmartPosApplication

fun main(args: Array<String>) {
    runApplication<SmartPosApplication>(*args)
}
