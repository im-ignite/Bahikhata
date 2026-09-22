package com.example.ui.util

import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी")
}

class AppStrings(val language: AppLanguage) {
    val isHindi: Boolean get() = language == AppLanguage.HINDI

    // App Header & Branding
    val appTitle: String get() = if (isHindi) "राय फिश (RAI FISH)" else "RAI FISH"
    val syncStatusSyncing: String get() = if (isHindi) "सिंकिंग..." else "Syncing"
    val syncStatusDriveSynced: String get() = if (isHindi) "ड्राइव सिंक" else "Drive Synced"
    val syncStatusOffline: String get() = if (isHindi) "ऑफ़लाइन" else "Offline"
    val settingsButton: String get() = if (isHindi) "सेटिंग्स" else "Settings"
    val darkModeToggle: String get() = if (isHindi) "डार्क मोड बदलें" else "Toggle Dark Mode"
    val notificationAlerts: String get() = if (isHindi) "सूचना अलर्ट" else "Notification Alerts"

    // Navigation Tabs (Date Sales, New Sale, Fish Types, Reports, Clients)
    val navDaily: String get() = if (isHindi) "तारीख बिक्री" else "Date Sales"
    val navSales: String get() = if (isHindi) "बिक्री दर्ज" else "New Sale"
    val navProducts: String get() = if (isHindi) "मछली किस्में" else "Fish Types"
    val navReports: String get() = if (isHindi) "बिक्री रिपोर्ट" else "Sales Report"
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
    val appInfoTitle: String get() = if (isHindi) "RAI FISH ऐप जानकारी" else "RAI FISH App Info"
    val appInfoSubtitle: String get() = if (isHindi) "तालाब से सीधे ग्राहकों को मछली बिक्री, तारीखवार ग्राहक रिकॉर्ड व रिपोर्ट" else "Direct pond-to-client fish sales, date-wise client records & reports"
    val clearAllDataTitle: String get() = if (isHindi) "सभी डेटा साफ़ करें (Clean App)" else "Clear All Data (Clean App)"
    val clearAllDataSub: String get() = if (isHindi) "सभी बिक्री, ग्राहक और मछली रिकॉर्ड मिटाकर ऐप को पूरी तरह खाली करें" else "Erase all sales, clients & fish records to start completely fresh"
    val clearAllDataConfirmTitle: String get() = if (isHindi) "क्या आप सारा डेटा मिटाना चाहते हैं?" else "Erase All Data?"
    val clearAllDataConfirmMsg: String get() = if (isHindi) "यह सभी बिक्री रिकॉर्ड, ग्राहक और मछली डेटा हटा देगा। नया रिकॉर्ड दर्ज करने के लिए ऐप बिल्कुल तैयार हो जाएगी।" else "This will permanently delete all sales, clients, and fish records. The app will be ready for fresh entry."
    val clearAllDataSuccess: String get() = if (isHindi) "सभी डेटा साफ़ कर दिया गया है। ऐप पूरी तरह तैयार है।" else "All data cleared. App is ready for fresh use."

    // Fish Types / Products Screen
    val productsCatalogTitle: String get() = if (isHindi) "मछली किस्में और दरें" else "Fish Species & Rates"
    val productsSubtitle: String get() = if (isHindi) "तालाब मछली की किस्में और प्रति किग्रा दर (₹/किग्रा)" else "Pond fish species & rate per kg (₹/kg)"
    val addProductButton: String get() = if (isHindi) "नई मछली किस्म जोड़ें" else "Add Fish Species"
    val editProductTitle: String get() = if (isHindi) "मछली दर संपादित करें" else "Edit Fish Rate"
    val deleteProductTitle: String get() = if (isHindi) "मछली हटाएं" else "Delete Fish"
    val priceBasisWeightOnly: String get() = if (isHindi) "दर आधार: केवल वजन (₹/किग्रा)" else "Price basis: Weight only (₹/kg)"
    val liveCalculatorTitle: String get() = if (isHindi) "तालाब वजन और लाइव मूल्य कैलकुलेटर" else "Pond Live Weight & Price Calculator"
    val liveCalculatorSub: String get() = if (isHindi) "मछली का वजन (किग्रा) और पीस डालकर तुरंत ग्राहक का ₹ बिल निकालें" else "Enter fish weight (kg) & pieces to instantly compute client's ₹ bill"
    val selectProduct: String get() = if (isHindi) "मछली चुनें" else "Select Fish"
    val pcsInStock: String get() = if (isHindi) "पीस" else "pieces"
    val kgInStock: String get() = if (isHindi) "किग्रा" else "kg"
    val categoryLabel: String get() = if (isHindi) "प्रकार" else "Category"
    val formulaLabel: String get() = if (isHindi) "सूत्र" else "Formula"
    val autoPriceLabel: String get() = if (isHindi) "स्वचालित कुल मूल्य" else "Auto Total Price"

    // Date-wise Sales Records Screen (formerly Daily Batches)
    val dailyBatchesTitle: String get() = if (isHindi) "तारीखवार मछली बिक्री रिकॉर्ड" else "Date-wise Fish Sales Records"
    val dailyBatchesSubtitle: String get() = if (isHindi) "किसी भी तारीख को किस ग्राहक को कितनी मछली बिकी देखें" else "Check which client bought how much fish on any chosen date"
    val recordBatchButton: String get() = if (isHindi) "नई बिक्री दर्ज करें" else "Record Fish Sale"
    val totalWeightKpi: String get() = if (isHindi) "कुल मछली वजन" else "Total Fish Sold"
    val totalPiecesKpi: String get() = if (isHindi) "कुल पीस" else "Total Pieces"
    val totalSalesKpi: String get() = if (isHindi) "कुल बिक्री राशि" else "Total Sales"
    val transactionsKpi: String get() = if (isHindi) "ग्राहक बिक्री" else "Client Sales"
    val dateSelectorTitle: String get() = if (isHindi) "तारीख चुनें और बिक्री देखें" else "Select Date to View Sales"
    val todayButton: String get() = if (isHindi) "आज" else "Today"
    val dateSalesTitle: String get() = if (isHindi) "ग्राहकवार मछली बिक्री" else "Client Fish Sales"
    val daySalesAmountLabel: String get() = if (isHindi) "दिन की कुल बिक्री" else "Day's Total Sales"
    val dayFishWeightLabel: String get() = if (isHindi) "दिन का मछली वजन" else "Day's Fish Weight"
    val dayPiecesLabel: String get() = if (isHindi) "पीस" else "pieces"
    val dayClientsLabel: String get() = if (isHindi) "ग्राहक" else "clients"
    val recordFishSaleButton: String get() = if (isHindi) "+ मछली बिक्री दर्ज करें" else "+ Record Fish Sale"

    // Sales Screen
    val salesTitle: String get() = if (isHindi) "तालाब से सीधी बिक्री" else "Pond Fish Sales"
    val salesSubtitle: String get() = if (isHindi) "ग्राहक को बेची गई मछली का वजन (किग्रा) और स्वचालित ₹ बिल" else "Record fish sold to clients by weight in kg & auto bill in Rupees (₹)"
    val recordSaleButton: String get() = if (isHindi) "नई बिक्री दर्ज करें" else "Record Fish Sale"
    val weightPriceFormula: String get() = if (isHindi) "वजन × ₹/किग्रा = कुल बिल" else "Weight (kg) × ₹/kg = Total Bill"
    val clientNameLabel: String get() = if (isHindi) "ग्राहक / पार्टी का नाम" else "Client / Customer Name"
    val itemCommodityLabel: String get() = if (isHindi) "मछली की किस्म" else "Fish Variety"
    val pricePerKgLabel: String get() = if (isHindi) "प्रति किग्रा दर (₹/किग्रा)" else "Rate per kg (₹ / kg)"
    val piecesLabel: String get() = if (isHindi) "पीस" else "Pieces"
    val weightLabel: String get() = if (isHindi) "वजन (किग्रा)" else "Weight (kg)"
    val dateLabel: String get() = if (isHindi) "तारीख (YYYY-MM-DD)" else "Date (YYYY-MM-DD)"
    val updateSaleButton: String get() = if (isHindi) "बिक्री सुधारें" else "Update Sale"
    val deleteSaleButton: String get() = if (isHindi) "बिक्री हटाएं" else "Delete Sale"

    // Clients Screen
    val clientsTitle: String get() = if (isHindi) "ग्राहक डायरी और विवरण" else "Client Directory"
    val clientsSubtitle: String get() = if (isHindi) "ग्राहकों के फोन नंबर, पता और उनकी तारीखवार मछली खरीदारी" else "Track client details and complete date-wise fish purchases"
    val addCustomerButton: String get() = if (isHindi) "नया ग्राहक जोड़ें" else "Add New Client"
    val totalSpentLabel: String get() = if (isHindi) "कुल खरीदारी" else "Total Spent"
    val purchasedLabel: String get() = if (isHindi) "मछली खरीदी" else "purchased"
    val salesHistoryLabel: String get() = if (isHindi) "तारीखवार खरीदारी इतिहास" else "Purchase History"
    val deleteClientButton: String get() = if (isHindi) "ग्राहक हटाएं" else "Delete Client"

    // Reports Screen
    val reportsTitle: String get() = if (isHindi) "मछली बिक्री रिपोर्ट्स और विश्लेषण" else "Fish Sales Reports & Analytics"
    val reportsSubtitle: String get() = if (isHindi) "तारीख अनुसार किस ग्राहक को कितनी बिक्री हुई और ट्रेंड्स" else "Date-specific sales, client breakdown & performance"
    val shareCsvButton: String get() = if (isHindi) "CSV रिपोर्ट शेयर करें" else "Share CSV Report"
    val presetToday: String get() = if (isHindi) "आज" else "Today"
    val presetYesterday: String get() = if (isHindi) "कल" else "Yesterday"
    val preset7Days: String get() = if (isHindi) "पिछले 7 दिन" else "Last 7 Days"
    val preset30Days: String get() = if (isHindi) "पिछले 30 दिन" else "Last 30 Days"
    val presetMonth: String get() = if (isHindi) "इस महीने" else "This Month"
    val presetAllTime: String get() = if (isHindi) "सभी समय" else "All Time"

    // Specific Date Search Strings
    val searchSpecificDateTitle: String get() = if (isHindi) "विशिष्ट तारीख से खोजें" else "Search Specific Date"
    val searchSpecificDateSub: String get() = if (isHindi) "कोई भी तारीख चुनें और देखें उस दिन किस ग्राहक को कितनी बिक्री हुई" else "Choose any date to see exact sales and which clients bought fish"
    val selectDateLabel: String get() = if (isHindi) "तारीख चुनें" else "Select Date"
    val clientBreakdownTitle: String get() = if (isHindi) "उस दिन का ग्राहक अनुसार बिक्री विवरण" else "Client-wise Sales Breakdown for Date"
    val noSalesOnDateMsg: String get() = if (isHindi) "इस तारीख को कोई मछली बिक्री दर्ज नहीं हुई है।" else "No fish sales recorded for this date."
    val quickPickDate: String get() = if (isHindi) "तारीख बदलें" else "Change Date"

    // Google Sign-In & Cloud Sync
    val googleSignInTitle: String get() = if (isHindi) "Google खाते से साइन इन करें" else "Sign in with Google"
    val googleSignInSubtitle: String get() = if (isHindi) "RAI FISH में जारी रखने के लिए Google खाता चुनें" else "Choose a Google account to continue to RAI FISH"
    val googleSignInExplanation: String get() = if (isHindi) "अपने तालाब की मछली बिक्री, ग्राहक और दैनिक रिकॉर्ड को अपने सभी डिवाइस पर सुरक्षित रूप से सिंक करें" else "Sync fish pond sales, customers & records securely across all your Android devices"
    val chooseAccountPrompt: String get() = if (isHindi) "डिवाइस पर मौजूद Google खाते" else "Google Accounts on this Device"
    val chooseOtherAccount: String get() = if (isHindi) "अन्य Google खाता चुनें" else "Choose another Google Account"
    val continueOffline: String get() = if (isHindi) "ऑफ़लाइन जारी रखें" else "Continue Offline"
    val cloudSyncOnlineStatus: String get() = if (isHindi) "क्लाउड कनेक्टेड" else "Cloud Connected"
    val googleAccountHeader: String get() = if (isHindi) "Google खाता एवं क्लाउड सिंक" else "Google Account & Cloud Sync"
    val syncNowButton: String get() = if (isHindi) "अभी क्लाउड सिंक करें" else "Sync with Cloud Now"
    val autoSyncTitle: String get() = if (isHindi) "स्वचालित ऑनलाइन सिंक (Auto-Sync)" else "Automatic Online Sync"
    val autoSyncSubtitle: String get() = if (isHindi) "बिक्री दर्ज करते ही तुरंत Google क्लाउड में सुरक्षित हो जाएगी" else "Instantly stores sales and records to Google Cloud as you enter them"
    val switchAccount: String get() = if (isHindi) "खाता बदलें" else "Switch Account"
    val signOutButton: String get() = if (isHindi) "साइन आउट करें" else "Sign Out"
    val signedInAs: String get() = if (isHindi) "साइन इन खाता" else "Signed in as"
    val enterCustomGoogleEmail: String get() = if (isHindi) "Google ईमेल दर्ज करें" else "Enter Google Email"
    val quickSignInBtn: String get() = if (isHindi) "साइन इन करें" else "Sign In"

    // Hamburger Menu Drawer & Footer Strings
    val drawerHeaderTitle: String get() = if (isHindi) "राय फिश मेनू" else "RAI FISH Menu"
    val drawerHeaderTagline: String get() = if (isHindi) "तालाब मछली प्रबंधन व बिक्री" else "Pond Fish Sales & Management"
    val drawerNavSection: String get() = if (isHindi) "मुख्य नेविगेशन" else "Navigation"
    val drawerQuickControlsSection: String get() = if (isHindi) "क्विक कंट्रोल" else "Quick Controls"
    val drawerContactTitle: String get() = if (isHindi) "संपर्क एवं सहायता" else "Contact & Support"
    val drawerContactSub: String get() = if (isHindi) "मालिक / सहायता नंबर" else "Owner / Helpline"
    val drawerCallNow: String get() = if (isHindi) "कॉल करें" else "Call Now"
    val drawerContactPhone: String get() = "+91 9304565995"
    val drawerSupportEmail: String get() = "abhijeetanand1821@gmail.com"

}

val LocalAppStrings = compositionLocalOf { AppStrings(AppLanguage.ENGLISH) }
val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }
