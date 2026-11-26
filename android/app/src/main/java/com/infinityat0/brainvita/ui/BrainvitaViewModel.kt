package com.infinityat0.brainvita.ui

import android.app.Application
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.infinityat0.brainvita.game.Board
import com.infinityat0.brainvita.game.CENTER
import com.infinityat0.brainvita.game.Hole
import com.infinityat0.brainvita.game.Pos
import com.infinityat0.brainvita.game.Status
import com.infinityat0.brainvita.game.applyMove
import com.infinityat0.brainvita.game.countPegs
import com.infinityat0.brainvita.game.gameStatus
import com.infinityat0.brainvita.game.legalTargets
import com.infinityat0.brainvita.game.moveError
import com.infinityat0.brainvita.game.solvePerfect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class MessageKind { INFO, ERROR, WIN, LOSE }

data class Message(val text: String = "", val kind: MessageKind = MessageKind.INFO)

/** What a tap did, so the screen can pick the right haptic. */
enum class TapResult { IGNORED, SELECTED, DESELECTED, MOVED, REJECTED }

/**
 * Holds the game across configuration changes and drives the "Watch perfect
 * game" playback. The web version keeps nothing in localStorage, so there is
 * nothing to persist here either.
 */
class BrainvitaViewModel(app: Application) : AndroidViewModel(app) {

    var board by mutableStateOf(Board.standard())
        private set

    /** Previous boards, for undo. */
    private var history by mutableStateOf(listOf<Board>())

    var selected by mutableStateOf<Pos?>(null)
        private set

    private var stored by mutableStateOf(Message("Pick a marble to start."))

    /** True while the computer is playing the perfect game. */
    var demoActive by mutableStateOf(false)
        private set

    private var demoTarget by mutableStateOf<Pos?>(null)

    private var demoJob: Job? = null

    val moves: Int get() = history.size

    val marbles: Int by derivedStateOf { countPegs(board) }

    val status: Status by derivedStateOf { gameStatus(board) }

    val canUndo: Boolean get() = history.isNotEmpty() && !demoActive

    /** Holes the selected marble can jump to (or the computer's chosen landing hole). */
    val targets: List<Pos>
        get() {
            if (demoActive) return listOfNotNull(demoTarget)
            val s = selected ?: return emptyList()
            return legalTargets(board, s)
        }

    /** The status line; finished games override whatever was last said. */
    val message: Message
        get() = when (status) {
            Status.WON -> Message(
                if (board[CENTER, CENTER] == Hole.PEG) {
                    "Perfect game! The last marble is in the centre."
                } else {
                    "You win! Only one marble left."
                },
                MessageKind.WIN,
            )
            Status.STUCK -> Message(
                "No moves left with $marbles marbles. Undo or start a new game.",
                MessageKind.LOSE,
            )
            Status.PLAYING -> stored
        }

    fun tap(r: Int, c: Int): TapResult {
        if (demoActive || status != Status.PLAYING) return TapResult.IGNORED
        val here = Pos(r, c)
        val v = board[here]
        val sel = selected

        if (v == Hole.PEG) {
            if (sel == here) {
                selected = null
                stored = Message()
                return TapResult.DESELECTED
            }
            if (legalTargets(board, here).isEmpty()) {
                selected = null
                stored = Message("That marble has no jumps available.", MessageKind.ERROR)
                return TapResult.REJECTED
            }
            selected = here
            stored = Message("Now pick a highlighted hole.")
            return TapResult.SELECTED
        }
        if (v == Hole.OFF) return TapResult.IGNORED
        if (sel == null) {
            stored = Message("Pick a marble first.", MessageKind.ERROR)
            return TapResult.REJECTED
        }
        val error = moveError(board, sel, here)
        if (error != null) {
            stored = Message(error, MessageKind.ERROR)
            return TapResult.REJECTED
        }
        history = history + board
        board = applyMove(board, sel, here)
        selected = null
        stored = Message()
        return TapResult.MOVED
    }

    fun clearSelection() {
        if (selected != null && !demoActive) {
            selected = null
            stored = Message()
        }
    }

    fun undo() {
        if (demoActive || history.isEmpty()) return
        board = history.last()
        history = history.dropLast(1)
        selected = null
        stored = Message("Move undone.")
    }

    fun newGame() {
        stopDemo()
        history = emptyList()
        board = Board.standard()
        selected = null
        stored = Message("New game. Pick a marble.")
    }

    fun toggleDemo() {
        if (demoActive) stopDemo("Stopped. Carry on from here, or press New game.") else startDemo()
    }

    private fun stopDemo(text: String? = null) {
        if (!demoActive) return
        demoJob?.cancel()
        demoJob = null
        demoActive = false
        demoTarget = null
        selected = null
        if (text != null) stored = Message(text)
    }

    private fun startDemo() {
        history = emptyList()
        board = Board.standard()
        selected = null
        demoActive = true
        stored = Message("Watch the computer solve it.")
        demoJob = viewModelScope.launch {
            val moves = withContext(Dispatchers.Default) { solvePerfect(board) }
            if (moves == null) {
                demoActive = false
                stored = Message("Couldn't find a perfect game.", MessageKind.ERROR)
                return@launch
            }
            delay(DEMO_STEP_MS)
            moves.forEachIndexed { i, (from, to) ->
                // Phase 1: show which marble moves and where.
                selected = from
                demoTarget = to
                stored = Message("Computer: move ${i + 1} of ${moves.size}")
                delay(DEMO_STEP_MS)
                // Phase 2: make the jump.
                history = history + board
                board = applyMove(board, from, to)
                selected = null
                demoTarget = null
                delay(DEMO_STEP_MS / 3)
            }
            demoActive = false
        }
    }

    private companion object {
        const val DEMO_STEP_MS = 650L
    }
}
