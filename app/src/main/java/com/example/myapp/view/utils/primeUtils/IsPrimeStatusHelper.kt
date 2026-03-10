package com.example.myapp.view.utils.primeUtils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.myapp.data.dataclass.MembershipStatus
import com.example.myapp.ui.theme.colors

// ============================================================================
// HOW TO CHECK IF USER IS PRIME MEMBER
// ============================================================================

//   Simple helper function to check if user is Prime
/**
 * isUserPrimeMember
 *
 *
 * @param status The status parameter
 */
fun isUserPrimeMember(status: MembershipStatus?): Boolean {
    return status == MembershipStatus.ACTIVE
}

@Composable
        /**
         * getMembershipStatusColor
         *
         *
         * @param status The status parameter
         */
fun getMembershipStatusColor(status: MembershipStatus): Color {
    return when (status) {
        MembershipStatus.ACTIVE -> colors.customColor9.copy(alpha = 0.1f)
        MembershipStatus.EXPIRED -> MaterialTheme.colorScheme.errorContainer
        MembershipStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }
}