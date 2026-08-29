# Firestore 資安檢查與修補報告

- **專案**：`disaster-app-b2370`（CARES Mesh 防災 App 的 Firebase 後端）
- **檢查範圍**：Firestore 資料庫的連線狀態與安全規則
- **日期**：2026-08-29
- **狀態**：規則已撰寫並通過編譯驗證（`firebase ... --dry-run` ✔），**尚未正式部署**（待團隊確認後套用）

---

## 一、發現的問題

### 1. 連線本身：正常（非問題）
先確認 App 確實有接上 Firebase，結果一切正常：

- `firebase_options.dart`、`main.dart` 的 `Firebase.initializeApp` 皆正確，執行時無初始化錯誤。
- Firebase Auth（Email/密碼）與 Cloud Firestore 皆實際運作。
- 六個 collection 都有真實資料：`users`、`supply_items`、`supply_requests`、`supply_pledges`、`health_reports`、`sos_requests`（含當日新寫入的 SOS 與健康回報，證明端到端讀寫有效）。

### 2. 【嚴重】Firestore 安全規則完全開放
在**完全未登入**、僅使用內嵌於前端、本就公開的 web API key 的情況下，測試可對正式資料庫執行 **讀取、寫入、刪除** 任意 collection（測試寫入的資料已即時刪除，未殘留）。

這相當於規則停在 `allow read, write: if true;`（Firebase「測試模式」），造成：

- **個資外洩**：任何人可撈取所有使用者的電話、Email、緊急聯絡人、GPS 座標、血型、病史、健康狀況（如「割傷」）。
- **資料可被竄改／刪除**：任何人可竄改或清空所有 SOS 求救、健康回報、物資需求等救災關鍵資料。

嚴重性：**高**。此為含真實個資（PII）與救災資料的正式資料庫，且與 README 自述「未經安全審查」一致，屬應立即處理等級。

> 說明：Firebase 的 web API key **本來就是公開的**（會內嵌在前端，靠安全規則而非金鑰保密來防護）。因此真正的防線是 Firestore 安全規則，而不是隱藏金鑰。

---

## 二、解決方式

以「**預設全部拒絕 → 逐一開放最小必要權限**」重寫 Firestore 安全規則，並先對照程式碼中每個 collection 的實際存取方式，確保修補後 App 不會壞。

### 釐清的關鍵事實（規則設計依據）
- **登入是強制的**：App 以 Firebase Auth（Email/密碼 + 信箱驗證）為登入機制，未驗證不得進入（`login_screen.dart`）。因此使用 SOS／健康回報／物資功能時，使用者**必定為已登入狀態** → 規則要求「需登入」是安全的，不會破壞功能。
- **身分一致**：`AppUser.id = Firebase uid`（`register_screen.dart`，model 註解亦載明）。故 `users` 的 doc id、以及各筆資料的 `userId` / `reporterId` 皆等於 uid，可用來判斷「是否為本人」。
- **救援者需跨用戶讀取**：`health_screen`、SOS 需讀取「別人的」回報 → 回報類 collection 開放「登入者皆可讀」，但**禁止竄改/刪除他人記錄**。
- **物資計數需可更新**：建立物資需求/認領時會連帶 `update` `supply_items` 的計數欄位（`totalRequestedQty` / `totalPledgedQty`），且有「首次種子」寫入 → `supply_items` 需允許登入者建立/更新，但禁止刪除。

### 規則邏輯摘要
| Collection | 讀 | 建立 | 更新 / 刪除 |
|---|---|---|---|
| `users/{uid}` | 僅本人 | 僅本人 | 僅本人 |
| `sos_requests` | 登入者 | 登入者 | 僅原記錄擁有者（`userId`）|
| `health_reports` | 登入者 | 登入者 | 僅回報者（`reporterId`）|
| `supply_requests` | 登入者 | 登入者 | 僅擁有者（`userId`）|
| `supply_pledges` | 登入者 | 登入者 | 僅擁有者（`userId`）|
| `supply_items` | 登入者 | 登入者 | 更新可、**刪除禁止** |
| 其他任何路徑 | 拒絕 | 拒絕 | 拒絕 |

**修補前後對比**：

```
修補前：  未登入即可 讀 / 寫 / 刪 全部資料
修補後：  未登入 → 完全無法存取
          已登入 → 只能讀救災所需資料、只能改自己的資料
```

---

## 三、目前更改了什麼（本次新增/修改的檔案）

均位於 `flutter_ui/`：

1. **`firestore.rules`**（新增）— 上述完整安全規則。**已通過 `firebase deploy --only firestore:rules --dry-run` 編譯驗證。**
2. **`firebase.json`**（修改）— 新增 `"firestore": { "rules": "firestore.rules" }` 區塊，讓 Firebase CLI 能部署規則（原有的 `flutter` 設定完整保留）。
3. **`.firebaserc`**（新增）— 指定預設專案 `disaster-app-b2370`，供 CLI 部署使用。
4. **`FIRESTORE_SECURITY_REPORT.md`**（新增）— 本報告。

> 註：以上為「設定與規則」層級的變更，**未更動任何 App 程式邏輯**，因此不影響現有功能行為。

---

## 四、如何套用（部署）

於 `flutter_ui/` 目錄執行：

```bash
firebase deploy --only firestore:rules
```

（本機已安裝 Firebase CLI 15.11.0 並已登入、具該專案權限；dry-run 已驗證通過。）

部署後建議做**煙霧測試**：登入 → 送出一筆 SOS → 送出健康回報 → 送出物資需求/認領 → 確認皆成功；同時再跑一次「未登入讀取」測試，應被拒絕（`PERMISSION_DENIED`）。

---

## 五、後續建議（非本次範圍）

1. **收斂讀取範圍**：目前救援類回報開放「所有登入者可讀」以符合現行救援流程；若未來要區分「救援者/受災者」角色，可加上角色欄位進一步限制。
2. **建立時綁定擁有者**：可在 create 時加驗 `request.resource.data.userId == request.auth.uid`，防止登入者假冒他人 id 建立記錄（本次為避免影響救災關鍵寫入而未強制，建議部署後測試再逐步收緊）。
3. **啟用 App Check**：進一步阻擋非官方 App 的請求。
4. **欄位/型別驗證**：於規則中限制各 collection 可寫入的欄位與型別，降低髒資料風險。
5. **`users` 寫入路徑一致化**：目前寫入用 `pendingUser.id`、讀取用 `user.uid`（兩者實際相等），建議統一為 `user.uid` 以免日後誤植。
