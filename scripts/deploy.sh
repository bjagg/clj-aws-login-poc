#!/usr/bin/env bash
set -e

export AWS_REGION=us-east-1

HOSTED_ZONE_ID=Z05863342TZ6JN82178DO
DOMAIN_NAME=sso-poc.rpgsecrets.net
PROJECT_NAME=sso-poc
GOOGLE_SECRET_NAME=sso-poc/google-oauth

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

echo "Deploying site stack..."
aws cloudformation deploy \
  --stack-name ${PROJECT_NAME}-site \
  --template-file infra/cfn/site.yml \
  --parameter-overrides \
    ProjectName=${PROJECT_NAME} \
    HostedZoneId=${HOSTED_ZONE_ID} \
    DomainName=${DOMAIN_NAME} \
    CertificateArn=${CERT_ARN}

echo "Deploying cognito stack..."
aws cloudformation deploy \
  --stack-name ${PROJECT_NAME}-cognito \
  --template-file infra/cfn/cognito.yml \
  --parameter-overrides \
    ProjectName=${PROJECT_NAME} \
    CallbackURL=https://${DOMAIN_NAME}/callback \
    LogoutURL=https://${DOMAIN_NAME}/ \
    DomainPrefix=rpgsecrets-sso-poc \
    GoogleSecretName=${GOOGLE_SECRET_NAME}

echo "Deployment complete."
