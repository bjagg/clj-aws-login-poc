# Smoke Test (Hosted UI + Google SSO)

This directory contains a tiny static SPA used to validate:

-   CloudFront + S3 static hosting
-   Cognito Hosted UI redirects
-   Google IdP integration
-   Callback routing (SPA-style)

It is intentionally framework-free so you can confirm infrastructure
works before adding re-frame.

------------------------------------------------------------------------

## Files

-   `index.html` --- static entrypoint (generic, committed)
-   `app.example.js` --- configuration template (committed)
-   `app.js` --- your local configured version (**NOT committed**)

------------------------------------------------------------------------

## Setup

### 1) Copy the template

``` bash
cp smoke/app.example.js smoke/app.js
```

------------------------------------------------------------------------

### 2) Fill in placeholders in `smoke/app.js`

Replace the following values:

-   `__HOSTED_UI__`\
    (CloudFormation output: `HostedUiDomain`)
-   `__CLIENT_ID__`\
    (CloudFormation output: `UserPoolClientId`)
-   Confirm:
    -   `redirectUri`
    -   `logoutUri`

These must match your deployed domain exactly.

You can fetch the required values with:

``` bash
HOSTED_UI=$(aws cloudformation describe-stacks   --stack-name sso-poc-cognito   --region us-east-1   --query "Stacks[0].Outputs[?OutputKey=='HostedUiDomain'].OutputValue"   --output text)

CLIENT_ID=$(aws cloudformation describe-stacks   --stack-name sso-poc-cognito   --region us-east-1   --query "Stacks[0].Outputs[?OutputKey=='UserPoolClientId'].OutputValue"   --output text)

echo $HOSTED_UI
echo $CLIENT_ID
```

------------------------------------------------------------------------

### 3) Upload the smoke test to S3

``` bash
BUCKET=$(aws cloudformation describe-stacks   --stack-name sso-poc-site   --region us-east-1   --query "Stacks[0].Outputs[?OutputKey=='BucketName'].OutputValue"   --output text)

aws s3 sync smoke "s3://$BUCKET/" --delete
```

------------------------------------------------------------------------

### 4) Invalidate CloudFront (recommended after updates)

``` bash
DIST=$(aws cloudformation describe-stacks   --stack-name sso-poc-site   --region us-east-1   --query "Stacks[0].Outputs[?OutputKey=='DistributionId'].OutputValue"   --output text)

aws cloudfront create-invalidation   --distribution-id "$DIST"   --paths "/*"
```

------------------------------------------------------------------------

### 5) Test in browser

Open:

    https://sso-poc.<your-domain>/

Click **Login with Google**. After login, decoded JWT claims should
appear on the page.

------------------------------------------------------------------------

## Notes

-   This smoke test currently uses **implicit flow**
    (`response_type=token`) for simplicity.
-   In the PKCE section of this repo, the flow is upgraded to
    **Authorization Code + PKCE** and implicit flow is removed.
