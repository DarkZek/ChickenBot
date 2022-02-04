use serenity::client::Context;
use serenity::model::interactions::InteractionResponseType;
use serenity::model::interactions::application_command::{ApplicationCommand, ApplicationCommandInteraction};
use crate::commands::command::{Command, CommandInfoBuilder, CommandInfo, CommandCategory};
use async_trait::async_trait;
use lazy_static::lazy_static;
use crate::error::Error;

/**
 * Created by Marshall Scott on 8/01/22.
 */

lazy_static! {
    static ref INFO: CommandInfo = CommandInfoBuilder::new()
            .with_name("Delete Commands")
            .with_code("delete_commands")
            .with_description("Deletes all global commands for the bot")
            .with_category(CommandCategory::Administration)
            .with_allow_bots(true)
            .build();
}

pub struct DeleteCommandsCommand {}

#[async_trait]
impl Command for DeleteCommandsCommand {
    fn info(&self) -> &CommandInfo { &INFO }

    async fn triggered(&self, ctx: &Context, command: &ApplicationCommandInteraction) -> Result<(), Error> {

        let commands = match ApplicationCommand::get_global_application_commands(&ctx.http).await {
            Ok(res) => res,
            Err(e) => panic!("Error: {}", e)
        };

        for command in commands {
            if let Err(e) = ApplicationCommand::delete_global_application_command(&ctx.http, command.id).await {
                println!("Failed to delete global application command {}. {}", command.name, e)
            }
        }

        println!("Successfully deleted all global commands for bot");

        command.create_interaction_response(&ctx.http, |t| {
            t.kind(InteractionResponseType::ChannelMessageWithSource)
                .interaction_response_data(|message| message.content("Successfully deleted all global commands for bot"))
        }).await?;

        Ok(())
    }

    async fn new() -> Result<Self, Error> {
        Ok(DeleteCommandsCommand {})
    }
}