package com.yang.yangshi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yang.yangshi.ui.navigation.MainAppNavigation
import com.yang.yangshi.ui.theme.YangShiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YangShiTheme {
                MainAppNavigation()
            }
        }
    }
}
