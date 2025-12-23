#!/usr/bin/env bash
# ------------------------------------------------------------------------------
# env.sh
#
# Shared environment loader for infra scripts.
# - Loads .env if present
# - Exports variables so child processes (aws cli) can see them
# - Safe to source multiple times
# ------------------------------------------------------------------------------

set -o allexport

if [ -f ".env" ]; then
  echo "Loading environment from .env"
  # shellcheck disable=SC1091
  source ".env"
else
  echo "No .env file found; relying on exported shell variables"
fi

set +o allexport

# ------------------------------------------------------------------------------
# Minimal sanity checks (fail fast for common mistakes)
# ------------------------------------------------------------------------------
: "${PROJECT_NAME:?PROJECT_NAME must be set}"
: "${AWS_REGION:?AWS_REGION must be set}"
: "${HOSTED_ZONE_ID:?HOSTED_ZONE_ID must be set}"
: "${DOMAIN_NAME:?DOMAIN_NAME must be set}"
: "${SUBDOMAIN:?SUBDOMAIN must be set}"
