use std::env;
use serenity::model::interactions::InteractionResponseType;
use serenity::model::interactions::application_command::{ApplicationCommandInteraction, ApplicationCommandInteractionDataOptionValue, ApplicationCommandOptionType};
use crate::commands::command::{Command, CommandInfoBuilder, CommandInfo, CommandCategory, AppContext};
use async_trait::async_trait;
use lazy_static::lazy_static;
use serenity::builder::CreateApplicationCommand;
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
    cached_commit_msg: Option<Changelog>
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

    async fn triggered(&self, ctx: &AppContext, command: &ApplicationCommandInteraction) -> Result<(), Error> {

        let data = match command.data.options.get(0) {
            None => "Test".to_string(),
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
            cached_commit_msg
        })
    }
}

impl HelpCommand {

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