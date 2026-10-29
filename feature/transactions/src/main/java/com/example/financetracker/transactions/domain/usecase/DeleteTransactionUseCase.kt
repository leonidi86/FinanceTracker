package com.example.financetracker.transactions.domain.usecase

import com.example.financetracker.transactions.data.TransactionsRepository

class DeleteTransactionUseCase(
    private val repository: TransactionsRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.deleteTransaction(id)
}
