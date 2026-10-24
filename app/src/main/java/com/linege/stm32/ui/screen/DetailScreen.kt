package com.linege.stm32.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linege.stm32.data.Chapter
import com.linege.stm32.data.CodeFile
import com.linege.stm32.data.Experiment
import com.linege.stm32.data.Settings
import com.linege.stm32.data.TreeNotes
import com.linege.stm32.ui.theme.currentCodePalette
import com.linege.stm32.util.CodeFormatter
import com.linege.stm32.util.CodeHighlighter

/**
 * 实验详情：接线图 / 代码 / 结构
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    exp: Experiment,
    chapter: Chapter?,
    prev: Experiment?,
    next: Experiment?,
    onBack: () -> Unit,
    onOpenExperiment: (Experiment) -> Unit
) {
    var tab by remember(exp.id) { mutableIntStateOf(0) }

    val tabs = remember(exp.id) {
        buildList {
            if (exp.hasWiring) add("接线图")
            add("代码")
            add("结构")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = exp.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${exp.no} · 第 ${exp.chapter} 章 ${chapter?.name ?: ""}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            if (exp.desc.isNotEmpty()) {
                Text(
                    text = exp.desc,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            ScrollableTabRow(
                selectedTabIndex = tab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = tab == i,
                        onClick = { tab = i },
                        text = { Text(title, fontSize = 13.sp) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (tabs.getOrNull(tab)) {
                    "接线图" -> WiringTab(exp)
                    "代码" -> CodeTab(exp)
                    "结构" -> TreeTab(exp)
                }
            }

            if (prev != null || next != null) {
                PagerBar(prev, next, onOpenExperiment)
            }
        }
    }
}

// ============================================================
// 接线图
// ============================================================
@Composable
private fun WiringTab(exp: Experiment) {
    val ctx = LocalContext.current
    val imgPath = exp.image

    if (imgPath == null) {
        EmptyHint("该实验没有接线图")
        return
    }

    val palette = currentCodePalette()

    val bitmap = remember(imgPath) {
        runCatching {
            ctx.assets.open(imgPath).use { BitmapFactory.decodeStream(it) }
        }.getOrNull()
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoom, pan, _ ->
        scale = (scale * zoom).coerceIn(1f, 6f)
        offset += pan
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "双指缩放 · 拖动查看 · 双击放大",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (scale > 1.01f) {
                TextButton(onClick = {
                    scale = 1f
                    offset = Offset.Zero
                }) {
                    Text("重置", fontSize = 12.sp)
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .background(palette.background),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "${exp.title} 接线图",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(transformState)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale > 1.01f) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    } else {
                                        scale = 2.5f
                                    }
                                }
                            )
                        },
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.Center
                )
            } else {
                Text("图片加载失败", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ============================================================
// 代码
// ============================================================
@Composable
private fun CodeTab(exp: Experiment) {
    val ctx = LocalContext.current
    var fileIdx by remember(exp.id) { mutableIntStateOf(0) }

    if (exp.files.isEmpty()) {
        EmptyHint("该实验没有独立代码文件")
        return
    }

    val file = exp.files.getOrNull(fileIdx) ?: exp.files[0]

    Column(modifier = Modifier.fillMaxSize()) {
        if (exp.files.size > 1) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(exp.files.indices.toList()) { i ->
                    val f = exp.files[i]
                    FilterChip(
                        selected = i == fileIdx,
                        onClick = { fileIdx = i },
                        label = {
                            Text(
                                text = f.name,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${file.group} / ${file.name} · ${file.lines} 行",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = {
                    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("code", file.content))
                    Toast.makeText(ctx, "已复制 ${file.name}", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(
                    Icons.Filled.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text("复制", fontSize = 12.sp)
            }
        }

        CodeView(file)
    }
}

@Composable
private fun CodeView(file: CodeFile) {
    val palette = currentCodePalette()
    val ctx = LocalContext.current
    val fontSize = remember { Settings.getCodeFontSize(ctx) }

    // 展开 tab、拆分超长注释行
    val displayLines = remember(file.name, file.content) {
        CodeFormatter.format(file.content)
    }

    // 行号列宽度随最大行号自适应
    val maxNo = displayLines.lastOrNull { it.no != null }?.no ?: 1
    val gutterWidth = when {
        maxNo < 100 -> 30.dp
        maxNo < 1000 -> 38.dp
        else -> 46.dp
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
        contentPadding = PaddingValues(vertical = 6.dp)
    ) {
        items(displayLines.size) { i ->
            val line = displayLines[i]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
            ) {
                Text(
                    text = line.no?.toString() ?: "",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = palette.lineNo,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier
                        .width(gutterWidth)
                        .padding(end = 8.dp)
                )
                Text(
                    text = if (line.text.isEmpty()) {
                        AnnotatedString(" ")
                    } else {
                        CodeHighlighter.highlight(line.text, palette)
                    },
                    fontSize = fontSize.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = (fontSize * 1.45f).sp,
                    color = palette.plain,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp)
                )
            }
        }
    }
}

// ============================================================
// 工程结构
// ============================================================
@Composable
private fun TreeTab(exp: Experiment) {
    if (exp.tree.isEmpty()) {
        EmptyHint("无结构数据")
        return
    }

    val palette = currentCodePalette()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
        contentPadding = PaddingValues(14.dp)
    ) {
        item {
            Text(
                text = "📦 ${exp.dir}/",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        exp.tree.forEachIndexed { fi, folder ->
            val isLast = fi == exp.tree.lastIndex
            val branch = if (isLast) "└── " else "├── "
            val pre = if (isLast) "    " else "│   "

            item(key = "folder_${folder.name}") {
                Column(modifier = Modifier.padding(bottom = 2.dp)) {
                    Text(
                        text = "$branch📁 ${folder.name}/",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TreeNotes.of(folder.name)?.let { note ->
                        Text(
                            text = "      $note",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }
            }

            items(folder.files.size) { i ->
                val f = folder.files[i]
                val last2 = i == folder.files.lastIndex
                val b2 = if (last2) "└── " else "├── "
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$pre$b2📄 ${f.name}",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "(${f.sizeText})",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = palette.lineNo
                    )
                }
            }
        }
    }
}

// ============================================================
// 上下页
// ============================================================
@Composable
private fun PagerBar(
    prev: Experiment?,
    next: Experiment?,
    onOpen: (Experiment) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .then(if (prev != null) Modifier.clickable { onOpen(prev) } else Modifier)
                    .padding(12.dp)
            ) {
                if (prev != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(
                                text = "上一个",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${prev.no} ${prev.title}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .then(if (next != null) Modifier.clickable { onOpen(next) } else Modifier)
                    .padding(12.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (next != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            modifier = Modifier.padding(end = 4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "下一个",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${next.no} ${next.title}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
