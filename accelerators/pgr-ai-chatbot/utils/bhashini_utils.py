from dotenv import load_dotenv
import requests
import json
import yaml
import os


load_dotenv(
    dotenv_path="ops/.env",
)

BHASHINI_KEY = os.getenv("BHASHINI_KEY")

def load_config():
    with open('ops/CONFIG.YAML', 'r') as f:
        config = yaml.safe_load(f)
    return config

config = load_config()

def validate_lang(source_lang, target_lang):
    if source_lang not in config.get('languages'):
        return False
    if target_lang not in config.get('languages'):
        return False
    if source_lang == target_lang:
        return False
    return True


def bhashini_translate(text, source_lang, target_lang):
    if validate_lang(source_lang, target_lang):
        data = {
            "pipelineTasks": [
                {
                    "taskType": "translation",
                    "config": {
                        "language": {
                            "sourceLanguage": source_lang,
                            "targetLanguage": target_lang
                        },
                        "serviceId": "ai4bharat/indictrans-v2-all-gpu--t4"
                    }
                },
            ],
            "inputData": {
                "input": [
                    {
                        "source": text
                    }
                ]
            }
        }
        headers = {
            'content-type': 'application/json',
            'Authorization': BHASHINI_KEY
        }
        # try changing auth token if it expires
        response = requests.post(
            "https://dhruva-api.bhashini.gov.in/services/inference/pipeline", 
            headers=headers, 
            json=data
        )

        if response.status_code == 200:
            response_text = json.loads(response.content.decode('utf-8'))
            i_answer = response_text.get(
                'pipelineResponse', []
            )[0].get(
                'output', []
            )[0].get(
                'target'
            )
            return i_answer
        else:
            return text
    else:
        return text


def bhashini_asr(audio_content, source_lang, target_lang):
    
    data = {
        "pipelineTasks": [
            {
                "taskType": "asr",
                "config": {
                    "language": {
                        "sourceLanguage": source_lang
                    },
                    "serviceId": "ai4bharat/conformer-hi-gpu--t4"
                }
            },
            {
                "taskType": "translation",
                "config": {
                    "language": {
                        "sourceLanguage": source_lang,
                        "targetLanguage": target_lang
                    },
                    "serviceId": "ai4bharat/indictrans-v2-all-gpu--t4"
                }
            }
        ],
        "inputData": {
            "audio": [
                {
                    "audioContent":audio_content
                }
            ]
        }
    }
    headers = {
        'content-type': 'application/json',
        'Authorization': BHASHINI_KEY
    }
    response = requests.post(
        "https://dhruva-api.bhashini.gov.in/services/inference/pipeline", 
        headers=headers, 
        json=data
    )

    if response.status_code == 200:
        response_asr = json.loads(response.content.decode('utf-8'))
        answer = response_asr.get(
            'pipelineResponse', []
        )[1].get(
            'output', []
        )[0].get(
            'target'
        )
        return answer
    else:
        return audio_content

def bhashini_tts(text, lang):
    url = 'https://bhashini.ai/v1/synthesize'
    headers = {
        'accept': 'audio/mpeg',
        'Content-Type': 'application/json',
    }
    data = {
        'text': text,
        'languageId': lang,
        'voiceId': 1
    }
    response = requests.post(url, headers=headers, data=json.dumps(data))
    return response
    
    