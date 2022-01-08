use serenity::client::Context;
use serenity::model::interactions::{Interaction};
use crate::ChickenBot;

pub(crate) mod command;

pub mod invite;

impl ChickenBot {

    pub async fn interaction_created(&self, ctx: Context, interaction: Interaction) {

        if let Interaction::ApplicationCommand(command) = &interaction {
            for possible_command in &self.commands {
                if possible_command.info().code == command.data.name {
                    possible_command.triggered(ctx, command).await;
                    return
                }
            }

            println!("No command found for command: {:?}", command)
        }
    }
}