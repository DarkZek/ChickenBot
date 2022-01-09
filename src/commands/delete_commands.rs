use serenity::client::Context;
use serenity::model::interactions::InteractionResponseType;
use serenity::model::interactions::application_command::{ApplicationCommand, ApplicationCommandInteraction};
use crate::commands::command::{Command, CommandInfoBuilder, CommandInfo, CommandCategory};
use async_trait::async_trait;

/**
 * Created by Marshall Scott on 8/01/22.
 */

pub struct DeleteCommandsCommand {}

#[async_trait]
impl Command for DeleteCommandsCommand {
    fn info(&self) -> CommandInfo {
        CommandInfoBuilder::new()
            .with_name("Delete Commands")
            .with_code("delete_commands")
            .with_description("Deletes all global commands for the bot")
            .with_category(CommandCategory::Administration)
            .with_allow_bots(true)
            .build()
    }

    async fn triggered(&self, ctx: Context, command: &ApplicationCommandInteraction) {

        let commands = match ApplicationCommand::get_global_application_commands(&ctx.http).await {
            Ok(res) => res,
            Err(e) => panic!("Error: {}", e)
        };

        for command in commands {
            ApplicationCommand::delete_global_application_command(&ctx.http, command.id).await;
        }

        println!("Successfully deleted all global commands for bot");

        command.create_interaction_response(&ctx.http, |t| {
            t.kind(InteractionResponseType::ChannelMessageWithSource)
                .interaction_response_data(|message| message.content("Successfully deleted all global commands for bot"))
        }).await;

    }

    fn new() -> Self {
        DeleteCommandsCommand {}
    }
}