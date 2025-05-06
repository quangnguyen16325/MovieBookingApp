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
    val lastLogin: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        map["email"] = email
        map["fullName"] = fullName
        map["profileImage"] = profileImage
        map["phoneNumber"] = phoneNumber
        map["createdAt"] = createdAt ?: Timestamp.now()
        map["lastLogin"] = Timestamp.now()

        return map
    }
}