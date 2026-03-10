package com.example.myapp.data.dataclass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * ColorItem - Product color variant data model
 *
 * Represents a color option for products with associated hex code for display.
 *
 * @property id Unique color identifier
 * @property hexCode CSS hex color code (e.g., "#FF0000")
 * @property name Display name of the color
 */
@Parcelize
data class ColorItem(
    val id: String = "",
    val hexCode: String = " ",
    val name: String = "",
) : Parcelable
