package com.example.financetracker.transactions.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.transactions.domain.model.Category
import com.example.financetracker.transactions.domain.model.Transaction
import com.example.financetracker.transactions.domain.model.TransactionType
import com.example.financetracker.transactions.domain.model.TransactionsFilter
import com.example.financetracker.transactions.domain.model.TransactionsPage
import com.example.financetracker.transactions.domain.usecase.DeleteTransactionUseCase
import com.example.financetracker.transactions.domain.usecase.GetCategoriesUseCase
import com.example.financetracker.transactions.domain.usecase.GetTransactionsUseCase
import com.example.financetracker.transactions.ui.model.TransactionListItem
import com.example.financetracker.transactions.ui.model.TransactionUiMapper
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.YearMonth

data class TransactionsUiState(
    val periodTitle: String,
    val screen: TransactionsScreenState = TransactionsScreenState.Loading
)

sealed interface TransactionsScreenState {
    data object Loading : TransactionsScreenState
    data object Error : TransactionsScreenState
    data class Content(
        val selectedType: TransactionType,
        /** Категории, подходящие под выбранный тип. */
        val categories: List<String>,
        /** null — выбраны все категории. */
        val selectedCategory: String?,
        val items: List<TransactionListItem>,
        /** Список перезагружается после смены фильтра, фильтры при этом остаются на экране. */
        val isListLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val canLoadMore: Boolean = false
    ) : TransactionsScreenState
}

sealed interface TransactionsEvent {
    data object DeleteError : TransactionsEvent
    data object LoadMoreError : TransactionsEvent
}

class TransactionsViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val uiMapper: TransactionUiMapper,
    clock: Clock
) : ViewModel() {

    // Выбор периода не входит в задачу, всегда показывается текущий месяц
    private val period = YearMonth.now(clock)

    private val _uiState = MutableStateFlow(TransactionsUiState(periodTitle = uiMapper.formatPeriod(period)))
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    private val _events = Channel<TransactionsEvent>(Channel.BUFFERED)
    val events: Flow<TransactionsEvent> = _events.receiveAsFlow()

    private var filter = TransactionsFilter(from = period.atDay(1), to = period.atEndOfMonth())
    private var categories = emptyList<Category>()
    private var transactions = emptyList<Transaction>()
    private var currentPage = 0
    private var hasNextPage = false
    private var isListLoading = false
    private var isLoadingMore = false

    // Любая загрузка списка (первая страница или следующая) идёт через эту джобу,
    // чтобы ответ по старому фильтру не перезаписал новый
    private var loadJob: Job? = null

    init {
        loadAll()
    }

    fun retry() {
        loadAll()
    }

    fun onTypeSelected(type: TransactionType) {
        if (type == filter.type) return
        // Категория другого типа под новым фильтром дала бы пустой список
        val category = filter.category?.takeIf { name ->
            categories.any { it.name == name && type.accepts(it.isIncome) }
        }
        filter = filter.copy(type = type, category = category)
        reloadTransactions()
    }

    fun onCategorySelected(category: String?) {
        if (category == filter.category) return
        filter = filter.copy(category = category)
        reloadTransactions()
    }

    fun loadMore() {
        if (!hasNextPage || loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            isLoadingMore = true
            showContent()
            val result = getTransactionsUseCase(filter, currentPage + 1)
            isLoadingMore = false
            result
                .onSuccess { page ->
                    // После удаления операции страницы на сервере сдвигаются, и операция
                    // может прийти повторно
                    val loadedIds = transactions.mapTo(HashSet()) { it.id }
                    applyPage(page, transactions + page.transactions.filterNot { it.id in loadedIds })
                }
                .onFailure {
                    showContent()
                    _events.send(TransactionsEvent.LoadMoreError)
                }
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            deleteTransactionUseCase(id)
                .onSuccess {
                    transactions = transactions.filterNot { it.id == id }
                    showContent()
                }
                .onFailure { _events.send(TransactionsEvent.DeleteError) }
        }
    }

    private fun loadAll() {
        loadJob?.cancel()
        isListLoading = false
        isLoadingMore = false
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(screen = TransactionsScreenState.Loading) }
            val categoriesResult = async { getCategoriesUseCase() }
            val pageResult = getTransactionsUseCase(filter, page = 1)

            val loadedCategories = categoriesResult.await().getOrNull()
            val page = pageResult.getOrNull()
            if (loadedCategories == null || page == null) {
                _uiState.update { it.copy(screen = TransactionsScreenState.Error) }
                return@launch
            }
            categories = loadedCategories
            applyPage(page, page.transactions)
        }
    }

    private fun reloadTransactions() {
        loadJob?.cancel()
        isLoadingMore = false
        loadJob = viewModelScope.launch {
            isListLoading = true
            showContent()
            val result = getTransactionsUseCase(filter, page = 1)
            isListLoading = false
            result
                .onSuccess { page -> applyPage(page, page.transactions) }
                .onFailure { _uiState.update { it.copy(screen = TransactionsScreenState.Error) } }
        }
    }

    private fun applyPage(page: TransactionsPage, allTransactions: List<Transaction>) {
        transactions = allTransactions
        currentPage = page.currentPage
        hasNextPage = page.hasNextPage
        showContent()
    }

    private fun showContent() {
        _uiState.update {
            it.copy(
                screen = TransactionsScreenState.Content(
                    selectedType = filter.type,
                    categories = categories.filter { category -> filter.type.accepts(category.isIncome) }
                        .map(Category::name),
                    selectedCategory = filter.category,
                    items = if (isListLoading) emptyList() else uiMapper.toListItems(transactions),
                    isListLoading = isListLoading,
                    isLoadingMore = isLoadingMore,
                    canLoadMore = hasNextPage
                )
            )
        }
    }
}
