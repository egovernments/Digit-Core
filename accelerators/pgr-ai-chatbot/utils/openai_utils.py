from dotenv import load_dotenv
from utils.bhashini_utils import bhashini_translate
from utils.redis_utils import set_redis
import random
from pydub import AudioSegment
import time
import os


load_dotenv(
    dotenv_path="ops/.env",
)

with open("prompts/main.txt", "r") as file:
    main_prompt = file.read().replace('\n', ' ')

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
                "description": "City of the complaint"
            },
            "state": {
                "type": "string",
                "description": "State of the complaint"
            },
            "district": {
                "type": "string",
                "description": "district of the complaint"
            },
            "region": {
                "type": "string",
                "description": "region of the complaint"
            },
            "locality": {
                "type": "string",
                "description": "locality of the complaint"
            },
            "name": {
                "type": "string",
                "description": "name of the user"
            },
            "mobile_number": {
                "type": "string",
                "description": "mobile number of the user"
            },
        },
        "required": [
            "description",
            "service_code",
            "locality",
            "city",
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
        model=model_name,
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

def create_run(client, thread_id, assistant_id):
    run = client.beta.threads.runs.create(
        thread_id=thread_id,
        assistant_id=assistant_id,
    )
    return run.id, run.status


def upload_message(client, thread_id, input_message, assistant_id):
    message = client.beta.threads.messages.create(
        thread_id=thread_id,
        role="user",
        content=input_message
    )
    run_id, run_status = create_run(client, thread_id, assistant_id)
    return run_id, run_status

def get_run_status(client, thread_id, run_id):
    run = client.beta.threads.runs.retrieve(
        thread_id=thread_id,
        run_id=run_id
    )
    delay = 5
    try: 
        run_status = run.status
        print(f"run status inside method is {run_status}")
    except Exception as e:
        return None, None

    while run_status not in ["completed", "failed", "requires_action"]:
        time.sleep(delay)
        run = client.beta.threads.runs.retrieve(
            thread_id=thread_id,
            run_id=run.id,
        )
        run_id = run.id
        run_status = run.status
        delay = 8 if run_status == "requires_action" else 5

    return run_id, run_status

def get_tools_to_call(client, thread_id, run_id):
    run = client.beta.threads.runs.retrieve(
        run_id=run_id,
        thread_id=thread_id
    )
    tools_to_call = run.required_action.submit_tool_outputs.tool_calls
    return tools_to_call, run.id, run.status

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

def get_random_wait_messages(not_always=False, lang="en"):
    messages = [
        "Please wait",
        "I am processing your request",
        "Hold on",
        "I am on it",
        "I am working on it",
    ]
    if not_always:
        rand = random.randint(0, 2)
        if rand == 1:
            random_message = random.choice(messages)
            random_message = bhashini_translate(random_message, "en", lang)
        else:
            random_message = ""
    else:
        random_message = random.choice(messages)
        random_message = bhashini_translate(random_message, "en", lang)
    return random_message