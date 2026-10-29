package com.example.financetracker.transactions.data.dto

import com.google.gson.annotations.SerializedName

data class TransactionDto(
    @SerializedName("id") val id: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("title") val title: String?,
    @SerializedName("category") val category: String,
    @SerializedName("is_income") val isIncome: Boolean,
    // ISO datetime, например 2026-10-19T15:22:12.98Z
    @SerializedName("date") val date: String,
    // 0001-01-01T00:00:00Z означает, что операция не повторяется
    @SerializedName("nextAppearDate") val nextAppearDate: String?
)
