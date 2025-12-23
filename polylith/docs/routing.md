## Routing (no dependencies)

This project intentionally avoids routing libraries to keep the learning surface small.
Instead, it uses a **minimal hash-based router** implemented in plain ClojureScript.

### Design
- **`/callback`** is a real path used only for the Cognito redirect.
- In-app navigation uses **hash routes**:
  - `/#/` → Home
  - `/#/claims` → Claims (guarded)
- Route state is stored in app-db under `:route`.

### Why hash routing?
- No server configuration needed
- Works with static hosting (S3 + CloudFront)
- Easy to understand for beginners
- Keeps OAuth redirect (`/callback`) clean and unambiguous

### Guarded route example
The `/claims` page is protected:
- If the user is logged in → page renders
- If not logged in → redirected to Home and shown an error message

This is implemented in the `:route/changed` event.

### Where to look in code
- `routes.cljs` – hash parsing + navigation helpers
- `events.cljs` – route guard logic
- `subs.cljs` – route subscriptions
- `views.cljs` – navigation links + pages
