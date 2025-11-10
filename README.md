# 📱 NFC Dark Toolkit

一款功能完整的 Android NFC 工具應用程式，採用深色主題設計，支援 NFC 標籤讀取、寫入、模擬等多種功能。

## ✨ 功能特色

### 已實作功能 (M0 階段)

- ✅ **完整的專案架構**
  - MVVM + Repository + UseCase 架構
  - Hilt 依賴注入
  - Room 本地資料庫
  - DataStore 設定儲存
  - Navigation Component 導航

- ✅ **深色主題 UI**
  - Material Design 3 規範
  - 完整的深色配色方案
  - 支援系統主題切換
  - 現代化的 UI 元件

- ✅ **NFC 基礎功能**
  - NFC 狀態偵測與提示
  - 標籤自動偵測
  - 標籤資訊解析
  - NDEF 資料讀取
  - 支援多種標籤類型（Mifare、NTAG 等）

- ✅ **底部導航**
  - 掃描頁面
  - 寫入頁面
  - 模擬頁面
  - 歷史記錄頁面
  - 設定頁面

### 開發中功能

- 🔄 **寫入功能** (M1)
  - 文字寫入
  - URL 寫入
  - Wi-Fi 設定寫入
  - vCard 名片寫入
  - JSON 資料寫入

- 🔄 **標籤操作** (M2/M3)
  - 標籤格式化
  - 標籤鎖定
  - 標籤複製

- 🔄 **HCE 模擬** (M2)
  - 自訂 AID
  - APDU 指令處理
  - 虛擬卡片模擬

- 🔄 **歷史記錄** (M2)
  - 操作記錄儲存
  - 記錄查詢與篩選
  - 記錄匯出 (JSON/CSV)

## 🛠️ 技術棧

- **語言**: Kotlin
- **最低版本**: Android 8.0 (API 26)
- **目標版本**: Android 14 (API 34)
- **架構**: MVVM + Clean Architecture
- **依賴注入**: Hilt
- **資料庫**: Room
- **非同步**: Coroutines + Flow
- **UI**: XML + ViewBinding
- **導航**: Navigation Component

## 📦 主要依賴

```kotlin
// Hilt
implementation("com.google.dagger:hilt-android:2.48")

// Room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")

// Navigation
implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

// DataStore
implementation("androidx.datastore:datastore-preferences:1.0.0")

// Material Design
implementation("com.google.android.material:material:1.11.0")
```

## 🏗️ 專案結構

```
app/
├── java/com/wuzuan/nfcdarktoolkit/
│   ├── MainActivity.kt                 # 主 Activity
│   ├── NfcApp.kt                      # Application 類
│   ├── nfc/                           # NFC 核心功能
│   │   ├── NfcManager.kt             # NFC 管理器
│   │   ├── NdefReader.kt             # NDEF 讀取器
│   │   ├── NdefWriter.kt             # NDEF 寫入器
│   │   ├── HceService.kt             # HCE 服務
│   │   └── TagOperations.kt          # 標籤操作
│   ├── ui/                            # UI 層
│   │   ├── home/                     # 掃描頁面
│   │   ├── write/                    # 寫入頁面
│   │   ├── emulate/                  # 模擬頁面
│   │   ├── history/                  # 歷史記錄頁面
│   │   └── settings/                 # 設定頁面
│   ├── data/                          # 資料層
│   │   ├── local/db/                 # 本地資料庫
│   │   ├── local/prefs/              # 偏好設定
│   │   └── repository/               # Repository
│   ├── domain/                        # Domain 層
│   │   └── model/                    # 資料模型
│   └── di/                            # 依賴注入模組
└── res/                               # 資源文件
    ├── layout/                       # 佈局文件
    ├── values/                       # 值資源
    │   ├── colors.xml               # 顏色定義
    │   ├── themes.xml               # 主題定義
    │   ├── strings.xml              # 字串資源
    │   └── dimens.xml               # 尺寸定義
    ├── navigation/                   # 導航圖
    └── xml/                          # XML 配置
```

## 🚀 開始使用

### 環境要求

- Android Studio Koala (2023.3.1) 或更高版本
- JDK 17
- Android SDK (API 26-34)
- 支援 NFC 的 Android 裝置

### 建置步驟

1. **克隆專案**
```bash
git clone https://github.com/WuzuanTW/nfc-dark-toolkit.git
cd nfc-dark-toolkit
```

2. **用 Android Studio 開啟專案**

3. **同步 Gradle**
   - 點擊 `File > Sync Project with Gradle Files`

4. **建置並執行**
   - 連接支援 NFC 的 Android 裝置
   - 點擊 `Run > Run 'app'`

## 📱 使用說明

### 掃描 NFC 標籤

1. 開啟應用程式
2. 確保裝置已開啟 NFC
3. 點擊底部「掃描」圖示
4. 將手機靠近 NFC 標籤
5. 查看標籤詳細資訊

### 寫入資料到標籤

1. 點擊底部「寫入」圖示
2. 選擇要寫入的資料類型
3. 輸入資料內容
4. 將手機靠近標籤進行寫入

### 查看歷史記錄

1. 點擊底部「歷史」圖示
2. 瀏覽過去的操作記錄
3. 可進行搜尋和篩選

## 🎨 深色主題設計

本應用程式採用 Material Design 3 深色主題規範：

- **背景色**: `#121212` (省電且舒適)
- **主色**: `#BB86FC` (紫色)
- **次要色**: `#03DAC5` (青色)
- **卡片背景**: `#1E1E1E`
- **圓角**: 12dp
- **間距**: 遵循 8dp 網格系統

## ⚠️ 法律聲明

本應用程式僅供**合法用途**使用。

**禁止行為**：
- ❌ 複製未經授權的門禁卡
- ❌ 模擬金融卡或信用卡
- ❌ 複製交通卡
- ❌ 任何非法的 NFC 卡片複製行為

使用者需自行承擔所有法律責任。

## 📄 授權

本專案採用 MIT 授權條款 - 詳見 [LICENSE](LICENSE) 文件

## 👨‍💻 作者

**WuzuanTW**

## 🤝 貢獻

歡迎提交 Issue 和 Pull Request！

## 📝 更新日誌

### v1.0.0 (開發中)

- ✅ 完成專案基礎架構
- ✅ 實作深色主題 UI
- ✅ 實作 NFC 標籤掃描功能
- ✅ 實作底部導航
- 🔄 開發中：寫入功能
- 🔄 開發中：HCE 模擬
- 🔄 開發中：歷史記錄管理

## 📞 聯絡方式

如有任何問題或建議，歡迎透過 GitHub Issues 聯繫。

---

**Made with ❤️ by WuzuanTW**

