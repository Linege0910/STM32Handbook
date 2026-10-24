package com.linege.stm32.data

/**
 * 实验数据模型
 * 对应 assets/stm32.json 的结构
 */

data class Stm32Data(
    val experiments: List<Experiment>,
    val chapters: Map<Int, Chapter>,
    val total: Int
) {
    /** 按章节分组 */
    val byChapter: Map<Int, List<Experiment>> by lazy {
        experiments.groupBy { it.chapter }.toSortedMap()
    }
}

data class Chapter(
    val no: Int,
    val name: String,
    val desc: String
)

data class Experiment(
    val id: String,
    val dir: String,
    val chapter: Int,
    val index: Int,
    val no: String,
    val title: String,
    val desc: String,
    val image: String?,
    val files: List<CodeFile>,
    val tree: List<TreeFolder>
) {
    val totalLines: Int get() = files.sumOf { it.lines }
    val hasWiring: Boolean get() = !image.isNullOrEmpty()
}

data class CodeFile(
    val name: String,
    val group: String,
    val size: Int,
    val lines: Int,
    val content: String
)

data class TreeFolder(
    val name: String,
    val files: List<TreeFile>
)

data class TreeFile(
    val name: String,
    val size: Int
) {
    /** 文件大小的可读形式 */
    val sizeText: String
        get() = if (size < 1024) "$size B" else String.format("%.1f KB", size / 1024.0)
}

/** 目录说明 */
object TreeNotes {
    private val notes = mapOf(
        "User" to "用户代码：main.c 是程序入口",
        "Hardware" to "外设驱动：自己写的模块（OLED、传感器等）",
        "System" to "系统工具：Delay 延时、定时器等",
        "Start" to "启动文件与内核库：芯片启动代码、CMSIS",
        "Library" to "STM32 标准外设库：官方提供的驱动库",
        "DebugConfig" to "调试配置：Keil 调试器参数",
        "Objects" to "编译输出文件（可删）",
        "Listings" to "编译列表文件（可删）"
    )

    fun of(folder: String): String? = notes[folder]
}
