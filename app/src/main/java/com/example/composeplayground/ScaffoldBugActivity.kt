package com.example.composeplayground

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Reproduces not on every click. Some delay is needed for some reason.
 *
 * Problem with [Scaffold] is in `content: @Composable () -> Unit`.
 * Fixes if move inners of [Content] into [setContent] or move [stableObj] out of [ScaffoldBugActivity].
 */
class ScaffoldBugActivity : ComponentActivity() {
    init {
        Log.e("MY_TAG", "-------------------\n\n\n")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Content(stableObj)
        }
    }

    private val stableObj = StableClass() // in prod here is injected `viewModel`.
}

// private val stableObj = StableClass() // if moved here - bug cease to reproduce.

@Stable
internal class StableClass

@Composable
private fun Content(stableObj: StableClass) {
    // If WHOLE content of this method moved to [setContent{}] inside activity - bug
    // cease to reproduce.
    ComposableWrapper { // If wrapper removed - bug cease to reproduce.
        var counter by remember { mutableIntStateOf(1) }
        val clicks = remember { Modifier.clickable { counter++ }}
        // sometimes clicks causes StableComposable to recompose.
        Text("[ScaffoldBug] Increment counter: $counter", clicks)
        StableComposable(stableObj) // this is recomposed for some reason, even though it is @Stable
    }
    // ^ If replaced with code below - same problem occur
    /*LazyColumn { // If wrapper removed - bug cease to reproduce.
        if (counter < 999999) { // Important to observe state inside LazyColumn.content
            item("button") {
                Text("[LazyListBug] Increment counter: $counter", clicks)
            }
        }

        item("stable") {
            StableComposable(stableObj) // this is recomposed for some reason, even though it is @Stable
        }
    }*/
}

@Composable
private fun ComposableWrapper( // in prod this is [Scaffold]
    content: @Composable () -> Unit
) {
    content()
}

@Composable
private fun StableComposable(stableObj: StableClass) {
    Log.e("MY_TAG", "stableObj=${stableObj.hashCode()}")
}
