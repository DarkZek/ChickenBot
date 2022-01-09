use std::future::Future;
use serenity::client::Context;
use serenity::model::interactions::InteractionResponseType;
use serenity::model::interactions::application_command::{ApplicationCommandInteraction, ApplicationCommandInteractionDataOptionValue, ApplicationCommandOptionType};
use crate::commands::command::{Command, CommandInfoBuilder, CommandInfo, CommandCategory};
use async_trait::async_trait;
use reqwest::Error;
use serde_json::Value;
use serenity::builder::CreateApplicationCommand;
use crate::commands::errors::InvalidParameterType;
use crate::settings::SETTINGS;

/**
 * Created by Marshall Scott on 8/01/22.
 */

pub struct HelpCommand {
    cached_commit_msg: Option<String>
}

#[async_trait]
impl Command for HelpCommand {
    fn info(&self) -> CommandInfo {
        CommandInfoBuilder::new()
            .with_name("Help")
            .with_code("help")
            .with_description("Gets chicken bot help resources")
            .with_category(CommandCategory::Administration)
            .with_usage("/help")
            .build()
    }

    fn parameters(&self, command: &mut CreateApplicationCommand) {
        command.create_option(|option| {
            option.name("option")
                .description("The information you would like to recieve")
                .kind(ApplicationCommandOptionType::String)
                .add_string_choice("Source Code", "source")
                .add_string_choice("Changelog", "changes")
        });
    }

    async fn triggered(&self, ctx: Context, command: &ApplicationCommandInteraction) -> Result<(), Box<dyn std::error::Error>> {

        let data = match command.data.options.get(0) {
            None => "Test".to_string(),
            Some(val) => {
                let value = match val.resolved.as_ref().unwrap() {
                    ApplicationCommandInteractionDataOptionValue::String(val) => val,
                    _ => {
                        println!("Discord API returning non string type for string parameter");
                        return Err(Box::new(InvalidParameterType {}))
                    }
                };

                match value.as_str() {
                    "source" => {
                        SETTINGS.get().unwrap().repo_url.clone()
                    }
                    "changes" => {
                        match &self.cached_commit_msg {
                            Some(val) => val.clone(),
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

        command.create_interaction_response(&ctx.http, |response| {
            response
                .kind(InteractionResponseType::ChannelMessageWithSource)
                .interaction_response_data(|message| message.content(data))
        }).await?;

        Ok(())
    }

    async fn new() -> Self {

        HelpCommand {
            cached_commit_msg: HelpCommand::get_changes().await
        }
    }
}

impl HelpCommand {

    async fn get_changes() -> Option<String> {

        let mut val = reqwest::Client::builder()
            .user_agent(SETTINGS.get()?.user_agent.as_str())
            .build()
            .ok()?
            .get(&SETTINGS.get().as_ref().unwrap().changelog_url)
            .send()
            .await
            .ok()?;

        if val.status() != 200 {
            println!("Fetching git commits returned a '{}'. Error: '{}'", val.status(), val.text().await.ok()?);
            return None;
        }

        Some(val.json::<Vec<Value>>()
            .await
            .ok()?
            .get(0)
            .expect("Git commit url format unexpected")
            .get("commit")
            .expect("Git commit url format unexpected")
            .get("message")
            .expect("Git commit url format unexpected")
            .as_str()
            .unwrap()
            .to_string())
    }
}