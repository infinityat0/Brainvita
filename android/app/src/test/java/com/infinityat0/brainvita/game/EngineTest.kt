package com.infinityat0.brainvita.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Mirrors web/test/engine.test.js. */
class EngineTest {

    private fun p(r: Int, c: Int) = Pos(r, c)

    @Test
    fun newBoardHas32MarblesAndAnEmptyCentre() {
        val b = Board.standard()
        assertEquals(32, countPegs(b))
        assertEquals(Hole.EMPTY, b[CENTER, CENTER])
        assertEquals(Hole.OFF, b[0, 0])
        assertEquals(Hole.PEG, b[0, 2])
        assertEquals(Status.PLAYING, gameStatus(b))
    }

    @Test
    fun onlyFourOpeningMovesExistAllIntoTheCentre() {
        val b = Board.standard()
        val opening = mutableListOf<Move>()
        for (r in 0 until 7) for (c in 0 until 7) {
            for (t in legalTargets(b, p(r, c))) opening.add(Move(p(r, c), t))
        }
        assertEquals(4, opening.size)
        for (m in opening) assertEquals(p(3, 3), m.to)
    }

    @Test
    fun applyingAMoveRemovesTheJumpedMarble() {
        val b = Board.standard()
        val next = applyMove(b, p(1, 3), p(3, 3))
        assertEquals(Hole.EMPTY, next[1, 3])
        assertEquals(Hole.EMPTY, next[2, 3])
        assertEquals(Hole.PEG, next[3, 3])
        assertEquals(31, countPegs(next))
        assertEquals("original board is not mutated", 32, countPegs(b))
    }

    @Test
    fun illegalMovesAreRejectedWithAReason() {
        val b = Board.standard()
        assertTrue(moveError(b, p(3, 3), p(1, 3))!!.contains("marble to move"))
        assertTrue(moveError(b, p(1, 3), p(2, 3))!!.contains("empty hole"))
        val afterOne = applyMove(b, p(1, 3), p(3, 3))
        assertTrue(moveError(afterOne, p(3, 1), p(1, 3))!!.contains("diagonal", ignoreCase = true))
        assertTrue(moveError(afterOne, p(0, 3), p(2, 3))!!.contains("over a marble"))
        assertTrue(moveError(afterOne, p(4, 3), p(1, 3))!!.contains("exactly one"))
        val e = assertThrows(IllegalArgumentException::class.java) { applyMove(b, p(3, 0), p(3, 3)) }
        assertTrue(e.message!!.contains("exactly one"))
    }

    @Test
    fun detectsAWinAndAStuckPosition() {
        val one = Board.empty().with(mapOf(p(3, 3) to Hole.PEG))
        assertEquals(Status.WON, gameStatus(one))

        val two = Board.empty().with(mapOf(p(0, 2) to Hole.PEG, p(6, 4) to Hole.PEG))
        assertEquals(Status.STUCK, gameStatus(two))
    }

    @Test
    fun theEngineAdmitsAFullSolutionEndingInTheCentre() {
        // Independent memoised DFS proving a winning line exists under these rules.
        val seen = HashSet<String>()
        fun solve(b: Board): Boolean {
            if (countPegs(b) == 1) return b[CENTER, CENTER] == Hole.PEG
            if (!seen.add(b.key())) return false
            for (r in 0 until 7) for (c in 0 until 7) {
                if (b[r, c] == Hole.PEG) {
                    for (t in legalTargets(b, p(r, c))) if (solve(applyMove(b, p(r, c), t))) return true
                }
            }
            return false
        }
        assertTrue(solve(Board.standard()))
    }

    @Test
    fun solvePerfectReturnsALegal31MoveGameEndingInTheCentre() {
        var b = Board.standard()
        val moves = solvePerfect(b)
        assertNotNull(moves)
        assertEquals(31, moves!!.size)
        for ((from, to) in moves) {
            assertNull(moveError(b, from, to))
            b = applyMove(b, from, to)
        }
        assertEquals(1, countPegs(b))
        assertEquals(Hole.PEG, b[CENTER, CENTER])
        assertEquals(Status.WON, gameStatus(b))
    }

    @Test
    fun solvePerfectReportsNullWhenNoPerfectFinishExists() {
        val b = Board.empty().with(mapOf(p(0, 2) to Hole.PEG, p(6, 4) to Hole.PEG))
        assertNull(solvePerfect(b))
    }

    @Test
    fun boardEqualityIsStructural() {
        assertEquals(Board.standard(), Board.standard())
        assertFalse(Board.standard() == applyMove(Board.standard(), p(1, 3), p(3, 3)))
    }
}
