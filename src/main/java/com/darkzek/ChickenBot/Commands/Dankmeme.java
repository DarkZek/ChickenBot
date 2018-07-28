package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Events.CommandRecievedEvent;
import com.darkzek.ChickenBot.RedditPost;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by darkzek on 31/03/18.
 */
public class Dankmeme extends Command {

    public Dankmeme() {
        this.description = "Gets the latest from /r/dankmemes";
        this.name = "Dankmeme";
        this.usage = ">dankmeme";
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.COMMAND), "dankmeme");
        this.trigger.messageType = MessageType.BOTH;
    }

    @Override
    public void MessageRecieved(CommandRecievedEvent event) {
        RedditPost post;

        while (true) {
            post = GetRandomPost(event);
            if (post != null) {
                break;
            }
        }

        Reply(new EmbedBuilder()
                .setTitle(post.title, null)
                .setColor(Color.BLUE)
                .setFooter(post.upvotes + " upvotes", null)
                .setImage(post.imageLink)
                .build(), event);

        event.processed = true;
    }

    public RedditPost GetRandomPost(CommandRecievedEvent event) {

        String message = "";
        //Connect to reddit
        try {
            URL url = new URL("https://www.reddit.com/user/kerdaloo/m/dankmemer/top/.json?sort=top&t=day&limit=100");
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestProperty("User-agent", "Chicken-Bot");
            String line = null;
            StringBuilder tmp = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            while ((line = in.readLine()) != null) {
                tmp.append(line);
            }
            message = tmp.toString();

        } catch (IOException e) {
            Reply(Settings.messagePrefix + "Sorry I cant connect to reddit right now! Try again later\n```" + e.fillInStackTrace() + "```", event);
            return null;
        }

        JSONObject json = new JSONObject(message);

        JSONObject data = json.getJSONObject("data").getJSONArray("children").getJSONObject(new Random().nextInt(100)).getJSONObject("data");

        //Get the data
        int upvotes = data.getInt("score");
        String link = data.getString("url");
        String title = data.getString("title");

        RedditPost post = new RedditPost(upvotes, link, title);

        return post;
    }
}
