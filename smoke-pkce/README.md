# Smoke Test (PKCE)

This directory contains a tiny static SPA used to validate **Authorization Code + PKCE** with:

- CloudFront + S3 static hosting
- Cognito Hosted UI redirects
- Google IdP integration
- PKCE token exchange from the browser (`/oauth2/token`)

It is intentionally framework-free so you can confirm PKCE works before wiring it into re-frame.

---

## Files

- `index.html` — static entrypoint (committed)
- `app.example.js` — configuration template (committed)
- `app.js` — your local configured version (**DO NOT COMMIT**)

---

## Prerequisites

Your Cognito App Client must allow **Authorization Code flow** (code-only is fine).

This directory’s CloudFormation template sets:

- `AllowedOAuthFlows: [ code ]`

---

## Setup

### 1) Copy the template

```bash
cp app.example.js app.js
```

### 2) Fill in placeholders in `app.js`

Replace:

- `__HOSTED_UI__` (CloudFormation output: `HostedUiDomain`)
- `__CLIENT_ID__` (CloudFormation output: `UserPoolClientId`)

Fetch values:

```bash
HOSTED_UI=$(aws cloudformation describe-stacks \
  --stack-name sso-poc-pkce-cognito \
  --region us-east-1 \
  --query "Stacks[0].Outputs[?OutputKey=='HostedUiDomain'].OutputValue" \
  --output text)

CLIENT_ID=$(aws cloudformation describe-stacks \
  --stack-name sso-poc-pkce-cognito \
  --region us-east-1 \
  --query "Stacks[0].Outputs[?OutputKey=='UserPoolClientId'].OutputValue" \
  --output text)

echo $HOSTED_UI
echo $CLIENT_ID
```

> If you changed `PROJECT_NAME`, substitute it in the stack name above.

---

### 3) Deploy infra

From this directory:

```bash
./scripts/check-prereqs.sh
./scripts/deploy.sh
```

---

### 4) Upload the smoke test to S3

```bash
BUCKET=$(aws cloudformation describe-stacks \
  --stack-name sso-poc-pkce-site \
  --region us-east-1 \
  --query "Stacks[0].Outputs[?OutputKey=='BucketName'].OutputValue" \
  --output text)

aws s3 sync . "s3://$BUCKET/" --exclude "infra/*" --exclude "scripts/*" --delete
```

---

### 5) Invalidate CloudFront

```bash
DIST=$(aws cloudformation describe-stacks \
  --stack-name sso-poc-pkce-site \
  --region us-east-1 \
  --query "Stacks[0].Outputs[?OutputKey=='DistributionId'].OutputValue" \
  --output text)

aws cloudfront create-invalidation --distribution-id "$DIST" --paths "/*"
```

---

### 6) Test in browser

Open:

```
https://<your-domain>/
```

Click **Login with Google**. You should:

1. Be redirected to Cognito Hosted UI
2. Authenticate with Google
3. Return to `/callback?code=...`
4. Perform token exchange in the browser
5. End back at `/` with decoded JWT claims displayed

---

## Recommended .gitignore entry

Add this to your repo `.gitignore`:

```gitignore
# Smoke PKCE local config
smoke-pkce/app.js
```

---

## Switching Between Smoke Variants

Both this directory and the sibling smoke directory provision AWS resources that assume ownership
of the same domain and Cognito configuration.

If you deployed the other smoke variant previously, you **must destroy its stacks before deploying this one**,
unless you intentionally change the domain name and stack parameters.

### To destroy the other variant

From the other directory:

```bash
./scripts/destroy.sh
```

Wait for all stacks to reach `DELETE_COMPLETE`, then deploy this variant.

### Advanced: running both at once

If you want to run both variants simultaneously, override at least:

- `DOMAIN_NAME`
- `PROJECT_NAME`
- `COGNITO_DOMAIN_PREFIX`

for one of the variants so their AWS resources do not collide.
