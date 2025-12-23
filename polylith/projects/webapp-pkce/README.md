# webapp-pkce (shadow-cljs)

From this folder:

```bash
npm install
npx shadow-cljs watch app
```

Open: [http://localhost:3000]

## Runtime config

Copy:

- `../../bases/webapp/resources/public/config.example.js` -> `../../bases/webapp/resources/public/config.js`

Replace:

- `__HOSTED_UI__`
- `__CLIENT_ID__`

> For local Hosted UI testing, temporarily add `http://localhost:3000/callback` to your Cognito App Client Callback URLs.
