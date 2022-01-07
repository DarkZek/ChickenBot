package com.darkzek.ChickenBot;

public class RedditPost {
    public int upvotes = 0;
    public String imageLink;
    public String title;

    public RedditPost(int upvotes, String imageLink, String title) {
        this.upvotes = upvotes;
        this.imageLink = imageLink;
        this.title = title;
    }
}
