# Brainvita

Brainvita (English peg solitaire) in the browser. Jump a marble over a
neighbour into an empty hole to remove it; win by leaving a single marble,
ideally in the centre.

## Play locally

No build step. Serve the `public/` folder over HTTP (ES modules don't load from `file://`):

```sh
npm start          # or: python3 -m http.server -d public
```

## Test

```sh
npm test           # Node 18+, uses the built-in test runner
```

## Layout

- `public/engine.js` – pure game rules (board, move validation, win/stuck detection)
- `public/main.js` – UI: rendering, selection, undo, keyboard controls
- `public/index.html`, `public/style.css` – page and styling (light and dark)
- `test/` – engine tests, including a search proving the start position is solvable

## Deploy

Static site; `vercel.json` serves `public/` with no build. Deploy with
`vercel --prod`, or import the repository in the Vercel dashboard.
