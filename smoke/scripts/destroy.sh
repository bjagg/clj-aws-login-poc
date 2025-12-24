#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

export AWS_REGION="${AWS_REGION:-us-east-1}"

# Defaults (override by exporting env vars before running)
: "${PROJECT_NAME:=sso-poc-smoke}"

echo "Destroying stacks for PROJECT_NAME=${PROJECT_NAME} in region $AWS_REGION"
echo "  1) ${PROJECT_NAME}-cognito"
echo "  2) ${PROJECT_NAME}-site (empties bucket first)"
echo "  3) ${PROJECT_NAME}-cert (must be last; cert can be 'in use' until CloudFront is gone)"
echo

# 1) Delete Cognito first
echo "Deleting stack: ${PROJECT_NAME}-cognito"
aws cloudformation delete-stack --stack-name "${PROJECT_NAME}-cognito" --region "${AWS_REGION}" || true
aws cloudformation wait stack-delete-complete --stack-name "${PROJECT_NAME}-cognito" --region "${AWS_REGION}" || true

# 2) Empty bucket, then delete Site (CloudFront+S3+DNS)
echo "Looking up site bucket from stack output..."
BUCKET=$(aws cloudformation describe-stacks   --stack-name "${PROJECT_NAME}-site"   --region "${AWS_REGION}"   --query "Stacks[0].Outputs[?OutputKey=='BucketName'].OutputValue"   --output text 2>/dev/null || true)

if [ -n "${BUCKET:-}" ] && [ "${BUCKET}" != "None" ]; then
  echo "Emptying S3 bucket: $BUCKET"
  aws s3 rm "s3://${BUCKET}/" --recursive --region "${AWS_REGION}" || true
else
  echo "No bucket output found (stack may not exist)."
fi

echo "Deleting stack: ${PROJECT_NAME}-site"
aws cloudformation delete-stack --stack-name "${PROJECT_NAME}-site" --region "${AWS_REGION}" || true
aws cloudformation wait stack-delete-complete --stack-name "${PROJECT_NAME}-site" --region "${AWS_REGION}" || true

# 3) Delete Cert last (CloudFront requires the cert; deletion can fail if still in use)
echo "Deleting stack: ${PROJECT_NAME}-cert"
aws cloudformation delete-stack --stack-name "${PROJECT_NAME}-cert" --region "${AWS_REGION}" || true
aws cloudformation wait stack-delete-complete --stack-name "${PROJECT_NAME}-cert" --region "${AWS_REGION}" || true

echo "Done."
