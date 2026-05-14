import fs from "node:fs";
import path from "node:path";
import { ensureDir, parseArgs, readJson } from "./shared.mjs";

const args = parseArgs(process.argv);
const schoolPath = args.school || "content/demo-school.json";
const metricsPath = args.metrics || "content/seo-metrics-sample.json";
const outFile = args.out || "docs/seo/monthly-report-sample.md";

const school = readJson(schoolPath);
const metrics = readJson(metricsPath);

const report = `# Monthly SEO Report - ${metrics.month}

## Client
- Institution: ${metrics.institution}
- Domain: ${school.domain}
- City: ${school.city}

## KPI Snapshot
- Impressions: ${metrics.impressions}
- Clicks: ${metrics.clicks}
- CTR: ${metrics.ctr}
- Average position: ${metrics.avgPosition}
- Organic leads: ${metrics.organicLeads}

## Top Pages
${metrics.topPages.map((page) => `- ${page}`).join("\n")}

## High Impact Keywords
${metrics.highImpactKeywords.map((keyword) => `- ${keyword}`).join("\n")}

## Next Month Actions
${metrics.nextMonthActions.map((action, index) => `${index + 1}. ${action}`).join("\n")}
`;

ensureDir(path.dirname(outFile));
fs.writeFileSync(path.resolve(outFile), report, "utf8");
console.log(`SEO report generated at ${outFile}`);
