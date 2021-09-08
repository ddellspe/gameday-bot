# Gameday Tools
Gameday tools is a site/discord bot set up to assist with discord servers that want to have some gameday experiences in voice or text channels.
Information about the tools and even how to run your own bot can be found below.

## GamedayBot
To Run the gameday bot, you can run the docker image deployed to this repository with the following command:
```shell
docker run --rm -d --name gameday-bot -e BOT_TOKEN=<YOUR BOT'S TOKEN> ghcr.io/ddellspe/gameday-bot 
```
The Gameday Bot is the largest piece of the puzzle in supporting gameday atmosphere items on discord.
Currently, the Gameday bot has support for starting (`!start`) and stopping (`!end`) the gameday experience.
In addition, you can play certain YouTube video-based audio clips for touchdowns (`!touchdown`), field goals (`!fg`), and winning (`!victory`).
The audio channel as well as the text channel to have commands take place are required, as well as a role called "gameday manager" which is required to perform any action.
Currently, configuration is available in the [gameday_config.json file](src/main/resources/configs/gameday_config.json).