package com.example.financetracker.transactions.ui

import app.cash.turbine.test
import com.example.financetracker.transactions.FakeTransactionsRepository
import com.example.financetracker.transactions.domain.mapper.TransactionMapper
import com.example.financetracker.transactions.domain.model.TransactionType
import com.example.financetracker.transactions.domain.usecase.DeleteTransactionUseCase
import com.example.financetracker.transactions.domain.usecase.GetCategoriesUseCase
import com.example.financetracker.transactions.domain.usecase.GetTransactionsUseCase
import com.example.financetracker.transactions.transactionDto
import com.example.financetracker.transactions.ui.model.TransactionListItem
import com.example.financetracker.transactions.ui.model.TransactionUiMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelTest {

    private val repository = FakeTransactionsRepository()
    private val clock = Clock.fixed(Instant.parse("2026-10-21T12:00:00Z"), ZoneOffset.UTC)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TransactionsViewModel {
        val mapper = TransactionMapper(ZoneOffset.UTC)
        return TransactionsViewModel(
            getCategoriesUseCase = GetCategoriesUseCase(repository, mapper),
            getTransactionsUseCase = GetTransactionsUseCase(repository, mapper),
            deleteTransactionUseCase = DeleteTransactionUseCase(repository),
            uiMapper = TransactionUiMapper(),
            clock = clock
        )
    }

    private fun TransactionsViewModel.content() =
        uiState.value.screen as TransactionsScreenState.Content

    private fun TransactionsScreenState.Content.rowIds() =
        items.filterIsInstance<TransactionListItem.Row>().map { it.id }

    @Test
    fun `shows content with current month after loading`() {
        repository.pages = listOf(listOf(transactionDto("1"), transactionDto("2")))

        val viewModel = createViewModel()

        assertEquals("Октябрь 2026", viewModel.uiState.value.periodTitle)
        val content = viewModel.content()
        assertEquals(listOf("1", "2"), content.rowIds())
        assertEquals(listOf("Еда", "Транспорт", "Доходы"), content.categories)
        assertEquals(TransactionType.ALL, content.selectedType)
        assertNull(content.selectedCategory)
        assertFalse(content.canLoadMore)
    }

    @Test
    fun `shows error when transactions fail`() {
        repository.failTransactions = true

        val viewModel = createViewModel()

        assertEquals(TransactionsScreenState.Error, viewModel.uiState.value.screen)
    }

    @Test
    fun `shows error when categories fail`() {
        repository.failCategories = true

        val viewModel = createViewModel()

        assertEquals(TransactionsScreenState.Error, viewModel.uiState.value.screen)
    }

    @Test
    fun `retry after error shows content`() {
        repository.failTransactions = true
        val viewModel = createViewModel()

        repository.failTransactions = false
        repository.pages = listOf(listOf(transactionDto("1")))
        viewModel.retry()

        assertEquals(listOf("1"), viewModel.content().rowIds())
    }

    @Test
    fun `income type shows only income categories and transactions`() {
        repository.pages = listOf(
            listOf(transactionDto("1"), transactionDto("2", category = "Доходы", isIncome = true))
        )
        val viewModel = createViewModel()

        viewModel.onTypeSelected(TransactionType.INCOME)

        val content = viewModel.content()
        assertEquals(listOf("Доходы"), content.categories)
        assertEquals(listOf("2"), content.rowIds())
    }

    @Test
    fun `switching type resets category of other type`() {
        repository.pages = listOf(listOf(transactionDto("1")))
        val viewModel = createViewModel()
        viewModel.onCategorySelected("Еда")

        viewModel.onTypeSelected(TransactionType.INCOME)

        assertNull(viewModel.content().selectedCategory)
    }

    @Test
    fun `switching type keeps category of same type`() {
        repository.pages = listOf(listOf(transactionDto("1")))
        val viewModel = createViewModel()
        viewModel.onCategorySelected("Еда")

        viewModel.onTypeSelected(TransactionType.EXPENSE)

        assertEquals("Еда", viewModel.content().selectedCategory)
    }

    @Test
    fun `selecting category reloads from first page with category`() {
        repository.pages = listOf(listOf(transactionDto("1"), transactionDto("2", category = "Транспорт")))
        val viewModel = createViewModel()

        viewModel.onCategorySelected("Транспорт")

        assertEquals(listOf(null, "Транспорт"), repository.requestedCategories)
        assertEquals(listOf("2"), viewModel.content().rowIds())
    }

    @Test
    fun `load more appends next page`() {
        repository.pages = listOf(listOf(transactionDto("1")), listOf(transactionDto("2")))
        val viewModel = createViewModel()
        assertTrue(viewModel.content().canLoadMore)

        viewModel.loadMore()

        val content = viewModel.content()
        assertEquals(listOf("1", "2"), content.rowIds())
        assertFalse(content.canLoadMore)
        assertEquals(listOf(1, 2), repository.requestedPages)
    }

    @Test
    fun `load more does nothing on last page`() {
        repository.pages = listOf(listOf(transactionDto("1")))
        val viewModel = createViewModel()

        viewModel.loadMore()

        assertEquals(listOf(1), repository.requestedPages)
    }

    @Test
    fun `load more skips already loaded transactions`() {
        repository.pages = listOf(listOf(transactionDto("1")), listOf(transactionDto("1"), transactionDto("2")))
        val viewModel = createViewModel()

        viewModel.loadMore()

        assertEquals(listOf("1", "2"), viewModel.content().rowIds())
    }

    @Test
    fun `load more failure keeps list and sends event`() = runTest {
        repository.pages = listOf(listOf(transactionDto("1")), listOf(transactionDto("2")))
        val viewModel = createViewModel()
        repository.failTransactions = true

        viewModel.events.test {
            viewModel.loadMore()

            assertEquals(TransactionsEvent.LoadMoreError, awaitItem())
        }
        val content = viewModel.content()
        assertEquals(listOf("1"), content.rowIds())
        assertFalse(content.isLoadingMore)
    }

    @Test
    fun `delete removes row and its empty date header`() {
        repository.pages = listOf(
            listOf(
                transactionDto("1", date = "2026-10-21T10:00:00Z"),
                transactionDto("2", date = "2026-10-20T10:00:00Z")
            )
        )
        val viewModel = createViewModel()

        viewModel.deleteTransaction("2")

        val items = viewModel.content().items
        assertEquals(listOf("header_Среда, 21 октября", "1"), items.map { it.key })
        assertEquals(listOf("2"), repository.deletedIds)
        // Список не перезапрашивается
        assertEquals(listOf(1), repository.requestedPages)
    }

    @Test
    fun `delete failure keeps row and sends event`() = runTest {
        repository.pages = listOf(listOf(transactionDto("1")))
        val viewModel = createViewModel()
        repository.failDelete = true

        viewModel.events.test {
            viewModel.deleteTransaction("1")

            assertEquals(TransactionsEvent.DeleteError, awaitItem())
        }
        assertEquals(listOf("1"), viewModel.content().rowIds())
    }
}
