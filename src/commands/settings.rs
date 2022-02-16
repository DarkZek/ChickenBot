use std::env;
use crate::commands::command::{Command, CommandInfoBuilder, CommandInfo, CommandCategory, AppContext};
use async_trait::async_trait;
use diesel::{ExpressionMethods, RunQueryDsl};
use lazy_static::lazy_static;
use serenity::builder::CreateApplicationCommand;
use serenity::model::interactions::application_command::{ApplicationCommandInteraction, ApplicationCommandOptionType};
use serenity::model::prelude::InteractionApplicationCommandCallbackDataFlags;
use crate::db::schema::servers::dsl::servers;
use crate::db::schema::servers::{banter, distance_conversion, id};
use crate::error::Error;

/*
 * Created by Marshall Scott on 6/02/22.
 */

lazy_static! {
    static ref INFO: CommandInfo = CommandInfoBuilder::new()
            .with_name("Settings")
            .with_code("settings")
            .with_description("Changes Chicken Bot's configuration settings")
            .with_category(CommandCategory::Administration)
            .build();
}

pub struct SettingsCommand {
    options: Vec<String>
}

#[async_trait]
impl Command for SettingsCommand {
    fn info(&self) -> &CommandInfo { &INFO }

    fn parameters(&self, command: &mut CreateApplicationCommand) {
        command.create_option(|option| {
            option
                .name("set")
                .kind(ApplicationCommandOptionType::String)
                .description("Sets a setting")
                .create_sub_option(|option| {
                    option.create_sub_option(|option| {
                        option
                            .name("banter")
                            .kind(ApplicationCommandOptionType::String)
                            .description("Randomly reacting to other bots messages")
                            .add_string_choice("Enable", "true")
                            .add_string_choice("Disable", "false")
                    })
                })
                .create_sub_option(|option| {
                    option.create_sub_option(|option| {
                        option
                            .name("distance_conversion")
                            .kind(ApplicationCommandOptionType::String)
                            .description("Sending messages converting different measurements")
                            .add_string_choice("Enable", "true")
                            .add_string_choice("Disable", "false")
                    })
                })
        });
        // command.create_option(|option| {
        //     option
        //         .name("banter")
        //         .kind(ApplicationCommandOptionType::String)
        //         .description("Randomly reacting to other bots messages")
        //         .add_string_choice("Enable", "true")
        //         .add_string_choice("Disable", "false")
        // });
        // command.create_option(|option| {
        //     option
        //         .name("distance_conversion")
        //         .kind(ApplicationCommandOptionType::String)
        //         .description("Sending messages converting different measurements")
        //         .add_string_choice("Enable", "true")
        //         .add_string_choice("Disable", "false")
        // });
    }

    async fn triggered(&self, ctx: &AppContext, command: &ApplicationCommandInteraction) -> Result<(), Error> {

        if command.guild_id.is_none() {
            command.create_interaction_response(&ctx.api.http, |interaction| {
                interaction.interaction_response_data(|data| {
                    data.content("This command must be used in a guild").flags(InteractionApplicationCommandCallbackDataFlags::EPHEMERAL)
                })
            }).await?;
            return Ok(());
        }

        let guild_id = *command.guild_id.unwrap().as_u64() as i64;

        let connection = ctx.bot.connection.get()?;

        for option in &command.data.options {
            match option.name.as_str() {
                "banter" => {
                    let value = option.value.as_ref().unwrap().to_string().as_str() == "true";

                    diesel::update(servers)
                        .filter(id.eq(guild_id))
                        .set(banter.eq(value)).execute(&connection)?;
                }
                "distance_conversion" => {
                    let value = option.value.as_ref().unwrap().to_string().as_str() == "true";

                    diesel::update(servers)
                        .filter(id.eq(guild_id))
                        .set(distance_conversion.eq(value)).execute(&connection)?;
                }
                _ => {}
            }
        }

        command.create_interaction_response(&ctx.api.http, |interaction| {
            interaction.interaction_response_data(|data| {
                data.content("Successfully updated guild settings").flags(InteractionApplicationCommandCallbackDataFlags::EPHEMERAL)
            })
        }).await?;

        Ok(())
    }

    async fn new() -> Result<Self, Error> {
        Ok(SettingsCommand {
            options: vec![String::from("banter"), String::from("conversion")]
        })
    }
}

impl SettingsCommand {

    async fn get_response(message: &str) -> Result<String, Error> {

        let api_key = env::var("CLEVERBOT_KEY").unwrap();
        let client = reqwest::Client::new();

        let response = client.post(format!("https://www.cleverbot.com/getreply?key={}&input={}", api_key, message)).send().await?.text().await?;

        println!("{:?}", response);

        Ok(String::new())

    }
}