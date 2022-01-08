use std::env;
use serenity::client::Context;
use serenity::model::interactions::InteractionResponseType;
use serenity::model::interactions::application_command::ApplicationCommandInteraction;
use crate::commands::command::{Command, CommandInfoBuilder, CommandInfo, CommandCategory};
use async_trait::async_trait;

/**
 * Created by Marshall Scott on 8/01/22.
 */

pub struct InviteCommand {}

#[async_trait]
impl Command for InviteCommand {
    fn info(&self) -> CommandInfo {
        CommandInfoBuilder::new()
            .with_name("Invite")
            .with_code("invite")
            .with_description("Gets an invite link for chicken bot")
            .with_category(CommandCategory::Administration)
            .with_usage("/invite")
            .build()
    }

    async fn triggered(&self, ctx: Context, command: &ApplicationCommandInteraction) {

        let invite_link = format!("https://discordapp.com/oauth2/authorize?client_id={}&scope=applications.commands%20bot&permissions=1341643969", env::var("APPLICATION_ID").unwrap());

        if let Err(why) = command
            .create_interaction_response(&ctx.http, |response| {
                response
                    .kind(InteractionResponseType::ChannelMessageWithSource)
                    .interaction_response_data(|message| message.content(invite_link))
            })
            .await
        {
            println!("Cannot respond to slash command: {}", why);
        }
    }

    fn new() -> Self {
        InviteCommand {}
    }
}