package com.example.financetracker.transactions.data.dto

import com.google.gson.annotations.SerializedName

data class TransactionsPageDto(
    @SerializedName("currentPage") val currentPage: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("data") val data: List<TransactionDto>?
)
