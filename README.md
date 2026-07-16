# Java Firmware Upgrade Test Automation

這是一個使用 **Java 8、Maven、JUnit 5** 製作的「韌體升級自動化測試專案」。

本專案不會真的把韌體燒錄到實體 Switch、AP 或開發板，而是先用 **Mock Device（模擬裝置）** 模擬韌體升級流程，讓初學者能在沒有硬體的情況下練習：

- Java 物件導向設計
- 韌體檔案驗證
- SHA-256 Checksum
- 裝置狀態切換
- Timeout 與 Retry
- Log 錯誤分析
- JUnit 自動化測試
- GitHub Actions 自動測試

---

## 一、這個專案到底在做什麼？

把它想像成一台假的網路設備，原本韌體版本是：

```text
1.0.0
```

我們準備一個新韌體：

```text
1.1.0
```

Java 程式會自動完成以下流程：

```text
讀取裝置目前版本
→ 檢查韌體檔案是否正確
→ 確認韌體是不是給這個型號使用
→ 檢查 SHA-256 Checksum
→ 上傳韌體
→ 執行升級
→ 模擬重新開機
→ 確認裝置重新上線
→ 確認版本變成 1.1.0
→ 確認 IP、VLAN、裝置名稱沒有遺失
→ 檢查 Log 有沒有 ERROR 或 BOOT FAILURE
→ 產生測試結果
```

如果其中任何一個步驟出錯，程式就會回傳失敗原因。

---

## 二、專案資料夾結構

```text
java-firmware-upgrade-test-automation
├── pom.xml
├── README.md
├── LICENSE
├── .gitignore
├── .github
│   └── workflows
│       └── maven.yml
└── src
    ├── main
    │   └── java
    │       └── com
    │           └── renkai
    │               └── firmware
    │                   ├── App.java
    │                   ├── device
    │                   │   ├── DeviceClient.java
    │                   │   ├── DeviceConfiguration.java
    │                   │   ├── DeviceState.java
    │                   │   └── MockDeviceClient.java
    │                   ├── firmware
    │                   │   ├── ChecksumUtil.java
    │                   │   ├── FirmwarePackage.java
    │                   │   └── FirmwareValidator.java
    │                   ├── report
    │                   │   └── MarkdownReportGenerator.java
    │                   ├── upgrade
    │                   │   ├── FirmwareUpgradeService.java
    │                   │   ├── UpgradeResult.java
    │                   │   └── UpgradeStatus.java
    │                   └── validation
    │                       ├── LogValidator.java
    │                       └── ValidationResult.java
    └── test
        └── java
            └── com
                └── renkai
                    └── firmware
                        └── FirmwareUpgradeServiceTest.java
```

---

# 三、每一支程式在做什麼？

## 1. `App.java`

這是專案的主程式，也是最適合初學者先看的檔案。

它會：

1. 建立一台假的裝置
2. 設定目前韌體版本為 `1.0.0`
3. 建立新的韌體 `1.1.0`
4. 計算韌體的 SHA-256 Checksum
5. 呼叫升級服務
6. 顯示升級結果
7. 產生 Markdown 報告

簡單說：

> `App.java` 是把整個專案串起來執行的入口。

---

## 2. `DeviceState.java`

這是一個 Enum，用來表示裝置目前的狀態。

包含：

```text
ONLINE      裝置在線
VALIDATING  正在驗證
UPGRADING   正在升級
REBOOTING   正在重新開機
FAILED      發生失敗
```

簡單說：

> 它像是裝置目前狀態的選單，避免直接使用容易打錯的文字。

---

## 3. `DeviceConfiguration.java`

用來保存裝置原本的設定：

- IP Address
- VLAN ID
- Device Name

例如：

```text
IP：192.168.1.10
VLAN：20
Device Name：Lab-Switch
```

韌體升級前後會比較這些設定，確認升級後沒有遺失。

簡單說：

> 它代表裝置目前的基本設定資料。

---

## 4. `DeviceClient.java`

這是一個 Interface，定義「裝置應該具備哪些功能」。

例如：

- 取得型號
- 取得韌體版本
- 取得裝置狀態
- 取得設定
- 上傳韌體
- 開始升級
- 判斷是否在線
- 取得 Log

它本身沒有真的執行升級，只是先規定方法名稱。

簡單說：

> 它像一份裝置操作規格書，規定所有裝置類別都必須實作哪些功能。

未來接上真實設備時，可以新增：

```text
SshDeviceClient
SerialDeviceClient
```

而不需要大幅修改其他程式。

---

## 5. `MockDeviceClient.java`

這是模擬裝置的核心程式。

因為目前沒有真正的 Switch、AP 或開發板，所以使用它模擬：

- 韌體上傳
- 韌體升級
- 裝置重新開機
- 版本更新
- Timeout
- Reboot Failure
- Log 產生

正常情況會依序切換：

```text
ONLINE → UPGRADING → REBOOTING → ONLINE
```

也能故意模擬失敗：

```java
device.setSimulateTimeout(true);
```

或：

```java
device.setSimulateRebootFailure(true);
```

簡單說：

> 它是一台用 Java 寫出來的假設備，讓測試不用依賴實體硬體。

---

## 6. `FirmwarePackage.java`

代表一個韌體檔案，保存：

- 檔名
- 適用型號
- 韌體版本
- 韌體內容
- 預期 Checksum

例如：

```text
檔名：SW-1000-v1.1.0.bin
型號：SW-1000
版本：1.1.0
```

簡單說：

> 它是用 Java 物件表示一個韌體安裝包。

---

## 7. `ChecksumUtil.java`

負責計算 SHA-256 Checksum。

Checksum 可以理解為檔案的「數位指紋」。

如果韌體檔案內容遭到修改、下載不完整或損壞，算出來的 Checksum 就會不同。

簡單說：

> 它用來確認韌體檔案是否完整、是否被修改。

---

## 8. `FirmwareValidator.java`

在升級之前檢查韌體是否合法。

目前會檢查：

1. 韌體物件不能是空的
2. 副檔名必須是 `.bin`
3. 韌體型號必須和裝置型號相同
4. SHA-256 Checksum 必須正確

任何一項失敗，就會停止升級。

簡單說：

> 它是韌體升級前的安全檢查員。

---

## 9. `ValidationResult.java`

用來保存一般驗證結果。

內容包含：

- 是否通過
- 驗證訊息

例如：

```text
passed = true
message = No critical error keyword found
```

簡單說：

> 它把驗證成功或失敗，以及原因一起包裝起來。

---

## 10. `LogValidator.java`

檢查裝置 Log 是否包含危險關鍵字。

目前會找：

```text
ERROR
FAILED
PANIC
CRITICAL
SEGMENTATION FAULT
BOOT FAILURE
```

只要找到其中一個，就判定 Log 驗證失敗。

簡單說：

> 它會自動查看升級過程有沒有嚴重錯誤。

---

## 11. `UpgradeStatus.java`

定義韌體升級可能產生的結果：

```text
PASSED                 升級成功
VALIDATION_FAILED      韌體驗證失敗
TIMEOUT                升級逾時
REBOOT_FAILED          重新開機失敗
VERSION_MISMATCH       版本不符合
CONFIGURATION_CHANGED  設定被改變
LOG_ERROR              Log 發現錯誤
```

簡單說：

> 它統一管理所有測試結果種類。

---

## 12. `UpgradeResult.java`

保存一次完整升級測試的結果。

包含：

- 升級結果狀態
- 舊版本
- 目標版本
- 嘗試次數
- 執行時間
- 結果訊息

例如：

```text
Status：PASSED
Old Version：1.0.0
Target Version：1.1.0
Attempts：1
Message：Firmware upgrade completed successfully
```

簡單說：

> 它就是一次韌體升級測試的成績單。

---

## 13. `FirmwareUpgradeService.java`

這是整個專案最重要的核心流程。

它負責：

1. 記錄升級前版本與設定
2. 驗證韌體
3. 上傳韌體
4. 開始升級
5. 執行 Retry
6. 判斷是否 Timeout
7. 確認裝置是否重新上線
8. 驗證新版本
9. 比較設定是否保留
10. 分析 Log
11. 回傳 UpgradeResult

簡單說：

> 它是整個韌體升級流程的總指揮。

---

## 14. `MarkdownReportGenerator.java`

把升級結果輸出成 Markdown 報告。

報告會產生在：

```text
target/reports/firmware-upgrade-report.md
```

內容包含：

- 測試結果
- 升級前版本
- 目標版本
- 嘗試次數
- 執行時間
- 結果訊息

簡單說：

> 它會把程式結果整理成方便閱讀的測試報告。

---

## 15. `FirmwareUpgradeServiceTest.java`

這是 JUnit 自動化測試程式。

目前包含五個測試案例：

### `shouldUpgradeFirmwareSuccessfully()`

測試正常韌體能不能成功升級。

### `shouldRejectCorruptedFirmware()`

故意提供錯誤 Checksum，確認系統會拒絕損壞的韌體。

### `shouldRejectFirmwareForDifferentModel()`

故意提供錯誤型號的韌體，確認系統不會把 AP 韌體裝到 Switch。

### `shouldReportTimeout()`

故意讓裝置一直停在升級狀態，確認系統能判定 Timeout。

### `shouldReportRebootFailure()`

故意模擬裝置重新開機失敗，確認系統能重試並回報失敗。

簡單說：

> 它會自動測試主程式在正常與錯誤情況下是否都能正確處理。

---

## 16. `pom.xml`

這是 Maven 專案的設定檔。

它負責：

- 指定 Java 8
- 加入 JUnit 5
- 設定編譯方式
- 設定測試執行方式

簡單說：

> 它告訴 Maven 這個專案需要哪些套件，以及要怎麼編譯。

---

## 17. `.github/workflows/maven.yml`

這是 GitHub Actions 設定檔。

每次把程式 Push 到 GitHub 時，GitHub 會自動：

```text
下載程式
→ 安裝 Java 8
→ 執行 mvn clean test
→ 顯示測試成功或失敗
```

簡單說：

> 它讓 GitHub 幫你自動執行測試。

---

## 18. `.gitignore`

告訴 Git 哪些檔案不要上傳，例如：

```text
target/
IDE 設定檔
Log 檔案
```

---

## 19. `LICENSE`

本專案使用 MIT License，表示其他人可以在授權條件下使用、修改與分享此專案。

---

# 四、第一次操作：從零開始

以下以 Windows 為例。

## 步驟 1：解壓縮專案

下載 ZIP 後，對 ZIP 按滑鼠右鍵：

```text
解壓縮全部
```

建議解壓縮到：

```text
C:\JavaProject\java-firmware-upgrade-test-automation
```

不要直接在 ZIP 裡面執行。

---

## 步驟 2：確認 Java 是否安裝

開啟 CMD，輸入：

```cmd
java -version
```

再輸入：

```cmd
javac -version
```

正常會看到類似：

```text
java version "1.8.0_xxx"
javac 1.8.0_xxx
```

若顯示不是內部或外部命令，表示 Java 尚未安裝或環境變數沒有設定。

---

## 步驟 3：確認 Maven 是否安裝

在 CMD 輸入：

```cmd
mvn -version
```

正常會顯示 Maven 與 Java 版本。

如果出現：

```text
'mvn' 不是內部或外部命令
```

表示 Maven 尚未安裝或環境變數尚未設定。

---

## 步驟 4：進入專案資料夾

假設專案在：

```text
C:\JavaProject\java-firmware-upgrade-test-automation
```

在 CMD 輸入：

```cmd
cd /d C:\JavaProject\java-firmware-upgrade-test-automation
```

接著輸入：

```cmd
dir
```

你應該看得到：

```text
pom.xml
README.md
src
```

看到 `pom.xml` 才表示目前位置正確。

---

## 步驟 5：執行所有自動化測試

輸入：

```cmd
mvn clean test
```

第一次執行時，Maven 需要下載套件，可能會等待一段時間。

成功時最後會看到：

```text
BUILD SUCCESS
```

並且測試應該顯示：

```text
Tests run: 5, Failures: 0, Errors: 0
```

注意：測試案例中雖然有模擬 Timeout、Checksum 錯誤與重開機失敗，但這些是「預期的錯誤情境」。只要程式正確辨識錯誤，JUnit 測試仍然會通過。

---

## 步驟 6：執行主程式

先打包：

```cmd
mvn clean package
```

接著執行：

```cmd
java -cp target\java-firmware-upgrade-test-automation-1.0.0.jar com.renkai.firmware.App
```

正常會看到：

```text
Status: PASSED
Message: Firmware upgrade completed successfully
```

---

## 步驟 7：查看測試報告

執行主程式後，打開：

```text
target\reports\firmware-upgrade-report.md
```

可以使用記事本、VS Code 或 IntelliJ IDEA 開啟。

---

# 五、使用 IntelliJ IDEA 開啟

## 步驟 1

開啟 IntelliJ IDEA。

## 步驟 2

選擇：

```text
Open
```

## 步驟 3

選取整個資料夾：

```text
java-firmware-upgrade-test-automation
```

不要只選某一支 `.java`。

## 步驟 4

等待 IntelliJ 讀取 `pom.xml` 與下載 Maven 套件。

## 步驟 5

執行主程式：

```text
src/main/java/com/renkai/firmware/App.java
```

在 `App.java` 按滑鼠右鍵：

```text
Run 'App.main()'
```

## 步驟 6

執行測試：

```text
src/test/java/com/renkai/firmware/FirmwareUpgradeServiceTest.java
```

按滑鼠右鍵：

```text
Run 'FirmwareUpgradeServiceTest'
```

全部出現綠色勾勾代表測試通過。

---

# 六、怎麼看懂專案？建議閱讀順序

初學者不要一開始就看最複雜的程式，建議照下面順序：

```text
1. App.java
2. DeviceState.java
3. DeviceConfiguration.java
4. FirmwarePackage.java
5. ChecksumUtil.java
6. FirmwareValidator.java
7. MockDeviceClient.java
8. FirmwareUpgradeService.java
9. UpgradeResult.java
10. LogValidator.java
11. FirmwareUpgradeServiceTest.java
```

每看完一支程式，就回頭看 `App.java` 是如何呼叫它的。

---

# 七、可以自己動手修改的練習

## 練習 1：修改目前版本

在 `App.java` 找到：

```java
new MockDeviceClient("SW-1000", "1.0.0", configuration);
```

把 `1.0.0` 改成：

```java
"1.0.5"
```

重新執行，觀察報告中的舊版本。

---

## 練習 2：修改目標版本

把：

```java
"1.1.0"
```

改成：

```java
"2.0.0"
```

重新執行，確認裝置最後變成 `2.0.0`。

---

## 練習 3：模擬 Timeout

在建立裝置後加入：

```java
device.setSimulateTimeout(true);
```

重新執行，結果應該變成：

```text
TIMEOUT
```

測試完成後，把這行刪掉或改成：

```java
device.setSimulateTimeout(false);
```

---

## 練習 4：模擬重新開機失敗

加入：

```java
device.setSimulateRebootFailure(true);
```

重新執行後，結果應該是：

```text
REBOOT_FAILED
```

---

## 練習 5：故意製造 Checksum 錯誤

在 `App.java` 建立 `FirmwarePackage` 時，把：

```java
ChecksumUtil.sha256(firmwareData)
```

改成：

```java
"wrong-checksum"
```

重新執行後，應該得到：

```text
VALIDATION_FAILED
```

---

# 八、上傳到 GitHub

## 方法一：使用 GitHub 網頁

1. 登入 GitHub
2. 按右上角 `+`
3. 選擇 `New repository`
4. Repository name 輸入：

```text
java-firmware-upgrade-test-automation
```

5. 建立 Repository
6. 選擇 `uploading an existing file`
7. 將解壓縮後專案資料夾內的所有檔案拖進去
8. 輸入 Commit message：

```text
Initial commit: Java firmware upgrade test automation
```

9. 按 `Commit changes`

注意：要上傳的是資料夾裡的內容，GitHub 首頁應直接看得到 `README.md`、`pom.xml`、`src`，不要多包一層資料夾。

---

## 方法二：使用 Git 指令

在專案資料夾執行：

```cmd
git init
git add .
git commit -m "Initial commit: Java firmware upgrade test automation"
git branch -M main
git remote add origin 你的GitHubRepository網址
git push -u origin main
```

---

# 九、面試時怎麼介紹？

可以這樣說：

> 這是一套使用 Java 8 開發的韌體升級自動化測試系統。我使用 Mock Device 模擬網路設備，測試內容包含韌體副檔名、設備型號、SHA-256 Checksum、升級流程、重新開機、版本確認、設定保留與 Log 錯誤分析。我也使用 JUnit 建立正常升級、損壞韌體、型號不相容、Timeout 與重新開機失敗等測試案例，並透過 GitHub Actions 在程式上傳後自動執行測試。

要誠實補充：

> 目前版本採用 Mock Device，尚未直接連接實體硬體；未來可以再擴充 SSH、Serial Port 或 UART，連接真實網路設備或開發板。

---

# 十、目前專案的限制

這個專案目前是學習與作品集版本，因此：

- 沒有真的燒錄韌體
- 沒有真的控制 Switch 或 AP
- 沒有連接 Serial Port／UART
- 沒有透過 SSH 操作真實設備
- 裝置狀態由 Mock Device 模擬

但它已經可以展示：

- Java OOP
- Interface
- Enum
- Exception Handling
- SHA-256
- 狀態流程
- Retry 與 Timeout
- JUnit 自動化測試
- GitHub Actions CI

---

# 十一、未來可以怎麼擴充？

1. 使用 SSH 連接 Linux 或網路設備
2. 使用 Serial Port／UART 讀取 Boot Log
3. 上傳真實 `.bin` 韌體檔案
4. 加入韌體降版測試
5. 加入斷電復原測試
6. 加入多設備平行測試
7. 輸出 HTML 測試報告
8. 串接 Jenkins 或其他 CI/CD 平台

