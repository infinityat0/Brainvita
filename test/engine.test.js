import { test } from "node:test";
import assert from "node:assert/strict";
import {
  CENTER,
  EMPTY,
  OFF,
  PEG,
  applyMove,
  countPegs,
  createBoard,
  gameStatus,
  legalTargets,
  moveError,
  solvePerfect,
} from "../public/engine.js";

test("new board has 32 marbles and an empty centre", () => {
  const b = createBoard();
  assert.equal(countPegs(b), 32);
  assert.equal(b[CENTER][CENTER], EMPTY);
  assert.equal(b[0][0], OFF);
  assert.equal(b[0][2], PEG);
  assert.equal(gameStatus(b), "playing");
});

test("only four opening moves exist, all into the centre", () => {
  const b = createBoard();
  const opening = [];
  for (let r = 0; r < 7; r++)
    for (let c = 0; c < 7; c++)
      for (const t of legalTargets(b, [r, c])) opening.push([[r, c], t]);
  assert.equal(opening.length, 4);
  for (const [, to] of opening) assert.deepEqual(to, [3, 3]);
});

test("applying a move removes the jumped marble", () => {
  const b = createBoard();
  const next = applyMove(b, [1, 3], [3, 3]);
  assert.equal(next[1][3], EMPTY);
  assert.equal(next[2][3], EMPTY);
  assert.equal(next[3][3], PEG);
  assert.equal(countPegs(next), 31);
  assert.equal(countPegs(b), 32, "original board is not mutated");
});

test("illegal moves are rejected with a reason", () => {
  const b = createBoard();
  assert.match(moveError(b, [3, 3], [1, 3]), /marble to move/);
  assert.match(moveError(b, [1, 3], [2, 3]), /empty hole/);
  const afterOne = applyMove(b, [1, 3], [3, 3]);
  assert.match(moveError(afterOne, [3, 1], [1, 3]), /diagonal/i);
  assert.match(moveError(afterOne, [0, 3], [2, 3]), /over a marble/);
  assert.match(moveError(afterOne, [4, 3], [1, 3]), /exactly one/);
  assert.throws(() => applyMove(b, [3, 0], [3, 3]), /exactly one/);
});

test("detects a win and a stuck position", () => {
  const empty = createBoard().map((row) => row.map((v) => (v === OFF ? OFF : EMPTY)));
  const one = empty.map((r) => r.slice());
  one[3][3] = PEG;
  assert.equal(gameStatus(one), "won");

  const two = empty.map((r) => r.slice());
  two[0][2] = PEG;
  two[6][4] = PEG;
  assert.equal(gameStatus(two), "stuck");
});

test("the engine admits a full solution ending in the centre", () => {
  // Small memoised DFS proving a winning line exists under these rules.
  const seen = new Set();
  const key = (b) => b.flat().join("");
  function solve(b) {
    if (countPegs(b) === 1) return b[CENTER][CENTER] === PEG;
    const k = key(b);
    if (seen.has(k)) return false;
    seen.add(k);
    for (let r = 0; r < 7; r++)
      for (let c = 0; c < 7; c++)
        if (b[r][c] === PEG)
          for (const t of legalTargets(b, [r, c])) if (solve(applyMove(b, [r, c], t))) return true;
    return false;
  }
  assert.equal(solve(createBoard()), true);
});

test("solvePerfect returns a legal 31-move game ending in the centre", () => {
  let b = createBoard();
  const moves = solvePerfect(b);
  assert.equal(moves.length, 31);
  for (const [from, to] of moves) {
    assert.equal(moveError(b, from, to), null);
    b = applyMove(b, from, to);
  }
  assert.equal(countPegs(b), 1);
  assert.equal(b[CENTER][CENTER], PEG);
  assert.equal(gameStatus(b), "won");
});

test("solvePerfect reports null when no perfect finish exists", () => {
  const b = createBoard().map((row) => row.map((v) => (v === OFF ? OFF : EMPTY)));
  b[0][2] = PEG;
  b[6][4] = PEG;
  assert.equal(solvePerfect(b), null);
});
