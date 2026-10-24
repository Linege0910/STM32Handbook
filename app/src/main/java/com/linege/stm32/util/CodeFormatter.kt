package com.linege.stm32.util

/**
 * 代码格式化：把源码转成适合手机竖屏显示的物理行
 *
 * 解决两个问题：
 * 1. 源码用 \t 缩进（本工程 58% 的行含 tab），Compose 里 tab 宽度不可控 → 展开成固定 4 空格
 * 2. 原作者用大量 tab 把行内注释推到右侧，单行过长 → 智能拆到下一行
 */
object CodeFormatter {

    /** 一个物理显示行 */
    data class Line(
        /** 原始行号；续行为 null */
        val no: Int?,
        val text: String,
        /** 是否为注释续行（显示时可用次要色） */
        val isContinuation: Boolean = false
    )

    private const val TAB_SIZE = 4

    /** 超过这个长度就把行内注释拆到下一行 */
    private const val SPLIT_THRESHOLD = 46

    /** 纯注释行的前导空格超过这个值，就左移 */
    private const val MAX_COMMENT_INDENT = 8

    /**
     * 格式化整个文件
     */
    fun format(source: String): List<Line> {
        val out = ArrayList<Line>(512)

        // 统一换行，去掉 \r
        val rawLines = source.replace("\r\n", "\n").replace("\r", "\n").split("\n")

        rawLines.forEachIndexed { idx, raw ->
            val expanded = expandTabs(raw)
            val no = idx + 1

            val split = splitLongComment(expanded)
            if (split == null) {
                out.add(Line(no, expanded))
            } else {
                out.add(Line(no, split.first))
                out.add(Line(null, split.second, isContinuation = true))
            }
        }

        // 去掉末尾多余空行
        while (out.isNotEmpty() && out.last().text.isBlank()) {
            out.removeAt(out.size - 1)
        }

        return out
    }

    /** tab 展开为空格 */
    private fun expandTabs(line: String): String {
        if (!line.contains('\t')) return line
        val sb = StringBuilder(line.length + 16)
        line.forEach { ch ->
            if (ch == '\t') {
                repeat(TAB_SIZE) { sb.append(' ') }
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * 把过长的行内注释拆到下一行
     * @return Pair(代码行, 注释行) 或 null（不拆）
     */
    private fun splitLongComment(line: String): Pair<String, String>? {
        val idx = findLineComment(line)
        if (idx <= 0) return null

        val prefix = line.substring(0, idx)
        val comment = line.substring(idx).trim()
        if (comment.isEmpty()) return null

        // 情况 A：整行就是注释（前面全是空白）
        // 原作者常用大量 tab 把注释推到右侧做对齐，手机屏窄放不下 → 左移
        if (prefix.isBlank()) {
            val lead = prefix.length
            return if (lead > MAX_COMMENT_INDENT) {
                Pair("", " ".repeat(TAB_SIZE * 2) + comment)
            } else {
                null
            }
        }

        // 情况 B：代码 + 行尾注释，注释位置太靠右 → 拆到下一行
        if (idx < SPLIT_THRESHOLD) return null

        val code = prefix.trimEnd()
        if (code.isEmpty()) return null

        // 续行缩进：与代码首字符对齐，最多 8 格
        val lead = code.indexOfFirst { it != ' ' }.let { if (it < 0) 0 else it }
        val indent = minOf(lead + TAB_SIZE, 8)

        return Pair(code, " ".repeat(indent) + comment)
    }

    /**
     * 找到行内 `//` 注释的起始位置
     * 排除字符串里的 `//`（如 "http://..."）
     */
    private fun findLineComment(line: String): Int {
        var i = 0
        var inString = false
        var quote = ' '

        while (i < line.length - 1) {
            val c = line[i]

            if (inString) {
                if (c == '\\') {
                    i += 2
                    continue
                }
                if (c == quote) inString = false
                i++
                continue
            }

            when (c) {
                '"', '\'' -> {
                    inString = true
                    quote = c
                    i++
                }
                '/' -> {
                    if (line[i + 1] == '/') return i
                    if (line[i + 1] == '*') return -1  // 块注释不动
                    i++
                }
                else -> i++
            }
        }
        return -1
    }
}
