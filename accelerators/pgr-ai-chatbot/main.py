import asyncio
import logging
from core.ai import chat, audio_chat
from telegram import Update
from telegram.ext import (
    ApplicationBuilder,
    ContextTypes, 
    MessageHandler,
    CommandHandler, 
    filters,
)
from utils.openai_utils import (
    get_duration_pydub, 
    get_random_wait_messages
)
import os
import dotenv
import tempfile
import time

dotenv.load_dotenv("ops/.env")

token = os.getenv('TELEGRAM_BOT_TOKEN')

logging.basicConfig(
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    level=logging.INFO
)

async def start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    chat_id = update.effective_chat.id
    await context.bot.send_message(chat_id=chat_id, text="Hello I am Mr. Nags, start raising a complaint with hello I have a complaint")

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
    response_handler = MessageHandler(filters.TEXT & (~filters.COMMAND), respond)
    audio_handler = MessageHandler(filters.VOICE & (~filters.COMMAND), respond_audio)
    application.add_handler(response_handler)
    application.add_handler(start_handler)
    application.add_handler(audio_handler)
    application.run_polling()