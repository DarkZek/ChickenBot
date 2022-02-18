use crate::commands::command::{Command, CommandInfoBuilder, CommandInfo, CommandCategory, AppContext};
use async_trait::async_trait;
use lazy_static::lazy_static;
use serenity::builder::{ParseValue};
use serenity::model::channel::Message;
use serenity::utils::Color;
use crate::error::Error;

/*
 * Created by Marshall Scott on 6/02/22.
 */

lazy_static! {
    static ref INFO: CommandInfo = CommandInfoBuilder::new()
            .with_name("Chat")
            .with_description("Chats with humans")
            .with_usage("Say 11cm")
            .with_category(CommandCategory::Fun)
            .build();
}

pub struct DistanceConversionCommand {
    // Number is what you need to multiply by to convert to meters
    distances: Vec<Measurement>
}

#[async_trait]
impl Command for DistanceConversionCommand {
    fn info(&self) -> &CommandInfo { &INFO }

    async fn message(&self, ctx: &AppContext, message: &Message) -> Result<(), Error> {

        let user_id = ctx.api.cache.current_user().await.id;

        // Don't respond if the message is from this bot
        if user_id == message.author.id  {
            return Ok(())
        }

        // Stores the numbers that we want to convert
        let mut found_number = None;
        let mut found_measurement = None;

        // Signifies that the previous word was entirely a number so we can check if there's a space between the number and abbreviation
        let mut candidate = false;

        // Scan text
        'words: for word in message.content.split(' ') {

            // Check candidate
            if candidate {

                for distance in &self.distances {
                    let mut success = false;

                    // Check if this word is an abbreviation
                    for abbreviation in &distance.abbreviations {
                        if abbreviation.eq(&word) {
                            success = true;
                            break;
                        }
                    }

                    // Check if this word is a word
                    if !success && distance.name.eq(&word) || distance.name_multiple.eq(&word) {
                        success = true;
                    }

                    // Match! We've got a abbreviation, add it
                    if success {
                        found_measurement = Some(distance);
                        break 'words;
                    }
                }

                candidate = false
            }

            // Check if numeric
            for (i, char) in word.chars().into_iter().enumerate() {

                // Non numeric character
                if !Self::is_numeric(char) {

                    // If there's no numbers at all, don't bother
                    if i == 0 {
                        candidate = false;
                        continue 'words;
                    }

                    // Lets see if it ends with an abbreviation
                    let remaining_word = word.chars().skip(i).collect::<String>();

                    // Check if this word is an abbreviation
                    for distance in &self.distances {
                        for abbreviation in &distance.abbreviations {
                            if abbreviation.eq(&remaining_word) {

                                // Match! We've got a abbreviation, add if it parses
                                if let Ok(val) = word.chars().take(i).collect::<String>().parse::<f32>() {
                                    found_number = Some(val);
                                    found_measurement = Some(distance);
                                    break 'words;
                                }
                            }
                        }
                    }

                    candidate = false;
                    continue 'words;
                }
            }

            // If it got to this point it's been a valid number
            if let Ok(val) = word.parse::<f32>() {
                found_number = Some(val);
                candidate = true;
            }
        }

        if let Some(number) = found_number {
            if let Some(measurement) = found_measurement {
                let unit = if number > 1.0 { &measurement.name_multiple } else { &measurement.name };

                // The number in meters
                let converted_number_meters = number * measurement.length;

                let bananas = self.distances.get(self.distances.len() - 1).unwrap();

                let bananas_number = converted_number_meters / bananas.length;
                let bananas_unit = if bananas_number > 1.0 { &bananas.name_multiple } else { &bananas.name };

                message.channel_id.send_message(&ctx.api.http, |builder| {
                    builder.reference_message(&*message).allowed_mentions(|f| {
                        f.replied_user(false).parse(ParseValue::Users)
                    });

                    builder.embed(|e|
                        e.field(format!("{} {} is", number, unit), format!("{:.3} {}", converted_number_meters, "Meters"), false)
                        .field("or", format!("{:.3} {}", bananas_number, bananas_unit), false).color(Color::BLITZ_BLUE))
                }).await?;
            }
        }

        Ok(())
    }

    async fn new() -> Result<Self, Error> {
        Ok(DistanceConversionCommand {
            distances: vec![
                Measurement { name: String::from("Kilometer"), name_multiple: String::from("Kilometres"), abbreviations: vec![String::from("km"), String::from("Kilometre")], length: 1000.0 },
                //Measurement { name: String::from("Meter"), name_multiple: String::from("Metres"), abbreviations: vec![String::from("m"), String::from("Metre")], length: 1.0 },
                Measurement { name: String::from("Centimetre"), name_multiple: String::from("Centimetres"), abbreviations: vec![String::from("cm"), String::from("Centimeter")], length: 0.01 },
                Measurement { name: String::from("Millimetre"), name_multiple: String::from("Millimetres"), abbreviations: vec![String::from("mm"), String::from("Millimeter")], length: 0.001 },
                Measurement { name: String::from("Inch"), name_multiple: String::from("Inches"), abbreviations: vec![String::from("in"), String::from("\"")], length: 0.0254 },
                Measurement { name: String::from("Feet"), name_multiple: String::from("Feet"), abbreviations: vec![String::from("ft"), String::from("'")], length: 0.3048 },
                Measurement { name: String::from("Yard"), name_multiple: String::from("Yards"), abbreviations: vec![String::from("yd")], length: 0.9144 },
                Measurement { name: String::from("Mile"), name_multiple: String::from("Miles"), abbreviations: vec![String::from("mi")], length: 1609.344 },
                Measurement { name: String::from("Banana"), name_multiple: String::from("Bananas"), abbreviations: vec![], length: 0.2032 }
            ]
        })
    }
}

impl DistanceConversionCommand {
    fn is_numeric(msg: char) -> bool {
        msg == '0' ||
        msg == '1' ||
        msg == '2' ||
        msg == '3' ||
        msg == '4' ||
        msg == '5' ||
        msg == '6' ||
        msg == '7' ||
        msg == '8' ||
        msg == '9' ||
        msg == '.'
    }
}

struct Measurement {
    name: String,
    name_multiple: String,
    abbreviations: Vec<String>,
    length: f32
}