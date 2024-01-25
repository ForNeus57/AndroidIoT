import boto3

from boto3.dynamodb.conditions import Attr


def lambda_handler(event, context):
    username = event["username"]
    device_id = event["device_id"]
    session_id = event["session_id"]

    users_table = boto3.resource("dynamodb").Table("users")
    devices_table = boto3.resource("dynamodb").Table("devices")

    # check if user exists
    if users_table.get_item(Key={"username": username}).get("Item") is None:
        return {"success": False, "status": 404, "message": "User does not exist."}

    # check if session_id is valid
    if (
        users_table.get_item(Key={"username": username}).get("Item")["session_id"]
        != session_id
    ):
        return {"success": False, "status": 401, "message": "Invalid session_id."}

    try:
        response = devices_table.get_item(
            Key={"device_id": device_id, "username": username}
        )

        if response.get("Item") is None:
            return {
                "success": False,
                "message": f"Device with id: {device_id} does not exist",
            }
    except Exception as e:
        return {"success": False, "message": str(e)}

    try:
        response = devices_table.delete_item(
            Key={"device_id": device_id, "username": username}
        )
    except Exception as e:
        return {"success": False, "message": str(e)}

    return {"success": True, "message": f"Deleted the device with id: {device_id}"}
