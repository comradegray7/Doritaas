package com.example.myapp.data.dataclass

import kotlinx.serialization.Serializable

// Add to your data models file
/**
 * SearchResult - Container for search query results
 * 
 * @property exactMatches List of products matching the query exactly
 * @property similarProducts List of products that are similar or related
 */
data class SearchResult(
    val exactMatches: List<ProductItem>,
    val similarProducts: List<ProductItem>,
    val searchType: SearchType
)

/**
 * SearchData - Information about a search query
 * 
 * @property query The search text or term
 * @property count Number of times this query has been searched
 * @property lastSearched Timestamp of the last search
 */
@Serializable
data class SearchData(
    val query: String,
    val count: Int,
    val lastSearched: Long
)

/**
 * SearchType - Classification of search results
 * 
 * Defines how the search engine interpreted the query and what kind of results resulted.
 * 
 * @property EXACT_MATCH A direct match was found
 * @property SIMILAR_MATCH No exact match, but similar items found
 * @property CATEGORY_MATCH Matches based on category
 * @property DEFAULT_PRODUCTS Fallback to default recommendations
 * @property NO_RESULTS No items found
 */
enum class SearchType {
    EXACT_MATCH,      // Perfect match found
    SIMILAR_MATCH,    // Similar products found
    CATEGORY_MATCH,   // Related category products
    DEFAULT_PRODUCTS, // Fallback recommendations
    NO_RESULTS       // Invalid query
}