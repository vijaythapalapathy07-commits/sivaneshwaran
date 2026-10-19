package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.LogoGenRepository
import com.example.data.Project
import com.example.ui.AuthScreen
import com.example.ui.CreateLogoDialog
import com.example.ui.DesignsScreen
import com.example.ui.ExploreScreen
import com.example.ui.HomeScreen
import com.example.ui.LogoViewModel
import com.example.ui.ProfileScreen
import com.example.ui.ProjectDetailDialog
import com.example.ui.attemptAutoSignIn
import com.example.ui.signOut
import com.example.ui.theme.BgDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.GradientEnd
import com.example.ui.theme.GradientStart
import com.example.ui.theme.LogoGenTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LogoGenTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { currentUser = Firebase.auth.currentUser },
                onUnauthenticated = { /* show auth screen */ },
                scope = scope
            )
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = { currentUser = Firebase.auth.currentUser }
        )
    } else {
        MainFeatureScreen(
            userId = currentUser!!.uid,
            onSignOut = {
                signOut(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = { currentUser = null },
                    scope = scope
                )
            }
        )
    }
}

@Composable
fun MainFeatureScreen(
    userId: String,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { LogoGenRepository(context) }

    val viewModel: LogoViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LogoViewModel(repository, userId) as T
            }
        }
    )

    var currentTab by remember { mutableIntStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var createInitialEngine by remember { mutableStateOf("text_to_logo") }
    var createInitialPrompt by remember { mutableStateOf("") }
    var selectedProjectForDetail by remember { mutableStateOf<Project?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val actionMessage by viewModel.actionMessage.collectAsState()

    LaunchedEffect(actionMessage) {
        actionMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    BackHandler(enabled = currentTab != 0) {
        currentTab = 0
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BgDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            CustomBottomNav(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                onCreateClicked = {
                    createInitialEngine = "text_to_logo"
                    createInitialPrompt = ""
                    showCreateDialog = true
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onOpenCreate = { engine ->
                        createInitialEngine = engine
                        createInitialPrompt = ""
                        showCreateDialog = true
                    },
                    onOpenProject = { project ->
                        selectedProjectForDetail = project
                    },
                    onOpenProfile = {
                        currentTab = 3
                    },
                    onViewAllProjects = {
                        currentTab = 2
                    }
                )
                1 -> ExploreScreen(
                    viewModel = viewModel,
                    onOpenProject = { project ->
                        selectedProjectForDetail = project
                    }
                )
                2 -> DesignsScreen(
                    viewModel = viewModel,
                    onOpenCreate = {
                        createInitialEngine = "text_to_logo"
                        createInitialPrompt = ""
                        showCreateDialog = true
                    },
                    onOpenProject = { project ->
                        selectedProjectForDetail = project
                    }
                )
                3 -> ProfileScreen(
                    viewModel = viewModel,
                    onSignOut = onSignOut
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateLogoDialog(
            initialEngine = createInitialEngine,
            initialPrompt = createInitialPrompt,
            onDismiss = { showCreateDialog = false },
            onSynthesize = { title, prompt, engine, industry, style, primaryColor, accentColor, badge, isPublic ->
                viewModel.synthesizeLogo(
                    title = title,
                    prompt = prompt,
                    engine = engine,
                    industry = industry,
                    style = style,
                    primaryColor = primaryColor,
                    accentColor = accentColor,
                    badge = badge,
                    isPublic = isPublic,
                    onSuccess = {
                        showCreateDialog = false
                    }
                )
            }
        )
    }

    selectedProjectForDetail?.let { project ->
        ProjectDetailDialog(
            project = project,
            isOwner = project.userId == userId,
            onDismiss = { selectedProjectForDetail = null },
            onDelete = { id ->
                viewModel.deleteProject(id)
            },
            onRemix = { remixed ->
                createInitialEngine = remixed.engine
                createInitialPrompt = remixed.prompt
                showCreateDialog = true
            },
            onLike = { id, likes ->
                viewModel.likeProject(id, likes)
            }
        )
    }
}

@Composable
fun CustomBottomNav(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    onCreateClicked: () -> Unit
) {
    val navInsets = WindowInsets.navigationBars.asPaddingValues()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark.copy(alpha = 0.95f))
            .border(0.5.dp, BorderDark, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(bottom = navInsets.calculateBottomPadding(), top = 8.dp)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home (Tab 0)
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentTab == 0,
                modifier = Modifier.weight(1f)
            ) { onTabSelected(0) }

            // Explore (Tab 1)
            BottomNavItem(
                icon = Icons.Default.Explore,
                label = "Explore",
                isSelected = currentTab == 1,
                modifier = Modifier.weight(1f)
            ) { onTabSelected(1) }

            // Center + CREATE Button
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(listOf(GradientStart, GradientEnd))
                    )
                    .clickable { onCreateClicked() }
                    .testTag("nav_create_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Create",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CREATE",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Designs (Tab 2)
            BottomNavItem(
                icon = Icons.Default.FolderOpen,
                label = "Designs",
                isSelected = currentTab == 2,
                modifier = Modifier.weight(1f)
            ) { onTabSelected(2) }

            // Profile (Tab 3)
            BottomNavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = currentTab == 3,
                modifier = Modifier.weight(1f)
            ) { onTabSelected(3) }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) NeonCyan else TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) NeonCyan else TextSecondary,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
