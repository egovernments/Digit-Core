from dotenv import load_dotenv
from utils.redis_utils import set_redis
import time
import os


load_dotenv(
    dotenv_path="ops/.env",
)

openai_api_key = os.getenv("OPENAI_API_KEY")
assistant_id = os.getenv("ASSISTANT_ID")

#OPENAI FUNCTION CALLS

authenticate_user = {
    "name": "authenticate_user",
    "description": "Authenticate user",
    "parameters": {
        "type": "object",
        "properties": {
            "username": {
                "type": "string",
                "description": "Username"
            },
            "password": {
                "type": "string",
                "description": "Password"
            }
        },
        "required": ["username", "password"]
    }
}

raise_complaint ={
    "name": "raise_complaint",
    "description": "Raise complaint",
    "parameters": {
        "type": "object",
        "properties": {
            "description": {
                "type": "string",
                "description": "Detailed description of complaint"
            },
            "service_code": {
                "type": "string",
                "description": "service code of complaint extracted from description",
                "enum": [
                    "GarbageNeedsTobeCleared", "NoStreetLight", "StreetLightNotWorking",
                    "BurningOfGarbage", "OverflowingOrBlockedDrain", "illegalDischargeOfSewage",
                    "BlockOrOverflowingSewage", "ShortageOfWater", "DirtyWaterSupply", "BrokenWaterPipeOrLeakage",
                    "WaterPressureisVeryLess", "HowToPayPT", "WrongCalculationPT", "ReceiptNotGenerated",
                    "DamagedRoad", "WaterLoggedRoad", "ManholeCoverMissingOrDamaged", "DamagedOrBlockedFootpath",
                    "ConstructionMaterialLyingOntheRoad", "RequestSprayingOrFoggingOperation", "StrayAnimals", "DeadAnimals",
                    "DirtyOrSmellyPublicToilets", "PublicToiletIsDamaged", "NoWaterOrElectricityinPublicToilet", "IllegalShopsOnFootPath",
                    "IllegalConstructions", "IllegalParking"
                ]
            },
            "auth_token": {
                "type": "string",
                "description": "Authentication token of user"
            },
            "city": {
                "type": "string",
                "description": "City of complaint"
            },
            "state": {
                "type": "string",
                "description": "State of complaint"
            },
            "district": {
                "type": "string",
                "description": "district of complaint"
            },
            "region": {
                "type": "string",
                "description": "region of complaint"
            },
            "locality": {
                "type": "string",
                "description": "locality of complaint"
            },
            #"username": {
            #    "type": "string",
            #    "description": "username of user"
            #},
            #"password": {
            #    "type": "string",
            #    "description": "password of user"
            #},
            "name": {
                "type": "string",
                "description": "name of user"
            },
            "mobile_number": {
                "type": "string",
                "description": "mobile number of user"
            },
        },
        "required": [
            "description",
            "service_code",
            "city",
            "state",
            "district",
            "region",
            "locality",
            "username",
            "password",
            "name",
            "mobile_number"
        ]
    },
}

search_complaint = {
    "name": "search_complaint",
    "description": "Search complaint",
    "parameters": {
        "type": "object",
        "properties": {
            "auth_token": {
                "type": "string",
                "description": "Authentication token of user"
            },
            "name": {
                "type": "string",
                "description": "name of user"
            },
            "mobile_number": {
                "type": "string",
                "description": "mobile number of user"
            },
        },
        "required": ["auth_token", "name", "mobile_number"]
    }
}

def create_assistant(client, assistant_id):
    try:
        assistant = client.beta.assistants.retrieve(assistant_id=assistant_id)
        return assistant
    except Exception as e:
        assistant = client.beta.assistants.create(
        name="Complaint Assistant",
        instructions="You ara a helpful complaint assistant who will collect information about a complaint and raise the complaint. You are talking to common citizens who are not tech savvy, so ask questions one by one. You will also have to search for complaints raised by the user.",
        model="gpt-4",
        tools=[
                {
                    "type": "function",
                    "function": raise_complaint
                },
                {
                    "type": "function",
                    "function": search_complaint
                }
            ]
        )
        set_redis("assistant_id", assistant.id)
        return assistant

def create_thread(client):
    thread = client.beta.threads.create()
    return thread

def upload_message(client, thread_id, input_message, assistant_id):
    message = client.beta.threads.messages.create(
        thread_id=thread_id,
        role="user",
        content=input_message
    )

    run = client.beta.threads.runs.create(
        thread_id=thread_id,
        assistant_id=assistant_id,
    )
    
    return run

def get_run_status(run, client, thread):
    delay = 5
    run_status = run.status
    while run_status not in ["completed", "failed", "requires_action"]:
        time.sleep(delay)
        run = client.beta.threads.runs.retrieve(
            thread_id=thread.id,
            run_id=run.id,
        )
        run_status = run.status
        delay = 8 if run_status == "requires_action" else 5
    return run, run_status

def get_assistant_message(client, thread_id):
    messages = client.beta.threads.messages.list(
        thread_id=thread_id,
    )
    return messages.data[0].content[0].text.value


def transcribe_audio(audio_file, client):
    transcript = client.audio.transcriptions.create(
        model="whisper-1", 
        file=audio_file
    )
    return transcript.text