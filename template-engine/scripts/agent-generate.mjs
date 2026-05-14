import fs from "node:fs";
import path from "node:path";
import { spawnSync } from "node:child_process";
import { ensureDir, parseArgs, readJson } from "./shared.mjs";

const args = parseArgs(process.argv);
const intakePath = args.intake || "content/intake-sample.json";
const outDir = args.out || "dist/agent-output";
const intake = readJson(intakePath);

const variantId = intake.variant || "modern";
const variantPath = `variants/${variantId}.json`;

ensureDir(outDir);

const generatedContentPath = path.join(outDir, "generated-content.json");
const schoolContent = {
  schoolName: intake.schoolName,
  city: intake.city,
  domain: intake.domain,
  contactEmail: intake.contactEmail,
  contactPhone: intake.contactPhone,
  principalMessage: intake.principalMessage,
  aboutSummary: intake.aboutSummary,
  programs: intake.programs || [],
  ctaText: intake.ctaText || "Contact Admissions"
};
fs.writeFileSync(generatedContentPath, JSON.stringify(schoolContent, null, 2), "utf8");

const siteOut = path.join(outDir, "site");
const buildResult = spawnSync(
  process.execPath,
  [
    "scripts/generate-site.mjs",
    "--variant",
    variantPath,
    "--content",
    generatedContentPath,
    "--out",
    siteOut
  ],
  { stdio: "inherit" }
);

if (buildResult.status !== 0) {
  process.exit(buildResult.status ?? 1);
}

const executionChecklist = `# Execution Checklist - ${intake.schoolName}

## Plan
- Tier: ${String(intake.plan || "basic").toUpperCase()}
- Variant: ${variantId}
- Domain: ${intake.domain}

## Pre-Launch Tasks
- [ ] Confirm logo and branding approval.
- [ ] Validate page content and admissions details.
- [ ] Connect lead form destination email.
- [ ] Verify SEO metadata and sitemap.
- [ ] Run stakeholder walkthrough and sign-off.

## Launch Tasks
- [ ] Upload static build from \`site/\`.
- [ ] Configure domain and SSL.
- [ ] Submit sitemap in Search Console.
- [ ] Share handoff note with support SLA.
`;

fs.writeFileSync(path.join(outDir, "execution-checklist.md"), executionChecklist, "utf8");

const handoff = `# Handoff Summary

- Institution: ${intake.schoolName}
- City: ${intake.city}
- Selected Tier: ${intake.plan}
- Variant: ${variantId}
- Site Output: ${siteOut}
- Contact: ${intake.contactEmail} | ${intake.contactPhone}
`;

fs.writeFileSync(path.join(outDir, "handoff-summary.md"), handoff, "utf8");
console.log(`Automation output created in ${outDir}`);
