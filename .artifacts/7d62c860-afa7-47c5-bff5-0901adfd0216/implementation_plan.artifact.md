# Implementation Plan - Comprehensive Hindi Localization System

This plan outlines the steps to make the entire app fully localized in Hindi and English using the existing `AppStrings` and `CompositionLocalProvider` system, eliminating all hardcoded English strings across dialogs and screens.

## User Review Required

> [!IMPORTANT]
> Every dialog, bottom sheet, screen title, button, label, and placeholder will be mapped to `AppStrings` so that switching language in Settings instantly translates the entire app between English and Local Hindi.

## Proposed Changes

### [Localization & Strings]
#### [MODIFY] [Localization.kt](file:///C:/Users/IGNITE/StudioProjects/Bahikhata/app/src/main/java/com/example/ui/util/Localization.kt)
- Add missing translatable strings for all dialogs, forms, buttons, confirmations, and placeholders in both English and natural Local Hindi (`isHindi`).

### [Dialogs & Components]
#### [MODIFY] [EditSaleDialog.kt](file:///C:/Users/IGNITE/StudioProjects/Bahikhata/app/src/main/java/com/example/ui/components/EditSaleDialog.kt)
- Replace all hardcoded English text (labels, buttons, titles) with `strings.xxx`.

#### [MODIFY] [GoogleSignInDialog.kt](file:///C:/Users/IGNITE/StudioProjects/Bahikhata/app/src/main/java/com/example/ui/components/GoogleSignInDialog.kt)
- Localize backup buttons and dialog texts using `strings.xxx`.

#### [MODIFY] [SettingsDialog.kt](file:///C:/Users/IGNITE/StudioProjects/Bahikhata/app/src/main/java/com/example/ui/components/SettingsDialog.kt)
- Localize Google Drive sync headers, cloud folders, CSV export, clear data buttons using `strings.xxx`.

### [Screens]
#### [MODIFY] [CatalogScreen.kt](file:///C:/Users/IGNITE/StudioProjects/Bahikhata/app/src/main/java/com/example/ui/screens/CatalogScreen.kt)
- Localize product management dialogs, forms, labels, and placeholders using `strings.xxx`.

#### [MODIFY] [CustomersScreen.kt](file:///C:/Users/IGNITE/StudioProjects/Bahikhata/app/src/main/java/com/example/ui/screens/CustomersScreen.kt)
- Localize client profile dialogs, payment history dialogs, and deletion prompts using `strings.xxx`.

#### [MODIFY] [DashboardDailyScreen.kt](file:///C:/Users/IGNITE/StudioProjects/Bahikhata/app/src/main/java/com/example/ui/screens/DashboardDailyScreen.kt)
- Localize date sales list, filtering search bar, record fish sale dialog, and delete confirmation dialog using `strings.xxx`.

#### [MODIFY] [ReportsScreen.kt](file:///C:/Users/IGNITE/StudioProjects/Bahikhata/app/src/main/java/com/example/ui/screens/ReportsScreen.kt)
- Localize report filters and clear buttons using `strings.xxx`.

#### [MODIFY] [SalesScreen.kt](file:///C:/Users/IGNITE/StudioProjects/Bahikhata/app/src/main/java/com/example/ui/screens/SalesScreen.kt)
- Localize new sale dialog, customer selection, weight/pieces labels, and confirmation buttons using `strings.xxx`.

## Verification Plan

### Automated Tests
- Run Gradle unit tests and check that the app builds successfully:
  `gradle_build("app:assembleDebug")`

### Manual Verification
- Deploy/run the app, open Settings, switch language between English and Hindi, and verify that every screen, dialog, button, and label translates completely into natural local Hindi.
