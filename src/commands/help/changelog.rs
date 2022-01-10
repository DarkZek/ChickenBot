use std::fmt::Formatter;
use chrono::{DateTime, FixedOffset};
use reqwest::Response;
use serde_json::Value;
use crate::error::Error;

pub struct Changelog {
    time: DateTime<FixedOffset>,
    message: String,
    author: String
}

impl std::fmt::Display for Changelog {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "<{}> at <t:{}:f> | `{}`", self.author, self.time.timestamp(), self.message)
    }
}

impl Changelog {
    pub async fn new(data: Response) -> Result<Changelog, Error> {
        let json = data.json::<Vec<Value>>()
            .await?;

        let latest_change = json.get(0).ok_or(Error::Other(String::from("No commits in git log")))?;

        let latest_commit = latest_change.get("commit").ok_or(Error::Other(String::from("No commit object in commits list")))?;

        let latest_author = latest_commit.get("author").ok_or(Error::Other(String::from("No author object on commit in git log")))?;

        let time_data = latest_author.get("date").ok_or(Error::Other(String::from("No commits in git log")))?;
        let time = DateTime::parse_from_rfc3339(time_data.as_str().unwrap())?;

        let message = latest_commit.get("message").ok_or(Error::Other(String::from("No message in latest commit in git log")))?
            .as_str().ok_or(Error::Other(String::from("No string message in latest commit in git log")))?.to_string();

        let author_object = latest_change.get("author").ok_or(Error::Other(String::from("No author in latest changes in git log")))?;
        let author = author_object.get("login").ok_or(Error::Other(String::from("No login information about author in git log")))?.as_str()
            .ok_or(Error::Other(String::from("No string login information about author in git log")))?.to_string();

        Ok(Changelog {
            time,
            message,
            author
        })
    }
}