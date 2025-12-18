#!/bin/bash

ACCESS_TOKEN=$(./create_token.sh)
DEVICE_TOKEN="cPPBzesyBkmwq0sIJRISi2:APA91bETG5snqL9Qmgzm28Tb5T54k016CVMOmJxRPOr4XTy64KFqZTgTfd5d-T8gK6pxl9nAGdR-JzLqjY2F3AuCa7yzdeoeJbRbZXiWqHqqjZgBn3a9ix0"
PROJECT_ID="mashup-495e3"

if [ -z "$ACCESS_TOKEN" ]; then
  echo "❌ Missing ACCESS_TOKEN. Usage: ./node devtools/firebase/messaging/run.js <ACCESS_TOKEN>"
  exit 1
fi

JSON=$(cat <<EOF
{
  "message": {
    "token": "$DEVICE_TOKEN",
    "notification": {
      "title": "Mashup",
      "body": "New message!"
    },
    "data": {
      "screen": "chat",
      "id": "123"
    }
  }
}
EOF
)

curl -X POST \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$JSON" \
  "https://fcm.googleapis.com/v1/projects/$PROJECT_ID/messages:send"