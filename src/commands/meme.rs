use std::sync::Arc;
use serenity::client::Context;
use serenity::model::interactions::InteractionResponseType;
use serenity::model::interactions::application_command::ApplicationCommandInteraction;
use crate::commands::command::{Command, CommandInfoBuilder, CommandInfo, CommandCategory};
use async_trait::async_trait;
use serde_json::Value;
use serenity::builder::{CreateEmbed, CreateInteractionResponseData};
use serenity::model::prelude::InteractionApplicationCommandCallbackDataFlags;
use serenity::utils::Color;
use tokio::sync::Mutex;
use crate::error::Error;
use crate::error::Error::Other;

/**
 * Created by Marshall Scott on 8/01/22.
 */

pub struct MemesCommand {
    memes: Arc<Mutex<MemeManager>>
}

#[async_trait]
impl Command for MemesCommand {
    fn info(&self) -> CommandInfo {
        CommandInfoBuilder::new()
            .with_name("Meme Me")
            .with_code("meme")
            .with_description("Fetches the hottest new memes straight from the source, iFunny")
            .with_category(CommandCategory::Fun)
            .build()
    }

    async fn triggered(&self, ctx: &Context, command: &ApplicationCommandInteraction) -> Result<(), Error> {

        let mut meme_manager = self.memes.lock().await;

        let meme = meme_manager.pop();

        if let Some(meme) = meme {
            command.create_interaction_response(&ctx.http, |t| {
                t.kind(InteractionResponseType::ChannelMessageWithSource)
                    .interaction_response_data(|message| {
                        meme.to_interaction(message);
                        message
                    })
            }).await?;
        } else {
            command.create_interaction_response(&ctx.http, |t| {
                t.kind(InteractionResponseType::ChannelMessageWithSource)
                    .interaction_response_data(|message| {
                        message.content("Failed to get meme. Try again later").flags(InteractionApplicationCommandCallbackDataFlags::EPHEMERAL)
                    })
            }).await?;
        }

        // Fill buffer with more memes
        meme_manager.topup().await;

        Ok(())
    }

    async fn new() -> Result<Self, Error> {
        Ok(MemesCommand {
            memes: Arc::new(Mutex::new(MemeManager::new().await))
        })
    }
}

pub struct MemeManager {
    cache: Vec<Meme>
}

pub struct Meme {
    upvotes: i64,
    image: String,
    title: String
}

impl MemeManager {
    async fn new() -> MemeManager {
        let mut manager = MemeManager { cache: vec![] };
        manager.topup().await;
        manager
    }

    fn pop(&mut self) -> Option<Meme> {
        self.cache.pop()
    }

    async fn topup(&mut self) {
        if &self.cache.len() < &5 {
            match Meme::fetch().await {
                Ok(mut memes) => self.cache.append(&mut memes),
                Err(e) => println!("Failed to fetch memes. Error: {}", e)
            }
        }
    }
}

impl Meme {

    pub fn to_interaction(&self, message: &mut CreateInteractionResponseData) {
        let mut embed = CreateEmbed::default();
        embed.title(&self.title)
            .color(Color::BLUE)
            .footer(|footer| footer.text(format!("{} upvotes | React :call_me: for more", self.upvotes)))
            .image(&self.image);

        message.add_embed(embed);
    }

    pub async fn fetch() -> Result<Vec<Meme>, Error> {

        let mut memes = Vec::new();

        let meme: Value = reqwest::get("https://www.reddit.com/user/kerdaloo/m/dankmemer/top/.json?sort=top&t=day&limit=20").await?
            .json::<Value>().await?;

        let posts = meme.get("data").ok_or(Other(String::from("No data field")))?
            .get("children").ok_or(Other(String::from("No children field")))?
            .as_array().ok_or(Other(String::from("Children field not an array")))?;

        for post in posts {
            let post = post.get("data").ok_or(Other(String::from("No data in post")))?
                .as_object().ok_or(Other(String::from("Can't convert post data to object")))?;

            // If it doesn't contain the components of a meme, discard
            if !post.contains_key("ups") || !post.contains_key("title") || !post.contains_key("url") {
                continue;
            }

            memes.push(Meme {
                upvotes: post.get("ups").expect("Upvotes not present").as_i64().unwrap_or(-1),
                image: post.get("url").expect("URL not present").as_str().unwrap_or("https://i.imgur.com/PbGjZUh.jpg").to_string(),
                title: post.get("title").expect("Title not present").as_str().unwrap_or("Title not found").to_string(),
            })
        }

        Ok(memes)
    }
}