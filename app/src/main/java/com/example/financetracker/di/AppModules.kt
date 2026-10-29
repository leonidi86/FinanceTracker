package com.example.financetracker.di

import com.example.financetracker.core.di.coreModule
import com.example.financetracker.transactions.di.transactionsModule

val appModules = listOf(
    coreModule,
    transactionsModule
)
