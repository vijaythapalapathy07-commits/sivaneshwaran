package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class LogoGenRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createProject_validPayload_createsDocumentAndReturnsId() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = LogoGenRepository(firestore)

        val project = Project(
            title = "Apex Dynamics",
            prompt = "Futuristic glowing neon geometric letter L",
            engine = "text_to_logo",
            industry = "Tech & AI",
            style = "3D Emblem",
            badge = "VECTOR READY",
            variantsCount = 4L,
            primaryColor = "#00E5FF",
            accentColor = "#A855F7",
            isPublic = false
        )

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.createProject(project) }
        assertTrue(createResult.isSuccess)
        val projectId = createResult.getOrThrow()
        assertTrue(projectId.isNotEmpty())
    }

    @Test
    fun getUserProjects_authenticatedOwner_returnsMatchingProjects() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = LogoGenRepository(firestore)

        val project = Project(
            title = "Kitsune Roast",
            prompt = "Japanese fox coffee brand emblem",
            engine = "brand_kit",
            industry = "Coffee & Food",
            style = "Minimal",
            badge = "BRAND KIT",
            variantsCount = 4L,
            primaryColor = "#F59E0B",
            accentColor = "#EF4444",
            isPublic = false
        )

        val projectId = withTimeout(DEFAULT_TIMEOUT_MS) { repository.createProject(project).getOrThrow() }

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserProjects(aliceUid).first { list -> list.any { it.id == projectId } }
        }
        assertTrue(emitted.any { it.id == projectId })
    }

    @Test
    fun getProject_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = LogoGenRepository(firestore)

        val privateProject = Project(
            title = "Secret Project",
            prompt = "Confidential design",
            engine = "text_to_logo",
            industry = "Tech & AI",
            style = "Minimal",
            badge = "AI Gen v3",
            variantsCount = 2L,
            primaryColor = "#000000",
            accentColor = "#FFFFFF",
            isPublic = false
        )

        val projId = withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.createProject(privateProject).getOrThrow() }

        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("projects").document(projId).get().await()
            }
            fail("Expected PERMISSION_DENIED when accessing private project")
        } catch (e: Throwable) {
            val firestoreException = (e as? FirebaseFirestoreException)
                ?: (e.cause as? FirebaseFirestoreException)
                ?: (e.cause?.cause as? FirebaseFirestoreException)
            assertTrue(firestoreException != null)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
        }
    }

    @Test
    fun observeUserProjects_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = LogoGenRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeUserProjects("random_uid").first()
            }
            fail("Expected PERMISSION_DENIED for unauthenticated query")
        } catch (e: Throwable) {
            val firestoreException = (e as? FirebaseFirestoreException)
                ?: (e.cause as? FirebaseFirestoreException)
                ?: (e.cause?.cause as? FirebaseFirestoreException)
            assertTrue(firestoreException != null)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@logogen.ai"
        const val BOB_EMAIL = "bob@logogen.ai"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 6000L
    }
}
