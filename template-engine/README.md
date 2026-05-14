# Schools Template Engine

This repository contains a productized static template engine for school, college, and university website delivery.

## What it includes
- Tier configuration for Basic, Pro, and Premium.
- Three starter visual variants: Traditional, Modern, Premium.
- Config-driven static page generation.
- SEO checklist, monthly report template, and SEO report generator.
- Internal automation script that consumes intake JSON and generates ready-to-share output bundles.

## Quick start
1. Update `content/demo-school.json` with client data.
2. Pick a variant file from `variants/`.
3. Run:

```bash
npm run build:site
```

4. Output appears in `dist/index.html`, `dist/sitemap.xml`, and `dist/robots.txt`.

## Build all starter variants

```bash
npm run build:all
```

Output folders:
- `dist/traditional`
- `dist/modern`
- `dist/premium`

## Internal automation flow

Use the intake-to-site automation workflow:

```bash
npm run agent:generate
```

This creates:
- Generated site per client
- Execution checklist
- Handoff summary
