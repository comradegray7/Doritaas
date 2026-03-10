package com.example.myapp.view.utils

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Reusable custom shape using MaterialTheme.shapes.medium
 */
object CustomShape {
    @Composable
    fun mediumShape(): CornerBasedShape = MaterialTheme.shapes.medium

    @Composable
    fun extraLargeShape(): CornerBasedShape = MaterialTheme.shapes.extraLarge

    @Composable
    fun circleShape(): RoundedCornerShape = CircleShape
}
