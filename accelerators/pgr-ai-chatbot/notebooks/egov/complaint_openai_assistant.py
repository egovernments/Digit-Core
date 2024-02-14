import openai
from openai._client import OpenAI
client = OpenAI()

import requests
import json

client = OpenAI()

def get_auth_token(username, password):
    url = 'https://health-qa.digit.org/user/oauth/token'
    headers = {
        'Authorization': 'Basic ZWdvdi11c2VyLWNsaWVudDo=',
        'Content-Type': 'application/x-www-form-urlencoded'
    }
    data = {
        'grant_type': 'password',
        'scope': 'read',
        'username': username,
        'password': password,
        'tenantId': 'default',
        'userType': 'EMPLOYEE'
    }
    response = requests.post(url, headers=headers, data=data)
    
    # Check if the request was successful (status code 200)
    if response.status_code == 200:
        # Process the response
        return response.json()
    else:
        return None

def file_complaint(auth_token,complaint,locality):
    print(auth_token)
    headers = {'Content-Type': 'application/json'}
    data = {
              "RequestInfo": {
                  "apiId": "Rainmaker",
                  "action": "",
                  "did": 1,
                  "key": "",
                  "msgId": "20170310130900|en_IN",
                  "requesterId": "",
                  "ts": 1513579888683,
                  "ver": ".01",
                  "authToken": auth_token
              },
              "service": {
                  "tenantId": "default",
                  "serviceCode": "SyncNotWorking",
                  "description": complaint,
                  "additionalDetail": {
                  },
                  "source": "mobile",
                  "address": {
                    "tenantId": "default",
                    "id": "2b62b10d-6113-4f29-a67e-b4b6574fc127",
                    "locality": {
                      "code": "GDzW4LwELgi",
                      "name": locality
                    }
                  },
                  "user": {
                    "userName": "9033767820",
                    "name": "dsmr1",
                    "type": "EMPLOYEE",
                    "mobileNumber": "9033767820",
                    "roles": [],
                    "tenantId": "default",
                    "uuid": "8c11b1df-dded-4c9b-b413-65bd09c028fc",
                    "active": True
                  }
              },
              "workflow": {
                  "action": "CREATE",
                  "assignes": [],
                  "comments": "complaint"
              }
          }
    url = "https://health-qa.digit.org/pgr-services/v2/request/_create"

    response = requests.post(url, headers=headers, data=json.dumps(data))

    if response.status_code == 200:
        response_data = response.json()
        return response_data
    else:
        print(f"Error: {response.status_code}")
        return None

def run_conversation(auth_token):
    complaint = input("What is your complaint? Also mention the locality.")

    messages = [{"role": "system", "content": "You are a helpful assitant who helps users file complaints by calling a function"},
                {"role": "assistant", "content": "What is your complaints? Also mention the locality."},
                {"role": "user", "content": complaint} 
              ]
    tools = [
        {
            "type": "function",
            "function": {
                "name": "file_complaint",
                "description": "File the complaint and get a complaint ID.",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "complaint": {
                            "type": "string",
                            "description": "Description of the complaint",
                        },
                        "locality": {
                            "type": "string",
                            "description": "Locality",
                        }
                    },
                    "required": ["complaint", "locality"],
                },
            },
        }
    ]
    response = client.chat.completions.create(
        model="gpt-4-1106-preview",
        messages=messages,
        tools=tools,
        tool_choice="auto",  # auto is default, but we'll be explicit
    )
    response_message = response.choices[0].message
    tool_calls = response_message.tool_calls
    if tool_calls:
        available_functions = {
            "file_complaint": file_complaint,
        } 
        messages.append(response_message)  
        for tool_call in tool_calls:
            function_name = tool_call.function.name
            function_to_call = available_functions[function_name]
            function_args = json.loads(tool_call.function.arguments)
            function_response = function_to_call(
                auth_token,
                complaint=function_args.get("complaint"),
                locality=function_args.get("locality")
            )
            messages.append(
                {
                    "tool_call_id": tool_call.id,
                    "role": "tool",
                    "name": function_name,
                    "content": function_response["ServiceWrappers"][0]["service"]["id"],
                }
            ) 
        second_response = client.chat.completions.create(
            model="gpt-4-1106-preview",
            messages=messages,
        )  
        return second_response.choices[0].message.content

username = input("Hello! I am DIGIT assistant. I help in filing complaints. Enter your DIGIT user name.")
password = input("Please enter your password")
auth_token = get_auth_token(username, password)
if(auth_token is not None):
  print(run_conversation(auth_token["access_token"]))