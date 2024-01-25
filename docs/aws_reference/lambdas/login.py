import boto3

from boto3.dynamodb.conditions import Attr

import hashlib
import uuid


def hash_password(password):
    return hashlib.sha256(password.encode()).hexdigest()


def lambda_handler(event, context):
    for field in ["username", "password"]:
        if event.get(field) is None:
            return {
                "success": False,
                "status": 400,
                "message": f"Field: {field} is required",
            }

    username = event["username"]
    password = event["password"]

    table = boto3.resource("dynamodb").Table("users")

    try:
        response = table.get_item(Key={"username": username})

        if response.get("Item") is None:
            return {"success": False, "status": 404, "message": "User does not exist."}

        actual_password_hash = response["Item"]["password_hash"]
        hashed_password = hash_password(password)

        if actual_password_hash != hashed_password:
            return {"success": False, "status": 401, "message": "Wrong password."}
    except Exception as e:
        return {"success": False, "status": 500, "message": str(e)}

    # generate a new session_id
    session_id = str(uuid.uuid4())

    try:
        table.update_item(
            Key={"username": username},
            UpdateExpression="SET session_id = :session_id",
            ExpressionAttributeValues={":session_id": session_id},
        )
    except Exception as e:
        return {"success": False, "status": 500, "message": str(e)}

    return {
        "success": True,
        "status": 200,
        "message": f"Approved login.",
        "session_id": session_id,
    }
