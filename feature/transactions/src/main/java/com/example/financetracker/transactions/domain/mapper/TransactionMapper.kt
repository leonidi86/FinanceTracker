package com.example.financetracker.transactions.domain.mapper

import com.example.financetracker.transactions.data.dto.CategoryDto
import com.example.financetracker.transactions.data.dto.TransactionDto
import com.example.financetracker.transactions.data.dto.TransactionsPageDto
import com.example.financetracker.transactions.domain.model.Category
import com.example.financetracker.transactions.domain.model.Transaction
import com.example.financetracker.transactions.domain.model.TransactionsPage
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

/**
 * Сервер отдаёт даты в UTC, а группировать операции нужно по дням пользователя,
 * поэтому даты переводятся в [zoneId].
 */
class TransactionMapper(private val zoneId: ZoneId) {

    fun map(dto: TransactionsPageDto): TransactionsPage = TransactionsPage(
        transactions = dto.data.orEmpty().map(::map),
        currentPage = dto.currentPage,
        totalPages = dto.totalPages
    )

    fun map(dto: TransactionDto): Transaction = Transaction(
        id = dto.id,
        amount = BigDecimal.valueOf(dto.amount),
        title = dto.title?.takeIf { it.isNotBlank() },
        category = dto.category,
        isIncome = dto.isIncome,
        date = toLocalDate(dto.date),
        // Неповторяющиеся операции приходят с датой 0001-01-01
        nextAppearDate = dto.nextAppearDate?.let(::toLocalDate)?.takeIf { it.year > 1 }
    )

    fun map(dto: CategoryDto): Category = Category(name = dto.name, isIncome = dto.isIncome)

    private fun toLocalDate(isoDateTime: String): LocalDate =
        OffsetDateTime.parse(isoDateTime).atZoneSameInstant(zoneId).toLocalDate()
}
