package com.example.ui.util

import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी")
}

class AppStrings(val language: AppLanguage) {
    val isHindi: Boolean get() = language == AppLanguage.HINDI

    // App Header & Branding
    val appTitle: String get() = if (isHindi) "ट्रेडसिंक" else "TradeSync"
    val syncStatusSyncing: String get() = if (isHindi) "सिंकिंग..." else "Syncing"
    val syncStatusDriveSynced: String get() = if (isHindi) "ड्राइव सिंक" else "Drive Synced"
    val syncStatusOffline: String get() = if (isHindi) "ऑफ़लाइन" else "Offline"
    val settingsButton: String get() = if (isHindi) "सेटिंग्स" else "Settings"
    val darkModeToggle: String get() = if (isHindi) "डार्क मोड बदलें" else "Toggle Dark Mode"
    val notificationAlerts: String get() = if (isHindi) "सूचना अलर्ट" else "Notification Alerts"

    // Navigation Tabs (Profile renamed to Products)
    val navDaily: String get() = if (isHindi) "दैनिक" else "Daily"
    val navSales: String get() = if (isHindi) "बिक्री" else "Sales"
    val navProducts: String get() = if (isHindi) "उत्पाद" else "Products"
    val navReports: String get() = if (isHindi) "रिपोर्ट्स" else "Reports"
    val navClients: String get() = if (isHindi) "ग्राहक" else "Clients"

    // Settings Dialog
    val settingsTitle: String get() = if (isHindi) "सेटिंग्स" else "Settings"
    val languageSectionTitle: String get() = if (isHindi) "ऐप की भाषा" else "App Language"
    val languageSectionSubtitle: String get() = if (isHindi) "ऐप के लिए भाषा चुनें (English / हिन्दी)" else "Select application language (English / Hindi)"
    val englishOption: String get() = if (isHindi) "English (अंग्रेज़ी)" else "English"
    val hindiOption: String get() = if (isHindi) "हिन्दी (Hindi)" else "Hindi (हिन्दी)"
    val languageActiveHint: String get() = if (isHindi) "सक्रिय भाषा: हिन्दी" else "Active language: English"
    val doneButton: String get() = if (isHindi) "हो गया" else "Done"
    val closeButton: String get() = if (isHindi) "बंद करें" else "Close"
    val appInfoTitle: String get() = if (isHindi) "ट्रेडसिंक ऐप जानकारी" else "TradeSync App Info"
    val appInfoSubtitle: String get() = if (isHindi) "वजन-आधारित बिक्री, इन्वेंटरी और गूगल ड्राइव बैकअप" else "Weight-based sales, inventory & Google Drive backup"

    // Products Screen (formerly Profile)
    val productsCatalogTitle: String get() = if (isHindi) "उत्पाद सूची" else "Products Catalog"
    val productsSubtitle: String get() = if (isHindi) "वजन आधार (₹/किग्रा) और इन्वेंट्री स्टॉक प्रबंधन" else "Weight-basis pricing (₹/kg) & stock management"
    val addProductButton: String get() = if (isHindi) "नया उत्पाद जोड़ें" else "Add Product"
    val editProductTitle: String get() = if (isHindi) "उत्पाद संपादित करें" else "Edit Product"
    val deleteProductTitle: String get() = if (isHindi) "उत्पाद हटाएं" else "Delete Product"
    val priceBasisWeightOnly: String get() = if (isHindi) "मूल्य आधार: केवल वजन" else "Price basis: Weight only"
    val liveCalculatorTitle: String get() = if (isHindi) "वजन-आधारित लाइव मूल्य कैलकुलेटर" else "Weight-Based Live Price Calculator"
    val liveCalculatorSub: String get() = if (isHindi) "पीस और वजन दर्ज करके स्वचालित ₹ मूल्य और इन्वेंट्री कटौती देखें" else "Type pieces & weight to calculate price and preview inventory deduction"
    val selectProduct: String get() = if (isHindi) "उत्पाद चुनें" else "Select Product"
    val pcsInStock: String get() = if (isHindi) "पीस स्टॉक में" else "pcs in stock"
    val kgInStock: String get() = if (isHindi) "किग्रा स्टॉक में" else "kg in stock"
    val categoryLabel: String get() = if (isHindi) "श्रेणी" else "Category"
    val formulaLabel: String get() = if (isHindi) "सूत्र" else "Formula"
    val autoPriceLabel: String get() = if (isHindi) "स्वचालित कुल मूल्य" else "Auto Total Price"

    // Daily Batches Screen
    val dailyBatchesTitle: String get() = if (isHindi) "दैनिक इन्वेंट्री बैच" else "Daily Inventory Batches"
    val dailyBatchesSubtitle: String get() = if (isHindi) "प्रतिदिन वजन, पीस और गूगल ड्राइव सिंकिंग ट्रैक करें" else "Track daily weight, pieces & Google Drive syncing"
    val recordBatchButton: String get() = if (isHindi) "दैनिक बैच दर्ज करें" else "Record Daily Batch"
    val totalWeightKpi: String get() = if (isHindi) "कुल वजन" else "Total Weight"
    val totalPiecesKpi: String get() = if (isHindi) "कुल पीस" else "Total Pieces"
    val totalSalesKpi: String get() = if (isHindi) "कुल बिक्री" else "Total Sales"
    val transactionsKpi: String get() = if (isHindi) "लॉग प्रविष्टियां" else "Log Entries"

    // Sales Screen
    val salesTitle: String get() = if (isHindi) "बिक्री और प्रेषण" else "Sales & Dispatches"
    val salesSubtitle: String get() = if (isHindi) "स्वचालित वजन-आधारित मूल्य (₹) और इन्वेंट्री कटौती" else "Automatic weight-based pricing in Rupees (₹) & inventory deduction"
    val recordSaleButton: String get() = if (isHindi) "नई बिक्री दर्ज करें" else "Record New Sale"
    val weightPriceFormula: String get() = if (isHindi) "वजन × ₹/किग्रा गणना" else "Weight × ₹/kg calculation"
    val clientNameLabel: String get() = if (isHindi) "ग्राहक / पार्टी का नाम" else "Client / Customer Name"
    val itemCommodityLabel: String get() = if (isHindi) "वस्तु / माल" else "Item / Commodity"
    val pricePerKgLabel: String get() = if (isHindi) "प्रति किग्रा मूल्य (₹/किग्रा)" else "Price per kg (₹ / kg)"
    val piecesLabel: String get() = if (isHindi) "पीस" else "Pieces"
    val weightLabel: String get() = if (isHindi) "वजन (किग्रा)" else "Weight (kg)"
    val dateLabel: String get() = if (isHindi) "तारीख (YYYY-MM-DD)" else "Date (YYYY-MM-DD)"
    val updateSaleButton: String get() = if (isHindi) "बिक्री अपडेट करें" else "Update Sale"
    val deleteSaleButton: String get() = if (isHindi) "बिक्री हटाएं" else "Delete Sale"

    // Clients Screen
    val clientsTitle: String get() = if (isHindi) "ग्राहक और सीआरएम" else "Customers & CRM"
    val clientsSubtitle: String get() = if (isHindi) "ग्राहक प्रोफाइल, फोन, पता और बिक्री इतिहास ट्रैक करें" else "Track customer profiles, phone numbers, and full sales history"
    val addCustomerButton: String get() = if (isHindi) "नया ग्राहक जोड़ें" else "Add Customer"
    val totalSpentLabel: String get() = if (isHindi) "कुल खर्च" else "Total Spent"
    val purchasedLabel: String get() = if (isHindi) "खरीदा गया" else "purchased"
    val salesHistoryLabel: String get() = if (isHindi) "बिक्री इतिहास" else "Sales History"
    val deleteClientButton: String get() = if (isHindi) "ग्राहक हटाएं" else "Delete Client"

    // Reports Screen
    val reportsTitle: String get() = if (isHindi) "विजुअल एनालिटिक्स और रिपोर्ट्स" else "Visual Reports & Analytics"
    val reportsSubtitle: String get() = if (isHindi) "दैनिक रुझान, वजन मेट्रिक्स और CSV शेयरिंग" else "Daily trends, weight metrics & CSV export"
    val shareCsvButton: String get() = if (isHindi) "CSV रिपोर्ट शेयर करें" else "Share CSV Report"
    val presetToday: String get() = if (isHindi) "आज" else "Today"
    val preset7Days: String get() = if (isHindi) "पिछले 7 दिन" else "Last 7 Days"
    val preset30Days: String get() = if (isHindi) "पिछले 30 दिन" else "Last 30 Days"
    val presetMonth: String get() = if (isHindi) "इस महीने" else "This Month"
    val presetAllTime: String get() = if (isHindi) "सभी समय" else "All Time"
}

val LocalAppStrings = compositionLocalOf { AppStrings(AppLanguage.ENGLISH) }
val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }
