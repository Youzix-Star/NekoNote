# NekoUi

给 AI 用的 Android 界面框架：直接套用，不用从零画界面。

四个页签、两套引擎（Miuix / Material 3）、组件画廊、设置、关于、崩溃兜底。没有业务逻辑 ——
页面要的数据都在 `demo/` 里，换掉它就是另一个应用。

包名 `top.youzix.nekonote` · minSdk 33 · targetSdk 35

## 构建

只在 GitHub Actions 里构建。推送 `main` 产出 APK artifact，推送 `v*` 标签额外创建 Release。

## 许可

[AGPL-3.0](LICENSE)。界面参考 [SukiSU-Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra)（GPL-3.0）
与 [NekoPlus](https://github.com/Youzix-Star/NekoPlus)（AGPL-3.0），署名见应用内「开源许可」。
