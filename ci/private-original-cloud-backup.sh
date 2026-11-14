#!/usr/bin/env bash
# Owner-authorized PRIVATE cloud backup of TWO original GitHub Actions artifact ZIPs.
# Never run from a PR or default branch. Do not print API keys, upload PMTiles
# to Actions artifacts, enable public bucket access or modify app download gates.
set -euo pipefail
umask 077

[[ "${GITHUB_REPOSITORY:-}" == "ThanhTaxiXanh/NaviTaxiMap" ]] || {
  echo "::error::Unexpected repository"; exit 2;
}
[[ "${GITHUB_REF:-}" == "refs/heads/develop/phase0-android-maplibre" ]] || {
  echo "::error::Private backup must originate from develop/phase0-android-maplibre"; exit 2;
}
[[ "${GITHUB_EVENT_NAME:-}" == "push" ]] || {
  echo "::error::Cloud backup requires a reviewed push with [private-backup-go] marker"; exit 2;
}

needed=(B2_KEY_ID B2_APPLICATION_KEY B2_BUCKET
        R2_ACCESS_KEY_ID R2_SECRET_ACCESS_KEY R2_ACCOUNT_ID R2_BUCKET)
missing=0
for variable in "${needed[@]}"; do
  if [[ -z "${!variable:-}" ]]; then
    echo "::error::GitHub Actions Secret missing: ${variable}"
    missing=1
  fi
done
[[ "$missing" -eq 0 ]] || exit 2
[[ "${B2_BUCKET}" =~ ^[a-zA-Z0-9][a-zA-Z0-9-]{2,62}$ ]] || {
  echo "::error::B2_BUCKET is not a simple safe bucket name"; exit 2;
}
[[ "${R2_BUCKET}" =~ ^[a-z0-9][a-z0-9-]{2,62}$ ]] || {
  echo "::error::R2_BUCKET is not a simple safe bucket name"; exit 2;
}
# Cloudflare ID checked only after B2 backup is safely verified.

for bin in curl python3 rclone sha256sum; do
  command -v "$bin" >/dev/null || { echo "::error::Missing required tool $bin"; exit 2; }
done

root="$(mktemp -d "${RUNNER_TEMP:?}/navitaxi-private-zip.XXXXXXXX")"
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
    -H "Authorization: Bearer ${GH_ACTIONS_TOKEN:?}" \
    -H "Accept: application/vnd.github+json" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    "https://api.github.com/repos/${GITHUB_REPOSITORY}/actions/artifacts/${id}/zip" \
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
# Revisioned prefix, never overwrite historical versions or make them public.
prefix="originals/github-run-43378133931"
b2remote="b2ntm:${B2_BUCKET}/${prefix}"

upload_and_readback() {
  local label="$1" remote="$2"
  echo "Starting $label private ZIP preservation (no public ACL)"
  rclone copy "$src" "$remote" --immutable --transfers 1 --checkers 2 \
    --retries 3 --low-level-retries 10 --stats 30s
  rclone check "$src" "$remote" --download --one-way --transfers 1 --checkers 1 \
    --retries 3 --low-level-retries 10
  rclone copyto "$root/original-zip-sha256.txt" "$remote/original-zip-sha256.txt" --immutable
  rclone copyto "$root/verification-receipt.json" "$remote/verification-receipt.json" --immutable
  echo "PRIVATE_${label}_READBACK_PASS originals=2 pmtiles=35 bytes=843734803"
}

# Backblaze first: misconfigured R2 account ID must not prevent B2 preservation.
upload_and_readback B2 "$b2remote"

# GitHub evidence contains metadata ONLY, even if R2 later fails.
mkdir -m 700 -p "$GITHUB_WORKSPACE/private-backup-receipt-only"
cp "$root/verification-receipt.json" "$GITHUB_WORKSPACE/private-backup-receipt-only/"
cp "$root/original-zip-sha256.txt" "$GITHUB_WORKSPACE/private-backup-receipt-only/"
printf '%s\n' "PRIVATE_B2_READBACK_PASS originals=2 pmtiles=35 bytes=843734803" \
  > "$GITHUB_WORKSPACE/private-backup-receipt-only/b2-confirmation.txt"

# Accept ONLY Cloudflare's official 32-character ID or its canonical R2 endpoint,
# not arbitrary user-supplied hosts. Never print the raw secret value.
r2_id="$R2_ACCOUNT_ID"
r2_id="${r2_id#"${r2_id%%[![:space:]]*}"}"
r2_id="${r2_id%"${r2_id##*[![:space:]]}"}"
if [[ "$r2_id" =~ ^https://([a-fA-F0-9]{32})\.r2\.cloudflarestorage\.com/?$ ]]; then
  r2_id="${BASH_REMATCH[1]}"
elif [[ "$r2_id" =~ ^([a-fA-F0-9]{32})\.r2\.cloudflarestorage\.com$ ]]; then
  r2_id="${BASH_REMATCH[1]}"
fi
if [[ ! "$r2_id" =~ ^[a-fA-F0-9]{32}$ ]]; then
  echo "::error::R2_ACCOUNT_ID invalid; use Cloudflare's 32-hex Account ID or official R2 endpoint. B2 readback already verified; R2 pending."
  exit 2
fi
export RCLONE_CONFIG_R2NTM_TYPE=s3
export RCLONE_CONFIG_R2NTM_PROVIDER=Cloudflare
export RCLONE_CONFIG_R2NTM_REGION=auto
export RCLONE_CONFIG_R2NTM_ENDPOINT="https://${r2_id}.r2.cloudflarestorage.com"
export RCLONE_CONFIG_R2NTM_ACCESS_KEY_ID="$R2_ACCESS_KEY_ID"
export RCLONE_CONFIG_R2NTM_SECRET_ACCESS_KEY="$R2_SECRET_ACCESS_KEY"
export RCLONE_CONFIG_R2NTM_NO_CHECK_BUCKET=true

r2remote="r2ntm:${R2_BUCKET}/${prefix}"
upload_and_readback R2 "$r2remote"
printf '%s\n' "PRIVATE_R2_READBACK_PASS originals=2 pmtiles=35 bytes=843734803" \
  > "$GITHUB_WORKSPACE/private-backup-receipt-only/r2-confirmation.txt"
echo "PRIVATE_B2_AND_R2_BACKUP_PASS originals=2 pmtiles=35 bytes=843734803"
