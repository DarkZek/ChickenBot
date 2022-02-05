use serenity::client::Context;
use serenity::model::prelude::application_command::ApplicationCommandInteraction;
use async_trait::async_trait;
use serenity::builder::CreateApplicationCommand;
use serenity::model::channel::Message;
use serenity::model::prelude::message_component::MessageComponentInteraction;
use crate::ChickenBot;
use crate::error::Error;

#[derive(Debug)]
pub enum CommandCategory {
    Hidden,
    Fun,
    Internet,
    Administration
}

#[derive(Debug)]
pub enum CommandType {
    Slash,
    Message,
}

#[async_trait]
#[allow(dead_code)]
#[allow(unused_variables)]
pub trait Command : Sync + Send {
    fn info(&self) -> &CommandInfo;
    fn parameters(&self, command: &mut CreateApplicationCommand) {}
    async fn triggered(&self, ctx: &AppContext, command: &ApplicationCommandInteraction) -> Result<(), Error> { Ok(()) }
    async fn button_clicked(&self, ctx: &AppContext, message: &MessageComponentInteraction) -> Result<(), Error> { Ok(()) }
    async fn message(&self, ctx: &AppContext, message: &Message) -> Result<(), Error> { Ok(()) }
    fn shutdown(&self) {}
    async fn new() -> Result<Self, Error> where Self: Sized;
}

#[derive(Debug)]
pub struct CommandInfo {
    pub name: String,
    pub code: String,
    pub description: String,
    pub usage: String,
    pub category: CommandCategory,
    pub command_type: CommandType,
    pub allow_bots: bool
}

pub struct CommandInfoBuilder {
    name: Option<String>,
    code: Option<String>,
    description: Option<String>,
    usage: Option<String>,
    category: Option<CommandCategory>,
    command_type: Option<CommandType>,
    allow_bots: Option<bool>
}

impl CommandInfoBuilder {

    pub fn new() -> CommandInfoBuilder {
        CommandInfoBuilder {
            name: None,
            code: None,
            description: None,
            usage: None,
            category: None,
            command_type: None,
            allow_bots: None
        }
    }

    pub fn with_name(&mut self, name: &str) -> &mut CommandInfoBuilder {
        self.name = Some(name.to_string());
        self
    }

    pub fn with_code(&mut self, code: &str) -> &mut CommandInfoBuilder {
        self.code = Some(code.to_string());
        self
    }

    pub fn with_description(&mut self, description: &str) -> &mut CommandInfoBuilder {
        self.description = Some(description.to_string());
        self
    }

    pub fn with_usage(&mut self, usage: &str) -> &mut CommandInfoBuilder {
        self.usage = Some(usage.to_string());
        self
    }

    pub fn with_category(&mut self, category: CommandCategory) -> &mut CommandInfoBuilder {
        self.category = Some(category);
        self
    }

    pub fn with_command_type(&mut self, command_type: CommandType) -> &mut CommandInfoBuilder {
        self.command_type = Some(command_type);
        self
    }

    pub fn with_allow_bots(&mut self, allow_bots: bool) -> &mut CommandInfoBuilder {
        self.allow_bots = Some(allow_bots);
        self
    }

    pub fn build(&mut self) -> CommandInfo {
        if self.name.is_none() {
            panic!("Command Builder attempted to build with no name!")
        }

        return CommandInfo {
            name: self.name.take().unwrap(),
            code: self.code.take().unwrap_or(String::new()),
            description: self.description.take().unwrap_or(String::new()),
            usage: self.usage.take().unwrap_or(String::new()),
            category: self.category.take().unwrap_or(CommandCategory::Hidden),
            command_type: self.command_type.take().unwrap_or(CommandType::Slash),
            allow_bots: self.allow_bots.take().unwrap_or(false)
        }
    }

}

pub struct AppContext<'a> {
    pub api: &'a Context,
    pub bot: &'a ChickenBot
}