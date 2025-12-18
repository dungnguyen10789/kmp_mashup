#!/bin/bash

# ---- CONFIG ----
KEY_FILE="./mashup-495e3-firebase-adminsdk-fbsvc-7e256bf911.json"
SCOPE="https://www.googleapis.com/auth/firebase.messaging"
TOKEN_URL="https://oauth2.googleapis.com/token"
# ----------------

# Extract values from json
client_email=$(jq -r '.client_email' "$KEY_FILE")
private_key=$(jq -r '.private_key' "$KEY_FILE" | sed 's/\\n/\n/g')

# Generate JWT header + payload
header=$(echo -n '{"alg":"RS256","typ":"JWT"}' | openssl base64 -e | tr -d '=' | tr '/+' '_-')

iat=$(date +%s)
exp=$((iat+3600))

payload=$(echo -n "{\"iss\":\"$client_email\",\"scope\":\"$SCOPE\",\"aud\":\"$TOKEN_URL\",\"exp\":$exp,\"iat\":$iat}" \
    | openssl base64 -e | tr -d '=' | tr '/+' '_-')

# Write private key to temp file
KEY_TMP=$(mktemp)
echo "$private_key" > "$KEY_TMP"

# Sign JWT
signature=$(printf "%s.%s" "$header" "$payload" \
    | openssl dgst -sha256 -sign "$KEY_TMP" \
    | openssl base64 -e | tr -d '=' | tr '/+' '_-')

rm "$KEY_TMP"

jwt="$header.$payload.$signature"

# Exchange JWT -> OAuth Token
access_token=$(curl -s -X POST "$TOKEN_URL" \
  -d "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=$jwt" \
  | jq -r '.access_token')

echo "$access_token"
