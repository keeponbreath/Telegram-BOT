# Telegram BOT [My tasks]
___
### This bot will remind you of the tasks you planned.
___
Just create the bot in Telegram and put the following commands:

| Command name | Description                   |
|--------------|-------------------------------|
| start        | To start the bot              |
| tasks        | To show all uncompleted tasks |
| alltasks     | To show all tasks             |
| create       | To create a new task          |

Then generate an API Token and put in a bot.properties file in [resources directory](src/main/resources). You also need to put a bot name into this file.</br>
bot.properties file should look like this:</br>
_bot.name=YOUR_BOT_NAME_HERE</br>
bot.token=YOUR_API_TOKEN_HERE_</br></br>
Start the bot by [docker-compose file](docker-compose.yml). In this project I've decided to use Redis as data store because it is pretty fast and durable.</br></br>
Follow the bot instructions and enjoy!
