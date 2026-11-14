#!/usr/bin/env bash
# Owner-authorized PRIVATE cloud backup of TWO original GitHub Actions artifact ZIPs.
# Never run from a PR or default branch. Do not print API keys, upload PMTiles
# to Actions artifacts, enable public bucket access or modify app download gates.
set -euo pipefail
umask 077

[[ "\${GITHUB_REPOSITORY:-}" == "ThanhTaxiXanh/NaviTaxiMap" ]] || {
  echo "::error::Unexpected repository"; exit 2;
}
[[ "\${GITHUB_REF:-}" == "refs/heads/develop/phase0-android-maplibre" ]] || {
  echo "::error::Private backup must originate from develop/phase0-android-maplibre"; exit 2;
}
[[ "\${GITHUB_EVENT_NAME:-}" == "push" ]] || {
  echo "::error::Cloud backup requires a reviewed push with [private-backup-go] marker"; exit 2;
}

needed=(B2_KEY_ID B2_APPLICATION_KEY B2_BUCKET
        R2_ACCESS_KEY_ID R2_SECRET_ACCESS_KEY R2_ACCOUNT_ID R2_BUCKET)
missing=0
for variable in "\${needed[@]}"; do
  if [[ -z "\${!variable:-}" ]]; then
    echo "::error::GitHub Actions Secret missing: \${variable}"
    missing=1
  fi
done
[[ "$missing" -eq 0 ]] || exit 2
[[ "\${B2_BUCKET}" =~ ^[a-zA-Z0-9][a-zA-Z0-9-]{2,62}$ ]] || {
  echo "::error::B2_BUCKET is not a simple safe bucket name"; exit 2;
}
[[ "\${R2_BUCKET}" =~ ^[a-z0-9][a-z0-9-]{2,62}$ ]] || {
  echo "::error::R2_BUCKET is not a simple safe bucket name"; exit 2;
}
[[ "\${R2_ACCOUNT_ID}" =~ ^[a-fA-F0-9]{32}$ ]] || {
  echo "::error::R2_ACCOUNT_ID must be the 32-character Cloudflare account ID"; exit 2;
}

for bin in curl python3 rclone sha256sum; do
  command -v "$bin" >/dev/null || { echo "::error::Missing required tool $bin"; exit 2; }
done

root="$(mktemp -d "\${RUNNER_TEMP:?}/navitaxi-private-zip.XXXXXXXX")"
chmod 700 "$root"
trap 'rm -rf -- "$root"' EXIT
src="$root/original-zip"
mkdir -m 700 "$src"

zip_province="NaviTaxiMap-34-Provinces-PRIVATE-ORIGINAL.zip"
zip_national="NaviTaxiMap-Vietnam-PRIVATE-ORIGINAL.zip"

# Download the *actual* GitHub Actions artifact ZIP containers (no re-zipping
# extracted maps). Original artifact ID is pinned, never a mutable "latest".
download_original_zip() {
  local id="$1" dest="$2"
  curl --fail --location --silent --show-error --retry 3 \
    --connect-timeout 25 --max-time 1200 \
    -H "Authorization: Bearer \${GH_ACTIONS_TOKEN:?}" \
    -H "Accept: application/vnd.github+json" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    "https://api.github.com/repos/\${GITHUB_REPOSITORY}/actions/artifacts/\${id}/zip" \
    -o "$dest"
  [[ -s "$dest" ]] || { echo "::error::Original artifact download returned empty data"; exit 3; }
}
download_original_zip 14015875271 "$src/$zip_province"
download_original_zip 14015113186 "$src/$zip_national"

python3 tools/vietnam34/verify_private_preservation.py \
  --provinces "$src/$zip_province" \
  --national "$src/$zip_national" \
  --receipt "$root/verification-receipt.json"

# Whole-ZIP checksums are additional evidence to the full SHA of the 35 PMTiles.
(
  cd "$src"
  sha256sum "$zip_province" "$zip_national" > "$root/original-zip-sha256.txt"
)

# rclone config is assembled from step-scoped masked GitHub Secrets via env.
# Backblaze native B2 and Cloudflare S3-compatible API.
export RCLONE_CONFIG=/dev/null
export RCLONE_CONFIG_B2NTM_TYPE=b2
export RCLONE_CONFIG_B2NTM_ACCOUNT="$B2_KEY_ID"
export RCLONE_CONFIG_B2NTM_KEY="$B2_APPLICATION_KEY"
export RCLONE_CONFIG_R2NTM_TYPE=s3
export RCLONE_CONFIG_R2NTM_PROVIDER=Cloudflare
export RCLONE_CONFIG_R2NTM_REGION=auto
export RCLONE_CONFIG_R2NTM_ENDPOINT="https://\${R2_ACCOUNT_ID}.r2.cloudflarestorage.com"
export RCLONE_CONFIG_R2NTM_ACCESS_KEY_ID="$R2_ACCESS_KEY_ID"
export RCLONE_CONFIG_R2NTM_SECRET_ACCESS_KEY="$R2_SECRET_ACCESS_KEY"
export RCLONE_CONFIG_R2NTM_NO_CHECK_BUCKET=true

# Revisioned object prefix: never replace an earlier date's original source.
prefix="originals/github-run-43378133931"
b2remote="b2ntm:\${B2_BUCKET}/\${prefix}"
r2remote="r2ntm:\${R2_BUCKET}/\${prefix}"

# Copy ONLY the two source ZIPs. --immutable refuses to replace changed data.
# Never use sync, delete, copy public, or public ACL flags.
for remote in "$b2remote" "$r2remote"; do
  echo "Uploading two verified private ZIPs (remote names withheld)"
  rclone copy "$src" "$remote" --immutable --transfers 1 --checkers 2 \
    --retries 3 --low-level-retries 10 --stats 30s
  # Actual readback, not mere listed file sizes or multipart ETags.
  rclone check "$src" "$remote" --download --transfers 1 --checkers 1 \
    --retries 3 --low-level-retries 10
  echo "PRIVATE_CLOUD_READBACK_PASS"
done

# Upload only two small metadata receipts, after BOTH storage backends passed.
for remote in "$b2remote" "$r2remote"; do
  rclone copyto "$root/original-zip-sha256.txt" "$remote/original-zip-sha256.txt" --immutable
  rclone copyto "$root/verification-receipt.json" "$remote/verification-receipt.json" --immutable
  rclone check "$src" "$remote" --download --one-way --transfers 1 --checkers 1
done

# Evidence for GitHub Actions contains no raw PMTiles and no secret values.
mkdir -m 700 -p "$GITHUB_WORKSPACE/private-backup-receipt-only"
cp "$root/verification-receipt.json" "$GITHUB_WORKSPACE/private-backup-receipt-only/"
cp "$root/original-zip-sha256.txt" "$GITHUB_WORKSPACE/private-backup-receipt-only/"
echo "PRIVATE_B2_AND_R2_BACKUP_PASS originals=2 pmtiles=35 bytes=843734803"
