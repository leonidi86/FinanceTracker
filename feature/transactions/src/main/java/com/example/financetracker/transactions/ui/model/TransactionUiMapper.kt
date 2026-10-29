package com.example.financetracker.transactions.ui.model

import com.example.financetracker.transactions.domain.model.Transaction
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val RU = Locale.forLanguageTag("ru")
private const val NBSP = ' '
private const val CURRENCY = "₽"

class TransactionUiMapper {

    // Неразрывные пробелы, чтобы сумма не переносилась на середине числа
    private val amountFormat = DecimalFormat("#,##0.##", DecimalFormatSymbols(RU).apply {
        groupingSeparator = NBSP
        decimalSeparator = ','
    }).apply { roundingMode = RoundingMode.HALF_UP }

    private val dayHeaderFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", RU)
    private val repeatDateFormatter = DateTimeFormatter.ofPattern("d MMMM", RU)
    private val periodFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", RU)

    /**
     * Список приходит отсортированным по дате, поэтому перед каждой новой датой
     * достаточно вставить разделитель.
     */
    fun toListItems(transactions: List<Transaction>): List<TransactionListItem> = buildList {
        var previousDate: LocalDate? = null
        for (transaction in transactions) {
            if (transaction.date != previousDate) {
                add(TransactionListItem.DateHeader(formatDayHeader(transaction.date)))
                previousDate = transaction.date
            }
            add(toRow(transaction))
        }
    }

    /** «829,54 ₽» для расхода, «+5 000 ₽» для дохода. Копейки показываются, только если они есть. */
    fun formatAmount(amount: BigDecimal, isIncome: Boolean): String {
        val sign = if (isIncome) "+" else ""
        return "$sign${amountFormat.format(amount)}$NBSP$CURRENCY"
    }

    /** «Вторник, 21 августа». */
    fun formatDayHeader(date: LocalDate): String = dayHeaderFormatter.format(date).capitalized()

    /** «Октябрь 2026». */
    fun formatPeriod(period: YearMonth): String = periodFormatter.format(period).capitalized()

    private fun toRow(transaction: Transaction) = TransactionListItem.Row(
        id = transaction.id,
        title = transaction.title,
        category = transaction.category,
        amount = formatAmount(transaction.amount, transaction.isIncome),
        isIncome = transaction.isIncome,
        repeatDate = transaction.nextAppearDate?.let(repeatDateFormatter::format)
    )

    private fun String.capitalized(): String = replaceFirstChar { it.titlecase(RU) }
}
