package com.linege.stm32.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * 数据仓库：从 assets 读取并解析 JSON
 * 完全离线，不依赖网络
 */
object Repository {

    private const val TAG = "STM32Repo"
    private const val ASSET_JSON = "stm32.json"

    @Volatile
    private var cached: Stm32Data? = null

    /** 同步读取（已在内存中时很快） */
    fun peek(): Stm32Data? = cached

    /** 加载数据（后台线程解析） */
    suspend fun load(ctx: Context): Stm32Data = withContext(Dispatchers.IO) {
        cached?.let { return@withContext it }

        val t0 = System.currentTimeMillis()

        val text = ctx.assets.open(ASSET_JSON).bufferedReader().use { it.readText() }
        Log.d(TAG, "读取 JSON: ${text.length} 字符, ${System.currentTimeMillis() - t0}ms")

        val root = JSONObject(text)

        // 章节
        val chapters = mutableMapOf<Int, Chapter>()
        val chObj = root.optJSONObject("chapters")
        chObj?.keys()?.forEach { key ->
            val o = chObj.optJSONObject(key) ?: return@forEach
            val no = key.toIntOrNull() ?: return@forEach
            chapters[no] = Chapter(
                no = no,
                name = o.optString("name", "未分类"),
                desc = o.optString("desc", "")
            )
        }

        // 实验
        val arr = root.optJSONArray("experiments") ?: JSONArray()
        val exps = ArrayList<Experiment>(arr.length())

        for (i in 0 until arr.length()) {
            val e = arr.optJSONObject(i) ?: continue

            // 代码文件
            val files = ArrayList<CodeFile>()
            val fArr = e.optJSONArray("files")
            if (fArr != null) {
                for (j in 0 until fArr.length()) {
                    val f = fArr.optJSONObject(j) ?: continue
                    files.add(
                        CodeFile(
                            name = f.optString("name", ""),
                            group = f.optString("group", ""),
                            size = f.optInt("size", 0),
                            lines = f.optInt("lines", 0),
                            content = f.optString("content", "")
                        )
                    )
                }
            }

            // 工程结构
            val tree = ArrayList<TreeFolder>()
            val tArr = e.optJSONArray("tree")
            if (tArr != null) {
                for (j in 0 until tArr.length()) {
                    val t = tArr.optJSONObject(j) ?: continue
                    val tFiles = ArrayList<TreeFile>()
                    val tfArr = t.optJSONArray("files")
                    if (tfArr != null) {
                        for (k in 0 until tfArr.length()) {
                            val tf = tfArr.optJSONObject(k) ?: continue
                            tFiles.add(TreeFile(tf.optString("name", ""), tf.optInt("size", 0)))
                        }
                    }
                    tree.add(TreeFolder(t.optString("name", ""), tFiles))
                }
            }

            val img = e.optString("image", "")

            exps.add(
                Experiment(
                    id = e.optString("id", "exp_$i"),
                    dir = e.optString("dir", ""),
                    chapter = e.optInt("chapter", 0),
                    index = e.optInt("index", i),
                    no = e.optString("no", ""),
                    title = e.optString("title", ""),
                    desc = e.optString("desc", ""),
                    image = if (img.isEmpty()) null else img,
                    files = files,
                    tree = tree
                )
            )
        }

        val data = Stm32Data(
            experiments = exps,
            chapters = chapters,
            total = root.optInt("total", exps.size)
        )

        cached = data
        Log.d(TAG, "解析完成: ${exps.size} 个实验, 总耗时 ${System.currentTimeMillis() - t0}ms")
        data
    }

    /** 读取接线图 */
    fun openImage(ctx: Context, path: String) = ctx.assets.open(path)
}
