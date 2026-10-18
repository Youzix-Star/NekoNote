# NekoNote

一个只在设备上生活的 Markdown 笔记应用：写、读、找、归类，没有账号也没有同步。

界面直接沿用 [NekoUi](https://github.com/Youzix-Star/NekoUi) 的外壳：同一套 Miuix / Material 3 双引擎、
液态玻璃底栏、二级页返回手势、`demo` 换成了真正的笔记数据。应用内可随时切换引擎，笔记不受影响。

包名 `top.youzix.nekonote` · minSdk 33 · targetSdk 35 · v0.1.0

## 有什么

- **笔记列表**：搜索标题和正文、置顶、按标签筛选、长按置顶；排序可选最近更新 / 创建时间 / 标题
- **Markdown 编辑器**：纯白无边界编辑页（无卡片、无横线），源码实时着色（标题井号、强调、标签、链接、代码块），源码与预览一键切换，底栏是悬浮图标胶囊（基础 / 格式两态）
- **标签页**：标签自动从正文里的 `#标签` 读出来，另有置顶与最近编辑两个入口
- **自动保存**：输入停顿后落盘，退出页面再落一次；空笔记退出即删除
- **设置**：主题、模糊、字号、默认预览、默认排序、界面引擎、存储位置、关于与开源许可
- **崩溃兜底**：独立 `:crash` 进程的崩溃页，关于页里可查看 / 复制 / 清空

数据存在应用私有的 `filesDir/notes/notes.json` 里，不联网、不上传。

## 构建

只在 GitHub Actions 里构建。推送 `main` 产出 APK artifact，推送 `v*` 标签额外创建 Release。
签名从 Actions Secrets 的 `KEYSTORE_BASE64` / `KEYSTORE_PASSWORD` / `KEY_ALIAS` / `KEY_PASSWORD` 读取；
没有时退回 runner 的 debug key，APK 仍可安装。

## 许可

[AGPL-3.0](LICENSE)。界面框架来自 [NekoUi](https://github.com/Youzix-Star/NekoUi)（AGPL-3.0），
其界面参考 [SukiSU-Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra)（GPL-3.0）与
[NekoPlus](https://github.com/Youzix-Star/NekoPlus)（AGPL-3.0），署名见应用内「开源许可」。
