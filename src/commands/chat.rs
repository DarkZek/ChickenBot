use std::env;
use crate::commands::command::{Command, CommandInfoBuilder, CommandInfo, CommandCategory, AppContext};
use async_trait::async_trait;
use lazy_static::lazy_static;
use serenity::model::channel::Message;
use crate::error::Error;

/*
 * Created by Marshall Scott on 8/01/22.
 */

lazy_static! {
    static ref INFO: CommandInfo = CommandInfoBuilder::new()
            .with_name("Chat")
            .with_description("Chats with humans")
            .with_category(CommandCategory::Fun)
            .build();
}

pub struct ChatCommand {}

#[async_trait]
impl Command for ChatCommand {
    fn info(&self) -> &CommandInfo { &INFO }

    async fn message(&self, ctx: &AppContext, message: &Message) -> Result<(), Error> {

        let user_id = ctx.api.cache.current_user().await.id;

        // Don't respond if theres an @everyone or they're not a bot and it's in a guild
        if (!message.mentions_user_id(user_id) && (message.referenced_message.is_some() && message.referenced_message.as_ref().unwrap().author.id != user_id ))
            || message.author.bot || user_id == message.author.id  {
            return Ok(())
        }

        message.channel_id.start_typing(&ctx.api.http)?;

        let response = Self::get_response(&message.content).await?;

        message.reply(&ctx.api.http, "Yeet").await?;

        Ok(())
    }

    async fn new() -> Result<Self, Error> {
        Ok(ChatCommand {})
    }
}

impl ChatCommand {

    async fn get_response(message: &str) -> Result<String, Error> {

        let api_key = env::var("CLEVERBOT_KEY").unwrap();
        let client = reqwest::Client::new();

        let response = client.post(format!("https://www.cleverbot.com/getreply?key={}&input={}", api_key, message)).send().await?.text().await?;

        println!("{:?}", response);

        Ok(String::new())

    }
}