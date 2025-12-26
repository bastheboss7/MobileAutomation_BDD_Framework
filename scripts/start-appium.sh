#!/usr/bin/env bash
set -euo pipefail

PORT=${1:-4723}

if ! command -v appium >/dev/null 2>&1; then
  echo "Error: Appium is not installed. Install with: npm install -g appium" >&2
  exit 1
fi

# Start Appium server
appium --port "${PORT}" --log-level warn &
APPIUM_PID=$!

echo "Appium started on port ${PORT} (PID: ${APPIUM_PID})."
