package com.linege.stm32.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.linege.stm32.ui.theme.CodePalette

/**
 * C 语言语法高亮
 *
 * 支持：
 *  - 关键字 / 类型 / 函数调用（含自定义函数）
 *  - 字符串 / 数字 / 注释 / 预处理指令
 *  - 括号单独上色，方便配对识别
 */
object CodeHighlighter {

    private val KEYWORDS = setOf(
        "if", "else", "while", "for", "do", "switch", "case", "break", "continue",
        "return", "goto", "sizeof", "typedef", "struct", "union", "enum",
        "static", "extern", "const", "volatile", "inline", "register", "auto",
        "default", "true", "false", "NULL"
    )

    private val TYPES = setOf(
        "void", "char", "short", "int", "long", "float", "double",
        "unsigned", "signed", "bool",
        "uint8_t", "uint16_t", "uint32_t", "uint64_t",
        "int8_t", "int16_t", "int32_t", "int64_t",
        "u8", "u16", "u32", "u64", "size_t",
        "BitAction", "ENABLE", "DISABLE"
    )

    /** 需要单独上色的括号 */
    private val BRACKETS = setOf('(', ')', '{', '}', '[', ']')

    private sealed class Token {
        class Plain(val text: String) : Token()
        class Styled(
            val text: String,
            val color: Color,
            val italic: Boolean = false,
            val bold: Boolean = false
        ) : Token()
    }

    /**
     * 高亮单行代码
     * @param line 源码行
     * @param palette 当前主题的代码配色
     */
    fun highlight(line: String, palette: CodePalette): AnnotatedString {
        val tokens = tokenize(line, palette)
        return buildAnnotatedString {
            tokens.forEach { tk ->
                when (tk) {
                    is Token.Plain -> append(tk.text)
                    is Token.Styled -> withStyle(
                        SpanStyle(
                            color = tk.color,
                            fontStyle = if (tk.italic) FontStyle.Italic else FontStyle.Normal,
                            fontWeight = if (tk.bold) FontWeight.Bold else FontWeight.Normal
                        )
                    ) {
                        append(tk.text)
                    }
                }
            }
        }
    }

    private fun tokenize(src: String, p: CodePalette): List<Token> {
        val out = ArrayList<Token>()

        // 整行预处理指令
        if (src.trimStart().startsWith("#")) {
            out.add(Token.Styled(src, p.preprocessor, bold = true))
            return out
        }

        val sb = StringBuilder()
        fun flush() {
            if (sb.isNotEmpty()) {
                out.add(Token.Plain(sb.toString()))
                sb.clear()
            }
        }

        var i = 0
        val n = src.length

        while (i < n) {
            // 行注释
            if (i + 1 < n && src[i] == '/' && src[i + 1] == '/') {
                flush()
                out.add(Token.Styled(src.substring(i), p.comment, italic = true))
                return out
            }

            // 块注释
            if (i + 1 < n && src[i] == '/' && src[i + 1] == '*') {
                val end = src.indexOf("*/", i + 2)
                flush()
                if (end != -1) {
                    out.add(Token.Styled(src.substring(i, end + 2), p.comment, italic = true))
                    i = end + 2
                } else {
                    out.add(Token.Styled(src.substring(i), p.comment, italic = true))
                    return out
                }
                continue
            }

            // 字符串 / 字符
            if (src[i] == '"' || src[i] == '\'') {
                val quote = src[i]
                var j = i + 1
                while (j < n) {
                    if (src[j] == '\\') { j += 2; continue }
                    if (src[j] == quote) { j++; break }
                    j++
                }
                val end = minOf(j, n)
                flush()
                out.add(Token.Styled(src.substring(i, end), p.string))
                i = end
                continue
            }

            // 数字
            if (src[i].isDigit() && (i == 0 || !isWordChar(src[i - 1]))) {
                var j = i
                while (j < n && (src[j].isLetterOrDigit() || src[j] == '.')) j++
                flush()
                out.add(Token.Styled(src.substring(i, j), p.number))
                i = j
                continue
            }

            // 标识符
            if (isWordChar(src[i])) {
                var j = i
                while (j < n && isWordChar(src[j])) j++
                val word = src.substring(i, j)

                // 后面跳过空白若是左括号，即为函数调用
                var k = j
                while (k < n && (src[k] == ' ' || src[k] == '\t')) k++
                val isCall = k < n && src[k] == '('

                when {
                    word in KEYWORDS -> {
                        flush()
                        out.add(Token.Styled(word, p.keyword, bold = true))
                    }
                    word in TYPES -> {
                        flush()
                        out.add(Token.Styled(word, p.type))
                    }
                    isCall -> {
                        flush()
                        out.add(Token.Styled(word, p.function))
                    }
                    else -> sb.append(word)
                }
                i = j
                continue
            }

            // 括号单独上色
            if (src[i] in BRACKETS) {
                flush()
                out.add(Token.Styled(src[i].toString(), p.bracket))
                i++
                continue
            }

            sb.append(src[i])
            i++
        }

        flush()
        return out
    }

    private fun isWordChar(c: Char): Boolean =
        c.isLetterOrDigit() || c == '_' || (c.code in 0x4E00..0x9FFF)
}
