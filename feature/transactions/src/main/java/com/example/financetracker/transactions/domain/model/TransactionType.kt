package com.example.financetracker.transactions.domain.model

enum class TransactionType {
    ALL,
    INCOME,
    EXPENSE;

    fun accepts(isIncome: Boolean): Boolean = when (this) {
        ALL -> true
        INCOME -> isIncome
        EXPENSE -> !isIncome
    }
}
