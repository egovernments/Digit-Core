from dotenv import load_dotenv
import redis
import os

load_dotenv(
    dotenv_path="ops/.env"
)

REDIS_HOST = os.getenv("REDIS_HOST")

redis_client = redis.Redis(host=REDIS_HOST, port=6379, db=0)

def get_redis_value(key):
    return redis_client.get(key)

def set_redis(key, value, expire=900):
    redis_client.set(key, value, ex=expire)
    
def delete_redis(key):
    redis_client.delete(key)