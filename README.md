<img src="logo.png" width="30%" alt="Polylith" id="logo">

# Minimal re-frame SSO PoC (AWS Cognito + Google)

This repository is a **proof of concept** demonstrating how to add
**social login (Google SSO)** to a **minimal ClojureScript / re-frame
SPA**, using **AWS-native infrastructure** and **CloudFormation**.

The goals of this repo are:

- Demonstrate a clean **SPA authentication flow** using Amazon Cognito
    Hosted UI
- Show how to keep **secrets and ARNs out of a public repo**
- Provide a **Polylith-friendly layout** that can be copied into a
    larger platform later
- Keep infrastructure **tear-down friendly** for experimentation

## Auth Flow (PoC)

1. User clicks **Sign in**
2. Browser redirects to **Cognito Hosted UI**
3. Cognito delegates authentication to **Google**
4. Cognito redirects back to `/callback`
5. SPA parses the result and establishes user session state

> Note: The initial PoC may use the implicit flow for simplicity. A
> follow-up step demonstrates migrating to **Authorization Code + PKCE**
> for best practices.

------------------------------------------------------------------------

## Prerequisites

This repository provisions a proof-of-concept Single Sign-On (SSO) setup
using:

- AWS CloudFormation
- Amazon Cognito (Hosted UI)
- Google as a social identity provider
- S3 + CloudFront for static SPA hosting

Before deploying the CloudFormation stacks, complete the following
steps.

------------------------------------------------------------------------

## 1. AWS Account and Domain

You must have:

- An active **AWS account**
- A **public domain name** registered through AWS (Route 53)
- A **public Route 53 hosted zone** for that domain

You will need: - The **Hosted Zone ID** for your domain
(e.g. `Z123456ABCDEFG`)

Confirm via:

``` bash
aws route53 list-hosted-zones-by-name --dns-name example.com
```

------------------------------------------------------------------------

## 2. Choose a Subdomain for the PoC

Pick a subdomain for the SSO proof of concept.

Example: - `sso-poc.example.com`

This value is used for: - CloudFront distribution - Cognito callback
URL - Cognito logout URL - Google OAuth redirect URI

------------------------------------------------------------------------

## 3. Google OAuth (Social Login) Setup

### 3.1 Create or Select a Google Cloud Project

1. Go to [https://console.cloud.google.com/]
2. Create a new project or select an existing one

------------------------------------------------------------------------

### 3.2 Configure OAuth Consent Screen

1. APIs & Services → OAuth consent screen
2. Select **External**
3. Provide:
    - App name
    - Support email
    - Developer contact email
4. Save

Verification is not required for this PoC.

------------------------------------------------------------------------

### 3.3 Create OAuth Client Credentials

1. APIs & Services → Credentials
2. Create Credentials → OAuth client ID
3. Application type: **Web application**
4. Authorized redirect URI:

```{=html}
<!-- -->
    https://sso-poc.example.com/callback
```

You will receive: - Client ID - Client Secret

------------------------------------------------------------------------

## 4. Store Google OAuth Credentials in AWS Secrets Manager

Create this **before** deploying CloudFormation.

``` bash
aws secretsmanager create-secret   --name sso-poc/google-oauth   --description "Google OAuth credentials for Cognito SSO PoC"   --secret-string '{
    "client_id": "GOOGLE_CLIENT_ID_HERE",
    "client_secret": "GOOGLE_CLIENT_SECRET_HERE"
  }'   --region us-east-1
```

Required structure:

``` json
{
  "client_id": "<Google OAuth Client ID>",
  "client_secret": "<Google OAuth Client Secret>"
}
```

------------------------------------------------------------------------

## 5. Tools Required Locally

- AWS CLI v2
- Permissions to create CloudFormation, Cognito, S3, CloudFront,
  Route53, ACM

Verify:

``` bash
aws sts get-caller-identity
```

------------------------------------------------------------------------

## 6. Region

All infrastructure is deployed to:

```

    us-east-1
```

This is required for CloudFront + ACM.

------------------------------------------------------------------------

## 7. Required Values Summary

| Value               | Example |
| ------------------- | -------------------------------- |
| Hosted Zone ID        | Z123456ABCDEFG |
| Domain name           | sso-poc.example.com |
| Google Client ID      | xxxx.apps.googleusercontent.com |
| Google Client Secret  | \*\*\*\*\*\* |
| Secrets Manager name  | sso-poc/google-oauth |

------------------------------------------------------------------------

## Helper Scripts

Before deploying, run:

```bash
./scripts/check-prereqs.sh
```

This repo includes simple shell scripts to deploy and tear down
infrastructure.

### Deploy

``` bash
./scripts/deploy.sh
```

### Destroy

``` bash
./scripts/destroy.sh
```

These scripts assume: - AWS credentials are already configured -
Required secrets exist in AWS Secrets Manager
