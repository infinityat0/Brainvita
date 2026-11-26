package com.infinityat0.brainvita.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Drives the real Compose UI on the JVM. Screenshots land in
 * app/build/screenshots/ for a quick visual check without an emulator.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi")
class BrainvitaScreenTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun selectingAndJumpingAMarble() {
        compose.setContent { BrainvitaTheme(dark = false) { BrainvitaScreen() } }
        compose.onNodeWithContentDescription("Row 2, column 4: marble").assertExists()
        compose.onNodeWithContentDescription("Row 4, column 4: empty").assertExists()
        snapshot("start")

        compose.onNodeWithContentDescription("Row 2, column 4: marble").performClick()
        compose.onNodeWithContentDescription("Row 2, column 4: marble, selected").assertExists()
        compose.onNodeWithContentDescription("Row 4, column 4: empty, can jump here").assertExists()
        snapshot("selected")

        compose.onNodeWithContentDescription("Row 4, column 4: empty, can jump here").performClick()
        compose.onNodeWithContentDescription("Row 2, column 4: empty").assertExists()
        compose.onNodeWithContentDescription("Row 3, column 4: empty").assertExists()
        compose.onNodeWithContentDescription("Row 4, column 4: marble").assertExists()
        compose.onNodeWithText("31").assertExists()
        snapshot("after-move")
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp-night-xxhdpi")
    fun darkModeAfterMoveAndUndo() {
        compose.setContent { BrainvitaTheme(dark = true) { BrainvitaScreen() } }
        compose.onNodeWithContentDescription("Row 4, column 2: marble").performClick()
        compose.onNodeWithContentDescription("Row 4, column 4: empty, can jump here").performClick()
        snapshot("after-move-dark")

        compose.onNodeWithText("Undo").performClick()
        compose.onNodeWithContentDescription("Row 4, column 4: empty").assertExists()
        compose.onNodeWithText("Move undone.").assertExists()

        compose.onNodeWithText("How to play").performClick()
        compose.onNodeWithText("Got it").assertExists()
        snapshot("rules-dark")
    }

    /** Draws the activity's window into a PNG (captureToImage() hangs under Robolectric). */
    private fun snapshot(name: String) {
        compose.waitForIdle()
        val view = compose.activity.window.decorView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        val dir = File("build/screenshots").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
