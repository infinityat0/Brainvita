package com.infinityat0.brainvita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.infinityat0.brainvita.ui.BrainvitaScreen
import com.infinityat0.brainvita.ui.BrainvitaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            BrainvitaTheme {
                BrainvitaScreen()
            }
        }
    }
}
