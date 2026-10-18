# AGENTS.md

`NekoNote`：一个 Markdown 笔记应用，界面层从 `NekoUi` 整份复制而来。包名 `top.youzix.nekonote`。
它保留了 NekoUi 的外壳（双引擎、液态玻璃底栏、二级页返回手势、崩溃兜底），只把演示数据换成了真的笔记。

## 结构

| 路径 | 是什么 |
|---|---|
| `data/` | 笔记的模型与存储：`Note`、`NoteRepository`（`filesDir/notes/notes.json`）、`NotePrefs`、`NoteDate` |
| `markdown/` | `Markdown`（块解析 + 行内 span + 标签）、`MarkdownHighlight`（编辑时的语法着色）、`MarkdownEdit`（工具栏的全部编辑动作） |
| `ui/miuix/notes/` | Miuix 引擎：笔记列表、编辑器（纯白页 + 悬浮图标胶囊）、标签页、Markdown 渲染 |
| `ui/material3/notes/` | Material 3 引擎：同一批页面，另一套实现，另一套 Markdown 渲染 |
| `ui/miuix/**`、`ui/material3/**` | 设置、关于、许可，以及框架自带的组件 |
| `AppState` | 引擎 / 模糊 / 主题 / 排序 / 字号 / 默认预览 / 当前标签筛选 |
| `UiEnginePrefs`、`NotePrefs` | 两套持久化（界面选择 / 笔记编辑器选择） |
| `crash/` | 崩溃记录与独立进程 `:crash` 的崩溃页。**这是框架的一部分，不要删** |
| `NekoNoteApp` | 装崩溃处理器、打开笔记仓库的地方；`:crash` 进程里不装 |

## 四条不变量

1. **两套引擎各写一遍。** `ui/miuix/notes/**` 与 `ui/material3/notes/**` 互不共享 Composable；
   改一个页签必须两个都改，否则切过去就少东西。共享的只有 `data/`、`markdown/`、`AppState`、
   两套 Prefs、`ui/AppIcons`、`ui/nav/Routes`、`crash/`。
2. **笔记只有一份。** 内存里的 `NoteRepository.notes` 是唯一来源，界面不自己造笔记、不缓存第二份正文。
   编辑时 `NoteRepository.touch` 立即更新内存，落盘由 `persist()` 在后台串行完成。
3. **Markdown 解析只写一次。** `markdown/Markdown.kt` 产出 `MdBlock` 与 `AnnotatedString`；
   两个引擎各自决定标题多大、代码块什么颜色，但绝不各写一个解析器。
4. **崩溃处理必须一直在。** `NekoNoteApp` 里 `CrashHandler.install`、清单里 `:crash` 的 Activity、
   关于页的「崩溃日志」入口，三处缺一不可。

## 笔记的约定

- 标签是 `Note.tags` 显式字段，在标题下方单独一行直接编辑（空格/逗号分隔，`#` 可写可不写）；旧文件没有 `tags` 字段时，解码会从正文的 `#标签` 迁移一次。
- 编辑页是纯 `surface` 页面，没有卡片、底色和横线：标题、日期/字数、正文直接排在页面上，左右 20dp。
- 标题不再画 `#`；标题 26sp 加粗、弱化色，占位「未命名笔记」。标题下依次是可编辑标签行、完整日期 + 字数的元信息。
- 编辑时的着色在 `markdown/MarkdownHighlight.kt`：只加 span、不改文本（`OffsetMapping.Identity`），两个引擎只负责传入自己的主题色。
- 底部工具栏是参考式的悬浮胶囊：52dp 高、圆形、阴影 8dp、`SpaceEvenly` 均匀分布；基础态 6 个描边图标（待办、列表、引用、代码块、链接、文本格式），格式态 5 个格式图标 + 竖分隔线 + 关闭。两套引擎各自实现，图标来自 `AppIcons` 的 Outlined 集合。
- 空笔记（标题、正文与标签都空）离开编辑器时会被删除；新建笔记在第一次输入时才真正落库。
- 排序时置顶永远在最前；标签筛选存在 `AppState.noteTagFilter`，标签页点一下会把它带给笔记页。
- 笔记列表与预览里的标签用组件画廊同款 `StatusTag` 胶囊显示（`secondaryContainer`，即「强调」标签那组颜色）；编辑页的标签行是纯文本输入。
- 存储格式是 `{"version":1,"notes":[{"tags":[...],...}]}`；改字段要兼容旧文件（解码用 `opt*`，缺字段给默认值；`tags` 缺失时从正文迁移）。

## 加一个页面（检查清单）

1. 在 `ui/nav/Routes.kt` 里加一个 `data object` / `data class`。
2. 两个 `*App.kt` 的 `NavDisplay { entry<Route.X> { … } }` 里都注册；页面自己带 `Scaffold` + 返回箭头，
   `onBack` 调 `navigator.pop()`。返回手势与动画由 miuix-nav 提供，**不要**再手写返回动画。
3. Miuix 页面的签名对齐现有页面（`(bottomPadding, …)`），Material 页面对齐 `(outerPadding, …)`。
4. `python3 scripts/klex.py <改过的文件>`（词法 / 括号 / 未使用 import），然后交给 CI。

## 工作方式

- **不在本地构建。** 这台设备没有 Android SDK，也别装；唯一验证是 GitHub Actions（3～6 分钟一轮）。
- 不要凭记忆写 Compose / miuix 的签名。miuix 源码：
  `https://repo1.maven.org/maven2/top/yukonga/miuix/kmp/miuix-ui-android/<版本>/miuix-ui-android-<版本>-sources.jar`。
- 整份重写文件用 `cat > file <<'EOF'`；这台机器的 `write` 工具建不了硬链接（Android 禁 `link(2)`）。
- 签名：CI 从 Secrets 读 `KEYSTORE_BASE64` / `KEYSTORE_PASSWORD` / `KEY_ALIAS` / `KEY_PASSWORD`，
  没有时退回 debug key，APK 仍可安装。

## 许可

本仓库 AGPL-3.0。界面框架来自 NekoUi（AGPL-3.0）；`ui/miuix/components/`、`ui/material3/components/`
里的状态标签、TonalCard、WarningCard 移植自 SukiSU-Ultra（GPL-3.0）。
