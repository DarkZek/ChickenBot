enum CommandCategory {
    Hidden,
    Fun,
    Internet,
    Administration
}

enum CommandType {
    Slash,
    Message,
}

struct Command {
    name: String,
    code: String,
    description: String,
    usage: String,
    category: CommandCategory,
    command_type: CommandType,
    allow_bots: bool
}

struct CommandBuilder {
    name: Option<String>,
    code: Option<String>,
    description: Option<String>,
    usage: Option<String>,
    category: Option<CommandCategory>,
    command_type: Option<CommandType>,
    allow_bots: Option<bool>
}

impl CommandBuilder {

    pub fn new() -> CommandBuilder {
        CommandBuilder {
            name: None,
            code: None,
            description: None,
            usage: None,
            category: None,
            command_type: None,
            allow_bots: None
        }
    }

    pub fn with_name(&mut self, name: &str) -> &mut CommandBuilder {
        self.name = Some(name.to_string());
        self
    }

    pub fn with_code(&mut self, code: &str) -> &mut CommandBuilder {
        self.code = Some(code.to_string());
        self
    }

    pub fn with_description(&mut self, description: &str) -> &mut CommandBuilder {
        self.description = Some(description.to_string());
        self
    }

    pub fn with_usage(&mut self, usage: &str) -> &mut CommandBuilder {
        self.usage = Some(usage.to_string());
        self
    }

    pub fn with_category(&mut self, category: CommandCategory) -> &mut CommandBuilder {
        self.category = Some(category);
        self
    }

    pub fn with_command_type(&mut self, command_type: CommandType) -> &mut CommandBuilder {
        self.command_type = Some(command_type);
        self
    }

    pub fn with_allow_bots(&mut self, allow_bots: bool) -> &mut CommandBuilder {
        self.allow_bots = Some(allow_bots);
        self
    }

    pub fn build(self) -> Command {
        if self.name.is_none() {
            panic!("Command Builder attempted to build with no name!")
        }

        return Command {
            name: self.name.unwrap(),
            code: self.code.unwrap_or(String::new()),
            description: self.description.unwrap_or(String::new()),
            usage: self.usage.unwrap_or(String::new()),
            category: self.category.unwrap_or(CommandCategory::Hidden),
            command_type: self.command_type.unwrap_or(CommandCategory::Slash),
            allow_bots: self.allow_bots.unwrap_or(false)
        }
    }

}