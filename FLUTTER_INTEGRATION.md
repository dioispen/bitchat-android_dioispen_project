# Flutter 與原生 App 混合（只共用 UI）整合指南

此專案目前以 **Android 原生（Kotlin/Compose + Foreground Service + BLE mesh）** 為核心。若要 **跨 iOS** 且 **最快上線**，建議採用「**只共用 Flutter UI**」：

- Flutter：只負責 UI 與互動（聊天室畫面、列表、設定等）
- Android / iOS：各自保留原生 mesh / BLE / 背景行為
- Flutter 與 Native：透過 **MethodChannel / EventChannel** 傳指令與事件

## 你將得到什麼

- `flutter_ui/`：Flutter UI（含一個可跑的聊天室 Demo 畫面）
- `scripts/flutter/create_module.ps1`：一鍵在本 repo 內建立 Flutter module（需要你先安裝 Flutter SDK）

> 注意：此 repo 的自動化腳本會 **建立 Flutter module 的平台資料夾（.android/.ios）**。沒有安裝 Flutter SDK 時，無法產生這些檔案，也無法把 Flutter 正式掛進 Android Gradle build。

## 前置需求

- 安裝 Flutter SDK（Windows）
- 確認 `flutter` 指令可用：

```bash
flutter --version
```

## 產生 Flutter module（在此 repo 內）

在專案根目錄執行：

```powershell
.\scripts\flutter\create_module.ps1
```

腳本會：

- 若 `flutter_ui/` 不存在，建立資料夾
- 執行 `flutter create -t module flutter_ui`
- 以 `flutter_ui/lib/` 內的 UI + Channel 骨架做為起始內容（若已存在則保留）

## Android 端接入（下一步）

當 module 生成完成後，Android 端會做：

- `settings.gradle.kts` include `:flutter_ui`
- `app/build.gradle.kts` 加入 `implementation(project(":flutter_ui"))`
- 新增一個入口 Activity（例如 `FlutterChatActivity`）啟動 Flutter UI
- 在 Android 註冊 `MethodChannel`/`EventChannel`，把 mesh 事件推給 Flutter

（我可以在你跑完 `create_module.ps1` 後，直接把 Android 端修改一次到位，並確保 Gradle sync/compile 過。）

