package com.example.financetracker.core.di

import com.example.financetracker.core.network.networkModule
import org.koin.dsl.module

val coreModule = module {
    includes(networkModule)
}
