package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.GlobalEmote;
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
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.channel.text.GenericTextChannelEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.JSONObject;
import org.w3c.dom.Text;

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

    public static RedditPost bufferRedditPost;

    public Dankmeme() {
        this.description = "Gets the latest from /r/dankmemes";
        this.name = "Dankmeme";
        this.usage = ">dankmeme";
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.COMMAND), "dankmeme");
        this.trigger.messageType = MessageType.BOTH;

        //Fill buffer
        Dankmeme.bufferRedditPost = Dankmeme.GetRandomPost();
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

        SendMeme(post, event.getTextChannel());
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

    public static RedditPost getMeme() {

        //Get new reddit post
        Thread thread = new Thread(() -> Dankmeme.bufferRedditPost = Dankmeme.GetRandomPost());

        thread.start();
        if (bufferRedditPost == null) {
            return GetRandomPost();
        } else {
            return bufferRedditPost;
        }
    }

    public static RedditPost GetRandomPost() {

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
            return new RedditPost(0, "https://png.pngtree.com/element_origin_min_pic/16/09/30/1357edfe28ef21f.jpg", "Sorry I cant connect to reddit right now! Try again later");
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
