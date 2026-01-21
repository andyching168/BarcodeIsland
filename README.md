# BarcodeIsland

可在 HyperOS 的「超級島」上顯示條碼的 Android 應用程式，供發票載具或商店會員條碼顯示使用。

## 功能特色

### 📱 卡牌管理
- **新增卡片** - 輸入名稱、條碼內容、選擇顏色和條碼類型
- **編輯卡片** - 長按卡片可編輯資訊
- **刪除卡片** - 長按卡片選擇刪除，會跳出確認對話框
- **防重複** - 同一內容的條碼無法重複新增

### 📊 條碼類型
| 類型 | 說明 |
|------|------|
| Code-39 | 財政部發票載具 |
| Code-128 | 7-11、全家等會員條碼 |

### 🎨 卡片顏色
- 10 種預設顏色可選
- 通知背景會顯示卡片指定的顏色

### 🔔 HyperIsland 通知
- 點擊卡片即可在 HyperIsland 顯示條碼通知
- 通知標題顯示「卡片名稱 - 條碼內容」
- 通知背景為卡片指定的顏色
- 右上角顯示條碼類型（Code-39 / Code-128）
- 支援長截圖顯示條碼圖片

### ⚡ 快速操作
- **啟用全部** - 按鈕可一次顯示所有卡片的通知
- **關閉全部** - 右上角 X 按鈕可關閉所有島嶼

## 技術架構

### 技術栈
- **Kotlin 2.2.0**
- **Jetpack Compose** - UI 框架
- **HyperIsland Kit 0.4.3** - 超級島通知支援
- **ZXing** - 條碼生成
- **SharedPreferences + JSON** - 資料儲存（因 Kotlin 版本相容性）

### 專案結構
```
app/src/main/java/com/andyching168/barcodeisland/
├── MainActivity.kt           # 主畫面與 UI 邏輯
├── BarcodeGenerator.kt       # 條碼生成器（Code-39 / Code-128）
├── NotificationManager.kt    # HyperIsland 通知管理
└── data/
    ├── CardData.kt           # 卡片資料模型
    └── CardPreferencesManager.kt  # 資料 CRUD 與 Flow
```

## 安裝與建置

### 需求
- Android Studio
- Android SDK 26+
- Kotlin 2.2.0
- Gradle 8.x

### 建置指令
```bash
./gradlew assembleDebug
```

APK 位置：`app/build/outputs/apk/debug/app-debug.apk`

## 版本歷史

### v1.0.0
- 初始版本
- 卡牌 CRUD 功能
- Code-39 / Code-128 條碼支援
- HyperIsland 通知顯示
- 卡片顏色自定義

## 授權

Apache License 2.0
