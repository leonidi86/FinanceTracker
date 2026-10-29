package com.example.financetracker.transactions.domain.usecase

import com.example.financetracker.transactions.data.TransactionsRepository
import com.example.financetracker.transactions.domain.mapper.TransactionMapper
import com.example.financetracker.transactions.domain.model.TransactionsFilter
import com.example.financetracker.transactions.domain.model.TransactionsPage

const val PAGE_SIZE = 10

class GetTransactionsUseCase(
    private val repository: TransactionsRepository,
    private val mapper: TransactionMapper
) {
    /**
     * Загружает страницу [page] с учётом [filter].
     *
     * Сервер не умеет фильтровать по типу операции, поэтому тип отсекается на клиенте.
     * Если на странице не осталось подходящих операций, сразу грузится следующая,
     * чтобы пагинация не застревала на пустых страницах.
     */
    suspend operator fun invoke(filter: TransactionsFilter, page: Int): Result<TransactionsPage> {
        var nextPage = page
        while (true) {
            val loaded = repository
                .getTransactions(filter.from, filter.to, filter.category, nextPage, PAGE_SIZE)
                .mapCatching { mapper.map(it) }
                .getOrElse { return Result.failure(it) }

            val matching = loaded.transactions.filter { filter.type.accepts(it.isIncome) }
            if (matching.isNotEmpty() || !loaded.hasNextPage) {
                return Result.success(loaded.copy(transactions = matching))
            }
            nextPage = loaded.currentPage + 1
        }
    }
}
