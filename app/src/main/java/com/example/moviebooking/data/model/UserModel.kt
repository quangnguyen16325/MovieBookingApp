package com.example.moviebooking.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class UserModel(
    @DocumentId val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val profileImage: String? = null,
    val phoneNumber: String? = null,
    val createdAt: Timestamp? = null,
    val lastLogin: Timestamp? = null,
    val membershipPoints: Int = 0,
    val membershipLevel: MembershipLevel = MembershipLevel.BASIC
) {
    fun toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        map["email"] = email
        map["fullName"] = fullName
        map["profileImage"] = profileImage
        map["phoneNumber"] = phoneNumber
        map["createdAt"] = createdAt ?: Timestamp.now()
        map["lastLogin"] = Timestamp.now()
        map["membershipPoints"] = membershipPoints
        map["membershipLevel"] = membershipLevel.toString()

        return map
    }
}

enum class MembershipLevel {
    BASIC,
    SILVER,
    GOLD,
    DIAMOND,
    PREMIUM;

    companion object {
        fun fromPoints(points: Int): MembershipLevel {
            return when {
                points >= 1200 -> DIAMOND
                points >= 600 -> GOLD
                points >= 200 -> SILVER
                else -> BASIC
            }
        }
    }
}