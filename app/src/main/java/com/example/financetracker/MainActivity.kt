package com.example.financetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.financetracker.core.ui.theme.FinanceTrackerTheme
import com.example.financetracker.transactions.ui.TransactionsScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceTrackerTheme {
                // Bottom sheet добавления операции пока не реализован, FAB ничего не делает
                TransactionsScreen()
            }
        }
    }
}
