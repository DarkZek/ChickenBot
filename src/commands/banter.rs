use std::time::Duration;
use crate::commands::command::{Command, CommandInfoBuilder, CommandInfo, CommandCategory, AppContext};
use async_trait::async_trait;
use lazy_static::lazy_static;
use rand::Rng;
use serenity::model::channel::Message;
use serenity::model::prelude::ReactionType;
use tokio::time::sleep;
use crate::error::Error;
use crate::get_server;

/*
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

    async fn message(&self, ctx: &AppContext, message: &Message) -> Result<(), Error> {

        // Don't respond if theres an @everyone or they're not a bot and it's in a guild
        if message.mention_everyone || !message.author.bot || message.guild_id.is_none() || ctx.api.cache.current_user().await.id == message.author.id  {
            return Ok(())
        }

        // Check guild has banter enabled
        if get_server( message.guild_id.unwrap().0, &mut *(ctx.bot.connection.get()?))?.banter {
            return Ok(())
        }

        // Only respond sometimes
        if rand::thread_rng().gen_range(0 as u8, 100 as u8) > 94 {
            return Ok(())
        }

        let n = rand::thread_rng().gen_range(0 as u8, 8 as u8);
        match n {
            0 => BanterCommand::banter_1(ctx, message).await?,
            1 => BanterCommand::banter_2(ctx, message).await?,
            2 => BanterCommand::banter_3(ctx, message).await?,
            3 => BanterCommand::banter_4(ctx, message).await?,
            4 => BanterCommand::banter_5(ctx, message).await?,
            5 => BanterCommand::banter_6(ctx, message).await?,
            6 => BanterCommand::banter_7(ctx, message).await?,
            7 => BanterCommand::banter_8(ctx, message).await?,
            _ => {}
        }

        Ok(())
    }

    async fn new() -> Result<Self, Error> {
        Ok(BanterCommand {})
    }
}

impl BanterCommand {
    async fn banter_1(ctx: &AppContext<'_>, message: &Message) -> Result<(), Error> {
        Self::react_reactions(ctx, message, vec!["🇪", "🇼"]).await?;
        Ok(())
    }
    async fn banter_2(ctx: &AppContext<'_>, message: &Message) -> Result<(), Error> {
        Self::react_reactions(ctx, message, vec!["🤮"]).await?;
        Ok(())
    }
    async fn banter_3(ctx: &AppContext<'_>, message: &Message) -> Result<(), Error> {
        message.reply_mention(&ctx.api.http, "^ and this is why we can't have nice things").await?;
        Ok(())
    }
    async fn banter_4(ctx: &AppContext<'_>, message: &Message) -> Result<(), Error> {
        message.reply_mention(&ctx.api.http, "There's only one thing worse than your response times and that's AT&T's customer service support.").await?;
        Ok(())
    }
    async fn banter_5(ctx: &AppContext<'_>, message: &Message) -> Result<(), Error> {
        message.reply_mention(&ctx.api.http, "I just saw a funny meme, i'll fax it to you since I know you struggle with connection issues").await?;
        Ok(())
    }
    async fn banter_6(ctx: &AppContext<'_>, message: &Message) -> Result<(), Error> {
        message.reply_mention(&ctx.api.http, "https://i.imgur.com/gtwqETJ.jpeg").await?;
        Ok(())
    }
    async fn banter_7(ctx: &AppContext<'_>, message: &Message) -> Result<(), Error> {
        message.reply_mention(&ctx.api.http, "https://i.imgur.com/T99Az22.jpeg").await?;
        Ok(())
    }
    async fn banter_8(ctx: &AppContext<'_>, message: &Message) -> Result<(), Error> {
        message.reply_mention(&ctx.api.http, "https://i.imgur.com/Zp3BAgt.jpeg").await?;
        Ok(())
    }

    async fn react_reactions(ctx: &AppContext<'_>, message: &Message, reactions: Vec<&str>) -> Result<(), Error> {

        for reaction in reactions {
            message.react(&ctx.api.http, ReactionType::Unicode(reaction.to_string())).await?;
            sleep(Duration::from_millis(250)).await;
        }

        Ok(())

    }
}