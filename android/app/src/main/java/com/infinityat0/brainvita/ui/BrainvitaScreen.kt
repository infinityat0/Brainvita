package com.infinityat0.brainvita.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infinityat0.brainvita.game.Hole
import com.infinityat0.brainvita.game.Pos
import com.infinityat0.brainvita.game.SIZE

private val MAX_BOARD = 440.dp

@Composable
fun BrainvitaScreen(vm: BrainvitaViewModel = viewModel()) {
    var showRules by rememberSaveable { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Brainvita", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Jump marbles until only one is left.",
                    color = LocalBoardColors.current.muted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                Stat("Moves", vm.moves.toString())
                Stat("Marbles", vm.marbles.toString())
            }

            Board(
                vm = vm,
                onTap = { r, c ->
                    when (vm.tap(r, c)) {
                        TapResult.MOVED -> haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                        TapResult.REJECTED -> haptics.performHapticFeedback(HapticFeedbackType.Reject)
                        TapResult.SELECTED, TapResult.DESELECTED ->
                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        TapResult.IGNORED -> Unit
                    }
                },
            )

            StatusMessage(vm.message)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = vm::undo, enabled = vm.canUndo) { Text("Undo") }
                Button(onClick = vm::newGame) { Text("New game") }
            }
            FilledTonalButton(onClick = vm::toggleDemo) {
                Text(if (vm.demoActive) "Stop" else "Watch perfect game")
            }
            TextButton(onClick = { showRules = true }) { Text("How to play") }
        }
    }

    if (showRules) RulesDialog(onDismiss = { showRules = false })
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = LocalBoardColors.current.muted,
            letterSpacing = 1.sp,
        )
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StatusMessage(message: Message) {
    val color = when (message.kind) {
        MessageKind.INFO -> MaterialTheme.colorScheme.onBackground
        MessageKind.WIN -> LocalBoardColors.current.accent
        MessageKind.ERROR, MessageKind.LOSE -> MaterialTheme.colorScheme.error
    }
    Text(
        message.text,
        color = color,
        fontWeight = FontWeight.Medium,
        style = if (message.kind == MessageKind.WIN) {
            MaterialTheme.typography.titleMedium
        } else {
            MaterialTheme.typography.bodyLarge
        },
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    )
}

/** The round wooden board, sized to the available width. */
@Composable
private fun Board(vm: BrainvitaViewModel, onTap: (Int, Int) -> Unit) {
    val colors = LocalBoardColors.current
    val targets = vm.targets

    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val side = min(maxWidth, MAX_BOARD)
        val gap = (side * 0.015f).coerceIn(4.dp, 8.dp)
        val pad = gap * 2
        val cell = (side - pad * 2 - gap * (SIZE - 1)) / SIZE

        Box(
            Modifier
                .size(side)
                .shadow(10.dp, CircleShape)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(colors.board, colors.boardEdge)))
                .padding(pad),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                for (r in 0 until SIZE) {
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        for (c in 0 until SIZE) {
                            val pos = Pos(r, c)
                            val hole = vm.board[pos]
                            if (hole == Hole.OFF) {
                                Spacer(Modifier.size(cell))
                            } else {
                                Hole(
                                    pos = pos,
                                    hole = hole,
                                    selected = vm.selected == pos,
                                    target = pos in targets,
                                    size = cell,
                                    enabled = !vm.demoActive,
                                    onTap = onTap,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Hole(
    pos: Pos,
    hole: Hole,
    selected: Boolean,
    target: Boolean,
    size: Dp,
    enabled: Boolean,
    onTap: (Int, Int) -> Unit,
) {
    val colors = LocalBoardColors.current
    val description = buildString {
        append("Row ${pos.r + 1}, column ${pos.c + 1}: ")
        append(if (hole == Hole.PEG) "marble" else "empty")
        if (selected) append(", selected")
        if (target) append(", can jump here")
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button) { onTap(pos.r, pos.c) }
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        // The hole itself (lit up when it is a legal landing spot).
        Box(
            Modifier
                .size(size * 0.76f)
                .clip(CircleShape)
                .background(if (target) colors.accent else colors.hole)
                .then(if (target) Modifier.border(3.dp, Color.White.copy(alpha = 0.25f), CircleShape) else Modifier),
        )
        if (hole == Hole.PEG) {
            Box(
                Modifier
                    .size(size * 0.88f)
                    .offset(y = if (selected) -(size * 0.05f) else 0.dp)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .drawBehind {
                        drawRect(
                            Brush.radialGradient(
                                colorStops = arrayOf(
                                    0f to colors.marbleHighlight,
                                    0.45f to colors.marble,
                                    1f to colors.marbleShade,
                                ),
                                center = Offset(this.size.width * 0.35f, this.size.height * 0.30f),
                                radius = this.size.width * 0.9f,
                            ),
                        )
                    }
                    .then(
                        if (selected) Modifier.border(3.dp, colors.accent, CircleShape) else Modifier,
                    ),
            )
        }
    }
}

@Composable
private fun RulesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Got it") } },
        title = { Text("How to play") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "Tap a marble, then tap an empty hole two spaces away to jump over the marble between them.",
                    "The jumped marble is removed. Jumps go up, down, left or right, never diagonally.",
                    "You win when only one marble remains; finishing in the centre is a perfect game.",
                    "Stuck? Press Watch perfect game to see the computer solve it in 31 moves, then use Undo to step back through the solution.",
                ).forEach { Text("• $it") }
            }
        },
    )
}
