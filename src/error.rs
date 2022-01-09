use std::fmt;
use chrono::ParseError;
use reqwest::StatusCode;
use serenity::prelude::SerenityError;

#[derive(Debug)]
pub enum Error {
    InvalidParameterType,
    ErrorHttpCode(StatusCode, Option<String>),
    Reqwest(reqwest::Error),
    Serenity(SerenityError),
    CronoParseError(ParseError),
    Other(String)
}

impl From<reqwest::Error> for Error {
    fn from(err: reqwest::Error) -> Self {
        Error::Reqwest(err)
    }
}

impl From<SerenityError> for Error {
    fn from(err: SerenityError) -> Self {
        Error::Serenity(err)
    }
}

impl From<ParseError> for Error {
    fn from(err: ParseError) -> Self {
        Error::CronoParseError(err)
    }
}

impl std::error::Error for Error {}

impl fmt::Display for Error {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            Error::InvalidParameterType => write!(f, "Discord sent parameter with invalid type"),
            Error::Reqwest(e) => e.fmt(f),
            Error::ErrorHttpCode(status, text) => write!(f, "A HTTP request returned a '{}'. Error: '{:?}'", status, text),
            Error::CronoParseError(e) => e.fmt(f),
            Error::Serenity(e) => e.fmt(f),
            Error::Other(str) => str.fmt(f),
        }
    }
}