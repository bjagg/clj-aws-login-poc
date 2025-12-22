#!/usr/bin/env bash
set -euo pipefail

export AWS_REGION=us-east-1
: "${PROJECT_NAME:=sso-poc}"

echo "Deleting stacks..."
aws cloudformation delete-stack --stack-name ${PROJECT_NAME}-cognito
aws cloudformation delete-stack --stack-name ${PROJECT_NAME}-site
aws cloudformation delete-stack --stack-name ${PROJECT_NAME}-cert
echo "Stacks deletion initiated."
