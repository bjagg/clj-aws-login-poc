#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

export AWS_REGION=${AWS_REGION:-us-east-1}

# Defaults (override by exporting env vars before running)
: "${PROJECT_NAME:=sso-poc-pkce}"

echo "Destroying stacks for PROJECT_NAME=${PROJECT_NAME} in region $AWS_REGION"
echo "  - ${PROJECT_NAME}-cognito"
echo "  - ${PROJECT_NAME}-site"
echo "  - ${PROJECT_NAME}-cert"
echo

# Delete in reverse dependency order
aws cloudformation delete-stack --stack-name "${PROJECT_NAME}-cognito" --region "${AWS_REGION}" || true
aws cloudformation delete-stack --stack-name "${PROJECT_NAME}-site"    --region "${AWS_REGION}" || true
aws cloudformation delete-stack --stack-name "${PROJECT_NAME}-cert"    --region "${AWS_REGION}" || true

echo "Waiting for stacks to delete (this can take a few minutes)..."
aws cloudformation wait stack-delete-complete --stack-name "${PROJECT_NAME}-cognito" --region "${AWS_REGION}" || true
aws cloudformation wait stack-delete-complete --stack-name "${PROJECT_NAME}-site"    --region "${AWS_REGION}" || true
aws cloudformation wait stack-delete-complete --stack-name "${PROJECT_NAME}-cert"    --region "${AWS_REGION}" || true

echo "Done."
