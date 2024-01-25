import boto3

from boto3.dynamodb.conditions import Attr


def lambda_handler(event, context):
    for field in ["device_id", "username", "session_id"]:
        if event.get(field) is None:
            return {"success": False, "message": f"Field: {field} is required"}

    device_id = event["device_id"]
    username = event["username"]
    session_id = event["session_id"]

    users_table = boto3.resource("dynamodb").Table("users")

    # check if user exists
    if users_table.get_item(Key={"username": username}).get("Item") is None:
        return {"success": False, "status": 404, "message": "User does not exist."}

    # check if session_id is valid
    if (
        users_table.get_item(Key={"username": username}).get("Item")["session_id"]
        != session_id
    ):
        return {"success": False, "status": 401, "message": "Invalid session_id."}

    table = boto3.resource("dynamodb").Table("data")

    try:
        response = table.scan(FilterExpression=Attr("device_id").eq(device_id))
    except Exception as e:
        return {"success": False, "message": str(e)}

    return {"success": True, "message": f"Success", "data": response["Items"]}
