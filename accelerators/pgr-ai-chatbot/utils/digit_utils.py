import requests
import json

def get_auth_token(data):
    url = 'https://staging.digit.org/user/oauth/token'
    headers = {
        'Authorization': 'Basic ZWdvdi11c2VyLWNsaWVudDo=',
        'Content-Type': 'application/x-www-form-urlencoded'
    }
    data.update({
        'grant_type': 'password',
        'scope': 'read',
        'tenantId': 'pg',
        'userType': 'citizen'
    })
    response = requests.post(url, headers=headers, data=data)
    
    # Check if the request was successful (status code 200)
    if response.status_code == 200:
        # Process the response
        return response.json()['access_token']
    else:
        return None
    

def file_complaint(data):
    headers = {'Content-Type': 'application/json'}
    data = {
    "service": {
        "tenantId": "pg.cityb",
        "serviceCode": "NoStreetlight",
        "description": "",
        "additionalDetail": {},
        "source": "web",
        "address": {
            "city": data["city"],
            "district": data["district"],
            "region": data["region"],
            "state": data["state"],
            "locality": {
                "code": "SUN11",
                "name": data["locality"]
            },
            "geoLocation": {}
        }
    },
    "workflow": {
        "action": "APPLY"
    },
    "RequestInfo": {
        "apiId": "Rainmaker",
        "authToken": data["auth_token"],
        "userInfo": {
            "id": 2079,
            "uuid": "7e2b023a-2f7f-444c-a48e-78d75911387a",
            "userName": data["username"],
            "name": data["name"],
            "mobileNumber": data["username"],
            "emailId": "",
            "locale": None,
            "type": "CITIZEN",
            "roles": [
                {
                    "name": "Citizen",
                    "code": "CITIZEN",
                    "tenantId": "pg"
                }
            ],
            "active": True,
            "tenantId": "pg",
            "permanentCity": "pg.citya"
        },
        "msgId": "1703653602370|en_IN",
        "plainAccessRequest": {}
    }
}
    url = "https://staging.digit.org/pgr-services/v2/request/_create"

    response = requests.post(url, headers=headers, data=json.dumps(data))

    if response.status_code == 200:
        response_data = response.json()
        return response_data
    else:
        print(response.content)
        print(f"Error: {response.status_code}")
        return None
    
def search_complaint(data):
    headers = {'Content-Type': 'application/json'}
    mobile_number = data["username"]
    url = f"https://staging.digit.org/pgr-services/v2/request/_search?tenantId=pg.cityb&mobileNumber={mobile_number}&_=1704443852959"

    data = {
        "RequestInfo":{
            "apiId":"Rainmaker",
            "authToken":data["auth_token"],
            "userInfo":{
                "id":2079,
                "uuid":"7e2b023a-2f7f-444c-a48e-78d75911387a",
                "userName":data["username"],
                "name":data["name"],
                "mobileNumber":data["mobile_number"],
                "emailId":"",
                "locale":None,
                "type":"CITIZEN",
                "roles":[
                    {
                        "name":"Citizen",
                        "code":"CITIZEN",
                        "tenantId":"pg"
                    }
                ],
                "active":True,
                "tenantId":"pg",
                "permanentCity":"pg.cityb"
            },
            "msgId":"1704443852959|en_IN",
            "plainAccessRequest":{}
        }
    }

    response = requests.post(url, headers=headers, data=json.dumps(data))

    if response.status_code == 200:
        response_data = response.json()
        return response_data
    else:
        print(response.content)
        print(f"Error: {response.status_code}")
        return None

    

