package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.model.SyncStatus
import com.example.data.repository.TradeRepository
import com.example.notification.NotificationHelper
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DashboardDailyScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealPrimary
import com.example.ui.util.AppLanguage
import com.example.ui.util.AppStrings
import com.example.ui.util.LocalAppLanguage
import com.example.ui.util.LocalAppStrings
import com.example.ui.viewmodel.TradeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TradeViewModel by viewModels {
        val db = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val notifHelper = NotificationHelper(applicationContext)
        val repository = TradeRepository(db, notifHelper)
        TradeViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()

            var userDarkModeOverride by remember { mutableStateOf<Boolean?>(null) }
            val isDark = userDarkModeOverride ?: systemDark

            MyApplicationTheme(darkTheme = isDark) {
                MainAppScreen(
                    viewModel = viewModel,
                    isDarkMode = isDark,
                    onToggleDarkMode = {
                        userDarkModeOverride = !isDark
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: TradeViewModel,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val googleAccount by viewModel.googleAccount.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val lastSyncLog by viewModel.lastSyncLog.collectAsStateWithLifecycle()

    val batches by viewModel.allBatches.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val sales by viewModel.allSales.collectAsStateWithLifecycle()
    val reportMetrics by viewModel.reportMetrics.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }
    val strings = remember(uiState.language) { AppStrings(uiState.language) }

    // Notification permission request for Android 13+
    var hasNotifPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotifPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notifications enabled for sync & inventory alerts", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    CompositionLocalProvider(
        LocalAppStrings provides strings,
        LocalAppLanguage provides uiState.language
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = strings.appTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        // Real-time Cloud Sync Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                .clickable {
                                    viewModel.triggerCloudSync()
                                    Toast.makeText(context, "Syncing with Google Drive...", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("sync_status_indicator")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (syncStatus == SyncStatus.SYNCING) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(10.dp),
                                        strokeWidth = 2.dp,
                                        color = TealPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = strings.syncStatusSyncing,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = TealPrimary
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                if (googleAccount.isLinked) SuccessGreen else Color.Gray,
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (googleAccount.isLinked) strings.syncStatusDriveSynced else strings.syncStatusOffline,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = if (googleAccount.isLinked) SuccessGreen else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    // Settings button at top
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = strings.settingsButton,
                            tint = TealPrimary
                        )
                    }

                    // Dark mode toggle
                    IconButton(
                        onClick = onToggleDarkMode,
                        modifier = Modifier.testTag("dark_mode_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = strings.darkModeToggle,
                            tint = if (isDarkMode) AmberAccent else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Notification toggle / trigger check
                    IconButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifPermission) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                val notifMsg = if (uiState.language == AppLanguage.HINDI)
                                    "गूगल ड्राइव सिंक और कम स्टॉक के लिए पुश नोटिफिकेशन चालू हैं"
                                else
                                    "Push notifications active for Drive sync & low stock alerts"
                                Toast.makeText(context, notifMsg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("notification_settings_btn")
                    ) {
                        Icon(
                            imageVector = if (hasNotifPermission) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = strings.notificationAlerts,
                            tint = if (hasNotifPermission) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                val navItems = listOf(
                    Triple(strings.navDaily, Icons.Default.Today, "nav_daily"),
                    Triple(strings.navSales, Icons.Default.ShoppingCart, "nav_sales"),
                    Triple(strings.navProducts, Icons.Default.Inventory2, "nav_products"),
                    Triple(strings.navReports, Icons.Default.BarChart, "nav_reports"),
                    Triple(strings.navClients, Icons.Default.Group, "nav_customers")
                )

                navItems.forEachIndexed { index, (label, icon, testTag) ->
                    val isSelected = uiState.activeTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setActiveTab(index) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = uiState.activeTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> DashboardDailyScreen(
                        batches = batches,
                        accountInfo = googleAccount,
                        syncStatus = syncStatus,
                        lastSyncLog = lastSyncLog,
                        onAddBatch = { name, pcs, wt, notes, date ->
                            viewModel.addDailyBatch(name, pcs, wt, notes, date)
                            Toast.makeText(context, "Batch saved & queued for Drive sync", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteBatch = { viewModel.deleteDailyBatch(it) },
                        onSyncNow = { viewModel.triggerCloudSync() },
                        onToggleAutoSync = { viewModel.toggleAutoSync(it) },
                        onExportCsv = {
                            val success = viewModel.exportCsvForGoogleDrive(context)
                            if (!success) {
                                Toast.makeText(context, "Failed to prepare CSV for export", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onLinkAccount = { email, name ->
                            viewModel.linkGoogleAccount(email, name)
                            Toast.makeText(context, "Linked Google Account: $email", Toast.LENGTH_SHORT).show()
                        },
                        onUnlinkAccount = {
                            viewModel.unlinkGoogleAccount()
                            Toast.makeText(context, "Unlinked Google Account", Toast.LENGTH_SHORT).show()
                        }
                    )

                    1 -> SalesScreen(
                        sales = sales,
                        products = products,
                        customers = customers,
                        onRecordSale = { custId, custName, prodId, itemName, pcs, wt, pricePerKg, date ->
                            viewModel.recordSale(custId, custName, prodId, itemName, pcs, wt, pricePerKg, date)
                            Toast.makeText(context, "Sale recorded! Inventory automatically deducted.", Toast.LENGTH_SHORT).show()
                        },
                        onAddCustomer = { name, phone, address, notes ->
                            viewModel.addCustomer(name, phone, address, notes)
                        },
                        onUpdateSale = { updated ->
                            viewModel.updateSale(updated)
                            Toast.makeText(context, "Sale record updated", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteSale = { toDelete ->
                            viewModel.deleteSale(toDelete)
                            Toast.makeText(context, "Sale record deleted", Toast.LENGTH_SHORT).show()
                        }
                    )

                    2 -> CatalogScreen(
                        products = products,
                        googleAccount = googleAccount,
                        onAddProduct = { name, pricePerKg, pcs, wt, cat ->
                            viewModel.addProduct(name, pricePerKg, pcs, wt, cat)
                            Toast.makeText(context, "Product saved to Main Profile", Toast.LENGTH_SHORT).show()
                        },
                        onUpdateProduct = { viewModel.updateProduct(it) },
                        onDeleteProduct = { viewModel.deleteProduct(it) }
                    )

                    3 -> ReportsScreen(
                        metrics = reportMetrics,
                        selectedPreset = uiState.selectedDateRangePreset,
                        batches = batches,
                        sales = sales,
                        onSelectPreset = { viewModel.setDateRangePreset(it) },
                        onExportCsvForDrive = {
                            val success = viewModel.exportCsvForGoogleDrive(context)
                            if (!success) {
                                Toast.makeText(context, "Failed to prepare CSV for Drive", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    4 -> CustomersScreen(
                        customers = customers,
                        sales = sales,
                        onAddCustomer = { name, phone, address, notes ->
                            viewModel.addCustomer(name, phone, address, notes)
                            Toast.makeText(context, "Customer added to CRM", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteCustomer = { toDelete ->
                            viewModel.deleteCustomer(toDelete)
                            Toast.makeText(context, "Client removed", Toast.LENGTH_SHORT).show()
                        },
                        onUpdateSale = { updated ->
                            viewModel.updateSale(updated)
                            Toast.makeText(context, "Sale record updated", Toast.LENGTH_SHORT).show()
                        },
                        onNavigateToSales = { viewModel.setActiveTab(1) }
                    )
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentLanguage = uiState.language,
            strings = strings,
            onLanguageSelected = { newLang ->
                viewModel.setLanguage(newLang)
                val toastMsg = if (newLang == AppLanguage.HINDI)
                    "भाषा बदलकर हिन्दी कर दी गई"
                else
                    "Language switched to English"
                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
}
