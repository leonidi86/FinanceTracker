package com.example.financetracker.transactions.data.dto

import com.google.gson.annotations.SerializedName

data class CategoryDto(
    @SerializedName("name") val name: String,
    @SerializedName("is_income") val isIncome: Boolean
)
