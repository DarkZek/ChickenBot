use std::fmt;
use backtrace::Backtrace;
use chrono::ParseError;
use reqwest::StatusCode;
use serenity::client::Context;
use serenity::model::interactions::{Interaction, InteractionResponseType};
use serenity::model::prelude::application_command::ApplicationCommandInteraction;
use serenity::model::prelude::InteractionApplicationCommandCallbackDataFlags;
use serenity::prelude::SerenityError;
use crate::commands::command::Command;
use crate::settings::SETTINGS;

#[derive(Debug)]
pub enum Error {
    InvalidParameterType,
    ErrorHttpCode(StatusCode, Option<String>),
    Reqwest(reqwest::Error),
    Serenity(SerenityError),
    CronoParseError(ParseError),
    Serde(serde_json::Error),
    Other(String)
}

impl From<reqwest::Error> for Error {
    fn from(err: reqwest::Error) -> Self {
        Error::Reqwest(err)
    }
}

impl From<SerenityError> for Error {
    fn from(err: SerenityError) -> Self {
        Error::Serenity(err)
    }
}

impl From<ParseError> for Error {
    fn from(err: ParseError) -> Self {
        Error::CronoParseError(err)
    }
}

impl From<serde_json::Error> for Error {
    fn from(err: serde_json::Error) -> Self {
        Error::Serde(err)
    }
}

impl std::error::Error for Error {}

impl fmt::Display for Error {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            Error::InvalidParameterType => write!(f, "Discord sent parameter with invalid type"),
            Error::Reqwest(e) => e.fmt(f),
            Error::ErrorHttpCode(status, text) => write!(f, "A HTTP request returned a '{}'. Error: '{:?}'", status, text),
            Error::CronoParseError(e) => e.fmt(f),
            Error::Serenity(e) => e.fmt(f),
            Error::Serde(e) => e.fmt(f),
            Error::Other(str) => str.fmt(f),
        }
    }
}

impl Error {
    pub async fn handle(e: Option<Self>, ctx: &Context, interaction: &ApplicationCommandInteraction, cmd: String) {
        // First, notify user of error
        let mut notified_user = false;

        match interaction.create_interaction_response(&ctx.http, |t| {
            t.kind(InteractionResponseType::ChannelMessageWithSource)
                .interaction_response_data(|message|
                    message.content("There was an error processing your request, it has been sent to the bot maintenance team.")
                        .flags(InteractionApplicationCommandCallbackDataFlags::EPHEMERAL))
        }).await {
            Ok(_) => notified_user = true,
            Err(e) => println!("Failed to notify user via interaction response! {}", e)
        }

        if !notified_user {
            // If the first method fails, try to DM them
            match interaction.user.direct_message(&ctx.http, |test| {
                test.content("There was an error processing your request, it has been sent to the bot maintenance team.")
            }).await {
                Ok(_) => notified_user = true,
                Err(e) => println!("Failed to notify user via DM! {}", e)
            }
        }

        match ctx.http.get_user(130173614702985216).await {
            Ok(user) => {
                match user.direct_message(&ctx.http, |message| {
                    println!("Command '{}' threw an error '{:?}':\nNotified User: {}", cmd, e, notified_user);
                    message.content(format!("Command '{}' threw an error '{:?}':\nNotified User: {}", cmd, e, notified_user))
                }).await {
                    Ok(_) => {}
                    Err(e) => println!("Error: Could not message user '{}' to send error message to", SETTINGS.get().as_ref().unwrap().user_manager)
                }
            }
            Err(e) => println!("Error: Could not look up user '{}' to send error message to", SETTINGS.get().as_ref().unwrap().user_manager)
        }
    }
}