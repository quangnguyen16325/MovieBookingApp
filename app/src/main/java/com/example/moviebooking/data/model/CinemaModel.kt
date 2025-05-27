package com.example.moviebooking.data.model

import com.google.firebase.firestore.DocumentId

data class CinemaModel(
    @DocumentId val id: String = "",
    val name: String = "",
    val address: String = "",
    val city: String = "",
    val imageUrl: String = "",
    val facilities: List<String> = listOf(), // amenities like parking, food, etc.
    val location: GeoLocation? = null,
    val numberOfScreens: Int = 0
) {
    fun toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        map["name"] = name
        map["address"] = address
        map["city"] = city
        map["imageUrl"] = imageUrl
        map["facilities"] = facilities
        map["numberOfScreens"] = numberOfScreens
        location?.let {
            map["location"] = mapOf(
                "latitude" to it.latitude,
                "longitude" to it.longitude
            )
        }
        return map
    }
}

data class GeoLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)