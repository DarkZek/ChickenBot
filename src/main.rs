#![feature(once_cell)]
#![feature(async_closure)]

#[macro_use]
extern crate diesel;
#[macro_use]
extern crate diesel_migrations;
extern crate dotenv;

use std::env;
use std::sync::Arc;
use std::sync::Mutex;
use std::sync::atomic::{AtomicBool, Ordering};
use diesel::PgConnection;
use diesel::r2d2::{ConnectionManager, Pool};
use diesel_migrations::embed_migrations;

use serenity::{
    async_trait,
    model::{
        gateway::Ready,
        interactions::Interaction,
    },
    prelude::*,
};
use serenity::model::channel::Message;
use serenity::model::gateway::Activity;

use serenity::model::id::GuildId;
use serenity::model::prelude::OnlineStatus;
use tokio::time::Duration;

use crate::commands::command::Command;
use crate::commands::delete_commands::DeleteCommandsCommand;
use crate::commands::dong::DongCommand;
use crate::commands::help::HelpCommand;
use crate::commands::meme::MemesCommand;
use crate::commands::summarize::SummarizeCommand;
use crate::db::{establish_connection, get_server};
use crate::presence::PresenceMessage;
use crate::settings::Settings;

pub mod commands;
pub mod modules;
pub mod settings;
pub mod error;
pub mod presence;
pub mod db;

const VERSION: &str = env!("CARGO_PKG_VERSION");

embed_migrations!();

struct ChickenBot {
    commands: Vec<Box<dyn Command>>,
    presence_loop_running: AtomicBool,
    guild_count: Arc<Mutex<usize>>,
    connection: Pool<ConnectionManager<PgConnection>>
}

impl ChickenBot {
    pub async fn new(connection: Pool<ConnectionManager<PgConnection>>) -> ChickenBot {
        ChickenBot {
            commands: ChickenBot::load_commands().await,
            presence_loop_running: AtomicBool::new(false),
            guild_count: Arc::new(Mutex::new(0)),
            connection
        }
    }

    pub async fn load_commands() -> Vec<Box<dyn Command>> {
        let mut commands: Vec<Box<dyn Command>> = vec![
            Box::new(HelpCommand::new().await.unwrap()),
            Box::new(MemesCommand::new().await.unwrap()),
            Box::new(DongCommand::new().await.unwrap()),
            Box::new(SummarizeCommand::new().await.unwrap()),
        ];

        if env::var("DEV").is_ok() {
            commands.push(Box::new(DeleteCommandsCommand::new().await.unwrap()));
        }

        commands
    }
}

#[async_trait]
impl EventHandler for ChickenBot {
    async fn ready(&self, ctx: Context, ready: Ready) {
        println!("{} v{} is connected!", ready.user.name, VERSION);

        self.register_commands(&ctx).await
    }

    async fn interaction_create(&self, ctx: Context, interaction: Interaction) {
        self.interaction_created(ctx, interaction).await
    }

    async fn message(&self, ctx: Context, message: Message) {
        self.message_sent(ctx, message).await
    }

    async fn cache_ready(&self, ctx: Context, guilds: Vec<GuildId>) {
        println!("Cache built successfully!");

        *self.guild_count.lock().unwrap() = guilds.len();

        let ctx = Arc::new(ctx);

        if !self.presence_loop_running.load(Ordering::Relaxed) {

            let ctx1 = Arc::clone(&ctx);
            let guilds_mutex = Arc::clone(&self.guild_count);

            tokio::spawn(async move {

                let presence = PresenceMessage::new().await;

                loop {
                    let mut guilds = 0;
                    if let Ok(val) = guilds_mutex.lock() {
                        guilds = *val;
                    }

                    let presence_message = presence.get_presence(guilds);
                    println!("Set presence to {}", presence_message);

                    ctx1.set_presence(Some(Activity::playing(presence_message)), OnlineStatus::Online).await;

                    tokio::time::sleep(Duration::from_secs(5 * 60)).await;
                }
            });

            // Now that the loop is running, we set the bool to true
            self.presence_loop_running.swap(true, Ordering::Relaxed);
        }
    }
}

#[tokio::main]
async fn main() {

    let connection = establish_connection();

    let mut conn = connection.get().unwrap();

    println!("{:?}", get_server(874804013286195211, &mut *conn));

    Settings::load();

    // Configure the client with your Discord bot token in the environment.
    let token = env::var("DISCORD_TOKEN").expect("Expected a token in the environment");

    // The Application Id is usually the Bot User Id.
    let application_id: u64 = env::var("APPLICATION_ID")
        .expect("Expected an application id in the environment")
        .parse()
        .expect("application id is not a valid id");

    // Build our client.
    let mut client = Client::builder(token)
        .event_handler(ChickenBot::new(connection).await)
        .application_id(application_id)
        .await
        .expect("Error creating client");

    if let Err(why) = client.start().await {
        println!("Client error: {:?}", why);
    }
}
