package com.example.financetracker.transactions.data

import com.example.financetracker.transactions.data.dto.CategoryDto
import com.example.financetracker.transactions.data.dto.TransactionsPageDto
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TransactionsApi {

    /** [from] и [to] в формате YYYY-MM-DD. Без [category] возвращаются операции всех категорий. */
    @GET("api/transactions")
    suspend fun getTransactions(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("category") category: String?,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): TransactionsPageDto

    @GET("api/categories")
    suspend fun getCategories(): List<CategoryDto>

    // Ответ 204 без тела, поэтому Response<Unit>, а не Unit
    @DELETE("api/transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: String): Response<Unit>
}
