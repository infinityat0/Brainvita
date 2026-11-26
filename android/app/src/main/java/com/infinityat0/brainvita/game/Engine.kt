package com.infinityat0.brainvita.game

import kotlin.math.abs

/** Pure game logic for Brainvita (English peg solitaire). Port of web/public/engine.js. */

const val SIZE = 7
const val CENTER = 3

enum class Hole { OFF, EMPTY, PEG }

data class Pos(val r: Int, val c: Int)

data class Move(val from: Pos, val to: Pos)

enum class Status { PLAYING, WON, STUCK }

private val DIRECTIONS = listOf(Pos(-2, 0), Pos(2, 0), Pos(0, -2), Pos(0, 2))

fun isOnBoard(r: Int, c: Int): Boolean {
    fun inArm(n: Int) = n in 2..4
    return r in 0 until SIZE && c in 0 until SIZE && (inArm(r) || inArm(c))
}

/** Immutable 7x7 board; moves return new boards. */
class Board private constructor(private val holes: Array<Hole>) {

    operator fun get(r: Int, c: Int): Hole = if (isOnBoard(r, c)) holes[r * SIZE + c] else Hole.OFF

    operator fun get(p: Pos): Hole = get(p.r, p.c)

    fun with(changes: Map<Pos, Hole>): Board {
        val next = holes.copyOf()
        for ((p, h) in changes) next[p.r * SIZE + p.c] = h
        return Board(next)
    }

    /** Stable string form, used to remember dead-end positions. */
    fun key(): String = holes.joinToString("") { it.ordinal.toString() }

    override fun equals(other: Any?) = other is Board && holes.contentEquals(other.holes)

    override fun hashCode() = holes.contentHashCode()

    companion object {
        /** Standard 33-hole cross board, every hole filled except the centre. */
        fun standard(): Board = Board(
            Array(SIZE * SIZE) { i ->
                val r = i / SIZE
                val c = i % SIZE
                when {
                    !isOnBoard(r, c) -> Hole.OFF
                    r == CENTER && c == CENTER -> Hole.EMPTY
                    else -> Hole.PEG
                }
            },
        )

        /** Every on-board hole empty. */
        fun empty(): Board = Board(
            Array(SIZE * SIZE) { i -> if (isOnBoard(i / SIZE, i % SIZE)) Hole.EMPTY else Hole.OFF },
        )
    }
}

/**
 * Checks a move and returns null if it is legal, otherwise a human-readable
 * reason it is not.
 */
fun moveError(board: Board, from: Pos, to: Pos): String? {
    if (board[from] != Hole.PEG) return "Pick a marble to move."
    if (board[to] != Hole.EMPTY) return "You can only land in an empty hole."
    val dr = to.r - from.r
    val dc = to.c - from.c
    if (dr != 0 && dc != 0) return "No diagonal jumps."
    if (abs(dr) + abs(dc) != 2) return "Jump over exactly one marble."
    if (board[from.r + dr / 2, from.c + dc / 2] != Hole.PEG) return "You can only jump over a marble."
    return null
}

fun isLegalMove(board: Board, from: Pos, to: Pos) = moveError(board, from, to) == null

/** Returns a new board with the move applied. Throws on an illegal move. */
fun applyMove(board: Board, from: Pos, to: Pos): Board {
    moveError(board, from, to)?.let { throw IllegalArgumentException(it) }
    val over = Pos((from.r + to.r) / 2, (from.c + to.c) / 2)
    return board.with(mapOf(from to Hole.EMPTY, over to Hole.EMPTY, to to Hole.PEG))
}

fun legalTargets(board: Board, from: Pos): List<Pos> =
    DIRECTIONS.map { Pos(from.r + it.r, from.c + it.c) }.filter { isLegalMove(board, from, it) }

fun hasAnyMove(board: Board): Boolean {
    for (r in 0 until SIZE) for (c in 0 until SIZE) {
        if (board[r, c] == Hole.PEG && legalTargets(board, Pos(r, c)).isNotEmpty()) return true
    }
    return false
}

fun countPegs(board: Board): Int {
    var n = 0
    for (r in 0 until SIZE) for (c in 0 until SIZE) if (board[r, c] == Hole.PEG) n++
    return n
}

fun gameStatus(board: Board): Status = when {
    countPegs(board) == 1 -> Status.WON
    hasAnyMove(board) -> Status.PLAYING
    else -> Status.STUCK
}

/**
 * Finds a sequence of moves that leaves a single marble in the centre (a
 * "perfect game"), or null if none exists from this position. Depth-first
 * search that remembers dead-end positions.
 */
fun solvePerfect(start: Board): List<Move>? {
    val dead = HashSet<String>()
    val path = ArrayList<Move>()

    fun search(b: Board): Boolean {
        if (countPegs(b) == 1) return b[CENTER, CENTER] == Hole.PEG
        val k = b.key()
        if (k in dead) return false
        for (r in 0 until SIZE) for (c in 0 until SIZE) {
            if (b[r, c] != Hole.PEG) continue
            val from = Pos(r, c)
            for (to in legalTargets(b, from)) {
                path.add(Move(from, to))
                if (search(applyMove(b, from, to))) return true
                path.removeAt(path.lastIndex)
            }
        }
        dead.add(k)
        return false
    }

    return if (search(start)) path.toList() else null
}
