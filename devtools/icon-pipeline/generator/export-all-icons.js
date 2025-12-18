// export-all-icons.js
// IcoMoon selection.json -> SVG + Icon.swift + Icon.kt

const fs = require("fs");
const path = require("path");

const ROOT = path.join(__dirname, "..");
const INPUT = path.join(ROOT, "input/selection.json");

const OUT_SVG = path.join(ROOT, "output/svg");
const OUT_SWIFT = path.join(ROOT, "output/swift");

const VIEWBOX = "0 0 1024 1024";

function ensureDir(p) {
  if (!fs.existsSync(p)) fs.mkdirSync(p, { recursive: true });
}

function sanitize(name) {
  return name.toLowerCase().replace(/\s+/g, "-").replace(/[^a-z0-9\-_]/g, "");
}

function kebabToCamel(name) {
  const parts = name.split("-");
  return (
    parts[0] +
    parts
      .slice(1)
      .map((p) => p.charAt(0).toUpperCase() + p.slice(1))
      .join("")
  );
}

function iconObjectToSVG(iconObj) {
  const icon = iconObj.icon;
  const props = iconObj.properties || {};

  const rawName = props.name || "icon";
  const safeName = sanitize(rawName);

  const paths = icon.paths || [];
  const pathTags = paths.map((d) => `<path d="${d}" />`).join("\n  ");

  const svg = `<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="${VIEWBOX}">
  ${pathTags}
</svg>
`;
  return { rawName, safeName, svg };
}

function generateSwiftEnum(iconNames) {
  ensureDir(OUT_SWIFT);
  const swiftFile = path.join(OUT_SWIFT, "Icon.swift");

  const cases = iconNames
    .map((raw) => {
      const safe = sanitize(raw);
      const camel = kebabToCamel(safe);
      return `    case ${camel} = "${safe}"`;
    })
    .join("\n");

  const code = `import Foundation

public enum Icon: String, CaseIterable {
${cases}
}
`;

  fs.writeFileSync(swiftFile, code, "utf8");
  console.log(`🧾 Swift enum generated: Icon.swift`);
}

function generateKotlinEnum(iconNames) {
  const kotlinFile = path.join(OUT_SWIFT, "Icon.kt");

  const entries = iconNames
    .map((raw) => {
      const safe = sanitize(raw);
      const camel = kebabToCamel(safe);
      const drawable = safe.replace(/-/g, "_");
      return `    ${camel}("${safe}", R.drawable.${drawable})`;
    })
    .join(",\n");

  const code = `//
// Auto-generated Icon.kt
//

package com.mashup.designsystem.icons

import androidx.annotation.DrawableRes
import com.mashup.R

enum class Icon(
    val key: String,
    @DrawableRes val resId: Int
) {
${entries}
}
`;

  fs.writeFileSync(kotlinFile, code, "utf8");
  console.log(`🧾 Kotlin enum generated: Icon.kt`);
}

function main() {
  console.log("🚀 Generating icons...");

  if (!fs.existsSync(INPUT)) {
    console.error("❌ selection.json missing!");
    process.exit(1);
  }

  ensureDir(OUT_SVG);
  ensureDir(OUT_SWIFT);

  const json = JSON.parse(fs.readFileSync(INPUT, "utf8"));
  const icons = json.icons || [];

  console.log(`🔍 Found ${icons.length} icons\n`);

  const rawNames = [];

  icons.forEach((iconObj) => {
    const { rawName, safeName, svg } = iconObjectToSVG(iconObj);
    rawNames.push(rawName);
    const svgFile = path.join(OUT_SVG, `${safeName}.svg`);
    fs.writeFileSync(svgFile, svg, "utf8");
  });

  generateSwiftEnum(rawNames);
  generateKotlinEnum(rawNames);

  console.log("\n🎉 DONE!");
}

main();
