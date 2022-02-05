use serenity::model::interactions::application_command::ApplicationCommandInteraction;
use crate::commands::command::{Command, CommandInfoBuilder, CommandInfo, CommandCategory, AppContext};
use async_trait::async_trait;
use lazy_static::lazy_static;
use rand::Rng;
use tokio::io::AsyncReadExt;
use crate::error::Error;
use serde::Deserialize;

/*
 * Created by Marshall Scott on 8/01/22.
 */

lazy_static! {
    static ref INFO: CommandInfo = CommandInfoBuilder::new()
            .with_name("Do Online Now Guys")
            .with_code("dong")
            .with_description("Fetches a random website to cure boredom")
            .with_category(CommandCategory::Fun)
            .build();
}

pub struct DongCommand {
    dongs: Vec<Dong>
}

#[async_trait]
impl Command for DongCommand {
    fn info(&self) -> &CommandInfo {
        &INFO
    }

    async fn triggered(&self, ctx: &AppContext, command: &ApplicationCommandInteraction) -> Result<(), Error> {

        if self.dongs.len() == 0 {
            println!("Error: Dongs list empty");
            return Ok(())
        }

        let dong = &self.dongs[rand::thread_rng().gen_range(0, self.dongs.len())];

        command.create_interaction_response(&ctx.api.http, |message| {
            message.interaction_response_data(|data| {
                data.content(format!( "```{}```<{}>", dong.description, dong.url))
            })
        }).await?;

        Ok(())
    }

    async fn new() -> Result<Self, Error> {

        let mut dongs = Vec::new();

        match tokio::fs::File::open("DONG.json").await {
            Ok(mut val) => {
                let mut file = String::new();
                if let Err(e) = val.read_to_string(&mut file).await {
                    println!("Failed reading DONG.json {}", e);
                }
                dongs = serde_json::from_str::<Vec<Dong>>(&file)?;
            }
            Err(e) => println!("Failed to open DONG.json. Error: {}", e)
        }

        Ok(DongCommand { dongs })
    }
}

#[derive(Deserialize)]
struct Dong {
    url: String,
    description: String
}