// Pure game logic for Brainvita (English peg solitaire). No DOM access here,
// so it can be unit-tested in Node.

export const SIZE = 7;
export const OFF = -1;
export const EMPTY = 0;
export const PEG = 1;
export const CENTER = 3;

const DIRECTIONS = [
  [-2, 0],
  [2, 0],
  [0, -2],
  [0, 2],
];

export function isOnBoard(r, c) {
  const inArm = (n) => n >= 2 && n <= 4;
  return r >= 0 && r < SIZE && c >= 0 && c < SIZE && (inArm(r) || inArm(c));
}

/** Standard 33-hole cross board, every hole filled except the centre. */
export function createBoard() {
  return Array.from({ length: SIZE }, (_, r) =>
    Array.from({ length: SIZE }, (_, c) => {
      if (!isOnBoard(r, c)) return OFF;
      return r === CENTER && c === CENTER ? EMPTY : PEG;
    }),
  );
}

export function cloneBoard(board) {
  return board.map((row) => row.slice());
}

export function cellAt(board, r, c) {
  return isOnBoard(r, c) ? board[r][c] : OFF;
}

/**
 * Checks a move and returns null if it is legal, otherwise a human-readable
 * reason it is not.
 */
export function moveError(board, from, to) {
  const [r0, c0] = from;
  const [r1, c1] = to;
  if (cellAt(board, r0, c0) !== PEG) return "Pick a marble to move.";
  if (cellAt(board, r1, c1) !== EMPTY) return "You can only land in an empty hole.";
  const dr = r1 - r0;
  const dc = c1 - c0;
  if (dr !== 0 && dc !== 0) return "No diagonal jumps.";
  const dist = Math.abs(dr) + Math.abs(dc);
  if (dist !== 2) return "Jump over exactly one marble.";
  if (cellAt(board, r0 + dr / 2, c0 + dc / 2) !== PEG) return "You can only jump over a marble.";
  return null;
}

export function isLegalMove(board, from, to) {
  return moveError(board, from, to) === null;
}

/** Returns a new board with the move applied. Throws on an illegal move. */
export function applyMove(board, from, to) {
  const error = moveError(board, from, to);
  if (error) throw new Error(error);
  const next = cloneBoard(board);
  const [r0, c0] = from;
  const [r1, c1] = to;
  next[r0][c0] = EMPTY;
  next[(r0 + r1) / 2][(c0 + c1) / 2] = EMPTY;
  next[r1][c1] = PEG;
  return next;
}

export function legalTargets(board, from) {
  const [r, c] = from;
  return DIRECTIONS.map(([dr, dc]) => [r + dr, c + dc]).filter((to) =>
    isLegalMove(board, from, to),
  );
}

export function hasAnyMove(board) {
  for (let r = 0; r < SIZE; r++) {
    for (let c = 0; c < SIZE; c++) {
      if (board[r][c] === PEG && legalTargets(board, [r, c]).length > 0) return true;
    }
  }
  return false;
}

export function countPegs(board) {
  return board.flat().filter((v) => v === PEG).length;
}

/** "playing" | "won" | "stuck" */
export function gameStatus(board) {
  if (countPegs(board) === 1) return "won";
  return hasAnyMove(board) ? "playing" : "stuck";
}
