use std::env;
use serenity::model::interactions::InteractionResponseType;
use serenity::model::interactions::application_command::{ApplicationCommandInteraction, ApplicationCommandInteractionDataOptionValue, ApplicationCommandOptionType};
use crate::commands::command::{Command, CommandInfoBuilder, CommandInfo, CommandCategory, AppContext};
use async_trait::async_trait;
use lazy_static::lazy_static;
use serenity::builder::{CreateApplicationCommand, CreateComponents, CreateEmbed};
use serenity::model::interactions::message_component::{ButtonStyle, MessageComponentInteraction};
use serenity::model::prelude::InteractionApplicationCommandCallbackDataFlags;
use crate::commands::help::changelog::Changelog;
use crate::error::Error;
use crate::settings::SETTINGS;

pub mod changelog;

/*
 * Created by Marshall Scott on 8/01/22.
 */

lazy_static! {
    static ref INFO: CommandInfo = CommandInfoBuilder::new()
            .with_name("Help")
            .with_code("help")
            .with_description("Gets chicken bot help resources")
            .with_category(CommandCategory::Administration)
            .with_usage("/help")
            .build();
}

pub struct HelpCommand {
    cached_commit_msg: Option<Changelog>,
    items_per_page: i32
}

#[async_trait]
impl Command for HelpCommand {
    fn info(&self) -> &CommandInfo { &INFO }

    fn parameters(&self, command: &mut CreateApplicationCommand) {
        command.create_option(|option| {
            option.name("option")
                .description("The information you would like to recieve")
                .kind(ApplicationCommandOptionType::String)
                .add_string_choice("Source Code", "source")
                .add_string_choice("Changelog", "changes")
        });
    }

    async fn button_clicked(&self, ctx: &AppContext, message: &MessageComponentInteraction) -> Result<(), Error> {

        let mut page = 0;

        if let Some(val) = message.data.custom_id.split("_").collect::<Vec<&str>>().get(3) {
            if let Ok(val) = val.parse::<usize>() {
                page = val;
            } else {
                // Invalid, just silently fail since this will only happen with old dialogs
                return Ok(())
            }
        } else {
            // Invalid, just silently fail since this will only happen with old dialogs
            return Ok(())
        }

        let start = page * self.items_per_page as usize;
        let end = ((page + 1) * self.items_per_page as usize).clamp(0, ctx.bot.commands.len() as usize);

        message.create_interaction_response(&ctx.api.http, |response| {
            response
                .kind(InteractionResponseType::UpdateMessage)
                .interaction_response_data(|message| {
                    message
                        .add_embed(Self::list_commands_embed(ctx, start, end))
                        .set_components(Self::list_commands_buttons(ctx, start, end, page))
                        .flags(InteractionApplicationCommandCallbackDataFlags::EPHEMERAL)
                })
        }).await?;

        Ok(())
    }

    async fn triggered(&self, ctx: &AppContext, command: &ApplicationCommandInteraction) -> Result<(), Error> {

        let data = match command.data.options.get(0) {
            None => {

                let page = 0;

                let start = page * self.items_per_page as usize;
                let end = ((page + 1) * self.items_per_page as usize).clamp(0, ctx.bot.commands.len() as usize);

                command.create_interaction_response(&ctx.api.http, |response| {
                    response
                        .kind(InteractionResponseType::ChannelMessageWithSource)
                        .interaction_response_data(|message| {
                            message
                                .add_embed(Self::list_commands_embed(ctx, start, end))
                                .set_components(Self::list_commands_buttons(ctx, start, end, page))
                                .flags(InteractionApplicationCommandCallbackDataFlags::EPHEMERAL)
                        })
                }).await?;

                return Ok(());
            },
            Some(val) => {
                let value = match val.resolved.as_ref().unwrap() {
                    ApplicationCommandInteractionDataOptionValue::String(val) => val,
                    _ => {
                        println!("Discord API returning non string type for string parameter");
                        return Err(Error::InvalidParameterType)?
                    }
                };

                match value.as_str() {
                    "source" => {
                        SETTINGS.get().unwrap().repo_url.clone()
                    }
                    "invite" => {
                        format!("https://discordapp.com/oauth2/authorize?client_id={}&scope=applications.commands%20bot&permissions=1341643969", env::var("APPLICATION_ID").unwrap())
                    }
                    "changes" => {
                        match &self.cached_commit_msg {
                            Some(val) => val.to_string(),
                            None => {
                                "Commit log not accessible currently. Please try again later".to_string()
                            }
                        }
                    }
                    _ => {
                        println!("Info command ran with invalid parameter {:?}", command);
                        "Invalid parameter".to_string()
                    }
                }
            }
        };

        command.create_interaction_response(&ctx.api.http, |response| {
            response
                .kind(InteractionResponseType::ChannelMessageWithSource)
                .interaction_response_data(|message| message.content(data).flags(InteractionApplicationCommandCallbackDataFlags::EPHEMERAL))
        }).await?;

        Ok(())
    }

    async fn new() -> Result<Self, Error> {

        let cached_commit_msg = match HelpCommand::get_changes().await {
            Ok(val) => Some(val),
            Err(_) => {
                println!();
                None
            }
        };

        Ok(HelpCommand {
            cached_commit_msg,
            items_per_page: 8
        })
    }
}

impl HelpCommand {

    fn list_commands_embed(ctx: &AppContext, start: usize, end: usize) -> CreateEmbed {
        let mut embed = CreateEmbed::default();

        for cmd in &ctx.bot.commands[start..end] {
            embed.field(format!("{} ({})", &cmd.info().name, &cmd.info().usage), &cmd.info().description, false);
        }

        embed
    }

    fn list_commands_buttons(ctx: &AppContext, start: usize, end: usize, page: usize) -> CreateComponents {
        let mut components = CreateComponents::default();

        components.create_action_row(|actions| {

            if start != 0 {
                actions.create_button(|btn| {
                    btn.custom_id(format!("_help_page_{}", page - 1)).label("Previous").style(ButtonStyle::Primary)
                });
            }

            if end != ctx.bot.commands.len() {
                actions.create_button(|btn| {
                    btn.custom_id(format!("_help_page_{}", page + 1)).label("Next").style(ButtonStyle::Primary)
                });
            }

            actions
        });

        components
    }

    async fn get_changes() -> Result<Changelog, Error> {

        let val = reqwest::Client::builder()
            .user_agent(&SETTINGS.get().unwrap().user_agent)
            .build()?
            .get(&SETTINGS.get().as_ref().unwrap().changelog_url)
            .send()
            .await?;

        if val.status() != 200 {
            return Err(Error::ErrorHttpCode(val.status(), val.text().await.ok()));
        }

        Changelog::new(val).await
    }
}