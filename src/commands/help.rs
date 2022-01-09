use std::env;
use serenity::client::Context;
use serenity::model::interactions::InteractionResponseType;
use serenity::model::interactions::application_command::{ApplicationCommandInteraction, ApplicationCommandOptionType};
use crate::commands::command::{Command, CommandInfoBuilder, CommandInfo, CommandCategory};
use async_trait::async_trait;
use serenity::builder::CreateApplicationCommand;

/**
 * Created by Marshall Scott on 8/01/22.
 */

pub struct HelpCommand {}

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
                .add_string_choice("Chicken Bot Source Code", "source")
        });
    }

    async fn triggered(&self, ctx: Context, command: &ApplicationCommandInteraction) {
        if let Err(why) = command
            .create_interaction_response(&ctx.http, |response| {
                response
                    .kind(InteractionResponseType::ChannelMessageWithSource)
                    .interaction_response_data(|message| message.content("Not implemented"))
            })
            .await
        {
            println!("Cannot respond to slash command: {}", why);
        }
    }

    fn new() -> Self {
        HelpCommand {}
    }
}