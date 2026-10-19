package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val credits: Long = 24L,
    val plan: String = "Pro Plan",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "email" to email,
            "displayName" to displayName,
            "avatarUrl" to avatarUrl,
            "credits" to credits,
            "plan" to plan,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return map.filterValues { it != null }
    }
}

@IgnoreExtraProperties
data class Project(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val prompt: String = "",
    val engine: String = "text_to_logo",
    val industry: String = "Tech & AI",
    val style: String = "3D Emblem",
    val badge: String = "VECTOR READY",
    val variantsCount: Long = 4L,
    val primaryColor: String = "#00E5FF",
    val accentColor: String = "#A855F7",
    val isPublic: Boolean = false,
    val likesCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "title" to title,
            "prompt" to prompt,
            "engine" to engine,
            "industry" to industry,
            "style" to style,
            "badge" to badge,
            "variantsCount" to variantsCount,
            "primaryColor" to primaryColor,
            "accentColor" to accentColor,
            "isPublic" to isPublic,
            "likesCount" to likesCount,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return map.filterValues { it != null }
    }
}
