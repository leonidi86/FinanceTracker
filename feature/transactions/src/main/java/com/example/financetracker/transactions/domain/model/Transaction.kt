package com.example.financetracker.transactions.domain.model

import java.math.BigDecimal
import java.time.LocalDate

data class Transaction(
    val id: String,
    val amount: BigDecimal,
    val title: String?,
    val category: String,
    val isIncome: Boolean,
    val date: LocalDate,
    /** Дата следующего повтора, null если операция не повторяется. */
    val nextAppearDate: LocalDate?
)
