# novabot
Discord bot for the NoVACord Discord server

## Commands
- `/list-reacts` - List reacts of the current meetup thread in chronological order

## Enviroment Variables
- `NOVABOT_TOKEN` - the discord bot token
- `NOVABOT_MEETUP_FORUM` - Snowflake / developer ID of the meetup forum channel.
- `NOVABOT_REACT_EXPIRY` - How long, in milliseconds, a react lasts in the database before it expires. (Default: 7776000000 (90 days))
