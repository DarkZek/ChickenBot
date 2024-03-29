use std::env;

use serenity::client::Context;
use serenity::model::channel::Message;
use serenity::model::id::GuildId;
use serenity::model::interactions::Interaction;
use serenity::model::interactions::application_command::ApplicationCommand;
use tokio::time::Duration;

use crate::ChickenBot;
use crate::commands::command::AppContext;
use crate::error::Error::Other;

pub mod command;
pub mod delete_commands;
pub mod help;
pub mod meme;
pub mod dong;
pub mod summarize;
pub mod banter;
pub mod chat;
pub mod distance_conversion;
pub mod settings;

impl ChickenBot {

    pub async fn message_sent(&self, ctx: &Context, message: Message) {

        let context = AppContext { api: ctx, bot: self };

        for i in &self.commands {
            if let Err(e) = i.message(&context, &message).await {
                e.handle(&context, None, &i.info().name).await
            }
        }

        // Check if we should reply
        if !message.content.starts_with(">") || message.content.starts_with(">:") || message.content.starts_with("> ") || message.content.contains(' ') {
            return;
        }

        let result = message.reply(&ctx.http, "Brawk! Chicken Bot has been updated to to exclusively use Slash Commands. Use /help to start. This warning will be removed in April 2022 when Discord restricts access to messages").await;

        tokio::time::sleep(Duration::from_secs(20)).await;

        if let Ok(msg) = result {
            msg.delete(&ctx.http).await;
        }

    }

    pub async fn interaction_created(&self, ctx: &Context, interaction: Interaction) {

        let context = AppContext { api: ctx, bot: self };

        match &interaction {
            Interaction::Ping(_) => {}
            Interaction::ApplicationCommand(command) => {
                for possible_command in &self.commands {
                    if possible_command.info().code == command.data.name {

                        // Run command
                        match possible_command.triggered(&context, command).await {
                            Ok(_) => {}
                            Err(e) => e.handle(&context, Some(&interaction), &possible_command.info().name).await
                        }

                        return
                    }
                }

                Other(format!("No handler found for command: {:?}", command)).handle( &context, Some(&interaction), &command.data.name.clone()).await;
            }
            Interaction::MessageComponent(message) => {
                for possible_command in &self.commands {

                    // Check if button starts with route to this command
                    if message.data.custom_id.starts_with(&format!("_{}", possible_command.info().code)) {
                        // Run command
                        match possible_command.button_clicked(&context, message).await {
                            Ok(_) => {}
                            Err(e) => e.handle(&context, Some(&interaction), &possible_command.info().name).await
                        }

                        return
                    }
                }

                Other(format!("No command found for message component event: {:?}", message.data.custom_id)).handle( &context, Some(&interaction), &message.data.custom_id.clone()).await;
            }
            Interaction::Autocomplete(autocomplete) => {
                for possible_command in &self.commands {
                    // Check if button starts with route to this command
                    if possible_command.info().code == autocomplete.data.name {
                        // Run command
                        match possible_command.autocomplete(&context, autocomplete).await {
                            Ok(_) => {}
                            Err(e) => e.handle(&context, Some(&interaction), &possible_command.info().name).await
                        }

                        return
                    }
                }

                Other(format!("No command found for autocomplete event: {:?}", autocomplete.data.name)).handle( &context, Some(&interaction), &autocomplete.data.name.clone()).await;
            }
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
                    if cmd.info().code.is_empty() {
                        continue;
                    }

                    commands.create_application_command(|command| {
                        cmd.parameters(command.name(&cmd.info().code).description(&cmd.info().description));
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
                    cmd.parameters(command.name(&cmd.info().code).description(&cmd.info().description));
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