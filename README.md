# Chicken Bot
## Aim
Chicken Bot is a discord bot based off the serenity-rs framework. Chicken Bot aims to provide entertainment your discord server, with features like DONG'S (Do Online Now Guys), Dank Memes and more.

## Running

Use the command `docker-compose up db` to start the database (only needs to be done once per development session), then run `cargo run`

# Setup

1. First make a copy of the example.env to .env and fill it out, then simply follow the running section to get it running.

### Production

For production releases remove DEV=1 from .env and rename prod-docker-compose.override.yml to docker-compose.override.yml
Use the command `docker-compose up bot` to run Chicken Bot.

### Dev

For SQL work install the diesel-rs CLI, to make database changes you should add a new migration that modifies the existing structure, documentation for installation and migration creation [here](https://diesel.rs/guides/getting-started)

## About

This repository is a personal, non-professionally driven project created and maintained by [Marshall Scott](https://marshalldoes.tech/). Because of this ongoing support is not guaranteed. 

Chatbot Functionality Repository: https://huggingface.co/darkzek/chickenbot-jon-snow
https://www.freecodecamp.org/news/discord-ai-chatbot/ Is used for the AI talking aspects

## Contributors

Thanks to all of the contributors for your efforts!

<table>
    <tr>
        <td style="text-align: center;">
            <a href="https://github.com/mirenbhakta"><img width="50px" style="border-radius: 50px;" src="https://avatars.githubusercontent.com/u/30156532?v=3?s=100" alt="Profile Picture"/></a>
            <br /><a href="#translation-robertlluberes" title="Translation">Miren Bhakta</a>
        </td>
        <td style="text-align: center;">
            <a href="https://github.com/Hades595"><img width="50px" style="border-radius: 50px;" src="https://avatars.githubusercontent.com/u/28858526?v=4" alt="Profile Picture"/></a>
            <br /><a href="#translation-robertlluberes" title="Translation">Chirag Mehta</a>
        </td>
    </tr>
</table>


## How can I help?
There are a few ways you can help chicken bot grow!
 - Support us monetarily. Servers to Chicken Bot are expensive!
 - Contribute resources. We're constantly looking for more DONGS, more reactions and to add more personality to Chicken Bot. Consider a merge request if you would like to add any of these!

## Donations
Donations help fund the development of Chicken Bot by helping offset the cost of running the servers needed!

If you would like to send a donation to help fund Chicken Bot please feel free to send your donations to
>daarkzek@protonmail.com

Via paypal.
