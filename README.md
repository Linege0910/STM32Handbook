# STM32 学习手册

[![Release](https://img.shields.io/github/v/release/Linege0910/STM32Handbook?label=release)](https://github.com/Linege0910/STM32Handbook/releases/latest)
[![Download](https://img.shields.io/badge/APK-20.67%20MB-3DDC84?logo=android&logoColor=white)](https://github.com/Linege0910/STM32Handbook/releases/latest)
[![minSdk](https://img.shields.io/badge/Android-7.0%2B-blue?logo=android)](https://github.com/Linege0910/STM32Handbook/releases/latest)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

基于江协科技（江科大）STM32F103 标准库教程整理的 Android 离线手册。

使用 **Jetpack Compose + Material Design 3** 构建，全部内容内置，**无需联网**。

## 特性

- **完全离线** —— 代码、接线图全部打包进 APK，不申请网络权限
- **39 个实验** —— 覆盖 GPIO、定时器、ADC、DMA、USART、I2C、SPI、RTC、低功耗、看门狗、FLASH
- **38 张接线图** —— 支持双指缩放、拖动、双击放大
- **代码查看** —— C 语法高亮、行号、文件切换、一键复制
- **工程结构** —— 树形展示目录与文件用途
- **全文搜索** —— 支持编号、标题、文件名
- **深色/浅色主题** —— 可跟随系统或手动切换

## 内容规模

| 项目 | 数量 |
|---|---|
| 实验 | 39 个 |
| 章节 | 15 个 |
| 代码文件 | 434 个 |
| 代码行数 | 34,317 行 |
| 接线图 | 38 张 |

## 下载

**[⬇ 下载 STM32Handbook-v1.0.0.apk](https://github.com/Linege0910/STM32Handbook/releases/download/v1.0.0/STM32Handbook-v1.0.0.apk)**　（20.67 MB）

- 系统要求：Android 7.0（API 24）及以上
- SHA256：`81A2E7B83DBF305DBD424EAB20A7A48DF2C491011724899BC885CE583C436EC5`
- 首次安装需允许「安装未知来源应用」

全部版本见 [Releases](https://github.com/Linege0910/STM32Handbook/releases)。

或自行编译，见下方「构建」。

## 章节列表

| 章节 | 内容 | 实验数 |
|---|---|---|
| 2 | 工程模板 | 1 |
| 3 | GPIO 入门 | 5 |
| 4 | OLED 显示 | 1 |
| 5 | 外部中断 | 2 |
| 6 | 定时器 | 8 |
| 7 | ADC 模数转换 | 2 |
| 8 | DMA 直接内存访问 | 2 |
| 9 | USART 串口 | 4 |
| 10 | I2C 通信 | 2 |
| 11 | SPI 通信 | 2 |
| 12 | RTC 与备份 | 2 |
| 13 | 低功耗 | 4 |
| 14 | 看门狗 | 2 |
| 15 | FLASH | 2 |

## 构建

### 环境要求

- Android Studio Ladybug 或更新
- JDK 17
- Android SDK 36
- Kotlin 2.4.20

### 步骤

```bash
# 克隆
git clone https://github.com/<你的用户名>/STM32Handbook.git
cd STM32Handbook

# 配置 SDK 路径
echo "sdk.dir=D\:\\Android\\Sdk" > local.properties   # 改成你的实际路径

# 编译
./gradlew assembleDebug
```

APK 输出：`app/build/outputs/apk/debug/app-debug.apk`

## 项目结构

```
app/src/main/
├── assets/
│   ├── stm32.json          # 全部内容数据（1.2 MB）
│   └── img/                # 接线图 38 张（3.7 MB）
├── java/com/linege/stm32/
│   ├── MainActivity.kt     # 入口 + 路由
│   ├── data/
│   │   ├── Models.kt       # 数据模型
│   │   ├── Repository.kt   # assets 解析
│   │   └── Settings.kt     # 主题等本地设置
│   ├── ui/
│   │   ├── theme/Theme.kt  # M3 配色 + 代码配色
│   │   ├── component/      # 底部导航
│   │   └── screen/         # 首页/章节/详情/搜索/关于
│   └── util/
│       ├── CodeHighlighter.kt  # C 语法高亮
│       └── CodeFormatter.kt    # tab 展开、长行拆分
└── res/
```

## 技术要点

### 代码语法高亮

自实现 C 语言分词器，无第三方依赖：

- 关键字、类型、函数调用、字符串、数字、注释、预处理指令
- 括号单独上色，便于配对识别
- 深色/浅色两套配色，均通过 WCAG AA 对比度标准（≥ 4.5:1）

### 代码行格式化

源码中 58% 的行使用 `\t` 缩进，Compose 中 tab 宽度不可控。`CodeFormatter` 做了三件事：

1. `\t` 展开为 4 空格
2. 行尾注释过远时拆到下一行
3. 纯注释行的过多前导空格左移

## 数据来源

代码与接线图整理自江协科技（江科大）STM32F103 标准库教程，仅供学习参考。

## 许可

代码部分采用 MIT License。教程内容版权归原作者所有。
