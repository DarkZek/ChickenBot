use std::{error::Error, fmt};

#[derive(Debug)]
pub struct InvalidParameterType {}

impl Error for InvalidParameterType {}

impl fmt::Display for InvalidParameterType {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "Discord sent parameter with invalid type")
    }
}