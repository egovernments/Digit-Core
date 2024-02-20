import requests
import json
import yaml

def load_config():
    with open('CONFIG.YAML', 'r') as f:
        config = yaml.safe_load(f)
    return config

config = load_config()

def bhashini_input(text, lang):
    
    # lang_ = config['languages'][lang]
    # print(f"The language is: {lang_}")
    print('i_working')
    '''
    language = {'hi': 'Hindi', 'en': 'English', 'pa': 'Punjabi'}
    lang_ = language.get(lang)
    '''
    
    data = {
    "pipelineTasks": [
        {
            "taskType": "translation",
            "config": {
                "language": {
                    "sourceLanguage": lang,
                    "targetLanguage": "en"
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
    headers = {'content-type': 'application/json','Authorization':'0ELDJvqbaDLzAGPIR1Dfv38ehE21HkMjxWkXYWq-Mk1bajlyyxXMyHGpwb3lD2cz'}
    # try changing auth token if it expires
    response = requests.post("https://dhruva-api.bhashini.gov.in/services/inference/pipeline", headers=headers, json=data)

    if response.status_code == 200:
        response_text = json.loads(response.content.decode('utf-8'))
        i_answer = response_text['pipelineResponse'][0]['output'][0]['target']
        print(i_answer)
        return i_answer
    else:
        return "Error: {response.status_code}"


def bhashini_output(text, lang):
    # lang_ = config['languages'][lang]
    # print(f"The language is: {lang_}")
    print('o_working')

    data = {
    "pipelineTasks": [
        {
            "taskType": "translation",
            "config": {
                "language": {
                    "sourceLanguage": "en",
                    "targetLanguage": lang
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
    headers = {'content-type': 'application/json','Authorization':'0ELDJvqbaDLzAGPIR1Dfv38ehE21HkMjxWkXYWq-Mk1bajlyyxXMyHGpwb3lD2cz'}
    # try changing auth token if it expires
    response = requests.post("https://dhruva-api.bhashini.gov.in/services/inference/pipeline", headers=headers, json=data)

    if response.status_code == 200:
        response_text = json.loads(response.content.decode('utf-8'))
        o_answer = response_text['pipelineResponse'][0]['output'][0]['target']
        print(o_answer)
        return o_answer
    else:
        return "Error: {response.status_code}"

def bhashini_asr(audio_content, lang):
    
    data = {
    "pipelineTasks": [
        {
            "taskType": "asr",
            "config": {
                "language": {
                    "sourceLanguage": lang
                },
                "serviceId": "ai4bharat/conformer-hi-gpu--t4"
            }
        },
        {
            "taskType": "translation",
            "config": {
                "language": {
                    "sourceLanguage": lang,
                    "targetLanguage": "en"
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
    headers = {'content-type': 'application/json','Authorization':'0ELDJvqbaDLzAGPIR1Dfv38ehE21HkMjxWkXYWq-Mk1bajlyyxXMyHGpwb3lD2cz'}
    response = requests.post("https://dhruva-api.bhashini.gov.in/services/inference/pipeline", headers=headers, json=data)

    if response.status_code == 200:
        response_asr = json.loads(response.content.decode('utf-8'))
        answer = response_asr['pipelineResponse'][1]['output'][0]['target']
        print(answer)
        return answer
        # return response_asr
    else:
        return "Error: {response.status_code}"
    
    '''
def bhashini_input_tts(text, lang):
    lang_ = config['languages'][lang]
    data = {
        "inputText": text,
        "inputLanguage": lang_,
        "outputLanguage": "English"
    }
    headers = {'Content-Type': 'application/json'}
    
    response = requests.post("https://tts.bhashini.ai/v1/translate", headers=headers, json=data)

    if response.status_code == 200:
        return response.text
    else:
        print("Error:", response.status_code, response.text)
'''

'''
def bhashini_output_tts(text,lang):
    lang_ = config['languages'][lang]
    print(f"The language is: {lang_}")
    print('done')

    data = {
        "inputText": text,
        "inputLanguage": "English",
        "outputLanguage": lang_
    }
    headers = {'Content-Type': 'application/json'}
    response = requests.post("https://tts.bhashini.ai/v1/translate", headers=headers, json=data)

    if response.status_code == 200:
        return response.text
    else:
        print("Error:", response.status_code, response.text)
'''