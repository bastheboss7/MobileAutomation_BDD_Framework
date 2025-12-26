#!/usr/bin/env bash
set -euo pipefail

AVD_NAME=${1:-Pixel_6_API_33}
HEADLESS=${HEADLESS:-true}

if ! command -v emulator >/dev/null 2>&1; then
  echo "Error: Android emulator not found in PATH. Ensure Android SDK is installed and ANDROID_HOME is set." >&2
  exit 1
fi

# Start emulator
if [[ "${HEADLESS}" == "true" ]]; then
  emulator -avd "${AVD_NAME}" -no-window -gpu off -no-snapshot &
else
  emulator -avd "${AVD_NAME}" &
fi

EMUPID=$!

echo "Emulator started (PID: ${EMUPID}). Waiting for device..."

# Wait for device to be ready
adb wait-for-device

# Verify device
adb devices

# Optionally unlock screen
adb shell input keyevent 82 || true

echo "Emulator ${AVD_NAME} is ready."
