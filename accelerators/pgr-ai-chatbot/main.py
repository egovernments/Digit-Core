import os
import base64
import tempfile
import time
from typing import Union

import asyncio
import logging
import dotenv

from telegram import Update, InlineKeyboardButton, InlineKeyboardMarkup
from telegram.ext import (
    ApplicationBuilder,
    ContextTypes,
    MessageHandler,
    CommandHandler,
    filters,
    CallbackContext,
    CallbackQueryHandler,
)

from core.ai import chat, audio_chat, bhashini_text_chat, bhashini_audio_chat
from utils.redis_utils import set_redis
from utils.openai_utils import get_duration_pydub, get_random_wait_messages

dotenv.load_dotenv("ops/.env")

token = os.getenv('TELEGRAM_BOT_TOKEN')

logging.basicConfig(
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    level=logging.INFO
)
class BotInitializer:
    _instance = None
    run_once = False
    
    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(BotInitializer, cls).__new__(cls)
            cls.run_once = True
        return cls._instance

async def start(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    BotInitializer()  # To initialize only once
    
    await context.bot.send_message(chat_id=update.effective_chat.id, text="Hello I am Mr. Nags, start raising a complaint with me")
    await relay_handler(update, context)

async def relay_handler(update: Update, context: CallbackContext):
    await language_handler(update, context)
    
async def language_handler(update: Update, context: CallbackContext):
    # Handle user's language selection
    keyboard = [
        [InlineKeyboardButton("English", callback_data='1')],
        [InlineKeyboardButton("हिंदी", callback_data='2')],
        [InlineKeyboardButton("ਪੰਜਾਬੀ", callback_data='3')],
    ]
    reply_markup = InlineKeyboardMarkup(keyboard)

    await context.bot.send_message(chat_id=update.effective_chat.id, text="Choose a Language:", reply_markup=reply_markup)

async def preferred_language_callback(update: Update, context: CallbackContext):
    
    callback_query = update.callback_query
    # await query.amswer()
    print("Callback data:", callback_query.data)
    languages = {"1": "en", "2": "hi", "3": "pa"}
    try:
        preferred_language = callback_query.data
        lang = languages.get(preferred_language)
        # LanguagePreference.set_language(lang)
        context.user_data['lang'] = lang
        print(lang)
    except (AttributeError, ValueError):
        lang = 'en'  # Set a default language
        await context.bot.send_message(chat_id=update.effective_chat.id, text="Error getting language! Setting default to English.")
    
    text_message = ""
    if lang == "en":
        text_message = "You have chosen English. \nPlease give your complaint now"
    elif lang == "hi":
        text_message = "आपने हिंदी चुनी है. \nकृपया अभी अपनी शिकायत दर्ज करें।"
    elif lang == "pa":
        text_message = "ਤੁਸੀਂ ਪੰਜਾਬੀ ਨੂੰ ਚੁਣਿਆ ਹੈ। \ਕਿਰਪਾ ਕਰਕੇ ਹੁਣੇ ਆਪਣੀ ਸ਼ਿਕਾਇਤ ਦਿਓ"
        
    set_redis('lang', lang)
    print(lang) # lang = LanguagePreference.get_language()
    
    await context.bot.send_message(chat_id=update.effective_chat.id, text=text_message)
    # print('ok1')

async def response_handler(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    await query_handler(update, context)

async def query_handler(update: Update, context: CallbackContext):
    lang = context.user_data.get('lang')

    if update.message.text:
        text = update.message.text
        await talk(update, context, text)
        print("ok1")
    elif update.message.voice:
        voice = await context.bot.get_file(update.message.voice.file_id)
        await talk__audio(update, context, voice)
        print("ok1")

    text_message = ""
    if lang == "en":
        text_message = "Thank you for being on the complaint process with me."
    elif lang == "hi":
        text_message = "मेरे साथ शिकायत प्रक्रिया में बने रहने के लिए धन्यवाद।"
    elif lang == "pa":
        text_message = "ਮੇਰੇ ਨਾਲ ਸ਼ਿਕਾਇਤ ਪ੍ਰਕਿਰਿਆ 'ਤੇ ਹੋਣ ਲਈ ਤੁਹਾਡਾ ਧੰਨਵਾਦ।"

    await context.bot.send_message(chat_id=update.effective_chat.id, text=text_message)

async def talk(update: Update, context: ContextTypes.DEFAULT_TYPE, text: str):
    
    chat_id = update.effective_chat.id
    # text = update.message.text
    lang = context.user_data.get('lang')
    # update_task = asyncio.create_task(progress_bar(context, chat_id, start_time))
    print('ok')
    response, history = bhashini_text_chat(chat_id,text, lang)
    await context.bot.send_message(chat_id=chat_id, text=response)

async def talk__audio(update: Update, context: ContextTypes.DEFAULT_TYPE, voice):    
    lang = context.user_data.get('lang')
    # getting audio file
    audio_file = voice
    print('ok_voice')
    # audio_file = await context.bot.get_file(update.message.voice.file_id)

    with tempfile.NamedTemporaryFile(suffix='.wav', delete=True) as temp_audio_file:
        await audio_file.download_to_drive(custom_path=temp_audio_file.name)
        chat_id = update.effective_chat.id
        print('ok2')

        # Convert audio file to base64 encoded string
        with open(temp_audio_file.name, "rb") as file:
            audio_data = file.read()
            audio_base64 = base64.b64encode(audio_data).decode('utf-8')

        response, history = bhashini_audio_chat(chat_id, audio_file=audio_base64, lang=lang)
        print('ok_')
        await context.bot.send_message(chat_id=update.effective_chat.id, text=response)

# for getting location,we can use this 
'''async def location(update: Update, context: ContextTypes.DEFAULT_TYPE) -> int:
    """Stores the location and asks for some info about the user."""
    user = update.message.from_user
    user_location = update.message.location
    logger.info(
        "Location of %s: %f / %f", user.first_name, user_location.latitude, user_location.longitude
    )
    await update.message.reply_text(
        "Since you're filing the complaint form your location, we're recording it."
    )'''

# for progress bar while waiting
'''
async def progress_bar(context, chat_id, start_time, update_interval=15, max_duration=90):
    while True:
        await asyncio.sleep(update_interval)
        wait_time = time.time() - start_time
        if wait_time > max_duration:
            break
        await context.bot.send_message(caht_id=chat_id, text="Thank you for your patience. We're wokring on it.")
'''
async def respond(update: Update, context: ContextTypes.DEFAULT_TYPE):
    start_time = time.time()
    text = update.message.text
    chat_id = update.effective_chat.id
    wait_message = get_random_wait_messages(
        not_always=True
    )
    if wait_message:
        await context.bot.send_message(chat_id=chat_id, text=wait_message)
    response, history = chat(chat_id, text)
    end_time = time.time()
    print(f"history status is {history.get('status')}")
    print(f"Time taken: {end_time - start_time}")
    await context.bot.send_message(chat_id=chat_id, text=response)



async def respond_audio(update: Update, context: ContextTypes.DEFAULT_TYPE):
    audio_file = await context.bot.get_file(update.message.voice.file_id)

    # Use a temporary file
    with tempfile.NamedTemporaryFile(suffix='.wav', delete=True) as temp_audio_file:
        await audio_file.download_to_drive(custom_path=temp_audio_file.name)
        chat_id = update.effective_chat.id
        wait_message = get_random_wait_messages(
            not_always=True
        )
        if wait_message:
            await context.bot.send_message(chat_id=chat_id, text=wait_message)
        response_audio, history = audio_chat(
            chat_id, audio_file=open(temp_audio_file.name, "rb")
        )
        response_audio.stream_to_file(temp_audio_file.name)
        duration = get_duration_pydub(temp_audio_file.name)
        await context.bot.send_audio(
            chat_id=chat_id, 
            audio=open(temp_audio_file.name, "rb"), 
            duration=duration, 
            filename="response.wav",
            performer="Mr. Nags",
        )

if __name__ == '__main__':
    application = ApplicationBuilder().token(token).read_timeout(30).write_timeout(30).build()
    start_handler = CommandHandler('start', start)
    # choose language -> then use Bhashini
    language_handler_ = CommandHandler('set_language', language_handler)
    chosen_language = CallbackQueryHandler(preferred_language_callback)
    # response_handler = MessageHandler(filters.TEXT & (~filters.COMMAND), talk)
    # audio_handler = MessageHandler(filters.VOICE & (~filters.COMMAND), talk__audio)
    application.add_handler(start_handler)
    application.add_handler(language_handler_)
    application.add_handler(chosen_language)
    application.add_handler(MessageHandler((filters.TEXT & (~filters.COMMAND)) | (filters.VOICE & (~filters.COMMAND)), response_handler))
    application.run_polling()
    

