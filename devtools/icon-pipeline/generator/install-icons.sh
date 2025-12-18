#!/bin/bash

set -e

ROOT=$(dirname "$0")/..
OUT=$ROOT/output

IOS_PROJECT="../../mobile/ios/Mashup/Mashup/Resources/Assets.xcassets/Icons"
IOS_SWIFT="../../mobile/ios/Mashup/Mashup/Presentation/DesignSystem/Icons"

ANDROID_DRAWABLE="../../mobile/android/app/src/main/res/drawable"
ANDROID_KT="../../mobile/android/app/src/main/java/com/mashup/designsystem/icons"

echo "🚀 Installing icons..."

mkdir -p "$IOS_PROJECT"
mkdir -p "$IOS_SWIFT"
mkdir -p "$ANDROID_DRAWABLE"
mkdir -p "$ANDROID_KT"

cp -R "$OUT/ios-imagesets/"* "$IOS_PROJECT"
cp "$OUT/swift/Icon.swift" "$IOS_SWIFT"

cp "$OUT/android-vectors/"*.xml "$ANDROID_DRAWABLE"
cp "$OUT/swift/Icon.kt" "$ANDROID_KT"

echo "🎉 Icons installed successfully!"
