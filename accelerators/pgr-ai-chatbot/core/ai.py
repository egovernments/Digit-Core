from openai import OpenAI
from utils.digit_utils import (
    get_auth_token, 
    file_complaint, 
    search_complaint
)
from utils.openai_utils import (
    create_thread,
    upload_message,
    get_run_status,
    get_assistant_message,
    create_assistant,
    transcribe_audio,
    generate_audio,
    get_tools_to_call
)
from utils.redis_utils import (
    get_redis_value,
    set_redis,
)

from utils.bhashini_utils import (
    bhashini_translate,
    bhashini_asr,
    bhashini_tts
)

import os
import json
import time
from dotenv import load_dotenv

load_dotenv(
    dotenv_path="ops/.env",
)

openai_api_key = os.getenv("OPENAI_API_KEY")

USERNAME = os.getenv("USERNAME")
PASSWORD = os.getenv("PASSWORD")

assistant_id = get_redis_value("assistant_id")

print(f"assistant id is {assistant_id}")

client = OpenAI(
    api_key=openai_api_key,
)

assistant = create_assistant(client, assistant_id)

assistant_id = assistant.id


def get_metadata(chat_id):
    """
    Get thread_id, run_id and status from redis
    """
    history = get_redis_value(chat_id)
    if history == None:
        history = {
            "thread_id": None,
            "run_id": None,
            "status": None,
        }
    else:
        history = json.loads(history)
    return history

def set_metadata(chat_id, history):
    """
    Set thread_id, run_id and status in redis
    """
    set_redis(chat_id, json.dumps(history))
    thread_id = history.get("thread_id")
    run_id = history.get("run_id")
    status = history.get("status")
    return thread_id, run_id, status

def get_or_create_thread_id(client, thread_id):
    """
    Get thread_id if exists else create a new thread
    using openAI assistant API
    """
    if thread_id:
        thread = client.beta.threads.retrieve(thread_id)
        thread_id = thread.id
    else:
        thread = create_thread(client)
        thread_id = thread.id
    return thread_id

def gather_complaint_details(input_message, history, assistant_id):
    """
    Converse with the user and gather complaint details using 
    openAI assistant API
    """
    thread_id = history.get("thread_id")
    status = history.get("status")
    print(thread_id, input_message, assistant_id)
    run_id, status = upload_message(client, thread_id, input_message, assistant_id)
    print("run.status is", status)
    run_id, status = get_run_status(client, thread_id, run_id)
    print(f"input message is {input_message}")
    print(f"run status is {status}")
    if status == "completed":
        assistant_message = get_assistant_message(client, thread_id)
    else:
        assistant_message = "something went wrong please check the openAI API"

    print(f"assistant message is {assistant_message}")

    history = {
        "thread_id": thread_id,
        "run_id": run_id,
        "status": status,
    }
    return assistant_message, history


def process_raise_complaint_action(parameters, tool_id, thread_id, run_id):
    """
    Run the raise_complaint function and get the service_id when the action
    required is raise_complaint
    """
    complaint = file_complaint(parameters)
    if complaint:
        service_id = complaint.get(
            "ServiceWrappers", []
        )[0].get(
            "service", {}
        ).get("serviceRequestId")
        tool_output_array = [
            {
                "tool_call_id": tool_id,
                "output": service_id
            }
        ]
        run = client.beta.threads.runs.submit_tool_outputs(
                thread_id=thread_id,
                run_id=run_id,
                tool_outputs=tool_output_array
        )
        run_id, status = get_run_status(client, thread_id, run.id)

        if status == "completed":
            assistant_message = get_assistant_message(client, thread_id)
        else:
            assistant_message = "something went wrong please check the openAI API"
        print(f"assistant message is {assistant_message}")

        history = {
            "thread_id": thread_id,
            "run_id": run_id,
            "status": status,
        }
        return assistant_message, history
    else:
        return "Complaint failed", history
    
def process_search_complaint_action(parameters, tool_id, thread_id, run_id):
    """
    
    Run the search_complaint function and get the application_status when 
    the action required is search_complaint

    """
    complaint = search_complaint(parameters)
    if complaint:
        application_status = complaint.get(
            "ServiceWrappers", []
        )[0].get(
            "service", {}
        ).get("applicationStatus")
        tool_output_array = [
            {
                "tool_call_id": tool_id,
                "output": application_status
            }
        ]
        run = client.beta.threads.runs.submit_tool_outputs(
            thread_id=thread_id,
            run_id=run_id,
            tool_outputs=tool_output_array
        )
        run_id, status = get_run_status(client, thread_id, run.id)

        if status == "completed":
            assistant_message = get_assistant_message(client, thread_id)
        else:
            assistant_message = "something went wrong please check the openAI API"

        print(f"assistant message is {assistant_message}")

        history = {
            "thread_id": thread_id,
            "run_id": run_id,
            "status": status,
        }
        return assistant_message, history
    else:
        return "Complaint not found", history
        
        
def compose_function_call_params(func_name, arguments):
    """
    Compose function call parameters based on the args
    provided by openAI function calling API
    """
    username = USERNAME
    auth_token = get_auth_token(
            {
                "username": username,
                "password": PASSWORD
            }
    )
    print(f"function name is {func_name}")
    parameters = json.loads(arguments)
    parameters["auth_token"] = auth_token
    parameters["username"] = username
    return parameters


def process_function_calls(tools_to_call, thread_id, run_id):
    """
    Method to manage all function calls for Mr Nags using openAI
    """
    for tool in tools_to_call:
        func_name = tool.function.name
        print(f"function name is {func_name}")
        parameters = compose_function_call_params(
            func_name, tool.function.arguments
        )
        if func_name == "raise_complaint":
            assistant_message, history = process_raise_complaint_action(
                parameters, tool.id, thread_id, run_id
            )
        elif func_name == "search_complaint":
            assistant_message, history = process_search_complaint_action(
                parameters, tool.id, thread_id, run_id
            )
        else:
            assistant_message = "This functionality is not supported yet. Please try again later."
            history = {
                "thread_id": thread_id,
                "run_id": run_id,
                "status": "requires_action",
            }
    return assistant_message, history

def chat(chat_id, input_message, client=client, assistant_id=assistant_id):
    """
    Main chat logic using OpenAI assistant API and fucntion calling API
    Please donot alter this function
    """
    assistant_message = "Something went wrong. Please try again later."    
    history = get_metadata(chat_id)
    print(history)
    thread_id = history.get("thread_id")
    run_id = history.get("run_id")
    status = history.get("status")
    thread_id = get_or_create_thread_id(client, thread_id)
    history["thread_id"] = thread_id
    print(f"thread id is {thread_id}")
    if status == "completed" or status == None:
        assistant_message, history = gather_complaint_details(
            input_message, history, assistant_id
        )      
        thread_id, run_id, status = set_metadata(chat_id, history)
        history = {
            "thread_id": thread_id,
            "run_id": run_id,
            "status": status,
        }
    if status == "requires_action":
        tools_to_call, run_id, status = get_tools_to_call(
            client, thread_id, run_id
        )
        assistant_message, history = process_function_calls(
            tools_to_call, thread_id, run_id
        )
        thread_id, run_id, status = set_metadata(chat_id, history)

    return assistant_message, history

def audio_chat(chat_id, audio_file):
    """
    Audio chat logic using OpenAI tts and stt
    """
    input_message = transcribe_audio(audio_file, client)
    assistant_message, history =  chat(chat_id, input_message)
    response_audio = generate_audio(assistant_message, client)
    return response_audio, assistant_message, history

def bhashini_text_chat(chat_id, text, lang): 
    """
    bhashini text chat logic
    """
    input_message = bhashini_translate(text, lang, "en")
    response_en, history = chat(chat_id, input_message)
    response = bhashini_translate(response_en, "en", lang)
    return response, response_en, history

def bhashini_audio_chat(chat_id, audio_file, lang):
    """
    bhashini voice chat logic
    """
    input_message = bhashini_asr(audio_file, lang, "en")
    response, history = chat(chat_id, input_message)
    response = bhashini_translate(response, "en", lang)
    audio_content = bhashini_tts(response, lang)
    return audio_content, response, history
