package br.com.bipos.smartposapi.domain.module

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "modules")
class Module(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    val name: ModuleType = ModuleType.SALE
)
