package com.yayyar.teahouse.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yayyar.teahouse.ui.theme.AccentBlue
import com.yayyar.teahouse.ui.theme.AccentGreen
import com.yayyar.teahouse.ui.theme.AccentPurple
import com.yayyar.teahouse.ui.theme.PrimaryLight

import androidx.compose.ui.res.stringResource
import com.yayyar.teahouse.R

import androidx.compose.material.icons.outlined.Palette
import com.yayyar.teahouse.ui.theme.ThemeSeed
import com.yayyar.teahouse.ui.theme.ThemeStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onBack: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit,
    onNavigateToPrinters: () -> Unit,
    onNavigateToThemeSettings: () -> Unit = {},
    viewModel: SettingViewModel = hiltViewModel(),
    showTopBar: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDarkModePref by viewModel.isDarkMode.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedThemeSeed by viewModel.selectedThemeSeed.collectAsState()
    val selectedThemeStyle by viewModel.selectedThemeStyle.collectAsState()
    val activePaymentCount by viewModel.activePaymentMethodsCount.collectAsState()
    val selectedPrinterName by viewModel.selectedPrinterName.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()

    val currentSeed = ThemeSeed.fromId(selectedThemeSeed)
    val currentStyle = ThemeStyle.fromId(selectedThemeStyle)

    val systemInDark = isSystemInDarkTheme()
    val isDarkEffective = isDarkModePref ?: systemInDark

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showItemLayoutDialog by remember { mutableStateOf(false) }

    val content: @Composable (Modifier) -> Unit = { contentModifier ->
        Column(
            modifier = contentModifier
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // General Settings Section
            SettingsSectionHeader(title = stringResource(R.string.settings_section_appearance))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Dark Mode Toggle
                    SettingsSwitchItem(
                        icon = Icons.Outlined.DarkMode,
                        iconBackground = Color(0xFF6366F1),
                        title = stringResource(R.string.settings_dark_mode),
                        subtitle = if (isDarkEffective) stringResource(R.string.settings_dark_enabled) else stringResource(R.string.settings_light_enabled),
                        checked = isDarkEffective,
                        onCheckedChange = { isChecked ->
                            viewModel.setDarkMode(isChecked)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Theme & Color Scheme
                    SettingsClickableItem(
                        icon = Icons.Outlined.Palette,
                        iconBackground = currentSeed.seedColor,
                        title = stringResource(R.string.settings_theme_title),
                        subtitle = stringResource(
                            R.string.settings_theme_sub,
                            stringResource(currentSeed.titleRes),
                            stringResource(currentStyle.titleRes)
                        ),
                        valueText = stringResource(currentSeed.titleRes),
                        onClick = onNavigateToThemeSettings
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Language Item
                    SettingsClickableItem(
                        icon = Icons.Outlined.Language,
                        iconBackground = AccentBlue,
                        title = stringResource(R.string.settings_language),
                        subtitle = if (selectedLanguage == "my") stringResource(R.string.settings_lang_my) else stringResource(R.string.settings_lang_en),
                        valueText = if (selectedLanguage == "my") "မြန်မာ" else "English",
                        onClick = { showLanguageDialog = true }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Item Layout Item
                    SettingsClickableItem(
                        icon = if (isGridView) Icons.Outlined.GridView else Icons.AutoMirrored.Outlined.ViewList,
                        iconBackground = AccentBlue,
                        title = stringResource(R.string.settings_item_layout),
                        subtitle = if (isGridView) stringResource(R.string.settings_item_layout_grid) else stringResource(R.string.settings_item_layout_list),
                        valueText = if (isGridView) stringResource(R.string.settings_item_layout_grid) else stringResource(R.string.settings_item_layout_list),
                        onClick = { showItemLayoutDialog = true }
                    )
                }
            }

            // Control & Management Section
            SettingsSectionHeader(title = stringResource(R.string.settings_section_control))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Payment Method Navigation
                    SettingsClickableItem(
                        icon = Icons.Outlined.Payments,
                        iconBackground = AccentGreen,
                        title = stringResource(R.string.settings_payment_methods),
                        subtitle = if (activePaymentCount > 0) stringResource(R.string.settings_payment_methods_sub, activePaymentCount) else stringResource(R.string.settings_payment_methods),
                        valueText = "$activePaymentCount",
                        onClick = onNavigateToPaymentMethods
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Printer Navigation
                    SettingsClickableItem(
                        icon = Icons.Outlined.Print,
                        iconBackground = AccentPurple,
                        title = stringResource(R.string.settings_printer),
                        subtitle = selectedPrinterName?.let { stringResource(R.string.settings_printer_sub_connected, it) } ?: stringResource(R.string.settings_printer_sub_none),
                        valueText = selectedPrinterName ?: stringResource(R.string.action_select),
                        onClick = onNavigateToPrinters
                    )
                }
            }

            // App Information Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.app_version),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showTopBar) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.settings_title),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            content(Modifier.fillMaxSize().padding(innerPadding))
        }
    } else {
        content(modifier.fillMaxSize())
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
//            title = {
//                Text(stringResource(R.string.settings_lang_dialog_title), fontWeight = FontWeight.Bold)
//            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setSelectedLanguage("en")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedLanguage == "en",
                            onClick = {
                                viewModel.setSelectedLanguage("en")
                                showLanguageDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_lang_en), style = MaterialTheme.typography.bodyLarge)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setSelectedLanguage("my")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedLanguage == "my",
                            onClick = {
                                viewModel.setSelectedLanguage("my")
                                showLanguageDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(stringResource(R.string.settings_lang_my), style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = stringResource(R.string.settings_lang_my_sub),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
    }

    if (showItemLayoutDialog) {
        AlertDialog(
            onDismissRequest = { showItemLayoutDialog = false },
//            title = {
//                Text(stringResource(R.string.settings_item_layout), fontWeight = FontWeight.Bold)
//            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setGridView(true)
                                showItemLayoutDialog = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isGridView,
                            onClick = {
                                viewModel.setGridView(true)
                                showItemLayoutDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_item_layout_grid), style = MaterialTheme.typography.bodyLarge)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setGridView(false)
                                showItemLayoutDialog = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !isGridView,
                            onClick = {
                                viewModel.setGridView(false)
                                showItemLayoutDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_item_layout_list), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showItemLayoutDialog = false }) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    iconBackground: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBackground.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconBackground,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryLight
            )
        )
    }
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    iconBackground: Color,
    title: String,
    subtitle: String,
    valueText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBackground.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconBackground,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (valueText != null) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}
