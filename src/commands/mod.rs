use std::env;
use serenity::client::Context;
use serenity::model::id::GuildId;
use serenity::model::interactions::{Interaction};
use serenity::model::interactions::application_command::ApplicationCommand;
use crate::ChickenBot;

pub mod command;
pub mod delete_commands;
pub mod help;

pub mod invite;

impl ChickenBot {

    pub async fn interaction_created(&self, ctx: Context, interaction: Interaction) {

        if let Interaction::ApplicationCommand(command) = &interaction {
            for possible_command in &self.commands {
                if possible_command.info().code == command.data.name {
                    possible_command.triggered(ctx, command).await;
                    return
                }
            }

            println!("No command found for command: {:?}", command)
        }
    }

    pub async fn register_commands(&self, ctx: &Context) {
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

                        // Doesn't work
                        cmd.parameters(command.name(cmd.info().code).description(cmd.info().description));
                        command

                        // Does work
                        //command.name(cmd.info().code).description(cmd.info().description)

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
                    cmd.parameters(command.name(cmd.info().code).description(cmd.info().description));
                    command
                }).await;

                match result {
                    Ok(_) => println!("Successfully registered global command {}", cmd.info().name),
                    Err(e) => println!("There was an error registering global command {}. {}", cmd.info().name, e)
                }
            }

            println!("Successfully registered {} global commands", self.commands.len())
        }
    }
}