use std::fs;
use std::lazy::SyncOnceCell;
use serde::Deserialize;

pub static SETTINGS: SyncOnceCell<Settings> = SyncOnceCell::new();

#[derive(Deserialize, Debug)]
pub struct Settings {
    pub prefix: String,
    pub message_prefix: String,
    pub changelog_url: String,
    pub repo_url: String,
    pub user_agent: String,
    pub user_manager: u64
}

impl Settings {
    pub fn load() {
        match fs::File::open("settings.json") {
            Ok(file) => {
                match serde_json::from_reader(file) {
                    Ok(val) => SETTINGS.set(val),
                    Err(e) => {
                        println!("Error parsing settings.json {}. Using default settings", e);
                        SETTINGS.set(Settings {
                            prefix: ">".parse().unwrap(),
                            message_prefix: "Brawk! ".to_string(),
                            changelog_url: "https://api.github.com/repos/DarkZek/ChickenBot/commits".to_string(),
                            repo_url: "https://github.com/DarkZek/ChickenBot".to_string(),
                            user_agent: "Chicken-Bot".to_string(),
                            user_manager: 130173614702985216
                        })
                    }
                }
            }
            Err(e) => {
                println!("Error reading settings.json {}. Using default settings", e);
                SETTINGS.set(Settings {
                    prefix: ">".parse().unwrap(),
                    message_prefix: "Brawk! ".to_string(),
                    changelog_url: "https://api.github.com/repos/DarkZek/ChickenBot/commits".to_string(),
                    repo_url: "https://github.com/DarkZek/ChickenBot".to_string(),
                    user_agent: "Chicken-Bot".to_string(),
                    user_manager: 130173614702985216
                })
            }
        };
    }
}

