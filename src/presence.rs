use rand::Rng;
use tokio::fs::File;
use tokio::io::AsyncReadExt;

pub struct PresenceMessage {
    messages: Vec<String>
}

impl PresenceMessage {
    pub async fn new() -> PresenceMessage {

        let mut messages = Vec::new();

        match File::open("presences.txt").await {
            Ok(mut val) => {
                let mut data = String::new();
                if let Err(e) = val.read_to_string(&mut data).await {
                    println!("Failed to read presences.txt, using defaults. {}", e);
                    messages.push(String::from("Serving $1 servers"))
                }

                for line in data.split('\n') {
                    messages.push(line.to_string());
                }
            }
            Err(e) => {
                println!("Failed to read presences.txt, using defaults. {}", e);
                messages.push(String::from("Serving $1 servers"))
            }
        }

        PresenceMessage {
            messages
        }
    }

    pub fn get_presence(&self, guilds: usize) -> String {
        let index = rand::thread_rng().gen_range(0, self.messages.len());
        let mut msg: String = self.messages[index].clone();
        msg = msg.replace("$1", &guilds.to_string());
        msg
    }
}