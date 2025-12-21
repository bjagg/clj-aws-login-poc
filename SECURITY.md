# Security Notes

This repository is intended to be safe to publish publicly. It demonstrates AWS Cognito + Google SSO
without committing secrets or account-specific configuration into Git.

## What is (and is not) sensitive

- **Secrets (must not be committed):**
  - Google OAuth *client_secret*
  - Any API keys, signing keys, private keys
  - Session cookies or token values captured during testing

- **Usually OK to commit:**
  - CloudFormation templates
  - Stack names, logical resource names
  - Route 53 hosted zone *IDs* (not secrets, but still account-specific; you may prefer not to publish them)
  - ARNs of resources **created by CloudFormation** (not credentials)

> Note: An ARN is an identifier, not a credential. It can reveal account IDs and resource names, so
> treat it as **account-specific metadata** you may choose to keep out of public docs.

## How this repo keeps secrets out of Git

### Secrets live in AWS Secrets Manager

Before deploying, you create a secret like:

- Secret name: `sso-poc/google-oauth`
- Secret JSON keys:
  - `client_id`
  - `client_secret`

CloudFormation references these values using **dynamic references**:

- `{{resolve:secretsmanager:...}}`

This means:

- Secret values are never stored in this repository
- Secret values do not appear in CloudFormation templates
- Secret values are not emitted in stack outputs

### Avoiding hard-coded ARNs

Where possible, this repo:

- Creates resources inside CloudFormation stacks
- Passes identifiers via **stack outputs** and shell script variables
- Avoids committing account-unique ARNs into templates or docs

If you see a template parameter like `CertificateArn`, it should be obtained at deploy time (e.g.,
from the cert stack output) rather than written into the repo.

## Recommended practices for contributors

- Do not paste token values into issues or PRs
- Do not commit `.env` files, AWS credential files, or local parameter overrides
- Prefer `aws cloudformation deploy --parameter-overrides ...` from your shell or CI
- Use `scripts/check-prereqs.sh` before running deployments

## Reporting security issues

If you discover a security issue, please report it privately (do not open a public issue) and include:

- What you found
- Steps to reproduce
- Impact and suggested mitigation
