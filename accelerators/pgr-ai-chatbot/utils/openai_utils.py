from dotenv import load_dotenv
from utils.redis_utils import set_redis
import random
from pydub import AudioSegment
import time
import os


load_dotenv(
    dotenv_path="ops/.env",
)

with open("prompts/main.txt", "r") as file:
    main_prompt = file.read()

print(main_prompt)

openai_api_key = os.getenv("OPENAI_API_KEY")
assistant_id = os.getenv("ASSISTANT_ID")
model_name = os.getenv("MODEL_NAME")

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
        instructions=main_prompt,
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
    try: 
        run_status = run.status
    except Exception as e:
        print(e)
    
    while run_status not in ["completed", "failed", "requires_action"]:
        print("Current run status:", run_status)  # check statement
        time.sleep(delay)
        run = client.beta.threads.runs.retrieve(
            thread_id=thread.id,
            run_id=run.id,
        )
        run_status = run.status
        delay = 8 if run_status == "requires_action" else 5

    print("Final run status:", run_status)  # check statement
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

def generate_audio(text, client):
    response = client.audio.speech.create(
                model="tts-1",
                voice="alloy",
                input=text
            )
    return response

def get_duration_pydub(file_path):
   audio_file = AudioSegment.from_file(file_path)
   duration = audio_file.duration_seconds
   return duration

def get_random_wait_messages(not_always=False):
    messages = [
        "Please wait...",
        "I am thinking...",
        "I am processing your request...",
        "Hold on...",
        "I am on it...",
        "I am working on it...",
    ]
    if not_always:
        rand = random.randint(0, 2)
        print(rand)
        if rand == 1:
            random_message = random.choice(messages)
        else:
            random_message = ""
    else:
        random_message = random.choice(messages)
    return random_message