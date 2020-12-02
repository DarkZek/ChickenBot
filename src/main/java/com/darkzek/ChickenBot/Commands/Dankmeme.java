package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.GlobalEmote;
import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Events.CommandRecievedEvent;
import com.darkzek.ChickenBot.RedditPost;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Created by darkzek on 31/03/18.
 */
public class Dankmeme extends Command {

    public static Queue<RedditPost> bufferRedditPost = new LinkedList();

    public Dankmeme() {
        this.description = "Gets the latest from /r/dankmemes";
        this.name = "Dankmeme";
        this.usage = ">dankmeme";
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.COMMAND), "dankmeme");
        this.trigger.messageType = MessageType.BOTH;

        //Fill buffer
        RefreshBuffer();
    }

    @Override
    public void MessageRecieved(CommandRecievedEvent event) {
        RedditPost post;

        while (true) {
            post = getMeme();
            if (post != null) {
                break;
            }
        }

        if (event.getChannelType() == ChannelType.PRIVATE) {
            DirectSendMeme(post, event.getAuthor());
        } else {
            SendMeme(post, event.getTextChannel());
        }
        event.processed = true;
    }

    public static void SendMeme(RedditPost post, TextChannel channel) {
        MessageEmbed builder = new EmbedBuilder()
                .setTitle(post.title, null)
                .setColor(Color.BLUE)
                .setFooter(post.upvotes + " upvotes | React :call_me: for more", null)
                .setImage(post.imageLink)
                .build();

        Message message = channel.sendMessage(builder).complete();

        if (message != null) {
            message.addReaction(GlobalEmote.CALL_ME.toString()).queue();
        }
    }

    public static void DirectSendMeme(RedditPost post, User sender) {
        MessageEmbed builder = new EmbedBuilder()
                .setTitle(post.title, null)
                .setColor(Color.BLUE)
                .setFooter(post.upvotes + " upvotes | React :call_me: for more", null)
                .setImage(post.imageLink)
                .build();

        Message message = sender.openPrivateChannel().complete().sendMessage(builder).complete();

        if (message != null) {
            message.addReaction(GlobalEmote.CALL_ME.toString()).queue();
        }
    }

    public static RedditPost getMeme() {

        if (bufferRedditPost.size() <= 5) {
            //Load new meme
            Thread thread = new Thread(() -> RefreshBuffer());

            thread.start();
        }

        if (bufferRedditPost == null) {
            return new RedditPost(0, "", "Error getting reddit post");
        } else {
            return bufferRedditPost.remove();
        }
    }

    public static void RefreshBuffer() {
        String message = "";
        //Connect to reddit
        try {
            URL url = new URL("https://www.reddit.com/user/kerdaloo/m/dankmemer/top/.json?sort=top&t=day&limit=1000");
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
            return;
        }

        JSONObject json = new JSONObject(message);

        for (int i = 0; i < 15; i++) {
            JSONObject data = json.getJSONObject("data").getJSONArray("children").getJSONObject(new Random().nextInt(100)).getJSONObject("data");

            //Get the data
            int upvotes = data.getInt("score");
            String link = data.getString("url");
            String title = data.getString("title");

            RedditPost post = new RedditPost(upvotes, link, title);

            bufferRedditPost.add(post);
        }
    }
}
