# Local signup/login (Cognito Hosted UI)

This repo uses **Cognito Hosted UI** for both:

- **Federated login** (e.g., Google)
- **Local username/password** signup + login (Cognito native users)

The SPA does **not** implement signup/login forms. It redirects to Cognito Hosted UI.

---

## Enable local (Cognito) users

In the AWS Console:

1. **Cognito → User pools → (your pool)**
2. Go to **Sign-in experience**
3. Ensure **Cognito user pool** is enabled as a sign-in option (local users)
4. Enable **Self sign-up** (if you want learners to create accounts)

### Common settings to choose

- **Sign-in options**: email (simplest) or username+email
- **Required attributes**: usually email
- **Verification**: email verification ON (production), OFF (PoC) if it blocks you

---

## Hosted UI + App client settings

1. **User pools → (your pool) → App integration**
2. Open your **App client**
3. Confirm:
   - **Authorization code grant** enabled
   - **Implicit grant** disabled
   - **No client secret** (public client for SPA)
   - Callback URLs include:
     - `http://localhost:3000/callback`
     - `https://<your-domain>/callback`
   - Sign-out URLs include:
     - `http://localhost:3000/`
     - `https://<your-domain>/`

---

## Test local signup/login (happy path)

1. Start your dev server (`shadow-cljs watch app`)
2. Click **Login** (Hosted UI opens)
3. Choose **Sign up** and create a user
4. Confirm you return to `/callback` and then back to `/`

### If signup fails

Common causes:

- Password policy too strict
- Required attributes not provided
- Email verification required but emails are not being delivered (SES)

---

## Notes

- This repo intentionally keeps UI simple; you can add a custom UI later, but Hosted UI is the fastest path to a correct OAuth/OIDC integration.
