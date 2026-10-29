package com.example.financetracker.transactions.domain.model

import java.time.LocalDate

data class TransactionsFilter(
    val from: LocalDate,
    val to: LocalDate,
    val type: TransactionType = TransactionType.ALL,
    /** null — все категории. */
    val category: String? = null
)
