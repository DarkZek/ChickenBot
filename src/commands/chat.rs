use std::env;
use std::time::Duration;
use crate::commands::command::{Command, CommandInfoBuilder, CommandInfo, CommandCategory, AppContext};
use async_trait::async_trait;
use lazy_static::lazy_static;
use rand::Rng;
use serenity::model::channel::Message;
use crate::error::Error;
use crate::settings::SETTINGS;
use serde::{Serialize, Deserialize};

/*
 * Created by Marshall Scott on 6/02/22.
 */

lazy_static! {
    static ref INFO: CommandInfo = CommandInfoBuilder::new()
            .with_name("Chat")
            .with_description("Chats with humans")
            .with_usage("Just chat")
            .with_category(CommandCategory::Fun)
            .build();
}

pub struct ChatCommand {}

#[async_trait]
impl Command for ChatCommand {
    fn info(&self) -> &CommandInfo { &INFO }

    async fn message(&self, ctx: &AppContext, message: &Message) -> Result<(), Error> {

        let user_id = ctx.api.cache.current_user().await.id;

        if message.author.bot || user_id == message.author.id {
            return Ok(())
        }

        if !(message.mentions_user_id(user_id) || (message.referenced_message.is_some() && message.referenced_message.as_ref().unwrap().author.id == user_id )) || message.referenced_message.is_none() {

            let index = rand::thread_rng().gen_range(0, 100);

            // 20% of the time go anyways
            if !(index < 20 && message.content.to_lowercase().contains("chicken")) {
                return Ok(())
            }
        }

        let typing = message.channel_id.start_typing(&ctx.api.http)?;

        let content = message.content.replace(&format!("<@!{}>", user_id), "");

        let response = Self::get_response(content.trim()).await?;

        if let Some(err) = response.error {
            return if err.contains(" is currently loading") {
                let loading_msg = message.reply(&ctx.api.http, "Booting neural engine :arrows_clockwise:").await?;
                tokio::time::sleep(Duration::from_secs_f32(response.estimated_time.unwrap())).await;
                self.message(ctx, message).await?;
                loading_msg.delete(&ctx.api.http).await?;
                return Ok(())
            } else {
                Err(Error::Other(err))
            }
        }

        if let Some(text) = response.generated_text {
            // You're a failure AI
            if text.len() > 100 {
                println!("AI Returned too long value: `{}`", text);
                typing.stop();
                return Ok(())
            }
            message.reply(&ctx.api.http, &text).await?;
        }

        Ok(())
    }

    async fn new() -> Result<Self, Error> {
        Ok(ChatCommand {})
    }
}

impl ChatCommand {

    async fn get_response(message: &str) -> Result<HuggingFaceResponse, Error> {

        let api_key = env::var("HUGGINGFACE_TOKEN").unwrap();
        let client = reqwest::Client::new();

        let response = client.post(&SETTINGS.get().as_ref().unwrap().huggingface_url)
            .header("Authorization", format!("Bearer {}", api_key))
            .json(&HuggingFaceRequest::new(String::from(message))).send().await?.json::<HuggingFaceResponse>().await?;

        return Ok(response);
    }
}

#[derive(Serialize, Deserialize, Debug)]
struct HuggingFaceResponse {
    error: Option<String>,
    generated_text: Option<String>,
    estimated_time: Option<f32>
}

#[derive(Serialize, Deserialize, Debug)]
struct HuggingFaceRequest {
    inputs: HuggingFaceRequestData
}

#[derive(Serialize, Deserialize, Debug)]
struct HuggingFaceRequestData {
    text: String
}

impl HuggingFaceRequest {
    pub fn new(text: String) -> HuggingFaceRequest {
        HuggingFaceRequest {
            inputs: HuggingFaceRequestData {
                text
            }
        }
    }
}