#![feature(once_cell)]
#![feature(async_closure)]

mod commands;

use std::env;
use crate::commands::command::Command;
use crate::commands::invite::InviteCommand;
use crate::commands::help::HelpCommand;

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
use crate::commands::delete_commands::DeleteCommandsCommand;

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
        let mut commands: Vec<Box<dyn Command>> = vec![
            Box::new(InviteCommand::new()),
            Box::new(HelpCommand::new()),
        ];

        if env::var("DEV").is_ok() {
            commands.push(Box::new(DeleteCommandsCommand::new()));
        }

        commands
    }
}

#[async_trait]
impl EventHandler for ChickenBot {
    async fn ready(&self, ctx: Context, ready: Ready) {
        println!("{} is connected!", ready.user.name);

        self.register_commands(&ctx).await
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

    if let Err(why) = client.start().await {
        println!("Client error: {:?}", why);
    }
}
