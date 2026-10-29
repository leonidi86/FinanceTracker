package com.example.financetracker.transactions.domain.usecase

import com.example.financetracker.transactions.data.TransactionsRepository
import com.example.financetracker.transactions.domain.mapper.TransactionMapper
import com.example.financetracker.transactions.domain.model.Category

class GetCategoriesUseCase(
    private val repository: TransactionsRepository,
    private val mapper: TransactionMapper
) {
    suspend operator fun invoke(): Result<List<Category>> =
        repository.getCategories().mapCatching { categories -> categories.map(mapper::map) }
}
