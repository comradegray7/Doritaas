package com.example.myapp.data.model

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.dataclass.BenefitUsage
import com.example.myapp.data.dataclass.MembershipStatus
import com.example.myapp.data.dataclass.MembershipType
import com.example.myapp.data.dataclass.PrimeMembership
import com.example.myapp.data.dataclass.PrimeTransaction
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.dataclass.TransactionStatus
import com.example.myapp.data.repository.PrimeMembershipRepository
import com.example.myapp.view.utils.formatDate
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

@HiltViewModel
/**
 * PrimeMembershipViewModel
 *
 */
class PrimeMembershipViewModel @Inject constructor(
    private val primeMembershipRepository: PrimeMembershipRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    companion object {
        private const val TAG = "PrimeMembershipVM"
    }
    private val _membershipState = MutableStateFlow(PrimeMembershipState())
    val membershipState: StateFlow<PrimeMembershipState> = _membershipState.asStateFlow()
    private val _snackBarData = MutableSharedFlow<SnackBarData>()
    val snackBarData: SharedFlow<SnackBarData> = _snackBarData.asSharedFlow()

    init {
        loadUserMembership()
    }

    /**
     * loadPrimeStatus
     *
     */
    fun loadPrimeStatus() {
        viewModelScope.launch {
            val currentUser = auth.currentUser

            if (currentUser == null) {
                Log.d("PrimeViewModel", "No user - clearing Prime status")
                clearPrimeStatus()
                return@launch
            }

            _membershipState.update { it.copy(isLoading = true) }

            primeMembershipRepository.getMembership(currentUser.uid)
                .onSuccess { membership ->
                    Log.d("PrimeViewModel", "Loaded Prime status: ${membership?.status}")
                    _membershipState.update {
                        it.copy(
                            membership = membership,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    Log.e("PrimeViewModel", "Failed to load Prime status", error)
                    _membershipState.update {
                        it.copy(
                            membership = null,
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    /**
     * loadAllPrimeMembers
     *
     */
    fun loadAllPrimeMembers() {
        viewModelScope.launch {
            _membershipState.update { it.copy(isLoading = true) }

            primeMembershipRepository.getAllPrimeMembers().fold(
                onSuccess = { members ->
                    _membershipState.update {
                        it.copy(
                            members = members,
                            filteredMembers = members,  
                            isLoading = false,
                            error = null
                        )
                    }
                },
                onFailure = { e ->
                    _membershipState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                }
            )
        }
    }

    fun showSnackBar(
        message: String,
        actionLabel: String = "OK",
        duration: SnackbarDuration = SnackbarDuration.Short,
        isError: Boolean = false
    ) {
        viewModelScope.launch {
            _snackBarData.emit(
                SnackBarData(
                    message = message,
                    actionLabel = actionLabel,
                    duration = duration,
                    isError = isError
                )
            )
        }
    }

    /**
     * loadUserMembership
     *
     */
    fun loadUserMembership() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _membershipState.update { it.copy(isLoading = true) }

            primeMembershipRepository.getMembership(userId).fold(
                onSuccess = { membership ->
                    _membershipState.update {
                        it.copy(
                            isLoading = false,
                            membership = membership,
                            isPrimeMember = membership?.status == MembershipStatus.ACTIVE,
                            error = null
                        )
                    }
                    Log.d(TAG, "✅ Membership loaded: ${membership?.membershipType}")
                },
                onFailure = { exception ->
                    _membershipState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message,
                            isPrimeMember = false
                        )
                    }
                    Log.e(TAG, "❌ Error loading membership: ${exception.message}")
                }
            )
        }
    }

    /**
     * createMembership
     *
     *
     * @param membershipType The membershipType parameter
     * @param paymentMethod The paymentMethod parameter
     */
    fun createMembership(membershipType: MembershipType, paymentMethod: String) {
        val userId = auth.currentUser?.uid ?: run {
            showSnackBar(
                message = "Please sign in to register for prime",
                isError = true
            )
            return
        }

        viewModelScope.launch {
            _membershipState.update { it.copy(isLoading = true) }

            primeMembershipRepository.createMembership(userId, membershipType, paymentMethod).fold(
                onSuccess = { membership ->
                    val transaction = PrimeTransaction(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        membershipType = membershipType,
                        amount = if (membershipType == MembershipType.MONTHLY)
                            membershipType.monthlyPrice else membershipType.annualPrice,
                        paymentMethod = paymentMethod,
                        transactionDate = System.currentTimeMillis(),
                        status = TransactionStatus.COMPLETED
                    )

                    primeMembershipRepository.recordTransaction(transaction)

                    _membershipState.update {
                        it.copy(
                            isLoading = false,
                            membership = membership,
                            isPrimeMember = true,
                            error = null
                        )
                    }

                    showSnackBar(
                        message = "Welcome to Prime! Your membership is now active.",
                        actionLabel = "OK",
                        duration = SnackbarDuration.Long,
                        isError = false
                    )

                    Log.d(TAG, "✅ Prime subscription successful")
                },
                onFailure = { exception ->
                    _membershipState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }

                    showSnackBar(
                        message = exception.message ?: "Failed to subscribe. Please try again.",
                        actionLabel = "Retry",
                        duration = SnackbarDuration.Long,
                        isError = true
                    )

                    Log.e(TAG, "❌ Prime subscription failed: ${exception.message}")
                }
            )
        }
    }

    /**
     * cancelMembership
     *
     */
    fun cancelMembership() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _membershipState.update { it.copy(isLoading = true) }

            primeMembershipRepository.cancelMembership(userId).fold(
                onSuccess = {
                    _membershipState.update {
                        it.copy(
                            isLoading = false,
                            isPrimeMember = false,
                            membership = it.membership?.copy(
                                status = MembershipStatus.CANCELLED,
                                autoRenew = false
                            )
                        )
                    }

                    showSnackBar(
                        message = "Membership cancelled. You'll have access until the end of your billing period.",
                        duration = SnackbarDuration.Long,
                        isError = false
                    )

                    Log.d(TAG, "✅ Membership cancelled")

                    //  Reload all members to update the list
                    loadAllPrimeMembers()
                },
                onFailure = { exception ->
                    _membershipState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }

                    showSnackBar(
                        message = exception.message ?: "Failed to cancel membership",
                        duration = SnackbarDuration.Short,
                        isError = true
                    )
                }
            )
        }
    }

    /**
     * loadTransactionHistory
     *
     */
    fun loadTransactionHistory() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            primeMembershipRepository.getTransactionHistory(userId).fold(
                onSuccess = { transactions ->
                    _membershipState.update {
                        it.copy(transactionHistory = transactions)
                    }
                    Log.d(TAG, "✅ Transaction history loaded: ${transactions.size} items")
                },
                onFailure = { exception ->
                    Log.e(TAG, "❌ Error loading transactions: ${exception.message}")
                    showSnackBar(
                        message = "Failed to load transaction history",
                        isError = true
                    )
                }
            )
        }
    }

    /**
     * loadBenefitUsageStats
     *
     */
    fun loadBenefitUsageStats() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            primeMembershipRepository.getBenefitUsageStats(userId).fold(
                onSuccess = { usageList ->
                    _membershipState.update {
                        it.copy(benefitUsage = usageList)
                    }
                    Log.d(TAG, "✅ Benefit usage stats loaded: ${usageList.size} items")
                },
                onFailure = { exception ->
                    Log.e(TAG, "❌ Error loading benefit stats: ${exception.message}")
                }
            )
        }
    }

    /**
     * getTotalSavings
     *
     */
    fun getTotalSavings(): Double {
        return _membershipState.value.benefitUsage.sumOf { it.discountAmount }
    }

    /**
     * searchPrime
     *
     *
     * @param query The query parameter
     */
    fun searchPrime(query: String) {
        if (query.isBlank()) {
            loadAllPrimeMembers()
            return
        }

        viewModelScope.launch {
            _membershipState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            primeMembershipRepository.searchPrime(query).fold(
                onSuccess = { members ->
                    _membershipState.update {
                        it.copy(
                            isLoading = false,
                            members = members,
                            filteredMembers = members, 
                            error = null
                        )
                    }
                },
                onFailure = { exception ->
                    _membershipState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                }
            )
        }
    }

    /**
     * clearPrimeStatus
     *
     */
    fun clearPrimeStatus() {
        viewModelScope.launch {
            Log.d("PrimeViewModel", "Clearing Prime status")
            _membershipState.update {
                it.copy(
                    membership = null,
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    /**
     * filterByStatus
     *
     *
     * @param status The status parameter
     */
    fun filterByStatus(status: MembershipStatus?) {
        val currentMembers = _membershipState.value.members  
        val filtered = if (status == null) {
            currentMembers // Show all members
        } else {
            currentMembers.filter { it.status == status } // Filter by status
        }

        _membershipState.update { it.copy(filteredMembers = filtered) }
    }

    /**
     * extendPrimeMembership
     *
     *
     * @param userId The userId parameter
     * @param months The months parameter
     */
    fun extendPrimeMembership(userId: String, months: Int) {
        viewModelScope.launch {
            _membershipState.update { it.copy(isLoading = true) }

            primeMembershipRepository.extendMembership(userId, months).fold(
                onSuccess = { updatedMembership ->
                    showSnackBar(
                        message = "Membership extended by $months month(s). New end date: ${formatDate(updatedMembership.endDate)}",
                        duration = SnackbarDuration.Long,
                        isError = false
                    )

                    //  Update local state with the new membership data
                    _membershipState.update { state ->
                        state.copy(
                            isLoading = false,
                            members = state.members.map { member ->
                                if (member.userId == userId) updatedMembership else member
                            },
                            filteredMembers = state.filteredMembers.map { member ->
                                if (member.userId == userId) updatedMembership else member
                            }
                        )
                    }
                },
                onFailure = { exception ->
                    _membershipState.update { it.copy(isLoading = false) }

                    showSnackBar(
                        message = exception.message ?: "Failed to extend membership.",
                        isError = true
                    )
                }
            )
        }
    }
}

/**
 * PrimeMembershipState
 *
 */
data class PrimeMembershipState(
    val isLoading: Boolean = true,
    val members: List<PrimeMembership> = emptyList(),
    val membership: PrimeMembership? = null,
    val isPrimeMember: Boolean = false,
    val filteredMembers: List<PrimeMembership> = emptyList(),
    val transactionHistory: List<PrimeTransaction> = emptyList(),
    val benefitUsage: List<BenefitUsage> = emptyList(),
    val error: String? = null
)