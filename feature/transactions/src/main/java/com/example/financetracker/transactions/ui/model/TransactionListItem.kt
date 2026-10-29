package com.example.financetracker.transactions.ui.model

/** Элемент списка операций: разделитель с датой или карточка операции. */
sealed interface TransactionListItem {
    val key: String

    data class DateHeader(val title: String) : TransactionListItem {
        override val key: String get() = "header_$title"
    }

    data class Row(
        val id: String,
        val title: String?,
        val category: String,
        val amount: String,
        val isIncome: Boolean,
        /** Дата следующего повтора, null если операция не повторяется. */
        val repeatDate: String?
    ) : TransactionListItem {
        override val key: String get() = id
    }
}
