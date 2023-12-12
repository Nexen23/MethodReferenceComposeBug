package com.example.composeplayground

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Method reference leads to `Unstabl100` and becomes reason for recomposition.
 */
class MethodReferenceBugActivity : ComponentActivity() {
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

@Stable
internal class StableClass(
    @Stable
    val data: Int = 123
)

@Composable
private fun Content(stableObj2: StableClass) {
    Column {
        var counter by remember { mutableIntStateOf(1) }
        val clicks = remember { Modifier.clickable { counter++ } }

        Text("[ScaffoldBug] Increment counter: $counter", clicks)
        StableComposable("Lambda", { stableObj2.data })
        StableComposable("MethodRef", stableObj2::data)
    }
}

@Composable
private fun StableComposable(text: String, stableObj3: () -> Int) {
    Log.e("MY_TAG", "[$text] stableObj::method=${stableObj3.hashCode()}")
    Text("[$text] StableComposableMethods")
}
