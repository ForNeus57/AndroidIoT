import uuid
import boto3


def lambda_handler(event, context):
    for field in ["username", "MAC", "session_id"]:
        if event.get(field) is None:
            return {
                "success": False,
                "status": 400,  # 400 is the HTTP status code for "Bad Request"
                "message": f"Field: {field} is required",
            }

    username = event["username"]
    MAC = event["MAC"]
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

    # generate a new device_id
    device_id = str(uuid.uuid4())

    try:
        # create a new device
        devices_table.put_item(
            Item={"device_id": device_id, "username": username, "MAC": MAC}
        )
    except Exception as e:
        return {"success": False, "status": 500, "message": str(e)}

    return {
        "success": True,
        "status": 200,
        "message": f"Created a new device with device_id: {device_id} for user: {username}.",
        "device_id": device_id,
    }
