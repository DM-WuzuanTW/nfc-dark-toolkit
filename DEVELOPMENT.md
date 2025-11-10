# 🔧 開發指南

## 專案狀態

### ✅ 已完成 (M0 階段)

#### 1. 專案基礎架構
- [x] Gradle 配置
- [x] Package 結構建立
- [x] Hilt 依賴注入設定
- [x] Navigation Component 配置
- [x] Room Database 設定
- [x] DataStore 設定

#### 2. 深色主題資源
- [x] `colors.xml` - 完整色彩定義
- [x] `themes.xml` - Material Design 3 主題
- [x] `strings.xml` - 繁體中文字串資源
- [x] `dimens.xml` - 尺寸規範

#### 3. NFC 核心類別
- [x] `NfcManager` - NFC 管理器
- [x] `NdefReader` - NDEF 讀取器
- [x] `NdefWriter` - NDEF 寫入器
- [x] `HceService` - HCE 服務
- [x] `TagOperations` - 標籤操作

#### 4. Data 層
- [x] `AppDatabase` - Room 資料庫
- [x] `HistoryEntity` - 歷史記錄實體
- [x] `HistoryDao` - DAO 介面
- [x] `HistoryRepository` - Repository
- [x] `SettingsDataStore` - 設定存儲
- [x] `SettingsRepository` - 設定 Repository

#### 5. Domain 層
- [x] `TagInfo` - 標籤資訊模型
- [x] `NdefRecordData` - NDEF 記錄模型
- [x] `NdefContent` - NDEF 內容模型
- [x] `HistoryRecord` - 歷史記錄模型
- [x] `HceConfig` - HCE 配置模型

#### 6. UI 層
- [x] `MainActivity` - 主 Activity
- [x] `ScanFragment` - 掃描頁面（功能完整）
- [x] `ScanViewModel` - 掃描 ViewModel
- [x] `WriteFragment` - 寫入頁面（UI 框架）
- [x] `EmulateFragment` - 模擬頁面（UI 框架）
- [x] `HistoryFragment` - 歷史頁面（UI 框架）
- [x] `SettingsFragment` - 設定頁面（UI 框架）

#### 7. Manifest 配置
- [x] NFC 權限設定
- [x] HCE Service 註冊
- [x] NFC Intent Filter
- [x] Tech Filter 配置
- [x] APDU Service 配置

### 🔄 下一步開發重點 (M1 階段)

#### 1. 寫入功能完善
```kotlin
// 需要在 WriteFragment 實作：
- [ ] 資料類型選擇 UI
- [ ] 文字輸入介面
- [ ] URL 輸入介面
- [ ] WriteViewModel 實作
- [ ] 與 NdefWriter 整合
- [ ] 寫入狀態反饋
```

#### 2. 完善掃描功能
```kotlin
- [ ] 加入更多標籤類型支援
- [ ] 優化 NDEF 資料顯示
- [ ] 加入資料複製功能
- [ ] 加入分享功能
```

#### 3. UI/UX 優化
```kotlin
- [ ] 加入載入動畫
- [ ] 優化錯誤提示
- [ ] 加入空狀態頁面
- [ ] 優化佈局適配
```

### 📋 M2 階段規劃

#### 1. 歷史記錄功能
- [ ] `HistoryViewModel` 實作
- [ ] RecyclerView Adapter
- [ ] 搜尋功能
- [ ] 篩選功能
- [ ] 詳細頁面

#### 2. HCE 模擬功能
- [ ] `EmulateViewModel` 實作
- [ ] AID 輸入介面
- [ ] APDU 測試工具
- [ ] 模擬狀態管理

#### 3. 進階寫入功能
- [ ] Wi-Fi 設定寫入
- [ ] vCard 名片寫入
- [ ] JSON 資料寫入

### 🎯 M3 階段規劃

#### 1. 標籤進階操作
- [ ] 格式化功能
- [ ] 鎖定功能
- [ ] 複製功能

#### 2. 設定功能
- [ ] `SettingsViewModel` 實作
- [ ] 主題切換
- [ ] 偏好設定
- [ ] 關於頁面

#### 3. 匯出功能
- [ ] JSON 匯出
- [ ] CSV 匯出
- [ ] 分享功能

## 🏗️ 架構說明

### MVVM 架構

```
┌─────────────┐
│     View    │  (Fragment + XML Layout)
│  (UI Layer) │
└──────┬──────┘
       │ observes
       ▼
┌─────────────┐
│  ViewModel  │  (業務邏輯 + UI 狀態)
└──────┬──────┘
       │ calls
       ▼
┌─────────────┐
│  Repository │  (資料來源抽象)
└──────┬──────┘
       │ uses
       ▼
┌─────────────┐
│  Data Layer │  (Room + DataStore + NFC)
└─────────────┘
```

### 資料流向

```
NFC Tag → NfcManager → NdefReader → ViewModel → UI
                          ↓
                    HistoryRepository
                          ↓
                      Room Database
```

## 🧪 測試建議

### 單元測試
```kotlin
// 應該測試的類別：
- NfcManager.parseTagInfo()
- NdefReader.parseNdefRecord()
- NdefWriter.createTextRecord()
- HistoryRepository CRUD 操作
```

### 整合測試
```kotlin
// 應該測試的流程：
- 標籤掃描到資料庫儲存
- 資料寫入到標籤
- HCE 模擬流程
```

### UI 測試
```kotlin
// 應該測試的場景：
- 導航流程
- NFC 狀態提示
- 錯誤處理
```

## 🎨 UI 設計規範

### 顏色使用

```kotlin
// 主要顏色
color_bg         (#121212) - 背景色
color_surface    (#1E1E1E) - 卡片背景
color_primary    (#BB86FC) - 主色（按鈕、強調）
color_secondary  (#03DAC5) - 次要色（連結、提示）

// 文字顏色
text_primary     (#FFFFFF) - 主要文字
text_secondary   (#B0B0B0) - 次要文字
text_disabled    (#757575) - 禁用文字

// 狀態顏色
color_success    (#4CAF50) - 成功
color_warning    (#FF9800) - 警告
color_error      (#CF6679) - 錯誤
color_info       (#2196F3) - 資訊
```

### 間距規範

```kotlin
spacing_tiny     4dp
spacing_small    8dp
spacing_medium   16dp
spacing_large    24dp
spacing_xlarge   32dp
```

### 元件規範

```kotlin
// 卡片
corner_radius: 12dp
elevation: 4dp
padding: 16dp

// 按鈕
height: 48dp
corner_radius: 8dp
padding_horizontal: 24dp
```

## 📝 Commit 規範

```
feat: 新功能
fix: 修復 bug
docs: 文件更新
style: 程式碼格式調整
refactor: 重構
test: 測試相關
chore: 建置工具或輔助工具變動
```

範例：
```
feat: 實作 NFC 標籤掃描功能
fix: 修正標籤 ID 顯示格式錯誤
docs: 更新 README 使用說明
```

## 🐛 已知問題

1. **HCE Service AID**
   - 目前使用固定 AID，需要支援動態配置

2. **標籤類型判斷**
   - NTAG 和 Mifare Ultralight 判斷需要優化

3. **錯誤處理**
   - 需要更完善的錯誤訊息和恢復機制

## 📚 參考資源

- [Android NFC 開發指南](https://developer.android.com/guide/topics/connectivity/nfc)
- [Material Design 3](https://m3.material.io/)
- [Hilt 依賴注入](https://developer.android.com/training/dependency-injection/hilt-android)
- [Room 持久化庫](https://developer.android.com/training/data-storage/room)

## 🤝 開發建議

1. **保持程式碼整潔**
   - 遵循 Kotlin 編碼規範
   - 適當的註解和文件
   - 避免過長的函數

2. **錯誤處理**
   - 使用 Result 類型包裝結果
   - 提供清晰的錯誤訊息
   - 記錄重要的錯誤日誌

3. **效能優化**
   - 避免在主執行緒執行 NFC 操作
   - 使用協程處理非同步操作
   - 適當使用快取

4. **使用者體驗**
   - 提供即時反饋
   - 優雅的載入狀態
   - 清晰的錯誤提示

---

**更新日期**: 2025-11-10
**版本**: M0 完成，準備進入 M1 階段

