package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.Project
import com.example.ui.components.LogoGraphic
import com.example.ui.theme.BgDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanGradientEnd
import com.example.ui.theme.CyanGradientStart
import com.example.ui.theme.GradientEnd
import com.example.ui.theme.GradientStart
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun HomeScreen(
    viewModel: LogoViewModel,
    onOpenCreate: (engine: String) -> Unit,
    onOpenProject: (Project) -> Unit,
    onOpenProfile: () -> Unit,
    onViewAllProjects: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val userProjects by viewModel.userProjects.collectAsState()
    val communityProjects by viewModel.communityProjects.collectAsState()
    val selectedIndustry by viewModel.selectedIndustry.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showFontMatcher by remember { mutableStateOf(false) }

    val industries = listOf(
        "• All Industries",
        "Tech & AI",
        "Gaming & Esports",
        "Coffee & Food",
        "Fashion",
        "Crypto & Web3"
    )

    // Filter community projects
    val filteredCommunity = communityProjects.filter { p ->
        val matchesInd = selectedIndustry.contains("All") || p.industry.equals(selectedIndustry, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() || p.title.contains(searchQuery, ignoreCase = true) || p.prompt.contains(searchQuery, ignoreCase = true)
        matchesInd && matchesSearch
    }

    // Default showcase projects if list is loading
    val displayProjects = if (userProjects.isNotEmpty()) {
        userProjects
    } else {
        listOf(
            Project(
                id = "mock_apex",
                title = "Apex Dynamics",
                prompt = "Holographic glowing neon geometric letter L",
                engine = "text_to_logo",
                industry = "Tech & AI",
                style = "3D Emblem",
                badge = "VECTOR READY",
                variantsCount = 4L,
                primaryColor = "#00E5FF",
                accentColor = "#A855F7"
            ),
            Project(
                id = "mock_kitsune",
                title = "Kitsune Roast",
                prompt = "Artisan Japanese fox coffee roastery emblem",
                engine = "brand_kit",
                industry = "Coffee & Food",
                style = "Minimal",
                badge = "BRAND KIT",
                variantsCount = 4L,
                primaryColor = "#F59E0B",
                accentColor = "#EF4444"
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // 1. Top Header Row (Logo, Title, Credits Badge, User Avatar)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(NeonCyan, NeonPurple)
                                )
                            )
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "LogoGen AI",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Home",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Credits Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                            .clickable { onOpenProfile() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${userProfile?.credits ?: 24} Credits",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // User Avatar
                    val currentUser = Firebase.auth.currentUser
                    val photoUrl = currentUser?.photoUrl?.toString() ?: userProfile?.avatarUrl

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated)
                            .border(1.5.dp, NeonCyan, CircleShape)
                            .clickable { onOpenProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = (currentUser?.displayName ?: "U").take(1).uppercase(),
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 2. Hero Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(BorderDark, NeonPurple.copy(alpha = 0.4f))),
                        RoundedCornerShape(20.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    SurfaceDark,
                                    Color(0xFF1E1638).copy(alpha = 0.8f),
                                    Color(0xFF131A2E).copy(alpha = 0.9f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        // Badge Chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF221F3D))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${userProfile?.credits ?: 24} Credits • ${userProfile?.plan ?: "Pro Plan"}",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Create Studio-Grade Logos\nwith Generative AI",
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 28.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Vector precision, dynamic lighting, and instant brand kit compilation powered by neural synthesis.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action Buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { onOpenCreate("text_to_logo") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                contentPadding = PaddingValues(),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("start_new_project_button")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(GradientStart, GradientEnd)
                                            ),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .padding(horizontal = 18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Start New Project",
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            IconButton(
                                onClick = { onViewAllProjects() },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = "Projects View",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 3. CREATIVE ENGINES Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CREATIVE ENGINES",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Fast Lane",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EngineCard(
                    title = "Text to Logo",
                    icon = Icons.Default.Psychology,
                    iconBg = Brush.linearGradient(listOf(NeonPurple, NeonViolet)),
                    modifier = Modifier.weight(1f)
                ) {
                    onOpenCreate("text_to_logo")
                }
                EngineCard(
                    title = "Sketch / Ref",
                    icon = Icons.Default.Draw,
                    iconBg = Brush.linearGradient(listOf(CyanGradientStart, CyanGradientEnd)),
                    modifier = Modifier.weight(1f)
                ) {
                    onOpenCreate("sketch_ref")
                }
                EngineCard(
                    title = "Vector Trace",
                    icon = Icons.Default.Share,
                    iconBg = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                    modifier = Modifier.weight(1f)
                ) {
                    onOpenCreate("vector_trace")
                }
                EngineCard(
                    title = "Brand Kit",
                    icon = Icons.Default.Palette,
                    iconBg = Brush.linearGradient(listOf(Color(0xFF64748B), Color(0xFF475569))),
                    modifier = Modifier.weight(1f)
                ) {
                    onOpenCreate("brand_kit")
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
        }

        // 4. Search Bar & Filter
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Search styles, tokens, industries...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderDark,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("search_bar")
                )

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(
                    onClick = { /* Toggle filter */ },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filter",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 5. Industry Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                industries.forEach { ind ->
                    val isSelected = (ind == selectedIndustry) || (ind.contains("All") && selectedIndustry.contains("All"))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) NeonPurple.copy(alpha = 0.3f) else SurfaceCard)
                            .border(
                                1.dp,
                                if (isSelected) NeonViolet else BorderDark,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { viewModel.setIndustryFilter(ind) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(NeonCyan)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = ind.removePrefix("• "),
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 6. Recent Projects Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Recent Projects",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${displayProjects.size} active",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    modifier = Modifier.clickable { onViewAllProjects() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View All",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View all",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recent Projects Horizontal Scroll
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(end = 8.dp)
            ) {
                items(displayProjects, key = { it.id }) { project ->
                    RecentProjectCard(
                        project = project,
                        onClick = { onOpenProject(project) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // 7. Community Feed Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Community Feed",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Trending designs remixed today",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Curated",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 8. Community Grid Cards (2 columns)
        val feed = if (filteredCommunity.isNotEmpty()) filteredCommunity else listOf(
            Project(
                id = "feed_vortex",
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
                id = "feed_aero",
                title = "Aero Crest",
                prompt = "Neon minimal eagle crest logo with sharp vector wings",
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
                id = "feed_tiger",
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
                id = "feed_nova",
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

        // Pair items for 2-column layout
        val chunkedFeed = feed.chunked(2)
        items(chunkedFeed) { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CommunityLogoCard(
                    project = pair[0],
                    modifier = Modifier.weight(1f),
                    onOpen = { onOpenProject(pair[0]) },
                    onLike = { viewModel.likeProject(pair[0].id, pair[0].likesCount) }
                )

                if (pair.size > 1) {
                    CommunityLogoCard(
                        project = pair[1],
                        modifier = Modifier.weight(1f),
                        onOpen = { onOpenProject(pair[1]) },
                        onLike = { viewModel.likeProject(pair[1].id, pair[1].likesCount) }
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // 9. Bottom Callout Banner ("Need custom font pairing?")
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, BorderDark, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyanGradientStart.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TextFields,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Need custom font pairing?",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "AI typography matcher is active",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = { showFontMatcher = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceElevated,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Launch", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showFontMatcher) {
        FontPairingDialog(onDismiss = { showFontMatcher = false })
    }
}

@Composable
fun EngineCard(
    title: String,
    icon: ImageVector,
    iconBg: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RecentProjectCard(
    project: Project,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(230.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header with badge and menu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                LogoGraphic(
                    title = project.title,
                    style = project.style,
                    primaryColorHex = project.primaryColor,
                    accentColorHex = project.accentColor,
                    modifier = Modifier.fillMaxSize()
                )

                // Badge top-left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = project.badge,
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Three dots top-right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "AI Gen v3",
                    color = NeonViolet,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Edited 12m ago • ${project.variantsCount} Variants",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun CommunityLogoCard(
    project: Project,
    modifier: Modifier = Modifier,
    onOpen: () -> Unit,
    onLike: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
            .clickable { onOpen() },
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                LogoGraphic(
                    title = project.title,
                    style = project.style,
                    primaryColorHex = project.primaryColor,
                    accentColorHex = project.accentColor,
                    modifier = Modifier.fillMaxSize()
                )

                // Like icon top right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { onLike() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Badge bottom left
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = project.style,
                        color = NeonCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.title,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "by @creator",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${project.likesCount}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
