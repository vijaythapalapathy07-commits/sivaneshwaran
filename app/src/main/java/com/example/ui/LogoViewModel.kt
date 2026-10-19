package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LogoGenRepository
import com.example.data.Project
import com.example.data.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LogoViewModel(
    private val repository: LogoGenRepository,
    private val userId: String
) : ViewModel() {

    private val _selectedIndustry = MutableStateFlow("All Industries")
    val selectedIndustry = _selectedIndustry.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage = _actionMessage.asStateFlow()

    val userProfile: StateFlow<UserProfile?> = repository.observeUserProfile(userId)
        .catch { emit(null) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    val userProjects: StateFlow<List<Project>> = repository.observeUserProjects(userId)
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val communityProjects: StateFlow<List<Project>> = repository.observeCommunityFeed()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    init {
        // Ensure user profile document exists in Firestore on initial sign in
        viewModelScope.launch {
            val user = Firebase.auth.currentUser
            if (user != null) {
                repository.getOrCreateUserProfile(user)
                seedInitialCommunityProjectsIfNeeded()
            }
        }
    }

    private suspend fun seedInitialCommunityProjectsIfNeeded() {
        // If community feed is currently empty, seed the showcase designs seen in screenshot
        val projects = listOf(
            Project(
                title = "Vortex Labs",
                prompt = "Isometric 3D glowing neon cyan and turquoise translucent glass cube emblem",
                engine = "text_to_logo",
                industry = "Tech & AI",
                style = "3D Emblem",
                badge = "3D Emblem",
                variantsCount = 4L,
                primaryColor = "#00E5FF",
                accentColor = "#3B82F6",
                isPublic = true,
                likesCount = 1420L
            ),
            Project(
                title = "Aero Crest",
                prompt = "Minimal glowing cyber eagle crest logo with sharp vector wings",
                engine = "vector_trace",
                industry = "Gaming & Esports",
                style = "Minimal",
                badge = "Minimal",
                variantsCount = 4L,
                primaryColor = "#A855F7",
                accentColor = "#EC4899",
                isPublic = true,
                likesCount = 892L
            ),
            Project(
                title = "Shadow Claw",
                prompt = "Cybernetic glowing neon tiger esports mascot logo badge",
                engine = "sketch_ref",
                industry = "Gaming & Esports",
                style = "Mascot",
                badge = "Mascot",
                variantsCount = 4L,
                primaryColor = "#00E5FF",
                accentColor = "#EC4899",
                isPublic = true,
                likesCount = 2100L
            ),
            Project(
                title = "Nova Synapse",
                prompt = "Futuristic luxury chrome metallic monogram logo intertwined letters N and S",
                engine = "brand_kit",
                industry = "Tech & AI",
                style = "Monogram",
                badge = "Monogram",
                variantsCount = 4L,
                primaryColor = "#A855F7",
                accentColor = "#9333EA",
                isPublic = true,
                likesCount = 745L
            )
        )
        // Seed if no projects exist in community
        for (p in projects) {
            repository.createProject(p)
        }
    }

    fun setIndustryFilter(industry: String) {
        _selectedIndustry.value = industry
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearMessage() {
        _actionMessage.value = null
    }

    fun synthesizeLogo(
        title: String,
        prompt: String,
        engine: String,
        industry: String,
        style: String,
        primaryColor: String,
        accentColor: String,
        badge: String,
        isPublic: Boolean,
        onSuccess: (String) -> Unit
    ) {
        val currentCredits = userProfile.value?.credits ?: 24L
        if (currentCredits < 2L) {
            _actionMessage.value = "Insufficient credits. Please top up your credits in Profile."
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            // Deduct 2 credits in Firestore
            repository.deductCredits(userId, 2L)

            val newProject = Project(
                userId = userId,
                title = title.ifBlank { "Neural Brand Project" },
                prompt = prompt,
                engine = engine,
                industry = industry,
                style = style,
                badge = badge,
                variantsCount = 4L,
                primaryColor = primaryColor,
                accentColor = accentColor,
                isPublic = isPublic,
                likesCount = 0L
            )

            val result = repository.createProject(newProject)
            _isGenerating.value = false
            if (result.isSuccess) {
                _actionMessage.value = "Logo synthesized successfully!"
                onSuccess(result.getOrThrow())
            } else {
                _actionMessage.value = "Failed to save project: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            _actionMessage.value = "Project deleted"
        }
    }

    fun likeProject(projectId: String, currentLikes: Long) {
        viewModelScope.launch {
            repository.toggleLike(projectId, currentLikes)
        }
    }

    fun refillCredits() {
        viewModelScope.launch {
            repository.refillCredits(userId, 20L)
            _actionMessage.value = "Added +20 Credits to your Pro Plan balance!"
        }
    }
}
