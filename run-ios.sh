#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT="$SCRIPT_DIR/iosApp/MockcatSample/MockcatSample.xcodeproj"
SCHEME="MockcatSample"
BUNDLE_ID="com.mockcat.MockcatSample"
SIMULATOR="iPhone 15 Pro"

# Find simulator UDID by name
UDID=$(xcrun simctl list devices available | grep "$SIMULATOR" | head -1 | sed 's/.*(\([A-F0-9-]*\)).*/\1/')
if [ -z "$UDID" ]; then
  echo "Error: No available simulator matching '$SIMULATOR'"
  exit 1
fi
echo "Simulator: $SIMULATOR ($UDID)"

# Build Kotlin framework
echo ""
echo "Building Kotlin framework..."
cd "$SCRIPT_DIR"
./gradlew :mockcat-logger-ui:linkDebugFrameworkIosSimulatorArm64

# Build iOS app
echo ""
echo "Building iOS app..."
xcodebuild \
  -project "$PROJECT" \
  -scheme "$SCHEME" \
  -destination "platform=iOS Simulator,id=$UDID" \
  -configuration Debug \
  build \
  | grep -E "^(error:|warning: |Build succeeded|BUILD)" || true

# Find the built .app
APP_PATH=$(find ~/Library/Developer/Xcode/DerivedData -name "$SCHEME.app" \
  -path "*/Debug-iphonesimulator/*" -newer "$PROJECT" 2>/dev/null | head -1)
if [ -z "$APP_PATH" ]; then
  APP_PATH=$(find ~/Library/Developer/Xcode/DerivedData -name "$SCHEME.app" \
    -path "*/Debug-iphonesimulator/*" 2>/dev/null | head -1)
fi
if [ -z "$APP_PATH" ]; then
  echo "Error: Could not find built $SCHEME.app"
  exit 1
fi
echo "App: $APP_PATH"

# Boot simulator if needed
STATE=$(xcrun simctl list devices | grep "$UDID" | grep -o "Booted\|Shutdown" || echo "Shutdown")
if [ "$STATE" != "Booted" ]; then
  echo ""
  echo "Booting simulator..."
  xcrun simctl boot "$UDID"
fi
open -a Simulator

# Install and launch
echo ""
echo "Installing and launching..."
xcrun simctl install "$UDID" "$APP_PATH"
xcrun simctl launch "$UDID" "$BUNDLE_ID"

echo ""
echo "Done."
