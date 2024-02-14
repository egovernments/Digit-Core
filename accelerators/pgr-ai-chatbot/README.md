# Mr Nags

Mr Nags is a complaint filing chatbot using egov digit platform.


## Installation

1. Create a file `.env` inside `ops` directory and add the following lines to it:
    ```
    OPENAI_API_KEY=<OPENAI_API_KEY>
    TELEGRAM_BOT_TOKEN=<TELEGRAM BOT TOKEN>
    REDIS_HOST=redis (localhost if running locally without docker)
    USERNAME=<digit-username>
    PASSWORD=<digit-password>
    MODEL_NAME=<model-name eg: gpt-3.5-turbo>
    ```
2. For normal running

    Run the following commands:
    ```
    pip install -r requirements.txt
    ```
    Once the installaton is complete, run the following command to start the bot:
    ```
    python main.py
    ```

3. For docker running
    
    Run the following command to start the bot:
    ```
    docker-compose up
    ```
4. Open Telegram and search for `@YourBotName` and start chatting with the bot.



## Usage

1. To start a conversation with the bot, start having a conversation by saying hey, hello or I have a complaint
2. The bot will ask you to describe your complaint
3. Once you have described your complaint, the bot will file the complaint and give you your complaint number.
4. Todo: Map Location details from chat to the proper parameters in digit API call(s). 




