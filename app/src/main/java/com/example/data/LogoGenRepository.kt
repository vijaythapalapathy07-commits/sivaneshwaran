package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.tasks.await

class LogoGenRepository(val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun getOrCreateUserProfile(user: FirebaseUser): Result<UserProfile> {
        val uid = user.uid
        val docRef = db.collection("users").document(uid)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                    ?: UserProfile(id = uid, userId = uid, email = user.email ?: "")
                Result.success(profile)
            } else {
                val initialProfile = UserProfile(
                    userId = uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "Creator",
                    avatarUrl = user.photoUrl?.toString() ?: "",
                    credits = 24L,
                    plan = "Pro Plan"
                )
                docRef.set(initialProfile.toCreateMap()).await()
                Result.success(initialProfile)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        val docRef = db.collection("users").document(userId)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) snapshot.toObject(UserProfile::class.java) else null
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    suspend fun deductCredits(userId: String, amount: Long = 2L): Result<Long> {
        val docRef = db.collection("users").document(userId)
        return try {
            val snapshot = docRef.get().await()
            val current = snapshot.getLong("credits") ?: 24L
            val nextCredits = (current - amount).coerceAtLeast(0L)
            docRef.update(
                mapOf(
                    "credits" to nextCredits,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(nextCredits)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun refillCredits(userId: String, amount: Long = 20L): Result<Long> {
        val docRef = db.collection("users").document(userId)
        return try {
            val snapshot = docRef.get().await()
            val current = snapshot.getLong("credits") ?: 0L
            val nextCredits = current + amount
            docRef.update(
                mapOf(
                    "credits" to nextCredits,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(nextCredits)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeUserProjects(userId: String): Flow<List<Project>> = flow {
        val path = "projects"
        val query = db.collection(path).whereEqualTo("userId", userId)
        emitAll(
            query.snapshots()
                .map { snapshot -> snapshot.toObjects(Project::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeCommunityFeed(): Flow<List<Project>> = flow {
        val path = "projects"
        val query = db.collection(path).whereEqualTo("isPublic", true)
        emitAll(
            query.snapshots()
                .map { snapshot -> snapshot.toObjects(Project::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createProject(project: Project): Result<String> {
        val uid = requireUserId()
        val projectsCol = db.collection("projects")
        val docRef = projectsCol.document()
        val payload = project.copy(userId = uid).toCreateMap()

        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteProject(projectId: String): Result<Unit> {
        val docRef = db.collection("projects").document(projectId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleLike(projectId: String, currentLikes: Long): Result<Long> {
        val docRef = db.collection("projects").document(projectId)
        val newLikes = currentLikes + 1
        return try {
            docRef.update(
                mapOf(
                    "likesCount" to newLikes,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(newLikes)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }
}
