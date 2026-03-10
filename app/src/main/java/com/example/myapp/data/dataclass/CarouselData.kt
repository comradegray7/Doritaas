package com.example.myapp.data.dataclass

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

/**
 * Represents an item in the image carousel
 *
 * @property id Unique identifier for the carousel item
 * @property imageUrl URL of the image to display
 * @property title Optional title for the carousel item
 * @property description Optional description for the carousel item
 * @property redirectUrl Optional URL to redirect when clicked
 * @property createdAt When this item was created
 * @property updatedAt When this item was last updated
 */
@Parcelize
data class CarouselItem(
    val id: String = "",
    val imageUrl: String = "",
    val title: String = "",
    val description: String = "",
    val redirectUrl: String? = null, //optional
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) : Parcelable



