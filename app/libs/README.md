# 高德地图 SDK 放置目录（成员 D）

从 https://lbs.amap.com 下载以下 SDK 的 AAR 包，放入本目录（`app/libs/`）：

- 3D 地图 SDK（3dmap）
- 定位 SDK（location）
- 搜索 SDK（search，逆地理用）

`app/build.gradle.kts` 已配置 `fileTree("libs", include = ["*.aar", "*.jar"])`，放入后同步 Gradle 即自动拾取。
高德 API Key 已填入 `AndroidManifest.xml`，隐私合规已在 `CampusHelpApp` 通过 `AmapPrivacyHelper` 调用。

放入 AAR 后通知我，我会把 `MapFragment` 占位视图替换为真实的 MapView + 周围任务 Marker（数据复用 `TaskRepository.observeAll()`）。
