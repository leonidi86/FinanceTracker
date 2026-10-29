package com.example.financetracker.transactions.domain.model

data class TransactionsPage(
    val transactions: List<Transaction>,
    val currentPage: Int,
    val totalPages: Int
) {
    val hasNextPage: Boolean get() = currentPage < totalPages
}
