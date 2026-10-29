package com.example.financetracker.transactions.data

import com.example.financetracker.core.network.safeApiCall
import com.example.financetracker.transactions.data.dto.CategoryDto
import com.example.financetracker.transactions.data.dto.TransactionsPageDto
import retrofit2.HttpException
import java.time.LocalDate

class TransactionsRepositoryImpl(
    private val transactionsApi: TransactionsApi
) : TransactionsRepository {

    override suspend fun getTransactions(
        from: LocalDate,
        to: LocalDate,
        category: String?,
        page: Int,
        pageSize: Int
    ): Result<TransactionsPageDto> = safeApiCall {
        // LocalDate.toString() как раз даёт YYYY-MM-DD
        transactionsApi.getTransactions(from.toString(), to.toString(), category, page, pageSize)
    }

    override suspend fun getCategories(): Result<List<CategoryDto>> = safeApiCall {
        transactionsApi.getCategories()
    }

    override suspend fun deleteTransaction(id: String): Result<Unit> = safeApiCall {
        val response = transactionsApi.deleteTransaction(id)
        if (!response.isSuccessful) throw HttpException(response)
    }
}
