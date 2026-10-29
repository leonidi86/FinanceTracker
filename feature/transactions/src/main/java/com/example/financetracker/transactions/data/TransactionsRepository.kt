package com.example.financetracker.transactions.data

import com.example.financetracker.transactions.data.dto.CategoryDto
import com.example.financetracker.transactions.data.dto.TransactionsPageDto
import java.time.LocalDate

interface TransactionsRepository {
    suspend fun getTransactions(
        from: LocalDate,
        to: LocalDate,
        category: String?,
        page: Int,
        pageSize: Int
    ): Result<TransactionsPageDto>

    suspend fun getCategories(): Result<List<CategoryDto>>

    suspend fun deleteTransaction(id: String): Result<Unit>
}
