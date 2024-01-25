import boto3

from boto3.dynamodb.conditions import Attr

import hashlib


def hash_password(password):
    return hashlib.sha256(password.encode()).hexdigest()


def lambda_handler(event, context):
    for field in ["username", "password"]:
        if event.get(field) is None:
            return {
                "success": False,
                "status": 400,
                "message": f"Field: {field} is required.",
            }

    username = event["username"]
    password = event["password"]

    table = boto3.resource("dynamodb").Table("users")

    # check if user already exists
    if table.get_item(Key={"username": username}).get("Item") is not None:
        return {"success": False, "status": 400, "message": "User already exists."}

    hashed_password = hash_password(password)

    try:
        response = table.put_item(
            Item={"username": username, "password_hash": hashed_password}
        )
    except Exception as e:
        return {"success": False, "status": 500, "message": str(e)}

    return {"success": True, "status": 200, "message": f"User {username} created."}
