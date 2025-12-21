#!/usr/bin/env bash
set -euo pipefail

# Defaults for this repo (override by exporting env vars before running)
: "${AWS_REGION:=us-east-1}"
: "${HOSTED_ZONE_ID:=Z05863342TZ6JN82178DO}"
: "${DOMAIN_NAME:=sso-poc.rpgsecrets.net}"
: "${GOOGLE_SECRET_NAME:=sso-poc/google-oauth}"

err() { echo "ERROR: $*" >&2; exit 1; }
ok()  { echo "OK: $*"; }

command -v aws >/dev/null 2>&1 || err "aws CLI not found. Install AWS CLI v2 and ensure it's on PATH."

aws --version >/dev/null 2>&1 || err "aws CLI appears broken (aws --version failed)."

# Validate AWS identity
ID_JSON=$(aws sts get-caller-identity --region "${AWS_REGION}" 2>/dev/null) || err "Unable to call STS. Are your AWS credentials configured?"
ACCOUNT_ID=$(echo "${ID_JSON}" | python3 -c 'import sys, json; print(json.load(sys.stdin)["Account"])')
ok "AWS credentials configured (Account ${ACCOUNT_ID}, Region ${AWS_REGION})"

# Validate hosted zone exists and matches domain
HZ_JSON=$(aws route53 get-hosted-zone --id "${HOSTED_ZONE_ID}" 2>/dev/null) || err "Hosted zone ${HOSTED_ZONE_ID} not found or not accessible."
HZ_NAME=$(echo "${HZ_JSON}" | python3 -c 'import sys, json; print(json.load(sys.stdin)["HostedZone"]["Name"])')

# Normalize compare: ensure trailing dot
DOMAIN_ROOT="${DOMAIN_NAME#*.}."
if [ "${HZ_NAME}" != "${DOMAIN_ROOT}" ]; then
  echo "WARN: Hosted zone name (${HZ_NAME}) does not match expected root (${DOMAIN_ROOT})."
  echo "      If this is intentional (e.g., separate zone for a subdomain), you can ignore."
else
  ok "Hosted zone matches domain root (${HZ_NAME})"
fi

# Validate secret exists (do not print contents)
aws secretsmanager describe-secret --secret-id "${GOOGLE_SECRET_NAME}" --region "${AWS_REGION}" >/dev/null 2>&1   || err "Secrets Manager secret '${GOOGLE_SECRET_NAME}' not found in ${AWS_REGION}. Create it before deploying."

ok "Secrets Manager secret exists (${GOOGLE_SECRET_NAME})"

# Optional: check that secret JSON has required keys (without printing sensitive values)
SECRET_STR=$(aws secretsmanager get-secret-value --secret-id "${GOOGLE_SECRET_NAME}" --query SecretString --output text --region "${AWS_REGION}" 2>/dev/null)   || err "Unable to read secret value (missing permissions?). Need secretsmanager:GetSecretValue."

python3 - <<'PY'
import json, os, sys
s = os.environ.get("SECRET_STR", "")
try:
    data = json.loads(s)
except Exception:
    print("ERROR: SecretString is not valid JSON.", file=sys.stderr)
    sys.exit(1)

missing = [k for k in ("client_id","client_secret") if k not in data or not str(data[k]).strip()]
if missing:
    print(f"ERROR: Secret is missing required keys: {missing}", file=sys.stderr)
    sys.exit(1)

print("OK: Secret contains required keys (client_id, client_secret)")
PY

# Validate we can talk to CloudFormation in region
aws cloudformation list-stacks --max-items 1 --region "${AWS_REGION}" >/dev/null 2>&1   || err "CloudFormation access check failed. Ensure permissions include cloudformation:* for deployment."

ok "CloudFormation access looks good"

echo
echo "All prerequisite checks passed."
echo "Next:"
echo "  ./scripts/deploy.sh"
