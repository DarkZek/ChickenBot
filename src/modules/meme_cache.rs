use std::sync::Arc;
use lazy_static::lazy_static;
use serde_json::Value;
use serenity::builder::{CreateEmbed, CreateInteractionResponseData};
use serenity::model::prelude::message_component::ButtonStyle;
use serenity::utils::Color;
use tokio::sync::Mutex;
use crate::error::Error;
use crate::error::Error::Other;

lazy_static! {
    pub static ref MEME_CACHE: Arc<Mutex<MemeManager>> = Arc::new(Mutex::new(MemeManager::new()));
}

pub struct MemeManager {
    cache: Vec<Meme>
}

pub struct Meme {
    upvotes: i64,
    image: String,
    title: String,
    link: String
}

impl MemeManager {
    fn new() -> MemeManager {
        MemeManager { cache: vec![] }
    }

    pub fn pop(&mut self) -> Option<Meme> {
        self.cache.pop()
    }

    pub async fn topup(&mut self) {
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
            .url(format!("https://reddit.com{}", self.link))
            .color(Color::BLUE)
            .footer(|footer| footer.text(format!("{} upvotes | Sourced from Reddit.com", self.upvotes)))
            .image(&self.image);

        message.add_embed(embed).components(|component| {
            component.create_action_row(|actions| {
                actions.create_button(|btn| {
                    btn.custom_id("_meme_new").label("Deploy Reinforcements").style(ButtonStyle::Primary)
                })
            })
        });
    }

    pub async fn fetch() -> Result<Vec<Meme>, Error> {

        let mut memes = Vec::new();

        let meme: Value = reqwest::get("https://www.reddit.com/user/kerdaloo/m/dankmemer/top/.json?sort=top&t=day&limit=20").await?
            .json::<Value>().await?;

        let posts = meme.get("data").and_then(|x|
            x.get("children").and_then(|x|
                x.as_array()));

        if let None = &posts {
            return Err(Other(String::from("Children field not an array")));
        }

        for post in posts.unwrap() {
            let post = post.get("data").ok_or(Other(String::from("No data in post")))?
                .as_object().ok_or(Other(String::from("Can't convert post data to object")))?;

            // If it doesn't contain the components of a meme, discard
            if !post.contains_key("ups") || !post.contains_key("title") || !post.contains_key("url") {
                continue;
            }

            let mut image = post.get("url").map(|x| x.as_str().unwrap());

            if let Some(url) = post.get("media")
                .and_then(|sources| sources.get("reddit_video")
                .and_then(|reddit_video| reddit_video.get("fallback_url"))) {

                image = url.as_str();

                if image.map(|x| x.contains(".gif")).unwrap_or_default() {
                    // Discord only supports gif videos, so don't show this post
                    continue;
                }
            }

            memes.push(Meme {
                upvotes: post.get("ups").expect("Upvotes not present").as_i64().unwrap_or(-1),
                image: image.unwrap_or("https://i.imgur.com/PbGjZUh.jpg").to_string(),
                title: post.get("title").expect("Title not present").as_str().unwrap_or("Title not found").to_string(),
                link: post.get("permalink").expect("permalink not present").as_str().unwrap_or("permalink not found").to_string(),
            })
        }

        Ok(memes)
    }
}