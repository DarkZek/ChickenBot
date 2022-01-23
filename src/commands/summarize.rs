use std::mem;
use std::sync::{Arc, Mutex};
use serenity::client::Context;
use serenity::model::interactions::application_command::ApplicationCommandInteraction;
use crate::commands::command::{Command, CommandInfoBuilder, CommandInfo, CommandCategory};
use async_trait::async_trait;
use html5ever::tendril::{ByteTendril, fmt, ReadExt};
use html5ever::tokenizer::{BufferQueue, Token, Tokenizer, TokenizerOpts, TokenSink, TokenSinkResult};
use html5ever::tokenizer::TagKind::{EndTag, StartTag};
use html5ever::tokenizer::Token::{CharacterTokens, TagToken};
use serenity::model::interactions::InteractionResponseType;
use crate::error::Error;
use crate::modules::summarizer::summarize;
use crate::static_regex;

/**
 * Created by Marshall Scott on 14/01/22.
 */

pub struct SummarizeCommand {}

#[async_trait]
impl Command for SummarizeCommand {
    fn info(&self) -> CommandInfo {
        CommandInfoBuilder::new()
            .with_name("Summarize Link")
            .with_code("tldr")
            .with_description("Summarizes the last link sent in chat")
            .with_category(CommandCategory::Internet)
            .build()
    }

    async fn triggered(&self, ctx: &Context, command: &ApplicationCommandInteraction) -> Result<(), Error> {

        return Err(Error::Other(String::from("Heya")));

        command.create_interaction_response(&ctx.http, |response| {
            response.kind(InteractionResponseType::DeferredChannelMessageWithSource)
        }).await?;

        // Get last link
        let mut response: Option<String> = None;

        let messages = command.channel_id.messages(&ctx.http, |msgs| msgs.limit(10)).await?;

        // Regex to find links
        let regex = match static_regex!("(http|ftp|https):\\/\\/([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:\\/~+#-]*[\\w@?^=%&\\/~+#-])") {
            Ok(val) => {val}
            Err(e) => {
                println!("Error parsing regex {}", e);
                return Ok(())
            }
        };

        // Look at last 10 messages
        for message in messages {
            if let Some(link) = regex.find(&message.content).unwrap() {
                let link = &message.content[link.start()..link.end()];

                let web_data = reqwest::get(link).await?.text().await?;

                if !web_data.contains("content=\"article\"") {
                    // Not an article!
                    continue
                }

                response = Some(web_data);

                break;
            }
        }

        if response.is_none() {
            command.create_followup_message(&ctx.http, |t| {
                t.content("No article links in the last 10 messages")
            }).await?;
            return Ok(());
        }

        let content = SummarizeHTMLTokenizer::start(response.unwrap());

        let val = match summarize(&content, 4) {
            Ok(val) => val,
            Err(e) => {
                println!("Summarization failed. {}", e);
                // Send error here
                return Ok(())
            }
        };

        command.create_followup_message(&ctx.http, |t| {
            t.content(val)
        }).await?;

        Ok(())
    }

    async fn new() -> Result<Self, Error> {
        Ok(SummarizeCommand {})
    }
}

#[derive(Clone)]
struct SummarizeHTMLTokenizer {
    content: Arc<Mutex<String>>,
    store_next: bool
}

impl SummarizeHTMLTokenizer {

    fn start(response: String) -> String {
        let content = Arc::new(Mutex::new(String::new()));

        // Create token reader
        let sink = SummarizeHTMLTokenizer { content: content.clone(), store_next: false };

        // Load data to tokenizer
        let mut chunk = ByteTendril::new();
        response.as_bytes().read_to_tendril(&mut chunk).unwrap();

        // Unsure what this does
        let mut input = BufferQueue::new();
        input.push_back(chunk.try_reinterpret::<fmt::UTF8>().unwrap());

        //Create tokenizer and tokenize!
        let mut tok = Tokenizer::new(
            sink,
            TokenizerOpts::default(),
        );

        let _ = tok.feed(&mut input);
        tok.end();

        mem::drop(tok);

        return Arc::try_unwrap(content).unwrap().into_inner().unwrap();
    }

}

impl TokenSink for SummarizeHTMLTokenizer {
    type Handle = ();

    fn process_token(&mut self, token: Token, _line_number: u64) -> TokenSinkResult<()> {
        match token {
            TagToken(tag) => {

                if tag.kind == StartTag && tag.name.to_string()=="p" && !tag.self_closing {
                    self.store_next = true;
                }

                if tag.kind == EndTag && tag.name.to_string()=="p" {
                    self.store_next = false;
                }
            },
            CharacterTokens(token) => {

                if !self.store_next || token.len() < 5 {
                    return TokenSinkResult::Continue;
                }

                {
                    let mut content = self.content.lock().unwrap();
                    *content = format!("{}\n{}", content, &token)
                }
            }
            _ => {
            },
        }
        TokenSinkResult::Continue
    }
}