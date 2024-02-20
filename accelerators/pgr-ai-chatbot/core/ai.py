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
    generate_audio
)
from utils.redis_utils import (
    get_redis_value,
    set_redis,
)

from utils.bhashini import (
    bhashini_input,
    bhashini_output,
    bhashini_asr
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
print(assistant_id)

client = OpenAI(
    api_key=openai_api_key,
)

assistant = create_assistant(client, assistant_id)

def chat(chat_id, input_message):
    
    assistant_message = "Something went wrong. Please try again later."
    
    history = get_redis_value(chat_id)
    print(history)

    if history == None:
        history = {
            "thread_id": None,
            "run_id": None,
            "status": None,
        }
    else:
        history = json.loads(history)
    thread_id = history.get("thread_id")
    run_id = history.get("run_id")
    status = history.get("status")

    try:
        run = client.beta.threads.runs.retrieve(thread_id, run_id)
    except:
        run = None
    try:
        thread = client.beta.threads.retrieve(thread_id)
        thread_id = thread.id
    except:
        thread = create_thread(client)
        thread_id = thread.id

    if status == "completed" or status == None:
        assistant_ID = assistant.id
        print(f"Assistant id: {assistant_ID}")
        run = upload_message(client, thread_id, input_message, assistant_ID)
        run, status = get_run_status(run, client, thread)

        assistant_message = get_assistant_message(client, thread_id)

        history = {
            "thread_id": thread_id,
            "run_id": run.id,
            "status": status,
        }
        set_redis(chat_id, json.dumps(history))
        print(history)
    
    if status == "requires_action":
        if run:
            tools_to_call = run.required_action.submit_tool_outputs.tool_calls
        else:
            run, status = get_run_status(run, client, thread)
            tools_to_call = run.required_action.submit_tool_outputs.tool_calls

        for tool in tools_to_call:
            username = USERNAME
            auth_token = get_auth_token(
                {
                    "username": username,
                    "password": PASSWORD
                }
            )
            func_name = tool.function.name
            print(f"Function name: {func_name}")
            parameters = json.loads(tool.function.arguments)
            parameters["auth_token"] = auth_token
            parameters["username"] = username
            print(f"Parameters: {parameters}")

            tool_output_array = []

            if func_name == "raise_complaint":
                complaint = file_complaint(parameters)
                if complaint:
                    tool_output_array.append(
                        {
                            "tool_call_id": tool.id,
                            "output": complaint["ServiceWrappers"][0]["service"]["serviceRequestId"]
                        }
                    )
                    run = client.beta.threads.runs.submit_tool_outputs(
                        thread_id=thread_id,
                        run_id=run.id,
                        tool_outputs=tool_output_array
                    )
                    run, status = get_run_status(run, client, thread)

                    message = get_assistant_message(client, thread_id)

                    history = {
                        "thread_id": thread.id,
                        "run_id": run.id,
                        "status": status,
                    }
                    set_redis(chat_id, json.dumps(history))
                    return message, history
                else:
                    return "Complaint failed", history
                
            elif func_name == "search_complaint":
                complaint = search_complaint(parameters)
                if complaint:
                    tool_output_array.append(
                        {
                            "tool_call_id": tool.id,
                            "output": complaint["ServiceWrappers"][0]["service"]["applicationStatus"]
                        }
                    )
                    run = client.beta.threads.runs.submit_tool_outputs(
                        thread_id=thread.id,
                        run_id=run.id,
                        tool_outputs=tool_output_array
                    )
                    run, status = get_run_status(run, client, thread)

                    message = get_assistant_message(client, thread_id)

                    history = {
                        "thread_id": thread_id,
                        "run_id": run.id,
                        "status": status,
                    }
                    set_redis(chat_id, json.dumps(history))
                    return message, history
                else:
                    return "Complaint not found", history
        
    return assistant_message, history

def audio_chat(chat_id, audio_file):
    input_message = transcribe_audio(audio_file, client)
    print(f"The input message is : {input_message}")
    assistant_message, history =  chat(chat_id, input_message)
    response_audio = generate_audio(assistant_message, client)
    return response_audio, history

def bhashini_text_chat(chat_id, text, lang): #lang
    # For some specific Indian languages like Tamil, Marathi, Kannada , Bhashini API works better than Google Translate API
    '''Supported languages are : Assamese, Bengali, Bodo, Dogri, English, Gujarati, Hindi, Kannada, Kashmiri, Konkani, Maithili, Malayalam, 
    Manipuri, Marathi, Nepali, Odia, Punjabi, Sanskrit, Santali, Sindhi, Tamil, Telugu, Urdu'''
    # Assuming original input is in Punjabi, translating into English using Bhashini API
    # lang = get_redis_value('lang').decode('utf-8')
    input_message = bhashini_input(text, lang)
    response, history = chat(chat_id, input_message)
    print(f"the response is: {response}")
    print(f"the history is: {history}")
    # translating English to Punjabi using Bhashini API
    temp_message = bhashini_output(response, lang)
    output_message = f"the problem you've entered is this:{temp_message}"
    
    return output_message, history

def bhashini_audio_chat(chat_id, audio_file, lang):
    input_message = bhashini_asr(audio_file, lang)
    print(f"The input message is : {input_message}")
    response, history =  chat(chat_id, input_message)
    output_message = bhashini_output(response, lang)
    # response_audio = generate_audio(assistant_message, client)
    # return response_audio, history
    return output_message, history
