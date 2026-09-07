# 壁语 WallText · Android 文字壁纸应用

把你的想法变成壁纸上的字。原生 Android（Kotlin + Room + 桌面小部件 + 壁纸引擎），整套源码在此仓库，由 GitHub Actions 自动构建 debug APK。

## ✨ 功能

- 输入想法 → 选择样式 → 一键渲染到壁纸
- 桌面小部件（1×1），点击直接弹出输入框，无需打开 App
- 6 种预设样式：经典白字 / 优雅黑字 / 极简灰字 / 醒目大字 / 柔和暖色 / 夜间模式
- 输入时实时预览壁纸效果
- 5 个快捷短语：专注 / 喝水 / 运动 / 早睡 / 阅读
- 自动保存最近 50 条想法，点击即可重新上墙
- 自动记忆上次使用的样式

## 🚀 快速开始（推荐：GitHub Actions 云端构建）

> 本仓库自带 GitHub Actions workflow，无需你本地安装 Android Studio / JDK / Android SDK。

### 1. 在 GitHub 创建空仓库

去 https://github.com/new 创建一个空仓库：
- 名字随意（如 `walltext-android`）
- 选 **Public**（免费无限 Actions 分钟）
- 不要勾选 "Add a README" / "Add .gitignore"

### 2. 推送代码

在解压后的项目目录执行：

```bash
cd walltext-android

# Windows PowerShell 上首次 push 如果 gradlew 报无权限，先：
# git update-index --chmod=+x gradlew

git init
git add .
git commit -m "Initial commit: WallText app source"
git branch -M main
git remote add origin https://github.com/<你的用户名>/<仓库名>.git
git push -u origin main
```

### 3. 等待 Actions 构建

去仓库的 **Actions** 标签页查看构建进度。
- 首次构建 5–15 分钟（下载 Gradle + Android SDK 依赖）
- 完成后在 run 页面底部 **Artifacts** 区下载 `WallText-debug.zip`

### 4. 安装到手机

把 APK 传到手机（USB / 微信传输 / 邮件都行），点击安装：
- 首次安装需到 系统设置 → 安全 → 允许"安装未知来源应用"
- 部分国产 ROM 需要授予对应的浏览器/文件管理器权限

### 5. 设置壁纸

打开 App：
1. 输入想法 → 选择样式 → 点击"上墙"
2. 系统会弹出"设为壁纸"对话框 → 确认
3. 长按桌面 → 小部件 → 找到"壁语" → 拖到桌面

## 🛠️ 本地构建（备选）

如果你想用 Android Studio 本地编译：

1. 安装 **Android Studio Hedgehog (2023.1.1)** 或更新版本
2. 安装 **JDK 17**（AS Hedgehog 自带）
3. 用 Android Studio 打开本目录，等待 Gradle sync 完成
4. 点击 Run ▶️ 构建并运行

或者命令行：

```bash
./gradlew assembleDebug
# APK 输出到 app/build/outputs/apk/debug/app-debug.apk
```

## 📁 项目结构

```
app/src/main/java/com/walltext/app/
├── WallTextApp.kt            # Application 类
├── Thought.kt                # Room Entity
├── TextStyle.kt              # 6 种预设样式枚举
├── ThoughtDao.kt             # Room DAO
├── AppDatabase.kt            # Room 数据库
├── WallpaperEngine.kt        # 壁纸渲染引擎
├── MainActivity.kt           # 主界面
├── QuickInputActivity.kt     # 悬浮输入对话框
├── ThoughtAdapter.kt         # 历史列表 RecyclerView 适配器
└── ThoughtWidget.kt          # 桌面小部件
```

## ⚙️ 技术栈

- Kotlin 1.9.20 / AGP 8.2.0 / Gradle 8.4
- minSdk 24 / targetSdk 34 / compileSdk 34
- Room 2.6.1（KSP 编译）
- Material 1.11.0 / ConstraintLayout 2.1.4
- 仅需 `SET_WALLPAPER` 权限（normal 权限，安装即授予）
- 无网络请求、无追踪、无敏感权限

## 🐛 常见问题

**Q: Actions 构建失败，提示 license 没接受？**
A: AGP 8.2 会自动接受 SDK license。如果失败，在 Actions run 页面点 "Re-run jobs"。

**Q: gradlew 报 `Permission denied`？**
A: 在 push 前先 `chmod +x gradlew`（Windows Git Bash）或 `git update-index --chmod=+x gradlew` 后重新 commit。

**Q: APK 安装失败？**
A: 检查：
- 开启"允许安装未知来源"
- 卸载旧版本（debug 签名变化时）
- 部分 MIUI/EMUI 需要在"应用管理"→右上角菜单→打开"安装权限"

**Q: 壁纸设置后没显示文字？**
A: 确认系统壁纸服务正常；部分启动器（如 MIUI）会缓存旧壁纸，重启手机或更换壁纸源后再试。

**Q: 桌面小部件点击没反应？**
A: 确认长按桌面时选的是"壁语"小部件（不是普通图标）；部分启动器需要把 App 加入"自启动"白名单。

## 🔮 后续可扩展（未实现）

- 深色模式适配
- 定时自动切换想法
- 番茄钟联动显示
- WebDAV 备份
- 自定义字体上传
- 多语言支持

## 📄 许可

MIT