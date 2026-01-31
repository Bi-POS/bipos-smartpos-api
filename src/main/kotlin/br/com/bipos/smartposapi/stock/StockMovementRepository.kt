package br.com.bipos.smartposapi.stock

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface StockMovementRepository : JpaRepository<StockMovement, UUID>
