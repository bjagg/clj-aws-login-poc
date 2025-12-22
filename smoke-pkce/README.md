# Smoke Test (Authorization Code + PKCE)

This directory contains a tiny static SPA used to validate **Authorization Code + PKCE** with:

- CloudFront + S3 static hosting
- Cognito Hosted UI redirects
- Google IdP integration
- Browser-based PKCE token exchange

It is intentionally framework-free so you can confirm PKCE works before wiring it into re-frame.

---

## Files

- `index.html` — static entrypoint (committed)
- `app.example.js` — configuration template (committed)
- `app.js` — your local configured version (**DO NOT COMMIT**)

---

## Deploy Infrastructure

> If you previously deployed another variant, explicitly set the project name first.

```bash
export PROJECT_NAME=sso-poc-pkce
./scripts/check-prereqs.sh
./scripts/deploy.sh
```

---

## Configure the PKCE Smoke App

### 1) Copy the template

```bash
cp app.example.js app.js
```

### 2) Fetch values from CloudFormation

```bash
HOSTED_UI=$(aws cloudformation describe-stacks \
  --stack-name ${PROJECT_NAME}-cognito \
  --region us-east-1 \
  --query "Stacks[0].Outputs[?OutputKey=='HostedUiDomain'].OutputValue" \
  --output text)

CLIENT_ID=$(aws cloudformation describe-stacks \
  --stack-name ${PROJECT_NAME}-cognito \
  --region us-east-1 \
  --query "Stacks[0].Outputs[?OutputKey=='UserPoolClientId'].OutputValue" \
  --output text)

echo $HOSTED_UI
echo $CLIENT_ID
```

### 3) Apply values to `app.js`

```bash
sed -i '' "s|__HOSTED_UI__|$HOSTED_UI|g" app.js
sed -i '' "s|__CLIENT_ID__|$CLIENT_ID|g" app.js
```

---

## Upload to S3 and Invalidate CloudFront

```bash
BUCKET=$(aws cloudformation describe-stacks \
  --stack-name ${PROJECT_NAME}-site \
  --region us-east-1 \
  --query "Stacks[0].Outputs[?OutputKey=='BucketName'].OutputValue" \
  --output text)

aws s3 sync . "s3://$BUCKET/" --exclude "infra/*" --exclude "scripts/*" --delete
```

```bash
DIST=$(aws cloudformation describe-stacks \
  --stack-name ${PROJECT_NAME}-site \
  --region us-east-1 \
  --query "Stacks[0].Outputs[?OutputKey=='DistributionId'].OutputValue" \
  --output text)

aws cloudfront create-invalidation --distribution-id "$DIST" --paths "/*"
```

---

## Test

Open:

```
https://<your-domain>/
```

Click **Login with Google**. You should briefly see `/callback?code=...`, then land back at `/`
with decoded JWT claims displayed.

---

## Notes

- This variant uses **Authorization Code + PKCE** only.
- This is the same OAuth flow you will use in the re-frame application.

---

## Switching Between Smoke Variants

Both this directory and the sibling smoke directory provision AWS resources that assume ownership
of the same domain and Cognito configuration.

If you deployed the other smoke variant previously, you **must destroy its stacks before deploying this one**,
unless you intentionally change the domain name and stack parameters.

### To destroy the other variant

From the other directory:

```bash
export PROJECT_NAME=<that-project-name>
./scripts/destroy.sh
```

Wait for all stacks to reach `DELETE_COMPLETE`, then deploy this variant.

### Advanced: running both at once

If you want to run both variants simultaneously, override at least:

- `DOMAIN_NAME`
- `PROJECT_NAME`
- `COGNITO_DOMAIN_PREFIX`

for one of the variants so their AWS resources do not collide.

