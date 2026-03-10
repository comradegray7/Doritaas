package com.example.myapp.data.dataclass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * BrandItem - Brand data model
 *
 * Represents a product brand or manufacturer.
 *
 * @property id Unique brand identifier
 * @property brandName Display name of the brand
 */
@Parcelize
data class BrandItem(
    val id: String = "",
    val brandName: String = ""
) : Parcelable