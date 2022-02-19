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
use crate::get_server;

/*
 * Created by Marshall Scott on 6/02/22.
 */

lazy_static! {
    static ref INFO: CommandInfo = CommandInfoBuilder::new()
            .with_name("Settings")
            .with_code("settings")
            .with_description("Changes Chicken Bot's configuration settings")
            .with_usage("/settings set banter: Enable")
            .with_category(CommandCategory::Administration)
            .build();
}

pub struct SettingsCommand {

}

#[async_trait]
impl Command for SettingsCommand {
    fn info(&self) -> &CommandInfo { &INFO }

    fn parameters(&self, command: &mut CreateApplicationCommand) {
        command.create_option(|option| {
            option
                .name("set")
                .kind(ApplicationCommandOptionType::SubCommand)
                .description("Sets a setting")
                .create_sub_option(|option| {
                    option
                        .name("banter")
                        .kind(ApplicationCommandOptionType::String)
                        .description("Randomly reacting to other bots messages")
                        .add_string_choice("Enable", "true")
                        .add_string_choice("Disable", "false")
                })
                .create_sub_option(|option| {
                    option
                        .name("distance_conversion")
                        .kind(ApplicationCommandOptionType::String)
                        .description("Sending messages converting different measurements")
                        .add_string_choice("Enable", "true")
                        .add_string_choice("Disable", "false")
                })
        });
        command.create_option(|option| {
            option
                .name("get")
                .kind(ApplicationCommandOptionType::SubCommand)
                .description("Gets a setting")
                .create_sub_option(|option| {
                    option
                        .name("option")
                        .description("The name of the setting that should be retrieved")
                        .required(true)
                        .kind(ApplicationCommandOptionType::String)
                        .add_string_choice("Banter", "banter")
                        .add_string_choice("Distance Conversion", "distance_conversion")
                })
        });
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

        if !command.member.as_ref().unwrap().permissions(&ctx.api).await?.administrator() {
            command.create_interaction_response(&ctx.api.http, |interaction| {
                interaction.interaction_response_data(|data| {
                    data.content("This command can only be ran by administrators").flags(InteractionApplicationCommandCallbackDataFlags::EPHEMERAL)
                })
            }).await?;
        }

        let guild_id = *command.guild_id.unwrap().as_u64();

        let mut connection = ctx.bot.connection.get()?;

        let subcommand = command.data.options.get(0).unwrap();

        let mut changed_values = Vec::new();

        if subcommand.name == "set" {
            for option in &subcommand.options {
                match option.name.as_str() {
                    "banter" => {
                        let value = option.value.as_ref().unwrap().as_str().unwrap_or("false").eq("true");

                        diesel::update(servers)
                            .filter(id.eq(guild_id as i64))
                            .set(banter.eq(value)).execute(&connection)?;

                        changed_values.push("banter");
                    }
                    "distance_conversion" => {
                        let value = option.value.as_ref().unwrap().to_string().as_str() == "true";

                        diesel::update(servers)
                            .filter(id.eq(guild_id as i64))
                            .set(distance_conversion.eq(value)).execute(&connection)?;

                        changed_values.push("distance_conversion");
                    }
                    _ => {}
                }
            }

            command.create_interaction_response(&ctx.api.http, |interaction| {
                interaction.interaction_response_data(|data| {
                    data.content(format!("Successfully updated guild settings {:?}", changed_values)).flags(InteractionApplicationCommandCallbackDataFlags::EPHEMERAL)
                })
            }).await?;

        } else {

            let mut val;

            let server = get_server(guild_id, &mut *connection)?;

            let name = subcommand.options.get(0).unwrap().value.as_ref().unwrap().as_str().unwrap();

            match name {
                "banter" => {
                    val = server.banter;
                }
                "distance_conversion" => {
                    val = server.distance_conversion;
                }
                _ => panic!("Unexpected setting variable fetched {:?}", name)
            }

            command.create_interaction_response(&ctx.api.http, |interaction| {
                interaction.interaction_response_data(|data| {
                    data.content(format!("{} = {}", name, val)).flags(InteractionApplicationCommandCallbackDataFlags::EPHEMERAL)
                })
            }).await?;

        }

        Ok(())
    }

    async fn new() -> Result<Self, Error> {
        Ok(SettingsCommand {})
    }
}