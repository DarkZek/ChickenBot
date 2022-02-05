use serenity::model::interactions::InteractionResponseType;
use serenity::model::interactions::application_command::ApplicationCommandInteraction;
use crate::commands::command::{Command, CommandInfoBuilder, CommandInfo, CommandCategory, AppContext};
use async_trait::async_trait;
use lazy_static::lazy_static;
use serenity::model::interactions::message_component::MessageComponentInteraction;
use serenity::model::prelude::InteractionApplicationCommandCallbackDataFlags;
use crate::error::Error;
use crate::modules::meme_cache::MEME_CACHE;
use crate::settings::SETTINGS;

/*
 * Created by Marshall Scott on 8/01/22.
 */

lazy_static! {
    static ref INFO: CommandInfo = CommandInfoBuilder::new()
            .with_name("Meme Me")
            .with_code("meme")
            .with_description("Fetches the hottest new memes straight from the source, iFunny")
            .with_category(CommandCategory::Fun)
            .build();
}

pub struct MemesCommand {}

#[async_trait]
impl Command for MemesCommand {
    fn info(&self) -> &CommandInfo { &INFO }

    async fn triggered(&self, ctx: &AppContext, command: &ApplicationCommandInteraction) -> Result<(), Error> {

        let mut meme_manager = MEME_CACHE.lock().await;

        let meme = meme_manager.pop();

        if let Some(meme) = meme {
            command.create_interaction_response(&ctx.api.http, |t| {
                t.kind(InteractionResponseType::ChannelMessageWithSource)
                    .interaction_response_data(|message| {
                        meme.to_interaction(message);
                        message
                    })
            }).await?;
        } else {
            command.create_interaction_response(&ctx.api.http, |t| {
                t.kind(InteractionResponseType::ChannelMessageWithSource)
                    .interaction_response_data(|message| {
                        message.content("Meme storage depleted. Try again later").flags(InteractionApplicationCommandCallbackDataFlags::EPHEMERAL)
                    })
            }).await?;
        }

        // Fill buffer with more memes
        meme_manager.topup().await;

        Ok(())
    }

    async fn button_clicked(&self, ctx: &AppContext, message: &MessageComponentInteraction) -> Result<(), Error> {

        // Check user has permission to chat in guild channel
        if let Some(guild_member) = &message.member {
            let channel = ctx.api.cache.guild_channel(message.channel_id).await.unwrap();
            let guild = ctx.api.cache.guild(message.guild_id.unwrap()).await.unwrap();

            if !guild.user_permissions_in(&channel, guild_member)?.send_messages() {
                message.create_interaction_response(&ctx.api.http, |t| {
                    t.interaction_response_data(|response| {
                        response.content(format!("{} You don't have permission to do that!", SETTINGS.get().unwrap().prefix))
                    })
                }).await?;

                return Ok(())
            }
        }

        let mut meme_manager = MEME_CACHE.lock().await;

        if let Some(meme) = meme_manager.pop() {
            message.create_interaction_response(&ctx.api.http, |t| {
                t.kind(InteractionResponseType::UpdateMessage)
                    .interaction_response_data(|message| {
                        meme.to_interaction(message);
                        message
                    })
            }).await?;
        } else {
            message.create_interaction_response(&ctx.api.http, |t| {
                t.kind(InteractionResponseType::ChannelMessageWithSource)
                    .interaction_response_data(|message| {
                        message.content("Meme storage depleted. Try again later").flags(InteractionApplicationCommandCallbackDataFlags::EPHEMERAL)
                    })
            }).await?;
        }

        // Fill buffer with more memes
        meme_manager.topup().await;

        return Ok(())
    }

    async fn new() -> Result<Self, Error> {
        // Topup meme cache
        MEME_CACHE.lock().await.topup().await;

        Ok(MemesCommand { })
    }
}