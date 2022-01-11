use std::env;

use serenity::client::Context;
use serenity::model::id::GuildId;
use serenity::model::interactions::{Interaction, InteractionResponseType};
use serenity::model::interactions::application_command::ApplicationCommand;

use crate::ChickenBot;
use crate::settings::SETTINGS;

pub mod command;
pub mod delete_commands;
pub mod help;
pub mod meme;

impl ChickenBot {

    pub async fn interaction_created(&self, ctx: Context, interaction: Interaction) {

        if let Interaction::ApplicationCommand(command) = &interaction {
            for possible_command in &self.commands {
                if possible_command.info().code == command.data.name {
                    match possible_command.triggered(&ctx, command).await {
                        Ok(_) => {}
                        Err(e) => {
                            println!("Command '{}' threw an error: {}", possible_command.info().name, e);

                            command.create_interaction_response(&ctx.http, |t| {
                                t.kind(InteractionResponseType::ChannelMessageWithSource)
                                    .interaction_response_data(|message| message.content("There was an error processing your request, it has been sent to the bot maintenance team."))
                            }).await.is_err().then(|| println!("Failed to notify user of error"));

                            if let Ok(user) = ctx.http.get_user(130173614702985216).await {
                                if let Err(e) = user.direct_message(&ctx.http, |message| {
                                    println!("Command '{}' threw an error: {}", possible_command.info().name, e);
                                    message.content(format!("Command '{}' threw an error: {}", possible_command.info().name, e))

                                }).await {
                                    println!("Error: Could not message user '{}' to send error message to. Error: {}", SETTINGS.get().as_ref().unwrap().user_manager, e)
                                }
                            } else {
                                println!("Error: Could not look up user '{}' to send error message to. Error: {}", SETTINGS.get().as_ref().unwrap().user_manager, e)
                            }
                        }
                    }

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
            let result = guild_id.set_application_commands(&ctx.http, |commands| {

                for cmd in &self.commands {
                    commands.create_application_command(|command| {
                        cmd.parameters(command.name(cmd.info().code).description(cmd.info().description));
                        command

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