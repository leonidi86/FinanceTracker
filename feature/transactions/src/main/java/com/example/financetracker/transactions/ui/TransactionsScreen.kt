package com.example.financetracker.transactions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financetracker.core.ui.components.ErrorState
import com.example.financetracker.core.ui.components.ShimmerBox
import com.example.financetracker.core.ui.components.SwipeToRevealBox
import com.example.financetracker.core.ui.theme.FinanceTrackerTheme
import com.example.financetracker.transactions.R
import com.example.financetracker.transactions.domain.model.TransactionType
import com.example.financetracker.transactions.ui.model.TransactionListItem
import org.koin.androidx.compose.koinViewModel

// Подгружать следующую страницу, когда до конца списка остаётся столько элементов
private const val LOAD_MORE_THRESHOLD = 3
private const val SHIMMER_ROWS = 6

@Composable
fun TransactionsScreen(
    onAddClick: () -> Unit = {},
    viewModel: TransactionsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val deleteErrorText = stringResource(R.string.transactions_delete_error)
    val loadMoreErrorText = stringResource(R.string.transactions_load_more_error)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val message = when (event) {
                TransactionsEvent.DeleteError -> deleteErrorText
                TransactionsEvent.LoadMoreError -> loadMoreErrorText
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    TransactionsScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onRetry = viewModel::retry,
        onTypeSelected = viewModel::onTypeSelected,
        onCategorySelected = viewModel::onCategorySelected,
        onLoadMore = viewModel::loadMore,
        onDelete = viewModel::deleteTransaction,
        onAddClick = onAddClick
    )
}

@Composable
private fun TransactionsScreenContent(
    uiState: TransactionsUiState,
    snackbarHostState: SnackbarHostState,
    onRetry: () -> Unit,
    onTypeSelected: (TransactionType) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onLoadMore: () -> Unit,
    onDelete: (String) -> Unit,
    onAddClick: () -> Unit
) {
    val screen = uiState.screen
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (screen is TransactionsScreenState.Content) {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.transactions_add))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = stringResource(R.string.transactions_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
            )
            when (screen) {
                TransactionsScreenState.Loading -> TransactionsLoading()
                TransactionsScreenState.Error -> ErrorState(
                    message = stringResource(R.string.transactions_error),
                    icon = Icons.Filled.BugReport,
                    retryText = stringResource(R.string.transactions_retry),
                    onRetry = onRetry
                )
                is TransactionsScreenState.Content -> TransactionsContent(
                    periodTitle = uiState.periodTitle,
                    content = screen,
                    onTypeSelected = onTypeSelected,
                    onCategorySelected = onCategorySelected,
                    onLoadMore = onLoadMore,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
private fun TransactionsContent(
    periodTitle: String,
    content: TransactionsScreenState.Content,
    onTypeSelected: (TransactionType) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onLoadMore: () -> Unit,
    onDelete: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val isNearEnd by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= listState.layoutInfo.totalItemsCount - LOAD_MORE_THRESHOLD
        }
    }
    // Размер списка в ключе, чтобы после подгрузки проверить конец ещё раз:
    // на большом экране новая страница может целиком поместиться без прокрутки
    LaunchedEffect(isNearEnd, content.canLoadMore, content.items.size) {
        if (isNearEnd && content.canLoadMore) onLoadMore()
    }

    LazyColumn(
        state = listState,
        // Место под FAB, чтобы он не закрывал сумму последней операции
        contentPadding = PaddingValues(bottom = 88.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(key = "period") {
            Text(
                text = periodTitle,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        item(key = "filters") {
            TransactionFilters(
                selectedType = content.selectedType,
                categories = content.categories,
                selectedCategory = content.selectedCategory,
                onTypeSelected = onTypeSelected,
                onCategorySelected = onCategorySelected
            )
        }
        when {
            content.isListLoading -> items(SHIMMER_ROWS) { ShimmerRow() }
            content.items.isEmpty() -> item(key = "empty") {
                Text(
                    text = stringResource(R.string.transactions_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            }
            else -> items(
                items = content.items,
                key = { it.key },
                contentType = { it::class }
            ) { item ->
                when (item) {
                    is TransactionListItem.DateHeader -> DateHeader(item.title, Modifier.animateItem())
                    is TransactionListItem.Row -> SwipeToRevealBox(
                        modifier = Modifier.animateItem(),
                        actions = { close ->
                            DeleteAction(onClick = {
                                close()
                                onDelete(item.id)
                            })
                        }
                    ) {
                        TransactionRow(item)
                    }
                }
            }
        }
        if (content.isLoadingMore) {
            item(key = "loadingMore") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun TransactionFilters(
    selectedType: TransactionType,
    categories: List<String>,
    selectedCategory: String?,
    onTypeSelected: (TransactionType) -> Unit,
    onCategorySelected: (String?) -> Unit
) {
    Column(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)) {
        val types = TransactionType.entries
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            types.forEachIndexed { index, type ->
                SegmentedButton(
                    selected = type == selectedType,
                    onClick = { onTypeSelected(type) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.secondary,
                        activeContentColor = MaterialTheme.colorScheme.onSecondary,
                        activeBorderColor = Color.Transparent,
                        inactiveContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        inactiveContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        inactiveBorderColor = Color.Transparent
                    ),
                    // В макете без галочки у выбранного сегмента
                    icon = {}
                ) {
                    Text(text = stringResource(type.titleRes()), maxLines = 1)
                }
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 12.dp)
        ) {
            item(key = "all") {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text(stringResource(R.string.transactions_all_categories)) }
                )
            }
            items(items = categories, key = { it }) { category ->
                FilterChip(
                    selected = category == selectedCategory,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) },
                    leadingIcon = {
                        Icon(
                            imageVector = categoryIcon(category),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                )
            }
        }
    }
}

private fun TransactionType.titleRes(): Int = when (this) {
    TransactionType.ALL -> R.string.transactions_type_all
    TransactionType.INCOME -> R.string.transactions_type_income
    TransactionType.EXPENSE -> R.string.transactions_type_expense
}

@Composable
private fun DateHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun TransactionRow(item: TransactionListItem.Row) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = categoryIcon(item.category),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Без названия главной строкой становится категория
                if (item.title != null) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(text = item.category, style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                if (item.repeatDate != null) {
                    Text(
                        text = stringResource(R.string.transactions_repeat),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.repeatDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = item.amount,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.isIncome) {
                        FinanceTrackerTheme.extendedColors.income
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun DeleteAction(onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.error)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.transactions_delete),
                tint = MaterialTheme.colorScheme.onError
            )
        }
    }
}

@Composable
private fun TransactionsLoading() {
    Column(modifier = Modifier.fillMaxSize()) {
        ShimmerBox(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .width(144.dp)
                .height(14.dp)
        )
        ShimmerBox(
            shape = RoundedCornerShape(percent = 50),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .height(40.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            repeat(4) {
                ShimmerBox(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(84.dp)
                        .height(32.dp)
                )
            }
        }
        repeat(SHIMMER_ROWS) { ShimmerRow() }
    }
}

@Composable
private fun ShimmerRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 16.dp)
    ) {
        ShimmerBox(modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        ShimmerBox(
            modifier = Modifier
                .weight(1f)
                .height(16.dp)
        )
        Spacer(modifier = Modifier.width(64.dp))
        ShimmerBox(
            modifier = Modifier
                .width(80.dp)
                .height(16.dp)
        )
    }
}

@Preview
@Composable
private fun TransactionsScreenContentPreview() {
    FinanceTrackerTheme {
        TransactionsScreenContent(
            uiState = TransactionsUiState(
                periodTitle = "Октябрь 2026",
                screen = TransactionsScreenState.Content(
                    selectedType = TransactionType.ALL,
                    categories = listOf("Транспорт", "Еда", "Здоровье"),
                    selectedCategory = null,
                    items = listOf(
                        TransactionListItem.DateHeader("Вторник, 21 октября"),
                        TransactionListItem.Row("1", "Поел в Теремке", "Еда", "829,54 ₽", false, "24 ноября"),
                        TransactionListItem.Row("2", null, "Здоровье", "1 240 ₽", false, null),
                        TransactionListItem.Row("3", "Саня вернул долг", "Доходы", "+5 000 ₽", true, null)
                    )
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onRetry = {},
            onTypeSelected = {},
            onCategorySelected = {},
            onLoadMore = {},
            onDelete = {},
            onAddClick = {}
        )
    }
}
