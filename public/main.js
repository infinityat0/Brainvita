import {
  CENTER,
  OFF,
  PEG,
  SIZE,
  applyMove,
  countPegs,
  createBoard,
  gameStatus,
  legalTargets,
  moveError,
} from "./engine.js";

const boardEl = document.getElementById("board");
const movesEl = document.getElementById("moves");
const marblesEl = document.getElementById("marbles");
const messageEl = document.getElementById("message");
const undoBtn = document.getElementById("undo");
const restartBtn = document.getElementById("restart");

let history = []; // previous boards, for undo
let board = createBoard();
let selected = null; // [r, c] or null
let focus = [CENTER, CENTER];
let message = { text: "Pick a marble to start.", kind: "" };

const cells = [];

function buildBoard() {
  boardEl.replaceChildren();
  for (let r = 0; r < SIZE; r++) {
    cells[r] = [];
    for (let c = 0; c < SIZE; c++) {
      const btn = document.createElement("button");
      btn.type = "button";
      btn.className = "cell";
      btn.dataset.r = r;
      btn.dataset.c = c;
      btn.tabIndex = -1;
      cells[r][c] = btn;
      boardEl.append(btn);
    }
  }
}

function isTarget(r, c) {
  return selected !== null && legalTargets(board, selected).some(([tr, tc]) => tr === r && tc === c);
}

function render() {
  const status = gameStatus(board);
  for (let r = 0; r < SIZE; r++) {
    for (let c = 0; c < SIZE; c++) {
      const v = board[r][c];
      const btn = cells[r][c];
      const isSel = selected && selected[0] === r && selected[1] === c;
      btn.classList.toggle("off", v === OFF);
      btn.classList.toggle("peg", v === PEG);
      btn.classList.toggle("selected", Boolean(isSel));
      btn.classList.toggle("target", isTarget(r, c));
      btn.tabIndex = focus[0] === r && focus[1] === c ? 0 : -1;
      btn.disabled = v === OFF;
      btn.setAttribute(
        "aria-label",
        `Row ${r + 1}, column ${c + 1}: ${v === PEG ? "marble" : "empty"}${isSel ? ", selected" : ""}`,
      );
    }
  }
  movesEl.textContent = history.length;
  marblesEl.textContent = countPegs(board);
  undoBtn.disabled = history.length === 0;

  if (status === "won") {
    const perfect = board[CENTER][CENTER] === PEG;
    message = {
      text: perfect ? "Perfect game! The last marble is in the centre." : "You win! Only one marble left.",
      kind: "win",
    };
  } else if (status === "stuck") {
    message = {
      text: `No moves left with ${countPegs(board)} marbles. Undo or start a new game.`,
      kind: "lose",
    };
  }
  messageEl.textContent = message.text;
  messageEl.className = `message ${message.kind}`;
}

function shake(r, c) {
  const btn = cells[r][c];
  btn.classList.remove("shake");
  void btn.offsetWidth; // restart animation
  btn.classList.add("shake");
}

function handleCell(r, c) {
  if (gameStatus(board) !== "playing") return;
  focus = [r, c];
  const v = board[r][c];

  if (v === PEG) {
    const isSame = selected && selected[0] === r && selected[1] === c;
    if (isSame) {
      selected = null;
      message = { text: "", kind: "" };
    } else if (legalTargets(board, [r, c]).length === 0) {
      selected = null;
      message = { text: "That marble has no jumps available.", kind: "error" };
      shake(r, c);
    } else {
      selected = [r, c];
      message = { text: "Now pick a highlighted hole.", kind: "" };
    }
  } else if (selected) {
    const error = moveError(board, selected, [r, c]);
    if (error) {
      message = { text: error, kind: "error" };
      shake(r, c);
    } else {
      history.push(board);
      board = applyMove(board, selected, [r, c]);
      selected = null;
      message = { text: "", kind: "" };
    }
  } else {
    message = { text: "Pick a marble first.", kind: "error" };
  }
  render();
}

function undo() {
  if (history.length === 0) return;
  board = history.pop();
  selected = null;
  message = { text: "Move undone.", kind: "" };
  render();
}

function restart() {
  history = [];
  board = createBoard();
  selected = null;
  focus = [CENTER, CENTER];
  message = { text: "New game. Pick a marble.", kind: "" };
  render();
}

boardEl.addEventListener("click", (e) => {
  const btn = e.target.closest(".cell");
  if (btn) handleCell(Number(btn.dataset.r), Number(btn.dataset.c));
});

boardEl.addEventListener("keydown", (e) => {
  const step = { ArrowUp: [-1, 0], ArrowDown: [1, 0], ArrowLeft: [0, -1], ArrowRight: [0, 1] }[e.key];
  if (!step) return;
  e.preventDefault();
  let [r, c] = focus;
  do {
    r += step[0];
    c += step[1];
  } while (r >= 0 && r < SIZE && c >= 0 && c < SIZE && board[r][c] === OFF);
  if (r < 0 || r >= SIZE || c < 0 || c >= SIZE) return;
  focus = [r, c];
  render();
  cells[r][c].focus();
});

document.addEventListener("keydown", (e) => {
  if ((e.ctrlKey || e.metaKey) && e.key === "z") {
    e.preventDefault();
    undo();
  } else if (e.key === "Escape" && selected) {
    selected = null;
    message = { text: "", kind: "" };
    render();
  }
});

undoBtn.addEventListener("click", undo);
restartBtn.addEventListener("click", restart);

buildBoard();
render();
