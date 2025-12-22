#!/usr/bin/env bash
set -euo pipefail

export AWS_REGION=us-east-1

# Repo defaults (override by exporting env vars before running)
: "${HOSTED_ZONE_ID:=Z05863342TZ6JN82178DO}"
: "${DOMAIN_NAME:=sso-poc.rpgsecrets.net}"
: "${PROJECT_NAME:=sso-poc}"
: "${GOOGLE_SECRET_NAME:=sso-poc/google-oauth}"
: "${COGNITO_DOMAIN_PREFIX:=rpgsecrets-sso-poc}"

echo "Deploying ACM certificate stack..."
aws cloudformation deploy \
  --stack-name ${PROJECT_NAME}-cert \
  --template-file infra/cfn/cert-dns.yml \
  --parameter-overrides \
    HostedZoneId=${HOSTED_ZONE_ID} \
    DomainName=${DOMAIN_NAME}

CERT_ARN=$(aws cloudformation describe-stacks \
  --stack-name ${PROJECT_NAME}-cert \
  --query "Stacks[0].Outputs[?OutputKey=='CertificateArn'].OutputValue" \
  --output text)

echo "Waiting for ACM certificate to be ISSUED (CloudFront requires an issued cert)..."
for i in {1..60}; do
  STATUS=$(aws acm describe-certificate --certificate-arn "${CERT_ARN}" --query "Certificate.Status" --output text --region "${AWS_REGION}")
  if [ "${STATUS}" = "ISSUED" ]; then
    echo "OK: Certificate is ISSUED."
    break
  fi
  if [ "${STATUS}" = "FAILED" ]; then
    echo "ERROR: Certificate validation FAILED. Inspect details:"
    aws acm describe-certificate --certificate-arn "${CERT_ARN}" --region "${AWS_REGION}" --query "Certificate.DomainValidationOptions" --output json
    exit 1
  fi
  echo "  Status=${STATUS} (waiting...)"
  sleep 10
done

STATUS=$(aws acm describe-certificate --certificate-arn "${CERT_ARN}" --query "Certificate.Status" --output text --region "${AWS_REGION}")
if [ "${STATUS}" != "ISSUED" ]; then
  echo "ERROR: Certificate is not ISSUED yet (Status=${STATUS}). Try again in a minute."
  exit 1
fi

echo "Deploying site stack (S3 + CloudFront + Route53 alias)..."
aws cloudformation deploy \
  --stack-name ${PROJECT_NAME}-site \
  --template-file infra/cfn/site.yml \
  --parameter-overrides \
    ProjectName=${PROJECT_NAME} \
    HostedZoneId=${HOSTED_ZONE_ID} \
    DomainName=${DOMAIN_NAME} \
    CertificateArn=${CERT_ARN}

echo "Deploying cognito stack (Hosted UI + Google IdP via Secrets Manager)..."
aws cloudformation deploy \
  --stack-name ${PROJECT_NAME}-cognito \
  --template-file infra/cfn/cognito.yml \
  --parameter-overrides \
    ProjectName=${PROJECT_NAME} \
    CallbackURL=https://${DOMAIN_NAME}/callback \
    LogoutURL=https://${DOMAIN_NAME}/ \
    DomainPrefix=${COGNITO_DOMAIN_PREFIX} \
    GoogleSecretName=${GOOGLE_SECRET_NAME}

echo "Deployment complete."
echo
echo "Useful outputs:"
aws cloudformation describe-stacks --stack-name ${PROJECT_NAME}-site --query "Stacks[0].Outputs" --output table
aws cloudformation describe-stacks --stack-name ${PROJECT_NAME}-cognito --query "Stacks[0].Outputs" --output table
