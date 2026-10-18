# AGENTS.md

`NekoUi`：一个可以直接套用的 Android 界面框架。包名 `top.youzix.nekonote`。
它不是某个应用，而是应用们共用的那一层：页面骨架、控件、主题、崩溃兜底都在，业务逻辑一概没有。

## 结构

| 路径 | 是什么 |
|---|---|
| `demo/` | 页面要的全部数据。`HomeDemo` / `ListDemo` / `LogDemo` / `DocsDemo`，换掉这些就是换掉应用内容 |
| `ui/miuix/**` | Miuix 引擎：首页、列表、组件画廊、设置、关于、许可、液态玻璃底栏 |
| `ui/material3/**` | Material 3 引擎：同一批页面，另一套实现；`components/` 是 SukiSU 的分段列表、搜索栏、状态标签 |
| `ui/common/` 之外 | `AppState`（引擎/模糊/主题/返回动画）、`UiEnginePrefs`（持久化）、`MainActivity`（选引擎） |
| `crash/` | 崩溃记录与独立进程 `:crash` 的崩溃页。**这是框架的一部分，不要删** |
| `NekoNoteApp` | 装崩溃处理器的地方；`:crash` 进程里不装（否则会叠第二层崩溃页） |

## 三条不变量

1. **两套引擎各写一遍。** `ui/miuix/**` 与 `ui/material3/**` 互不共享 Composable；改一个页签必须两个都改，
   否则切过去就少东西。共享的只有 `demo/`、`AppState`、`UiEnginePrefs`、`ui/AppIcons`、`ui/predictiveback`、`crash/`。
2. **不写业务逻辑。** 没有网络、没有权限、没有跨进程调用、没有 shell。需要数据就去 `demo/` 拿。
3. **崩溃处理必须一直在。** `NekoNoteApp` 里 `CrashHandler.install`、清单里 `:crash` 的 Activity、
   关于页的「崩溃日志」入口，三处缺一不可。

## 调试与崩溃

关于页签名行（「只有界面，没有别的。」）连点七下 → 关于页出现「调试 → 模拟崩溃」，
点它就会真的崩一次，用来验证 `:crash` 崩溃页。状态持久化在 `UiEnginePrefs.debugMode`。

## 加一个页面（检查清单）

1. `demo/` 里加数据（页面不自己造状态）。
2. `ui/miuix/<名字>/` 与 `ui/material3/<名字>/` 各写一个 Screen，签名对齐现有页面：
   miuix 是 `(contentPadding, scrollBehavior[, …])`，Material 是 `(outerPadding, useBlur[, …])`。
3. 页签：改两个 `*App.kt` 的 `navigationItems`/`labels`、`TAB_*` 常量、`titles`，并确认底部导航放得下。
4. 二级页：在 `ui/nav/Routes.kt` 里加一个 `data object`，然后在两个 `*App.kt` 的 `NavDisplay { entry<Route.X> { … } }`
   里注册；页面自己带 `Scaffold` + 返回箭头，`onBack` 调 `navigator.pop()`。返回手势与动画由 miuix-nav 提供，
   **不要**再手写返回动画。
5. `python3 scripts/klex.py <改过的文件>`（词法 / 括号 / 未使用 import），然后交给 CI。

## 工作方式

- **不在本地构建。** 这台设备没有 Android SDK，也别装；唯一验证是 GitHub Actions（3～6 分钟一轮）。
- 不要凭记忆写 Compose / miuix 的签名。miuix 源码：
  `https://repo1.maven.org/maven2/top/yukonga/miuix/kmp/miuix-ui-android/<版本>/miuix-ui-android-<版本>-sources.jar`。
- 整份重写文件用 `cat > file <<'EOF'`；这台机器的 `write` 工具建不了硬链接（Android 禁 `link(2)`）。
- 签名：CI 从 Secrets 读 `KEYSTORE_BASE64` / `KEYSTORE_PASSWORD` / `KEY_ALIAS` / `KEY_PASSWORD`，
  没有时退回 debug key，APK 仍可安装。

## 许可

本仓库 AGPL-3.0。`ui/miuix/components/`、`ui/material3/components/` 里的状态标签、TonalCard、WarningCard
移植自 SukiSU-Ultra（GPL-3.0）；其余界面框架来自 NekoPlus（AGPL-3.0）。
