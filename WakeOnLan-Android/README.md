# 局域网远程开机（Wake-on-LAN）Android App

一个简单的 Android 原生 App，用于在局域网内向电脑发送 Wake-on-LAN 魔术包，实现远程开机。

## 功能
- 添加/编辑/删除电脑设备
- 保存电脑名称、MAC 地址、目标广播地址、端口
- 点击“开机”立即发送 WOL 魔术包（UDP 广播）
- 数据保存在手机本地 SharedPreferences

## 使用前提（重要）
1. 电脑主板 BIOS/UEFI 中开启 **Wake-on-LAN / 网络唤醒 / Power On By PCI-E** 等相关选项。
2. 电脑网卡（有线网卡）驱动中开启 **魔术包唤醒 / Wake on Magic Packet**。
3. 手机和电脑必须在**同一个局域网**。
4. 电脑关机后网卡仍供电（通常需要连接电源，部分台式机需主板跳线/BIOS 设置）。

> 无线网卡通常不支持 WOL，建议使用有线网卡。

## 如何获得 MAC 地址
在电脑上打开命令行（CMD 或 PowerShell）执行：

```bat
ipconfig /all
```

找到正在使用的有线网卡的 **物理地址（Physical Address）**，例如 `00-1A-2B-3C-4D-5E`。

## 如何填写广播地址
- 如果路由器是 `192.168.1.1`，电脑 IP 是 `192.168.1.100`，一般填 `192.168.1.255`
- 也可以先填 `255.255.255.255` 试试，部分网络环境有效
- 端口默认 `9`（WOL 常用端口，也可用 7）

## 在 Android Studio 中打包 APK
1. 用 Android Studio 打开本项目根目录。
2. 等待 Gradle 同步完成。
3. 菜单 `Build > Build Bundle(s) / APK(s) > Build APK(s)`。
4. 生成的 APK 在 `app/build/outputs/apk/debug/app-debug.apk`。
5. 将 APK 传到 Android 手机安装（需允许安装未知来源应用）。

> 如果 Android Studio 提示升级/下载 SDK 组件，按提示安装 `Android SDK Platform 35` 即可。

## 项目结构
```text
.
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/wakeonlan/
│       │   ├── MainActivity.java
│       │   ├── Device.java
│       │   ├── DeviceStorage.java
│       │   └── WakeOnLan.java
│       └── res/
│           ├── drawable/
│           ├── layout/
│           └── values/
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## 没有 Android Studio？用 GitHub Actions 在线打包
项目里已附带 `.github/workflows/build-apk.yml`。你可以：
1. 把这个项目推送到 GitHub 仓库；
2. 在 GitHub 仓库页面点击 **Actions** > **Build APK** > **Run workflow**；
3. 运行完成后在 **Artifacts** 中下载 `wake-on-lan-apk.zip`，里面就是 APK。

## Android 16 / ColorOS 16 说明
本 App 目标版本为 Android 15（API 35），在 Android 16 / ColorOS 16 上可正常安装运行。
如果系统首次发送开机时提示“本地网络”相关权限，请选择允许；也可以在系统设置 → 应用 → 局域网远程开机 → 权限中手动开启。
