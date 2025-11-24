package io.github.xpler_example.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.xpler2.XplerModuleStatus
import io.github.xpler_example.ui.theme.XplerExampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XplerExampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        val instance = XplerModuleStatus.instance
                        if (instance?.isActivate == true) {
                            Text(
                                text = "Activated. " +
                                        "\nframeworkName: ${instance.frameworkName}, apiVersion: ${instance.apiVersion}"
                            )
                        } else {
                            Text(
                                text = "Is Not Activate.",
                            )
                        }
                    }
                }
            }
        }
    }
}