package com.linege.stm32

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.linege.stm32.data.Repository
import com.linege.stm32.data.Settings
import com.linege.stm32.data.Stm32Data
import com.linege.stm32.ui.screen.AboutScreen
import com.linege.stm32.ui.screen.ChapterScreen
import com.linege.stm32.ui.screen.DetailScreen
import com.linege.stm32.ui.screen.HomeScreen
import com.linege.stm32.ui.screen.SearchScreen
import com.linege.stm32.ui.theme.STM32Theme

sealed interface Route {
    data object Home : Route
    data class Chapter(val no: Int) : Route
    data class Detail(val expId: String) : Route
    data object Search : Route
    data object About : Route
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val ctx = LocalContext.current
            var themeMode by remember { mutableIntStateOf(Settings.getThemeMode(ctx)) }

            val dark = when (themeMode) {
                Settings.THEME_LIGHT -> false
                Settings.THEME_DARK -> true
                else -> isSystemInDarkTheme()
            }

            STM32Theme(darkTheme = dark) {
                App(
                    themeMode = themeMode,
                    onThemeChange = { mode ->
                        themeMode = mode
                        Settings.setThemeMode(ctx, mode)
                    }
                )
            }
        }
    }
}

@Composable
private fun App(
    themeMode: Int,
    onThemeChange: (Int) -> Unit
) {
    val ctx = LocalContext.current

    var data by remember { mutableStateOf<Stm32Data?>(null) }
    var route by remember { mutableStateOf<Route>(Route.Home) }
    var backStack by remember { mutableStateOf(listOf<Route>()) }

    LaunchedEffect(Unit) {
        data = Repository.load(ctx)
    }

    fun navigate(to: Route) {
        backStack = backStack + route
        route = to
    }

    fun goBack() {
        if (backStack.isNotEmpty()) {
            route = backStack.last()
            backStack = backStack.dropLast(1)
        }
    }

    BackHandler(enabled = backStack.isNotEmpty() || route != Route.Home) {
        if (backStack.isNotEmpty()) {
            route = backStack.last()
            backStack = backStack.dropLast(1)
        } else {
            route = Route.Home
        }
    }

    val d = data
    if (d == null) {
        LoadingScreen()
        return
    }

    when (val r = route) {
        is Route.Home -> HomeScreen(
            data = d,
            onOpenChapter = { navigate(Route.Chapter(it)) },
            onOpenExperiment = { navigate(Route.Detail(it.id)) },
            onOpenSearch = { route = Route.Search },
            onOpenAbout = { route = Route.About }
        )

        is Route.Chapter -> ChapterScreen(
            data = d,
            chapterNo = r.no,
            onBack = { goBack() },
            onOpenExperiment = { navigate(Route.Detail(it.id)) },
            onOpenSearch = { navigate(Route.Search) },
            onOpenAbout = { navigate(Route.About) }
        )

        is Route.Detail -> {
            val exp = d.experiments.find { it.id == r.expId }
            if (exp == null) {
                LaunchedEffect(Unit) { goBack() }
            } else {
                val idx = d.experiments.indexOf(exp)
                DetailScreen(
                    exp = exp,
                    chapter = d.chapters[exp.chapter],
                    prev = if (idx > 0) d.experiments[idx - 1] else null,
                    next = if (idx < d.experiments.size - 1) d.experiments[idx + 1] else null,
                    onBack = { goBack() },
                    onOpenExperiment = { navigate(Route.Detail(it.id)) }
                )
            }
        }

        is Route.Search -> SearchScreen(
            data = d,
            onBack = { goBack() },
            onOpenExperiment = { navigate(Route.Detail(it.id)) },
            onOpenChapter = { navigate(Route.Chapter(it)) },
            onOpenAbout = { navigate(Route.About) }
        )

        is Route.About -> AboutScreen(
            data = d,
            themeMode = themeMode,
            onThemeChange = onThemeChange,
            onBack = {
                if (backStack.isEmpty()) route = Route.Home else goBack()
            },
            onOpenSearch = { navigate(Route.Search) }
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(
                text = "正在加载…",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
