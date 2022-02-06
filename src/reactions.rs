use std::collections::HashMap;
use rand::Rng;
use tokio::io::AsyncReadExt;
use crate::error::Error;

pub struct Reactions {
    reaction: HashMap<ReactionType, Vec<String>>
}

#[derive(Hash, Eq, PartialEq)]
pub enum ReactionType {
    WhatAreYouDoing
}

impl Reactions {
    pub async fn load() -> Reactions {
        let mut reaction = HashMap::new();

        if let Ok(v) = Self::load_file("whatareyoudoing").await {
            reaction.insert(ReactionType::WhatAreYouDoing, v);
        }

        Reactions {
            reaction
        }
    }

    async fn load_file(name: &str) -> Result<Vec<String>, Error> {
        let mut output = String::new();
        tokio::fs::File::open(format!("reactions/{}.txt", name)).await?.read_to_string(&mut output).await?;
        Ok(output.split("\n").into_iter().map(|i| i.to_string()).collect::<Vec<String>>())
    }

    pub fn get(&self, reaction_type: ReactionType) -> &str {
        let options = self.reaction.get(&reaction_type).unwrap();

        options.get(rand::thread_rng().gen_range(0, options.len())).unwrap()
    }
}