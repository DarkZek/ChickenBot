use serenity::client::Context;
use serenity::model::interactions::application_command::{ApplicationCommand, ApplicationCommandInteraction};
use crate::commands::command::{Command, CommandInfoBuilder, CommandInfo, CommandCategory};
use async_trait::async_trait;
use lazy_static::lazy_static;
use serenity::model::channel::Message;
use crate::error::Error;

/**
 * Created by Marshall Scott on 8/01/22.
 */

lazy_static! {
    static ref INFO: CommandInfo = CommandInfoBuilder::new()
            .with_name("Bot Banter")
            .with_description("Brawls with other bots")
            .with_category(CommandCategory::Fun)
            .build();
}

pub struct BanterCommand {}

#[async_trait]
impl Command for BanterCommand {
    fn info(&self) -> &CommandInfo { &INFO }

    async fn message(&self, ctx: &Context, message: &Message) -> Result<(), Error> {
        todo!()
    }

    async fn new() -> Result<Self, Error> {
        Ok(BanterCommand {})
    }
}