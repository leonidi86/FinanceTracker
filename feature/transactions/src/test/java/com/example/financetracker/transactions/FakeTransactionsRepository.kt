package com.example.financetracker.transactions

import com.example.financetracker.transactions.data.TransactionsRepository
import com.example.financetracker.transactions.data.dto.CategoryDto
import com.example.financetracker.transactions.data.dto.TransactionDto
import com.example.financetracker.transactions.data.dto.TransactionsPageDto
import java.io.IOException
import java.time.LocalDate

class FakeTransactionsRepository : TransactionsRepository {

    var categories = listOf(
        CategoryDto("Еда", isIncome = false),
        CategoryDto("Транспорт", isIncome = false),
        CategoryDto("Доходы", isIncome = true)
    )

    /** Страницы по номеру, начиная с 1. */
    var pages: List<List<TransactionDto>> = emptyList()

    var failCategories = false
    var failTransactions = false
    var failDelete = false

    val requestedPages = mutableListOf<Int>()
    val requestedCategories = mutableListOf<String?>()
    val deletedIds = mutableListOf<String>()

    override suspend fun getTransactions(
        from: LocalDate,
        to: LocalDate,
        category: String?,
        page: Int,
        pageSize: Int
    ): Result<TransactionsPageDto> {
        requestedPages += page
        requestedCategories += category
        if (failTransactions) return Result.failure(IOException())
        val data = pages.getOrElse(page - 1) { emptyList() }
            .filter { category == null || it.category == category }
        return Result.success(TransactionsPageDto(currentPage = page, totalPages = pages.size, data = data))
    }

    override suspend fun getCategories(): Result<List<CategoryDto>> =
        if (failCategories) Result.failure(IOException()) else Result.success(categories)

    override suspend fun deleteTransaction(id: String): Result<Unit> {
        if (failDelete) return Result.failure(IOException())
        deletedIds += id
        return Result.success(Unit)
    }
}

fun transactionDto(
    id: String,
    date: String = "2026-10-21T10:00:00Z",
    amount: Double = 100.0,
    category: String = "Еда",
    isIncome: Boolean = false,
    title: String? = "Операция $id",
    nextAppearDate: String? = "0001-01-01T00:00:00Z"
) = TransactionDto(id, amount, title, category, isIncome, date, nextAppearDate)
