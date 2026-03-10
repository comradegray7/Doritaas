package com.example.myapp.data.repository

import android.util.Log
import com.example.myapp.data.FirestoreCollections
import com.example.myapp.data.dataclass.BenefitUsage
import com.example.myapp.data.dataclass.MembershipStatus
import com.example.myapp.data.dataclass.MembershipType
import com.example.myapp.data.dataclass.PrimeBenefits
import com.example.myapp.data.dataclass.PrimeMembership
import com.example.myapp.data.dataclass.PrimeTransaction
import com.example.myapp.data.dataclass.TransactionStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.UUID

/**
 * PrimeMembershipRepository
 *
 * Interface defining the contract for Prime membership management, 
 * including creation, renewal, cancellation, and transaction history tracking.
 */
interface PrimeMembershipRepository {
    suspend fun createMembership(userId: String, membershipType: MembershipType, paymentMethod: String): Result<PrimeMembership>
    suspend fun getMembership(userId: String): Result<PrimeMembership?>
    suspend fun updateMembership(membership: PrimeMembership): Result<Unit>
    suspend fun cancelMembership(userId: String): Result<Unit>
    suspend fun recordTransaction(transaction: PrimeTransaction): Result<Unit>
    suspend fun getTransactionHistory(userId: String): Result<List<PrimeTransaction>>
    suspend fun trackBenefitUsage(usage: BenefitUsage): Result<Unit>
    suspend fun getBenefitUsageStats(userId: String): Result<List<BenefitUsage>>
    suspend fun clearPrimeMembership(): Result<Unit>
    suspend fun  extendMembership(userId: String, months: Int): Result<PrimeMembership>
    suspend fun getAllPrimeMembers(): Result<List<PrimeMembership>>
    suspend fun searchPrime(query: String): Result<List<PrimeMembership>>

}

/**
 * PrimeMembershipRepositoryImpl
 *
 * Implementation of [PrimeMembershipRepository] using Firebase Firestore.
 */
class PrimeMembershipRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : PrimeMembershipRepository {

    companion object {
        private const val TAG = "PrimeMembershipRepo"
    }

    private val primeMembership  = firestore.collection(FirestoreCollections.PRIME_MEMBERSHIPS)
    private val primeBenefitUsage  = firestore.collection(FirestoreCollections.BENEFIT_USAGE)
    private val primeTransaction  = firestore.collection(FirestoreCollections.TRANSACTIONS)

    override suspend fun createMembership(
        userId: String,
        membershipType: MembershipType,
        paymentMethod: String
    ): Result<PrimeMembership> = withContext(Dispatchers.IO) {
        try {
            val existingMembership = getMembership(userId).getOrNull()

            if (existingMembership != null && existingMembership.status == MembershipStatus.ACTIVE) {
                return@withContext Result.failure(
                    Exception("User already has an active Prime membership.")
                )
            }

            val startDate = System.currentTimeMillis()
            val endDate = startDate + membershipType.getDurationInMillis()

            val membership = PrimeMembership(
                userId = userId,
                membershipType = membershipType,
                startDate = startDate,
                endDate = endDate,
                autoRenew = true,
                paymentMethod = paymentMethod,
                benefits = PrimeBenefits(),
                status = MembershipStatus.ACTIVE,
                createdAt = startDate,
                updatedAt = startDate
            )

            primeMembership
                .document(userId)
                .set(membership)
                .await()

            Log.d(TAG, "Prime membership created for user: $userId")
            Result.success(membership)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating membership: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getMembership(userId: String): Result<PrimeMembership?> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot =  primeMembership
                    .document(userId)
                    .get()
                    .await()

                val membership = snapshot.toObject(PrimeMembership::class.java)

                // Auto-update status if expired
                if (membership != null && membership.endDate < System.currentTimeMillis() &&
                    membership.status == MembershipStatus.ACTIVE) {
                    val updatedMembership = membership.copy(status = MembershipStatus.EXPIRED)
                    updateMembership(updatedMembership)
                    Result.success(updatedMembership)
                } else {
                    Result.success(membership)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error getting membership: ${e.message}", e)
                Result.failure(e)
            }
        }

    override suspend fun updateMembership(membership: PrimeMembership): Result<Unit> =
        withContext(Dispatchers.IO) {
            try { val updatedMembership = membership.copy(updatedAt = System.currentTimeMillis())
                primeMembership
                    .document(membership.userId)
                    .set(updatedMembership)
                    .await()

                Log.d(TAG, "✅ Membership updated for user: ${membership.userId}")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error updating membership: ${e.message}", e)
                Result.failure(e)
            }
        }

    override suspend fun cancelMembership(userId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val membershipResult = getMembership(userId)

                membershipResult.fold(
                    onSuccess = { membership ->
                        if (membership != null) {
                            val cancelledMembership = membership.copy(
                                status = MembershipStatus.CANCELLED,
                                autoRenew = false,
                                updatedAt = System.currentTimeMillis()
                            )
                            // Return the result of updateMembership
                            return@withContext updateMembership(cancelledMembership)
                        } else {
                            return@withContext Result.failure(Exception("No membership found"))
                        }
                    },
                    onFailure = { exception ->
                        return@withContext Result.failure(exception)
                    }
                )

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error cancelling membership: ${e.message}", e)
                Result.failure(e)
            }
        }

    override suspend fun recordTransaction(transaction: PrimeTransaction): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
               primeTransaction
                    .document(transaction.id)
                    .set(transaction)
                    .await()

                Log.d(TAG, "✅ Transaction recorded: ${transaction.id}")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error recording transaction: ${e.message}", e)
                Result.failure(e)
            }
        }

    override suspend fun getTransactionHistory(userId: String): Result<List<PrimeTransaction>> =
        withContext(Dispatchers.IO) {
            try {

                val query = primeTransaction
                    .whereEqualTo("userId", userId)
                    .orderBy("transactionDate", Query.Direction.DESCENDING)

                val snapshot = query.get().await()

                val transactions = snapshot.documents.mapNotNull {
                    it.toObject(PrimeTransaction::class.java)
                }

                Result.success(transactions)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error getting transaction history: ${e.message}", e)
                Result.failure(e)
            }
        }

    override suspend fun trackBenefitUsage(usage: BenefitUsage): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val usageId = primeBenefitUsage
                    .document()
                    .id

                firestore.collection(FirestoreCollections.BENEFIT_USAGE)
                    .document(usageId)
                    .set(usage)
                    .await()

                Log.d(TAG, "✅ Benefit usage tracked: ${usage.benefitType}")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error tracking benefit usage: ${e.message}", e)
                Result.failure(e)
            }
        }

    override suspend fun clearPrimeMembership(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                primeMembership.document(userId).delete().await()

                Log.d(TAG, "✅ Prime membership cleared for user: $userId")
                Result.success(Unit)
            } else {
                Result.failure(Exception("No authenticated user found to clear membership"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error clearing membership: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getBenefitUsageStats(userId: String): Result<List<BenefitUsage>> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = primeBenefitUsage
                    .whereEqualTo("userId", userId)
                    .orderBy("usedAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val usageList = snapshot.documents.mapNotNull {
                    it.toObject(BenefitUsage::class.java)
                }

                Log.d(TAG, "✅ Retrieved ${usageList.size} benefit usages for user: $userId")
                Result.success(usageList)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error getting benefit usage stats: ${e.message}", e)
                Result.failure(e)
            }
        }

    override suspend fun extendMembership(userId: String, months: Int): Result<PrimeMembership> {
        return try {
            val membershipDoc = firestore.collection("prime_memberships")
                .document(userId)
                .get()
                .await()

            val currentMembership = membershipDoc.toObject(PrimeMembership::class.java)
                ?: return Result.failure(Exception("Membership not found"))

            //  Calculate new end date from current end date (not from today)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = currentMembership.endDate
            calendar.add(Calendar.MONTH, months)
            val newEndDate = calendar.timeInMillis

            //   Update membership with new end date and set status to ACTIVE
            val updatedMembership = currentMembership.copy(
                endDate = newEndDate,
                status = MembershipStatus.ACTIVE, // Reactivate if it was expired
                updatedAt = System.currentTimeMillis()
            )

            firestore.collection("prime_memberships")
                .document(userId)
                .set(updatedMembership)
                .await()

            //  Record the extension transaction
            val transaction = PrimeTransaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                membershipType = currentMembership.membershipType,
                amount = 0.0, // Or calculate based on extension cost
                paymentMethod = "Extension",
                transactionDate = System.currentTimeMillis(),
                status = TransactionStatus.COMPLETED,
                description = "Membership extended by $months month(s)"
            )

            recordTransaction(transaction)

            Result.success(updatedMembership)
        } catch (e: Exception) {
            Log.e("PrimeMembershipRepo", "Error extending membership", e)
            Result.failure(e)
        }
    }

    override suspend fun getAllPrimeMembers(): Result<List<PrimeMembership>> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = primeMembership
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val members = snapshot.documents.mapNotNull {
                    it.toObject(PrimeMembership::class.java)
                }

                Log.d(TAG, "✅ Retrieved ${members.size} Prime members")
                Result.success(members)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error getting all Prime members: ${e.message}", e)
                Result.failure(e)
            }
        }

    override suspend fun searchPrime(query: String): Result<List<PrimeMembership>> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = primeMembership.get().await()

                val primeUsers = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(PrimeMembership::class.java)
                }.filter { membership ->
                    membership.userId.contains(query, ignoreCase = true) ||
                            membership.paymentMethod.contains(query, ignoreCase = true)
                }

                Log.d(TAG, "✅ Search found ${primeUsers.size} Prime members")
                Result.success(primeUsers)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error searching Prime members: ${e.message}", e)
                Result.failure(Exception("Failed to search primes: ${e.message}"))
            }
        }
}
