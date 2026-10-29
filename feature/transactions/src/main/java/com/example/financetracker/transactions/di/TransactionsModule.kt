package com.example.financetracker.transactions.di

import com.example.financetracker.transactions.data.TransactionsApi
import com.example.financetracker.transactions.data.TransactionsRepository
import com.example.financetracker.transactions.data.TransactionsRepositoryImpl
import com.example.financetracker.transactions.domain.mapper.TransactionMapper
import com.example.financetracker.transactions.domain.usecase.DeleteTransactionUseCase
import com.example.financetracker.transactions.domain.usecase.GetCategoriesUseCase
import com.example.financetracker.transactions.domain.usecase.GetTransactionsUseCase
import com.example.financetracker.transactions.ui.TransactionsViewModel
import com.example.financetracker.transactions.ui.model.TransactionUiMapper
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import java.time.Clock
import java.time.ZoneId

val transactionsModule = module {
    single<TransactionsApi> { get<Retrofit>().create(TransactionsApi::class.java) }
    single<TransactionsRepository> { TransactionsRepositoryImpl(get()) }
    single { TransactionMapper(ZoneId.systemDefault()) }
    single { TransactionUiMapper() }

    factory { GetCategoriesUseCase(get(), get()) }
    factory { GetTransactionsUseCase(get(), get()) }
    factory { DeleteTransactionUseCase(get()) }

    viewModel { TransactionsViewModel(get(), get(), get(), get(), Clock.systemDefaultZone()) }
}
