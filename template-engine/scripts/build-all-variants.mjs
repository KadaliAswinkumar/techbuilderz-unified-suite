import { spawnSync } from "node:child_process";

const variants = ["traditional", "modern", "premium"];

for (const variant of variants) {
  const outputDir = `dist/${variant}`;
  const result = spawnSync(
    process.execPath,
    [
      "scripts/generate-site.mjs",
      "--variant",
      `variants/${variant}.json`,
      "--content",
      "content/demo-school.json",
      "--out",
      outputDir
    ],
    { stdio: "inherit" }
  );

  if (result.status !== 0) {
    process.exit(result.status ?? 1);
  }
}

console.log("Generated all starter variants.");
