package com.example.financetracker.transactions.domain.mapper

import com.example.financetracker.transactions.transactionDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class TransactionMapperTest {

    private val mapper = TransactionMapper(ZoneOffset.UTC)

    @Test
    fun `parses date with nanoseconds`() {
        val transaction = mapper.map(transactionDto("1", date = "2026-10-19T15:22:12.980683295Z"))

        assertEquals(LocalDate.of(2026, 10, 19), transaction.date)
    }

    @Test
    fun `converts date to user time zone`() {
        val moscowMapper = TransactionMapper(ZoneId.of("Europe/Moscow"))

        val transaction = moscowMapper.map(transactionDto("1", date = "2026-10-19T22:30:00Z"))

        assertEquals(LocalDate.of(2026, 10, 20), transaction.date)
    }

    @Test
    fun `placeholder next appear date means no repeat`() {
        val transaction = mapper.map(transactionDto("1", nextAppearDate = "0001-01-01T00:00:00Z"))

        assertNull(transaction.nextAppearDate)
    }

    @Test
    fun `placeholder next appear date in negative offset zone means no repeat`() {
        val newYorkMapper = TransactionMapper(ZoneId.of("America/New_York"))

        val transaction = newYorkMapper.map(transactionDto("1", nextAppearDate = "0001-01-01T00:00:00Z"))

        assertNull(transaction.nextAppearDate)
    }

    @Test
    fun `real next appear date is kept`() {
        val transaction = mapper.map(transactionDto("1", nextAppearDate = "2026-11-21T15:22:12.98Z"))

        assertEquals(LocalDate.of(2026, 11, 21), transaction.nextAppearDate)
    }

    @Test
    fun `blank title becomes null`() {
        assertNull(mapper.map(transactionDto("1", title = "  ")).title)
        assertNull(mapper.map(transactionDto("1", title = null)).title)
    }

    @Test
    fun `amount keeps kopecks`() {
        val transaction = mapper.map(transactionDto("1", amount = 829.54))

        assertEquals(BigDecimal("829.54"), transaction.amount)
    }
}
