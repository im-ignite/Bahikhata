package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import com.example.ui.theme.bahi

data class BahiNavItem(val label: String, val icon: ImageVector, val testTag: String)

/**
 * One scaffold, three layouts:
 *  • phones (<600dp): floating tactile dock at the bottom; only the active tab shows its label
 *  • tablets / foldables / landscape (≥600dp): navigation rail on the left
 *  • content never stretches past 920dp, so lists stay readable on big screens
 */
@Composable
fun BahiNavScaffold(
    items: List<BahiNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    topBar: @Composable (isCompact: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val isCompact = maxWidth < 600.dp
        Row(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .ledgerPaper(),
        ) {
            if (!isCompact) BahiRail(items, selectedIndex, onSelect)
            Scaffold(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                containerColor = Color.Transparent,
                topBar = { topBar(isCompact) },
                bottomBar = { if (isCompact) BahiDock(items, selectedIndex, onSelect) },
            ) { inner ->
                Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.TopCenter) {
                    Box(Modifier.widthIn(max = 920.dp).fillMaxWidth().fillMaxHeight()) { content() }
                }
            }
        }
    }
}

@Composable
private fun BahiDock(items: List<BahiNavItem>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("bottom_nav_bar"),
    ) {
        TactileSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            depth = 3.dp,
            face = Brush.verticalGradient(listOf(cs.surface, lerp(cs.surface, cs.surfaceVariant, 0.6f))),
            edge = MaterialTheme.bahi.edge,
        ) {
            Row(
                Modifier.fillMaxWidth().padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { i, item ->
                    DockItem(item, selected = i == selectedIndex, onClick = { onSelect(i) })
                }
            }
        }
    }
}

@Composable
private fun DockItem(item: BahiNavItem, selected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val fg = if (selected) cs.onPrimary else cs.onSurfaceVariant
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) cs.primary else Color.Transparent)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .heightIn(min = 48.dp)
            .widthIn(min = 48.dp)
            .padding(horizontal = if (selected) 14.dp else 0.dp)
            .animateContentSize()
            .testTag(item.testTag),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(item.icon, contentDescription = item.label, tint = fg, modifier = Modifier.size(24.dp))
        AnimatedVisibility(visible = selected) {
            Text(
                item.label,
                color = fg,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun BahiRail(items: List<BahiNavItem>, selectedIndex: Int, onSelect: (Int) -> Unit, appName: String = "RAI FISH") {
    val cs = MaterialTheme.colorScheme
    val monogram = appName.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("")
    NavigationRail(
        containerColor = cs.surface,
        modifier = Modifier.fillMaxHeight(),
        header = {
            Spacer(Modifier.height(8.dp))
            TactileSurface(
                shape = RoundedCornerShape(14.dp),
                depth = 3.dp,
                face = Brush.linearGradient(listOf(MaterialTheme.bahi.heroTop, MaterialTheme.bahi.heroBottom)),
                edge = lerp(MaterialTheme.bahi.heroBottom, Color.Black, 0.4f),
            ) {
                Text(
                    monogram,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
        },
    ) {
        items.forEachIndexed { i, item ->
            NavigationRailItem(
                selected = i == selectedIndex,
                onClick = { onSelect(i) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                alwaysShowLabel = true,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = cs.onPrimary,
                    indicatorColor = cs.primary,
                    selectedTextColor = cs.primary,
                    unselectedIconColor = cs.onSurfaceVariant,
                    unselectedTextColor = cs.onSurfaceVariant,
                ),
                modifier = Modifier.testTag(item.testTag),
            )
        }
    }
}

/** "Synced" in green, or "Offline" in brass. Tap to connect. Replaces the always-green "Connect" label. */
@Composable
fun SyncChip(
    isLinked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    syncedLabel: String = "Synced",
    offlineLabel: String = "Offline",
) {
    val x = MaterialTheme.bahi
    val tone = if (isLinked) x.success else MaterialTheme.colorScheme.tertiary
    Box(
        modifier
            .heightIn(min = 48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier
                .clip(CircleShape)
                .background(tone.copy(alpha = 0.14f))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(8.dp).background(tone, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(
                if (isLinked) syncedLabel else offlineLabel,
                color = tone,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun BahiTopBar(
    isCompact: Boolean,
    isLinked: Boolean,
    isDarkMode: Boolean,
    onMenu: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onSettings: () -> Unit,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier,
    appName: String = "RAI FISH",
    syncedLabel: String = "Synced",
    offlineLabel: String = "Offline",
    menuLabel: String = "Menu",
    darkToggleLabel: String = "Toggle dark mode",
    settingsLabel: String = "Settings",
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(cs.background.copy(alpha = 0.94f))
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onMenu) { Icon(Icons.Default.Menu, contentDescription = menuLabel, tint = cs.onSurface) }
        Text(
            appName,
            style = MaterialTheme.typography.titleLarge,
            color = cs.onSurface,
            maxLines = 1,
            modifier = Modifier.handUnderline(cs.primary.copy(alpha = 0.75f)),
        )
        Spacer(Modifier.weight(1f))
        SyncChip(isLinked, onSyncClick, syncedLabel = syncedLabel, offlineLabel = offlineLabel)
        // On phones the dark-mode switch lives in the drawer, so the bar stays uncluttered.
        if (!isCompact) {
            IconButton(onClick = onToggleDarkMode, modifier = Modifier.testTag("dark_mode_toggle_btn")) {
                Icon(
                    if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = darkToggleLabel,
                    tint = cs.onSurface,
                )
            }
        }
        IconButton(onClick = onSettings, modifier = Modifier.testTag("settings_btn")) {
            Icon(Icons.Default.Settings, contentDescription = settingsLabel, tint = cs.primary)
        }
    }
}
