import fs from "node:fs";
import path from "node:path";
import { ensureDir, parseArgs, readJson, renderTemplate, slugify } from "./shared.mjs";

const args = parseArgs(process.argv);
const variantPath = args.variant || "variants/modern.json";
const contentPath = args.content || "content/demo-school.json";
const outDir = args.out || "dist";

const variant = readJson(variantPath);
const content = readJson(contentPath);
const layout = fs.readFileSync(path.resolve("templates/layout.html"), "utf8");

const programsList = `<ul>${(content.programs || [])
  .map((program) => `<li>${program}</li>`)
  .join("")}</ul>`;

const html = renderTemplate(layout, {
  schoolName: content.schoolName,
  city: content.city,
  domain: content.domain,
  contactEmail: content.contactEmail,
  contactPhone: content.contactPhone,
  principalMessage: content.principalMessage,
  aboutSummary: content.aboutSummary,
  ctaText: content.ctaText,
  primaryColor: variant.primaryColor,
  accentColor: variant.accentColor,
  heroTagline: variant.heroTagline,
  fontFamily: variant.fontFamily,
  variantName: variant.displayName,
  programsList
});

ensureDir(outDir);
fs.writeFileSync(path.resolve(outDir, "index.html"), html, "utf8");

const domainWithoutProtocol = String(content.domain || "").replace(/^https?:\/\//, "");
const siteRoot = `https://${domainWithoutProtocol}`;
const pageSlug = slugify(content.schoolName) || "school";

const sitemap = `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
  <url>
    <loc>${siteRoot}/</loc>
    <changefreq>weekly</changefreq>
  </url>
  <url>
    <loc>${siteRoot}/admissions/${pageSlug}</loc>
    <changefreq>monthly</changefreq>
  </url>
</urlset>`;
fs.writeFileSync(path.resolve(outDir, "sitemap.xml"), sitemap, "utf8");

const robots = `User-agent: *
Allow: /
Sitemap: ${siteRoot}/sitemap.xml`;
fs.writeFileSync(path.resolve(outDir, "robots.txt"), robots, "utf8");

console.log(`Generated site for ${content.schoolName} (${variant.displayName}) in ${outDir}`);
