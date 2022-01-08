#![feature(once_cell)]
#![feature(async_closure)]

mod commands;

use std::env;
use crate::commands::command::Command;
use crate::commands::invite::InviteCommand;

use serenity::{
    async_trait,
    model::{
        gateway::Ready,
        id::GuildId,
        interactions::{
            application_command::{
                ApplicationCommand,
            },
            Interaction,
            InteractionResponseType,
        },
    },
    prelude::*,
};
use serenity::builder::{CreateApplicationCommand, CreateApplicationCommands};

struct ChickenBot {
    commands: Vec<Box<dyn Command>>
}

impl ChickenBot {
    pub fn new() -> ChickenBot {
        ChickenBot {
            commands: ChickenBot::load_commands()
        }
    }

    pub fn load_commands() -> Vec<Box<dyn Command>> {
        vec![
            Box::new(InviteCommand::new()),
        ]
    }
}

#[async_trait]
impl EventHandler for ChickenBot {
    async fn ready(&self, ctx: Context, ready: Ready) {
        println!("{} is connected!", ready.user.name);

        if env::var("DEV").is_ok() {
            let guild_id = GuildId(
                env::var("GUILD_ID")
                    .expect("Expected GUILD_ID in environment")
                    .parse()
                    .expect("GUILD_ID must be an integer"),
            );

            // Loop through all commands and add them to the guild
            let result = GuildId::set_application_commands(&guild_id, &ctx.http, |commands| {

                for cmd in &self.commands {
                    commands.create_application_command(|command| {
                        command.name(cmd.info().code).description(cmd.info().description)
                    });
                }
                commands
            }).await;

            match result {
                Ok(res) => println!("Successfully registered {} local commands for guild {}", res.len(), env::var("GUILD_ID").unwrap()),
                Err(e) => println!("There was an error registering local commands. {}", e)
            }

        } else {

            // Loop through all commands and add them to the bot
            for cmd in &self.commands {
                let result = ApplicationCommand::create_global_application_command(&ctx.http, |command| {
                    command.name(cmd.info().code).description(cmd.info().description)
                }).await;

                match result {
                    Ok(_) => println!("Successfully registered global command {}", cmd.info().name),
                    Err(e) => println!("There was an error registering global command {}. {}", cmd.info().name, e)
                }
            }

            println!("Successfully registered {} global commands", self.commands.len())
        }
    }

    async fn interaction_create(&self, ctx: Context, interaction: Interaction) {
        self.interaction_created(ctx, interaction).await
    }
}

#[tokio::main]
async fn main() {
    // Configure the client with your Discord bot token in the environment.
    let token = env::var("DISCORD_TOKEN").expect("Expected a token in the environment");

    // The Application Id is usually the Bot User Id.
    let application_id: u64 = env::var("APPLICATION_ID")
        .expect("Expected an application id in the environment")
        .parse()
        .expect("application id is not a valid id");

    // Build our client.
    let mut client = Client::builder(token)
        .event_handler(ChickenBot::new())
        .application_id(application_id)
        .await
        .expect("Error creating client");

    // Finally, start a single shard, and start listening to events.
    //
    // Shards will automatically attempt to reconnect, and will perform
    // exponential backoff until it reconnects.
    if let Err(why) = client.start().await {
        println!("Client error: {:?}", why);
    }
}
