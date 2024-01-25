import boto3

from boto3.dynamodb.conditions import Attr


def lambda_handler(event, context):
    for field in ["username", "session_id"]:
        if event.get(field) is None:
            return {"success": False, "message": f"Field: {field} is required"}

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

    try:
        response = users_table.scan(FilterExpression=Attr("username").eq(username))
    except Exception as e:
        return {"success": False, "message": str(e)}

    return {
        "success": True,
        "message": f"Got an user {username}.",
        "data": response["Items"],
    }
