package com.example.myapp.data.dataclass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * SizeItem - Product size variant data model
 * 
 * Represents a size option for products (e.g., S, M, L, XL, 42, 44).
 * 
 * @property id Unique size identifier
 * @property size Display name of the size
 */
@Parcelize
data class SizeItem(
    val id: String = "",
    val size: String = ""
) : Parcelable