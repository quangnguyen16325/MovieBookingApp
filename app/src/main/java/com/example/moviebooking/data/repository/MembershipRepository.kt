package com.example.moviebooking.data.repository

import com.example.moviebooking.data.model.MembershipLevel
import com.example.moviebooking.data.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MembershipRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun addPoints(amount: Double): Result<UserModel> = withContext(Dispatchers.IO) {
        try {
            val currentUser = auth.currentUser ?: return@withContext Result.failure(Exception("User not authenticated"))
            
            // Tính điểm dựa trên số tiền (10000VND = 1 điểm)
            val pointsToAdd = (amount / 10000).toInt()
            
            // Lấy thông tin người dùng hiện tại
            val userDoc = usersCollection.document(currentUser.uid).get().await()
            val currentUserModel = userDoc.toObject(UserModel::class.java)
                ?: return@withContext Result.failure(Exception("User data not found"))

            // Tính điểm mới và cập nhật hạng thành viên
            val newPoints = if (currentUserModel.membershipLevel == MembershipLevel.PREMIUM) {
                currentUserModel.membershipPoints
            } else {
                currentUserModel.membershipPoints + pointsToAdd
            }
            val newLevel = if (currentUserModel.membershipLevel == MembershipLevel.PREMIUM) {
                MembershipLevel.PREMIUM
            } else {
                MembershipLevel.fromPoints(newPoints)
            }

            // Cập nhật thông tin người dùng
            val updatedUser = currentUserModel.copy(
                membershipPoints = newPoints,
                membershipLevel = newLevel
            )

            // Lưu vào Firestore
            usersCollection.document(currentUser.uid)
                .update(
                    mapOf(
                        "membershipPoints" to newPoints,
                        "membershipLevel" to newLevel.toString()
                    )
                )
                .await()

            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserMembershipInfo(): Result<UserModel> = withContext(Dispatchers.IO) {
        try {
            val currentUser = auth.currentUser ?: return@withContext Result.failure(Exception("User not authenticated"))
            
            val userDoc = usersCollection.document(currentUser.uid).get().await()
            val userModel = userDoc.toObject(UserModel::class.java)
                ?: return@withContext Result.failure(Exception("User data not found"))

            Result.success(userModel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 