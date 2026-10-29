package com.example.financetracker.transactions.ui.model

import com.example.financetracker.transactions.domain.model.Transaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

private const val NBSP = ' '

class TransactionUiMapperTest {

    private val mapper = TransactionUiMapper()

    @Test
    fun `expense amount has kopecks and minus sign`() {
        assertEquals("-829,54${NBSP}₽", mapper.formatAmount(BigDecimal("829.54"), isIncome = false))
    }

    @Test
    fun `income amount has plus sign`() {
        assertEquals("+5${NBSP}000${NBSP}₽", mapper.formatAmount(BigDecimal("5000"), isIncome = true))
    }

    @Test
    fun `thousands are separated and zero kopecks are dropped`() {
        assertEquals("-1${NBSP}240${NBSP}₽", mapper.formatAmount(BigDecimal("1240.00"), isIncome = false))
        assertEquals("-36${NBSP}550,24${NBSP}₽", mapper.formatAmount(BigDecimal("36550.24"), isIncome = false))
    }

    @Test
    fun `amount is rounded to two digits`() {
        assertEquals("-10,13${NBSP}₽", mapper.formatAmount(BigDecimal("10.125"), isIncome = false))
    }

    @Test
    fun `fractional amount always has two kopeck digits`() {
        assertEquals("-95,50${NBSP}₽", mapper.formatAmount(BigDecimal("95.5"), isIncome = false))
        assertEquals("+1${NBSP}299,90${NBSP}₽", mapper.formatAmount(BigDecimal("1299.9"), isIncome = true))
        assertEquals("-95${NBSP}₽", mapper.formatAmount(BigDecimal("95.00"), isIncome = false))
    }

    @Test
    fun `kopeck display is decided after rounding`() {
        assertEquals("-10${NBSP}₽", mapper.formatAmount(BigDecimal("9.999"), isIncome = false))
        assertEquals("-0,01${NBSP}₽", mapper.formatAmount(BigDecimal("0.005"), isIncome = false))
    }

    @Test
    fun `day header has capitalized weekday and month in genitive`() {
        assertEquals("Среда, 21 октября", mapper.formatDayHeader(LocalDate.of(2026, 10, 21)))
    }

    @Test
    fun `period is capitalized month and year`() {
        assertEquals("Октябрь 2026", mapper.formatPeriod(YearMonth.of(2026, 10)))
    }

    @Test
    fun `header is inserted before each new date`() {
        val items = mapper.toListItems(
            listOf(
                transaction("1", LocalDate.of(2026, 10, 21)),
                transaction("2", LocalDate.of(2026, 10, 21)),
                transaction("3", LocalDate.of(2026, 10, 20))
            )
        )

        assertEquals(
            listOf("header_Среда, 21 октября", "1", "2", "header_Вторник, 20 октября", "3"),
            items.map { it.key }
        )
    }

    @Test
    fun `empty list has no headers`() {
        assertEquals(emptyList<TransactionListItem>(), mapper.toListItems(emptyList()))
    }

    @Test
    fun `row shows repeat date only for repeating transaction`() {
        val items = mapper.toListItems(
            listOf(
                transaction("1", LocalDate.of(2026, 10, 21), nextAppearDate = LocalDate.of(2026, 11, 24)),
                transaction("2", LocalDate.of(2026, 10, 21))
            )
        )
        val rows = items.filterIsInstance<TransactionListItem.Row>()

        assertEquals("24 ноября", rows[0].repeatDate)
        assertNull(rows[1].repeatDate)
    }

    private fun transaction(id: String, date: LocalDate, nextAppearDate: LocalDate? = null) = Transaction(
        id = id,
        amount = BigDecimal.TEN,
        title = null,
        category = "Еда",
        isIncome = false,
        date = date,
        nextAppearDate = nextAppearDate
    )
}
