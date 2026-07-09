#!/usr/bin/env bash
# Generates CycloneDX SBOMs (cdxgen) and scans them plus the repo with trivy.
# Usage: scripts/security-scan.sh [--images]
#   --images  additionally build both Docker images and scan them
# Fails (exit 1) on HIGH/CRITICAL vulnerabilities, secrets or misconfigurations.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

SBOM_DIR="$REPO_ROOT/sbom"
mkdir -p "$SBOM_DIR"

SCAN_IMAGES=false
[[ "${1:-}" == "--images" ]] && SCAN_IMAGES=true

echo "==> Generating SBOMs (CycloneDX via cdxgen)"
cdxgen -t java backend -o "$SBOM_DIR/backend.cdx.json"
cdxgen -t npm frontend -o "$SBOM_DIR/frontend.cdx.json"

# cdxgen writes minified JSON; pretty-print for human readability
for sbom in "$SBOM_DIR"/*.cdx.json; do
    python3 -m json.tool "$sbom" > "$sbom.tmp" && mv "$sbom.tmp" "$sbom"
done

echo "==> Scanning SBOMs for HIGH/CRITICAL vulnerabilities"
trivy sbom --severity HIGH,CRITICAL --exit-code 1 "$SBOM_DIR/backend.cdx.json"
trivy sbom --severity HIGH,CRITICAL --exit-code 1 "$SBOM_DIR/frontend.cdx.json"

echo "==> Scanning repository for secrets and misconfigurations"
trivy fs --scanners secret,misconfig --exit-code 1 .

if $SCAN_IMAGES; then
    echo "==> Building and scanning Docker images"
    docker build -t ddl-backend:scan backend
    docker build -t ddl-frontend:scan frontend
    trivy image --severity HIGH,CRITICAL --exit-code 1 ddl-backend:scan
    trivy image --severity HIGH,CRITICAL --exit-code 1 ddl-frontend:scan
fi

echo "==> Security scan passed"
