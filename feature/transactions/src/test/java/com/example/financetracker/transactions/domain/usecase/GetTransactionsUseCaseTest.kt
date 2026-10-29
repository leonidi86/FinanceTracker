package com.example.financetracker.transactions.domain.usecase

import com.example.financetracker.transactions.FakeTransactionsRepository
import com.example.financetracker.transactions.domain.mapper.TransactionMapper
import com.example.financetracker.transactions.domain.model.TransactionType
import com.example.financetracker.transactions.domain.model.TransactionsFilter
import com.example.financetracker.transactions.transactionDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class GetTransactionsUseCaseTest {

    private val repository = FakeTransactionsRepository()
    private val useCase = GetTransactionsUseCase(repository, TransactionMapper(ZoneOffset.UTC))
    private val filter = TransactionsFilter(from = LocalDate.of(2026, 10, 1), to = LocalDate.of(2026, 10, 31))

    @Test
    fun `expense filter drops income on client`() = runTest {
        repository.pages = listOf(
            listOf(transactionDto("1"), transactionDto("2", category = "Доходы", isIncome = true))
        )

        val page = useCase(filter.copy(type = TransactionType.EXPENSE), page = 1).getOrThrow()

        assertEquals(listOf("1"), page.transactions.map { it.id })
    }

    @Test
    fun `skips pages without matching transactions`() = runTest {
        repository.pages = listOf(
            listOf(transactionDto("1")),
            listOf(transactionDto("2")),
            listOf(transactionDto("3", category = "Доходы", isIncome = true))
        )

        val page = useCase(filter.copy(type = TransactionType.INCOME), page = 1).getOrThrow()

        assertEquals(listOf("3"), page.transactions.map { it.id })
        assertEquals(3, page.currentPage)
        assertEquals(listOf(1, 2, 3), repository.requestedPages)
    }

    @Test
    fun `stops on last page even if nothing matches`() = runTest {
        repository.pages = listOf(listOf(transactionDto("1")), listOf(transactionDto("2")))

        val page = useCase(filter.copy(type = TransactionType.INCOME), page = 1).getOrThrow()

        assertTrue(page.transactions.isEmpty())
        assertFalse(page.hasNextPage)
    }

    @Test
    fun `passes selected category to server`() = runTest {
        useCase(filter.copy(category = "Транспорт"), page = 1)

        assertEquals(listOf("Транспорт"), repository.requestedCategories)
    }

    @Test
    fun `returns failure on network error`() = runTest {
        repository.failTransactions = true

        assertTrue(useCase(filter, page = 1).isFailure)
    }
}
