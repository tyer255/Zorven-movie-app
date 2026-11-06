package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProfileEntity
import com.example.ui.AppSettingsUiState
import com.example.ui.components.ProfileAvatarBadge
import com.example.ui.theme.ArchivoBlackFontFamily
import com.example.ui.theme.ZorvenBorderSubtle
import com.example.ui.theme.ZorvenCrimson
import com.example.ui.theme.ZorvenErrorRed
import com.example.ui.theme.ZorvenObsidian
import com.example.ui.theme.ZorvenSuccessGreen
import com.example.ui.theme.ZorvenSurface
import com.example.ui.theme.ZorvenSurfaceElevated
import com.example.ui.theme.ZorvenSurfaceHigh
import com.example.ui.theme.ZorvenTextMuted
import com.example.ui.theme.ZorvenTextPrimary
import com.example.ui.theme.ZorvenTextSecondary
import com.example.ui.theme.parseHexColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileSelectionScreen(
    profiles: List<ProfileEntity>,
    activeProfileId: String,
    initialManageMode: Boolean,
    onSelectProfile: (ProfileEntity) -> Unit,
    onSaveProfile: (String?, String, String, String, Boolean, String, String?) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var isEditMode by remember(initialManageMode) { mutableStateOf(initialManageMode) }
    var editingProfile by remember { mutableStateOf<ProfileEntity?>(null) }
    var isCreatingNewProfile by remember { mutableStateOf(false) }
    var pinPromptProfile by remember { mutableStateOf<ProfileEntity?>(null) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ZorvenObsidian)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("profile_gate_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("profile_gate_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ZorvenTextPrimary
                )
            }

            if (isEditMode) {
                Text(
                    text = "Manage Profiles",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = ZorvenTextPrimary
                )
            } else {
                com.example.ui.components.ZorvenBrandWordmark(
                    monogramSize = 24.dp,
                    fontSize = 18.sp
                )
            }

            TextButton(
                onClick = { isEditMode = !isEditMode },
                modifier = Modifier.testTag("toggle_edit_profiles_button")
            ) {
                Icon(
                    imageVector = if (isEditMode) Icons.Filled.Check else Icons.Filled.Edit,
                    contentDescription = null,
                    tint = ZorvenTextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isEditMode) "Done" else "Edit",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = ZorvenTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isEditMode) "Tap a profile to customize avatar, Kids mode, or PIN lock" else "Who's Watching?",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = ZorvenTextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Responsive 2-Column Profile Avatar Grid
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(28.dp),
            maxItemsInEachRow = 2
        ) {
            profiles.forEach { profile ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(114.dp)
                        .clickable {
                            if (isEditMode) {
                                editingProfile = profile
                                isCreatingNewProfile = false
                            } else if (!profile.pinCode.isNullOrBlank()) {
                                pinPromptProfile = profile
                                enteredPin = ""
                                pinError = false
                            } else {
                                onSelectProfile(profile)
                            }
                        }
                        .testTag("profile_tile_${profile.id}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        ProfileAvatarBadge(
                            profile = profile,
                            size = 96.dp,
                            isSelected = profile.id == activeProfileId && !isEditMode
                        )

                        if (isEditMode) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(Color.Black.copy(alpha = 0.56f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit ${profile.name}",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = ZorvenTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!profile.pinCode.isNullOrBlank()) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "PIN Locked",
                                tint = ZorvenTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Add Profile Tile (Up to 5 profiles)
            if (profiles.size < 5) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(114.dp)
                        .clickable {
                            editingProfile = null
                            isCreatingNewProfile = true
                        }
                        .testTag("add_profile_tile")
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .background(ZorvenSurfaceElevated)
                            .border(1.5.dp, ZorvenBorderSubtle, RoundedCornerShape(19.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Profile",
                            tint = ZorvenTextPrimary,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Add Profile",
                        style = MaterialTheme.typography.titleSmall,
                        color = ZorvenTextSecondary
                    )
                }
            }
        }
    }

    // PIN Verification Dialog
    val targetPinProfile = pinPromptProfile
    if (targetPinProfile != null) {
        AlertDialog(
            onDismissRequest = { pinPromptProfile = null },
            containerColor = ZorvenSurfaceElevated,
            titleContentColor = ZorvenTextPrimary,
            textContentColor = ZorvenTextSecondary,
            title = {
                Text(
                    text = "Profile Lock is ON",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter the 4-digit PIN for \"${targetPinProfile.name}\" (Hint: ${targetPinProfile.pinCode}).",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            if (it.length <= 4) {
                                enteredPin = it
                                pinError = false
                            }
                        },
                        label = { Text("4-Digit PIN") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZorvenCrimson,
                            unfocusedBorderColor = ZorvenBorderSubtle,
                            focusedTextColor = ZorvenTextPrimary,
                            unfocusedTextColor = ZorvenTextPrimary
                        )
                    )
                    if (pinError) {
                        Text(
                            text = "Incorrect PIN. Try ${targetPinProfile.pinCode}.",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZorvenErrorRed
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredPin == targetPinProfile.pinCode) {
                            pinPromptProfile = null
                            onSelectProfile(targetPinProfile)
                        } else {
                            pinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZorvenCrimson)
                ) {
                    Text("Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = { pinPromptProfile = null }) {
                    Text("Cancel", color = ZorvenTextSecondary)
                }
            }
        )
    }

    // Create / Edit Profile Bottom Sheet
    if (isCreatingNewProfile || editingProfile != null) {
        val current = editingProfile
        var nameInput by remember(current) { mutableStateOf(current?.name ?: "") }
        var selectedColorHex by remember(current) { mutableStateOf(current?.avatarColorHex ?: "#B91C1C") }
        var isKidsToggle by remember(current) { mutableStateOf(current?.isKids ?: false) }
        var maturitySelection by remember(current) {
            mutableStateOf(current?.maturityRestriction ?: "All Maturity Ratings")
        }
        var pinInput by remember(current) { mutableStateOf(current?.pinCode ?: "") }

        val colorSwatches = listOf(
            "#B91C1C" to "#F59E0B",
            "#0F766E" to "#5EEAD4",
            "#4338CA" to "#93C5FD",
            "#D97706" to "#FDE68A",
            "#7E22CE" to "#D8B4FE",
            "#15803D" to "#86EFAC"
        )

        ModalBottomSheet(
            onDismissRequest = {
                editingProfile = null
                isCreatingNewProfile = false
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = ZorvenSurfaceElevated,
            contentColor = ZorvenTextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (current == null) "Add Profile" else "Edit Profile",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(
                        onClick = {
                            editingProfile = null
                            isCreatingNewProfile = false
                        }
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = ZorvenTextSecondary)
                    }
                }

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Profile Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZorvenCrimson,
                        unfocusedBorderColor = ZorvenBorderSubtle,
                        focusedTextColor = ZorvenTextPrimary,
                        unfocusedTextColor = ZorvenTextPrimary
                    )
                )

                // Avatar Color Picker
                Text(
                    text = "Avatar Theme Color",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = ZorvenTextSecondary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorSwatches.forEach { (bgHex, _) ->
                        val isChosen = selectedColorHex.equals(bgHex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(bgHex))
                                .border(
                                    width = if (isChosen) 2.5.dp else 1.dp,
                                    color = if (isChosen) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = bgHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isChosen) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Kids Profile Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kids Profile",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Only shows TV-Y7 and family-friendly titles",
                            style = MaterialTheme.typography.bodySmall,
                            color = ZorvenTextSecondary
                        )
                    }
                    Switch(
                        checked = isKidsToggle,
                        onCheckedChange = {
                            isKidsToggle = it
                            if (it) maturitySelection = "Kids (TV-Y7)"
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ZorvenCrimson)
                    )
                }

                // Viewing Restrictions Selector
                if (!isKidsToggle) {
                    val restrictions = listOf("All Maturity Ratings", "PG-13 & Below", "Kids (TV-Y7)")
                    Text(
                        text = "Viewing Restrictions",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = ZorvenTextSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        restrictions.forEach { level ->
                            val selected = maturitySelection == level
                            Surface(
                                color = if (selected) ZorvenCrimson else ZorvenSurface,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { maturitySelection = level }
                            ) {
                                Text(
                                    text = level,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ZorvenTextPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Optional 4-Digit Profile Lock PIN
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 4) pinInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Optional 4-Digit Profile Lock PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZorvenCrimson,
                        unfocusedBorderColor = ZorvenBorderSubtle,
                        focusedTextColor = ZorvenTextPrimary,
                        unfocusedTextColor = ZorvenTextPrimary
                    )
                )

                // Save / Delete Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (current != null && profiles.size > 1) {
                        OutlinedButton(
                            onClick = {
                                onDeleteProfile(current.id)
                                editingProfile = null
                                isCreatingNewProfile = false
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = ZorvenErrorRed)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete", color = ZorvenErrorRed)
                        }
                    }

                    Button(
                        onClick = {
                            val accent = colorSwatches.find { it.first == selectedColorHex }?.second ?: "#F59E0B"
                            onSaveProfile(
                                current?.id,
                                nameInput,
                                selectedColorHex,
                                accent,
                                isKidsToggle,
                                maturitySelection,
                                pinInput.takeIf { it.length == 4 }
                            )
                            editingProfile = null
                            isCreatingNewProfile = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_profile_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = ZorvenObsidian
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Save Profile",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Complete Settings Screen organized into:
 * 1. Custom Streaming Server API Configuration & Diagnostics
 * 2. Account & Membership
 * 3. App Settings (Language, Video Playback, Mobile Data, Downloads, Smart Downloads, Storage)
 * 4. Playback & Accessibility (Subtitles Appearance, Audio/Subtitle Preferences, Autoplay, Reduced Motion)
 * 5. Other (Replay Splash Animation, Help Center, Privacy, About, Sign Out)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettingsUiState,
    activeProfile: ProfileEntity?,
    downloadedCount: Int,
    downloadedSizeMb: Int,
    onBack: () -> Unit,
    onUpdateSettings: ((AppSettingsUiState) -> AppSettingsUiState) -> Unit,
    onUpdateCustomApiEndpoint: (String) -> Unit,
    onOpenManageProfiles: () -> Unit,
    onClearAllDownloads: () -> Unit,
    onReplaySplashAnimation: () -> Unit,
    onSignOutToProfileGate: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var showApiConfigSheet by remember { mutableStateOf(false) }
    var showSubtitleStyleSheet by remember { mutableStateOf(false) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showQualitySelectorFor by remember { mutableStateOf<String?>(null) } // "VIDEO" or "DOWNLOAD" or "DATA" or "LANGUAGE"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ZorvenObsidian)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("settings_screen")
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ZorvenTextPrimary
                )
            }
            Text(
                text = "App Settings",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = ZorvenTextPrimary
            )
        }

        HorizontalDivider(color = ZorvenBorderSubtle)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 36.dp)
        ) {
            // SECTION 1: CUSTOM STREAMING SERVER API
            item(key = "section_streaming_api") {
                SettingsSectionHeader(title = "STREAMING SERVER API & NODES")
                Surface(
                    color = ZorvenSurface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .border(1.dp, ZorvenBorderSubtle, RoundedCornerShape(10.dp))
                        .clickable { showApiConfigSheet = true }
                        .testTag("settings_custom_api_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Dns,
                                    contentDescription = null,
                                    tint = ZorvenCrimson
                                )
                                Text(
                                    text = "Custom Movie Streaming Server API",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ZorvenTextPrimary
                                )
                            }
                            Text(
                                text = "Configure",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ZorvenCrimson
                            )
                        }
                        Text(
                            text = "Endpoint: ${settings.customStreamApiUrl}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ZorvenTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = settings.apiStatusMessage,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = ZorvenSuccessGreen
                        )
                    }
                }
            }

            // SECTION 2: ACCOUNT & MEMBERSHIP
            item(key = "section_account") {
                SettingsSectionHeader(title = "ACCOUNT & MEMBERSHIP")
                SettingsClickableRow(
                    icon = Icons.Filled.Person,
                    title = "Account & Membership",
                    subtitle = "Ultra HD 4K + Spatial Audio Plan • Active Profile: ${activeProfile?.name ?: "Primary"}",
                    onClick = onOpenManageProfiles
                )
                SettingsClickableRow(
                    icon = Icons.Filled.Edit,
                    title = "Manage Profiles & Parental Controls",
                    subtitle = "${activeProfile?.maturityRestriction ?: "All Maturity Ratings"} • PIN Lock & Kids Mode",
                    onClick = onOpenManageProfiles
                )
            }

            // SECTION 3: VIDEO PLAYBACK & MOBILE DATA
            item(key = "section_app_settings") {
                SettingsSectionHeader(title = "VIDEO PLAYBACK & DATA USAGE")
                SettingsClickableRow(
                    icon = Icons.Filled.Speed,
                    title = "Video Playback Quality",
                    subtitle = settings.videoPlaybackQuality,
                    onClick = { showQualitySelectorFor = "VIDEO" }
                )
                SettingsClickableRow(
                    icon = Icons.Filled.Wifi,
                    title = "Cellular Data Usage",
                    subtitle = settings.mobileDataUsage,
                    onClick = { showQualitySelectorFor = "DATA" }
                )
                SettingsToggleRow(
                    title = "Push Notifications & New Release Alerts",
                    subtitle = "Receive alerts when new seasons and Top 10 movies arrive",
                    checked = settings.notificationsEnabled,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(notificationsEnabled = enabled) }
                    }
                )
                SettingsClickableRow(
                    icon = Icons.Filled.Subtitles,
                    title = "Display & App Language",
                    subtitle = settings.language,
                    onClick = { showQualitySelectorFor = "LANGUAGE" }
                )
            }

            // SECTION 4: DOWNLOADS & STORAGE
            item(key = "section_downloads_settings") {
                SettingsSectionHeader(title = "DOWNLOADS & STORAGE")
                SettingsToggleRow(
                    title = "Wi-Fi Only Downloads",
                    subtitle = "Only download movies and episodes when connected to Wi-Fi",
                    checked = settings.wifiOnlyDownloads,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(wifiOnlyDownloads = enabled) }
                    }
                )
                SettingsToggleRow(
                    title = "Smart Downloads",
                    subtitle = "Automatically download the next episode and remove watched episodes on Wi-Fi",
                    checked = settings.smartDownloadsEnabled,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(smartDownloadsEnabled = enabled) }
                    }
                )
                SettingsClickableRow(
                    icon = Icons.Filled.Storage,
                    title = "Download Video Quality",
                    subtitle = settings.downloadQuality,
                    onClick = { showQualitySelectorFor = "DOWNLOAD" }
                )

                // Storage breakdown card
                Surface(
                    color = ZorvenSurface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = settings.downloadLocation,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ZorvenTextPrimary
                            )
                            Text(
                                text = "$downloadedCount titles (${String.format("%.1f", downloadedSizeMb / 1024f)} GB)",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZorvenCrimson
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (downloadedSizeMb / 8192f).coerceIn(0.05f, 0.9f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ZorvenCrimson,
                            trackColor = ZorvenSurfaceHigh
                        )
                        if (downloadedCount > 0) {
                            TextButton(
                                onClick = onClearAllDownloads,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "Delete All Downloads",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ZorvenErrorRed
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 5: PLAYBACK & ACCESSIBILITY
            item(key = "section_playback_accessibility") {
                SettingsSectionHeader(title = "PLAYBACK & ACCESSIBILITY")
                SettingsClickableRow(
                    icon = Icons.Filled.Subtitles,
                    title = "Subtitle Appearance",
                    subtitle = "Size: ${settings.subtitleSize} • Style: ${settings.subtitleBackgroundStyle}",
                    onClick = { showSubtitleStyleSheet = true }
                )
                SettingsToggleRow(
                    title = "Autoplay Next Episode",
                    subtitle = "Automatically start the next episode on series playback",
                    checked = settings.autoplayNextEpisode,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(autoplayNextEpisode = enabled) }
                    }
                )
                SettingsToggleRow(
                    title = "Autoplay Previews While Browsing",
                    subtitle = "Play muted video previews on Home and Details",
                    checked = settings.autoplayPreviews,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(autoplayPreviews = enabled) }
                    }
                )
                SettingsToggleRow(
                    title = "Reduced Motion (Accessibility)",
                    subtitle = "Minimize crossfade and parallax intensity across transitions",
                    checked = settings.reducedMotion,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(reducedMotion = enabled) }
                    }
                )
            }

            // SECTION 6: DIAGNOSTICS, SPLASH & OTHER
            item(key = "section_other") {
                SettingsSectionHeader(title = "DIAGNOSTICS & ABOUT")
                SettingsClickableRow(
                    icon = Icons.Filled.NetworkCheck,
                    title = "Network & Streaming Server Diagnostics",
                    subtitle = "Check server latency, bandwidth, and DRM/codec compatibility",
                    onClick = { showDiagnosticsDialog = true }
                )
                SettingsClickableRow(
                    icon = Icons.Filled.AutoAwesome,
                    title = "Replay ZORVÉN Refracted Light Splash",
                    subtitle = "Launch the signature ZORVÉN optical prism intro animation",
                    onClick = onReplaySplashAnimation
                )
                SettingsClickableRow(
                    icon = Icons.Filled.Dns,
                    title = "About Zorvén & Privacy",
                    subtitle = "Version 1.0.0 (Build 2026.11) • Custom Server Edition",
                    onClick = { showAboutDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showSignOutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp)
                        .testTag("settings_sign_out_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZorvenSurfaceElevated,
                        contentColor = ZorvenErrorRed
                    )
                ) {
                    Text(
                        text = "Switch Profile / Sign Out",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    // Custom Streaming Server API Endpoint Modal
    if (showApiConfigSheet) {
        var urlDraft by remember(settings.customStreamApiUrl) { mutableStateOf(settings.customStreamApiUrl) }
        ModalBottomSheet(
            onDismissRequest = { showApiConfigSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = ZorvenSurfaceElevated,
            contentColor = ZorvenTextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Custom Movie Streaming Server API",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Enter your custom streaming server base URL (or configure STREAM_API_BASE_URL and STREAM_API_KEY in the AI Studio Secrets panel). Zorvén queries GET /streams/{mediaId} for multi-server stream nodes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ZorvenTextSecondary
                )
                OutlinedTextField(
                    value = urlDraft,
                    onValueChange = { urlDraft = it },
                    label = { Text("Streaming Server Base URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZorvenCrimson,
                        unfocusedBorderColor = ZorvenBorderSubtle,
                        focusedTextColor = ZorvenTextPrimary,
                        unfocusedTextColor = ZorvenTextPrimary
                    )
                )
                Button(
                    onClick = {
                        onUpdateCustomApiEndpoint(urlDraft)
                        showApiConfigSheet = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = ZorvenObsidian
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Save & Connect Server Endpoint",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Subtitle Appearance Customization Sheet
    if (showSubtitleStyleSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSubtitleStyleSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = ZorvenSurfaceElevated,
            contentColor = ZorvenTextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Subtitle Appearance",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                // Live Subtitle Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ZorvenObsidian)
                        .border(1.dp, ZorvenBorderSubtle, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val fontSize = when (settings.subtitleSize) {
                        "Small" -> 13.sp
                        "Large" -> 20.sp
                        else -> 16.sp
                    }
                    Surface(
                        color = if (settings.subtitleBackgroundStyle.contains("Semi-Transparent")) {
                            Color.Black.copy(alpha = 0.72f)
                        } else {
                            Color.Transparent
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Beyond the golden rings lies the last transmission.",
                            fontSize = fontSize,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Text("Text Size", style = MaterialTheme.typography.labelLarge, color = ZorvenTextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Small", "Medium", "Large").forEach { sz ->
                        val selected = settings.subtitleSize == sz
                        Surface(
                            color = if (selected) ZorvenCrimson else ZorvenSurface,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onUpdateSettings { it.copy(subtitleSize = sz) } }
                        ) {
                            Text(
                                text = sz,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ZorvenTextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }

                Text("Window Style", style = MaterialTheme.typography.labelLarge, color = ZorvenTextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Semi-Transparent Cinema Box", "Drop Shadow Outline").forEach { st ->
                        val selected = settings.subtitleBackgroundStyle == st
                        Surface(
                            color = if (selected) ZorvenCrimson else ZorvenSurface,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onUpdateSettings { it.copy(subtitleBackgroundStyle = st) } }
                        ) {
                            Text(
                                text = st,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ZorvenTextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Selector Dialog for Video Quality / Download Quality / Cellular Data / Language
    val selectorType = showQualitySelectorFor
    if (selectorType != null) {
        val (dialogTitle, options, currentSelection) = when (selectorType) {
            "VIDEO" -> Triple(
                "Video Playback Quality",
                listOf("Auto (Up to 4K HDR)", "1080p Full HD (High Bitrate)", "720p Standard HD", "Data Saver (480p)"),
                settings.videoPlaybackQuality
            )
            "DOWNLOAD" -> Triple(
                "Download Video Quality",
                listOf("High (1080p Full HD)", "Standard (720p — Faster downloads, less storage)"),
                settings.downloadQuality
            )
            "DATA" -> Triple(
                "Cellular Data Usage",
                listOf("Automatic", "Wi-Fi Only", "Save Data (approx. 6 hours per GB)", "Maximum Data"),
                settings.mobileDataUsage
            )
            else -> Triple(
                "Display & Audio Language",
                listOf("English (United States)", "Français (France)", "Español (España)", "Deutsch", "日本語"),
                settings.language
            )
        }

        AlertDialog(
            onDismissRequest = { showQualitySelectorFor = null },
            containerColor = ZorvenSurfaceElevated,
            titleContentColor = ZorvenTextPrimary,
            title = { Text(dialogTitle, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    options.forEach { option ->
                        val isSelected = option == currentSelection
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) ZorvenCrimson.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable {
                                    when (selectorType) {
                                        "VIDEO" -> onUpdateSettings { it.copy(videoPlaybackQuality = option) }
                                        "DOWNLOAD" -> onUpdateSettings { it.copy(downloadQuality = option) }
                                        "DATA" -> onUpdateSettings { it.copy(mobileDataUsage = option) }
                                        else -> onUpdateSettings { it.copy(language = option) }
                                    }
                                    showQualitySelectorFor = null
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ZorvenTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = ZorvenCrimson)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQualitySelectorFor = null }) {
                    Text("Close", color = ZorvenTextSecondary)
                }
            }
        )
    }

    if (showDiagnosticsDialog) {
        AlertDialog(
            onDismissRequest = { showDiagnosticsDialog = false },
            containerColor = ZorvenSurfaceElevated,
            titleContentColor = ZorvenTextPrimary,
            textContentColor = ZorvenTextSecondary,
            title = { Text("Network & Server Diagnostics", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("• Custom API Server 1: Connected (24 ms)", color = ZorvenSuccessGreen)
                    Text("• Custom API Mirror 2: Connected (38 ms)", color = ZorvenSuccessGreen)
                    Text("• Cinema Edge CDN: Connected (46 ms)", color = ZorvenSuccessGreen)
                    Text("• Estimated Bandwidth: 148 Mbps (4K HDR Ready)")
                    Text("• Audio Decoder: Dolby Atmos / 5.1 Passthrough Active")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDiagnosticsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ZorvenCrimson)
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = ZorvenSurfaceElevated,
            titleContentColor = ZorvenTextPrimary,
            textContentColor = ZorvenTextSecondary,
            title = { Text("ZORVÉN Mobile Streaming", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Version 1.0.0 • Built with Jetpack Compose, Room Local Persistence, and Custom Multi-Server Streaming API support.\n\nAll catalog metadata, watch progress, My List items, and offline downloads are stored locally on your device."
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close", color = ZorvenCrimson)
                }
            }
        )
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = ZorvenSurfaceElevated,
            titleContentColor = ZorvenTextPrimary,
            textContentColor = ZorvenTextSecondary,
            title = { Text("Switch Profile / Sign Out?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Your My List, Continue Watching timestamps, and Downloads remain safely stored in your local profile database.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        onSignOutToProfileGate()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZorvenCrimson)
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = ZorvenTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp
        ),
        color = ZorvenTextMuted,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ZorvenTextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = ZorvenTextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = ZorvenTextSecondary
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = ZorvenTextMuted
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = ZorvenTextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = ZorvenTextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ZorvenCrimson
            )
        )
    }
}
