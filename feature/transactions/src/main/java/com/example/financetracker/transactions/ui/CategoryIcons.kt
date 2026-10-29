package com.example.financetracker.transactions.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.CurrencyRuble
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.ui.graphics.vector.ImageVector

// Сервер не присылает иконки, поэтому они подбираются по названию предустановленных категорий
private val categoryIcons = mapOf(
    "транспорт" to Icons.Outlined.DirectionsCar,
    "фастфуд" to Icons.Outlined.Restaurant,
    "еда" to Icons.Outlined.Restaurant,
    "здоровье" to Icons.Outlined.FavoriteBorder,
    "зарплата" to Icons.Outlined.Payments,
    "доходы" to Icons.Outlined.Payments,
    "образование" to Icons.Outlined.School,
    "продукты" to Icons.Outlined.ShoppingCart,
    "культура" to Icons.Outlined.TheaterComedy,
    "развлечения" to Icons.Outlined.TheaterComedy,
    "подписки" to Icons.Outlined.Subscriptions,
    "путешествия" to Icons.Outlined.Flight,
    "спорт" to Icons.Outlined.FitnessCenter,
    "одежда" to Icons.Outlined.Checkroom,
    "подарки" to Icons.Outlined.CardGiftcard
)

/** Иконка категории; для созданных пользователем категорий — универсальная. */
fun categoryIcon(category: String): ImageVector =
    categoryIcons[category.trim().lowercase()] ?: Icons.Outlined.CurrencyRuble
