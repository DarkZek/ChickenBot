use std::collections::HashMap;
use std::fmt::{Display, Formatter};
use std::lazy::{SyncOnceCell};
use std::ops::ControlFlow::{Break, Continue};
use fancy_regex::{Error, Regex};

// used very often
type RegexResult<T> = fancy_regex::Result<T>;

macro_rules! static_regex {
    ($lit:literal) => {
        {
            static REGEX: SyncOnceCell<Regex> = SyncOnceCell::new();
            REGEX.get_or_try_init(|| Regex::new($lit))
        }
    };
}

fn regex_split<'a> (regex: &'static Regex, text: &'a str) -> RegexResult<Vec<&'a str>> {
    regex_split_iter(regex, text)
        .collect::<_>()
}

fn regex_split_iter<'a> (regex: &'static Regex, text: &'a str) -> impl Iterator<Item = RegexResult<&'a str>> {
    let mut last = 0;
    let iter = regex.find_iter(text)
        .map(move |x| {
            x.map(|x| (x.start(), x.end()))
        })
        .chain([Ok((text.len(), text.len()))])
        .filter_map(move |x| {
            let (start, end) = match x {
                Ok(v) => { v }
                Err(e) => { return Some(Err(e)) }
            };
            let range = last..start;
            last = end;

            // omit empty strings
            if range.is_empty() {
                return None;
            }
            let val = &text[range];
            Some(Ok(val))
        });
    iter
}

// find words and their occurrences
fn get_word_count(text: &str) -> RegexResult<HashMap<&str, usize>> {
    let regex = static_regex!("\\s+|[',.\\(\\)*]")?;

    let mut words = HashMap::new();

    regex_split_iter(&regex, text)
        .try_for_each(|word| {
            let word = word?;
            if let Some(val) = words.get_mut(word) {
                *val += 1;
            } else {
                words.insert(word, 1);
            }
            RegexResult::<()>::Ok(())
        })?;
    Ok(words)
}

const STOP_WORDS: [&'static str; 111] = ["a","able","about","after","all","also","am","an","and",
    "any","are","as","at","be","because","been","but","by","can","cannot","could","did","do","does",
    "either","else","ever","every","for","from","get","got","had","has","have","he","her","hers",
    "him","his","how","I","if","in","into","is","it","its","just","let","like","likely","may","me",
    "might","most","must","my","neither","no","nor","not","of","off","often","on","only","or",
    "other","our","own","said","say","says","she","should","so","some","than","that","the","their",
    "them","then","there","these","they","this","they're","to","too","that's","us","was","we",
    "were","what","when","where","which","while","who","whom","why","will","with","would","yet",
    "you","your","you're"];

// removes specific words from the word count histogram
fn filter_stop_words(words: &mut HashMap<&str, usize>) {
    STOP_WORDS.iter()
        .for_each(|word| {
            if words.contains_key(word) {
                words.remove(word);
            }
        });
}

// sorts within a given section of counts
// all words with 1 occurrences will be sorted as a section, then 2, then 3, and so on
fn sort_sectioned<F>(sorted: &mut Vec<(&str, usize)>, mut compare: F)
    where F: FnMut(&(&str, usize), &(&str, usize)) -> std::cmp::Ordering {
    let mut last = sorted[0].1;
    let mut last_index = 0;

    for i in 0..sorted.len() {
        let this = sorted[i].1;
        if this != last {
            sorted[last_index..i].sort_by(&mut compare);
            last = this;
            last_index = i;
        }
    }
}

// takes the word count histogram and sorts it in various ways
// it is possible to expose control of how sorting is done to the caller
// for now the default is sorting by word size (in its respective section via the fn above)
fn sort(words: HashMap<&str, usize>) -> Vec<&str> {
    let mut sorted = words.into_iter().collect::<Vec<_>>();
    sorted.sort_by(|(_, a_size), (_, b_size)| a_size.cmp(b_size));

    // sort alphabetical
    //sort_sectioned(&mut sorted, |(a, _), (b, _)| a.cmp(b));

    // sort by word size
    sort_sectioned(&mut sorted, |(a, _), (b, _)| {
        a.len()
            .cmp(&b.len())
            .reverse()
    });

    sorted.into_iter()
        .map(|(text, _count)| text)
        .collect()
}

// split input string into sentences
fn split_sentences(text: &str) -> RegexResult<Vec<&str>> {
    let regex = static_regex!("(?<!\\d)\\.(?!\\d)|(?<=\\d)\\.(?!\\d)|(?<!\\d)\\.(?=\\d)")?;

    regex_split(regex, text)
}

// i wish there was a better way to do this
// but it could be worse
fn prep_sentences(text: &str) -> RegexResult<String> {
    let text = text.replace("Mr.", "Mr").replace("Ms.", "Ms").replace("Dr.", "Dr").replace("Jan.", "Jan").replace("Feb.", "Feb")
        .replace("Mar.", "Mar").replace("Apr.", "Apr").replace("Jun.", "Jun").replace("Jul.", "Jul").replace("Aug.", "Aug")
        .replace("Sep.", "Sep").replace("Spet.", "Sept").replace("Oct.", "Oct").replace("Nov.", "Nov").replace("Dec.", "Dec")
        .replace("St.", "St").replace("Prof.", "Prof").replace("Mrs.", "Mrs").replace("Gen.", "Gen")
        .replace("Corp.", "Corp").replace("Mrs.", "Mrs").replace("Sr.", "Sr").replace("Jr.", "Jr").replace("cm.", "cm")
        .replace("Ltd.", "Ltd").replace("Col.", "Col").replace("vs.", "vs").replace("Capt.", "Capt")
        .replace("Univ.", "University").replace("Sgt.", "Sgt").replace("ft.", "ft").replace("in.", "in")
        .replace("Ave.", "Ave").replace("Univ.", "University").replace("Lt.", "Lt").replace("etc.", "etc").replace("mm.", "mm")
        .replace("\n\n", "").replace("\n", "").replace("\r", "");

    let regex = static_regex!("([A-Z])\\.")?;
    let t = regex.replace(&text, "$1");
    Ok(t.to_string())
}

// finds the sentence a word occurs in
// in the java version this returned a String for some reason
// i have replaced it with an array index to the 'sentences' vec and it works the same
// this also helps avoid a lot of pointless string comparisons later (line 155 in the java version)
fn search<'a> (sentences: &Vec<&'a str>, word: &str) -> Option<usize> {
    sentences.iter()
        .enumerate()
        .find_map(|(index, sentence)| {
            if sentence.contains(word) {
                Some(index)
            } else {
                None
            }
        })
}


/// Summarizes the input text to the `max` number of sentences
///
/// # Arguments
///
/// * `text`:
/// * `max`:
///
/// returns: Result<String, SummarizeError>
///
/// # Examples
///
/// ```
/// let input = r#"This is a test. How cool is this function. Wow I am so impressed.
/// This function is the best! Does it matter how long my sentences are?"#;
///
/// let output = summarize(input, 3).unwrap();
///         assert_eq!(output,
/// r#"• This is a test
///
/// • How cool is this function
///
/// • Wow I am so impressed"#);
/// ```
pub fn summarize(text: &str, max: usize) -> Result<String, SummarizeError> {
    let text = text.trim();
    if text.is_empty() || text.eq(" ") || text.eq("\n") {
        return Err(SummarizeError::NothingToSummarize)
    }

    let mut freqs = get_word_count(text)?;
    filter_stop_words(&mut freqs);

    let sorted = sort(freqs);

    let src = prep_sentences(text)?;
    let sentences = {
        let v = split_sentences(&src)?;
        if v.len() == 0 {
            return Err(SummarizeError::TextHasNoDelimiters)
        }
        v
    };

    let mut summary_sentences = Vec::new();
    summary_sentences.push(0); // always include first sentence
    sorted.iter().try_for_each(|&word| {
        if let Some(first_matching) = search(&sentences, word) {
            if first_matching != 0 {
                summary_sentences.push(first_matching);
            }
        }

        if summary_sentences.len() >= max {
            Break(())
        } else {
            Continue(())
        }
    });

    let len = summary_sentences.iter()
        .fold(0, |count, index| count + sentences[*index].len());
    let mut summary = String::with_capacity(len);

    sentences.iter()
        .enumerate()
        .for_each(|(index, &sentence)| {
            if summary_sentences.contains(&index) {
                summary.push_str("• ");
                summary.push_str(sentence.trim());
                summary.push('\n');
                summary.push('\n');
            }
        });

    // remove last 2 new line chars
    summary.truncate(summary.len() - 2);

    Ok(summary)
}

#[derive(Debug)]
pub enum SummarizeError {
    NothingToSummarize,
    TextHasNoDelimiters,
    RegexError(fancy_regex::Error)
}

impl std::error::Error for SummarizeError {}

impl Display for SummarizeError {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        match self {
            SummarizeError::NothingToSummarize => { write!(f, "Nothing to summarize") }
            SummarizeError::TextHasNoDelimiters => { write!(f, "No sentences could not be found because the input text has no delimiters") }
            SummarizeError::RegexError(e) => { Display::fmt(e, f) }
        }
    }
}

impl From<fancy_regex::Error> for SummarizeError {
    fn from(err: Error) -> Self {
        SummarizeError::RegexError(err)
    }
}

#[cfg(test)]
mod tests {
    use crate::modules::summarizer::*;

    #[test]
    fn check_regex_errors() {
        let input = r#"* Kiwis stuck in Australia may be able to return if they face 'significant and severe hardship'
 More than 490,000 booster shots have been provided in total, while more than 3.87 million people – 92 per cent of those eligible – have received their second jabs.
 The ministry – which did not provide an update on Saturday – said 57 of the community cases over the past two days were in Auckland, seven in Waikato, 16 in Bay of Plenty, and two in Lakes. There were also two previously announced cases in Wellington and one in Taranaki.
 The 64 border cases include eight people who arrived from the US on Thursday. The other cases who arrived on Thursday were three people from Australia, two from the UK, two from Pakistan, one from France and one from India.
 BP Connect Ōtaki has been identified as a Covid location of interest. (file pic)
 Two of the cases who arrived on Friday were from the US, two were from Brazil, with one from the UK.
 The two cases in Wellington, announced on Saturday, were linked to recent travel to the Bay of Plenty and several locations of interest in Wellington. There was a possible link by one case to a drum & bass festival on January 3 in Tauranga.
 On Sunday, one of the cases in hospital was in Northland, five on the North Shore, 11 in Auckland, 12 in Middlemore and two in Tauranga. Both cases in ICU were in Auckland’s Middlemore Hospital.
 The queue for vaccine boosters at Unichem Central Pharmacy on Lambton Quay, Wellington on Wednesday – the day 1.2 million people became eligible for their booster shots.
 Eight of those in hospital in northern region wards were unvaccinated or not eligible for the vaccine, seven were partially immunised, 11 were fully vaccinated, and the vaccination status of one was unknown.
 The latest community cases take the total number of cases in the current community outbreak to 11,142, with 1087 cases classed as active. No unexpected wastewater detections were reported on Sunday.
 A pop-up Covid testing site was set up in the Coromandel Peninsula seaside town of Whangamatā during the weekend, after Waikato District Health Board said on Friday that the virus had been detected in the town’s wastewater.
 Stuff's Whole Truth project has published over 50 articles examining misinformation about the Covid-19 vaccine. These are the most common themes. (Te Reo subtitles.)
 Health officials had not been able to link the wastewater detection to a confirmed or recovered case, Waikato DHB said.
 The ministry advised of a range of new Covid places of interest on Sunday morning . They included one place classed as a close contact location – the Village Sports Bar & Cafe in Ngongotahā, Rotorua, from 6.30pm to 10.30pm on January 1.
 People at the venue at the time are required to self-isolate and have an immediate Covid test.
 Many of the other locations of interest advised on Sunday morning were in the Bay of Plenty, many of them in Mt Maunganui. Bendon Westgate in Auckland also featured several times."#;

        let _out = summarize(input, 5).unwrap();
    }

    #[test]
    fn check_regex_split() {
        let input = "test    test, yes a, b,  c  a \n things are   kinda";
        let regex = static_regex!("\\s+|[',.\\(\\)*]").unwrap();

        let expected_output = [
            "test",
            "test",
            "yes",
            "a",
            "b",
            "c",
            "a",
            "things",
            "are",
            "kinda",
        ];

        let mut count = 0;
        regex_split_iter(regex, input)
            .zip(&expected_output)
            .for_each(|(out, &test)| {
                count += 1;
                assert_eq!(out.unwrap(), test)
            });

        assert_eq!(count, expected_output.len());
    }

    #[test]
    fn test_example() {
        let input = r#"This is a test. How cool is this function. Wow I am so impressed.
        This function is the best! Does it matter how long my sentences are?"#;

        let output = summarize(input, 3).unwrap();
        assert_eq!(output,
r#"• This is a test

• How cool is this function

• Wow I am so impressed"#
        );
    }
}
