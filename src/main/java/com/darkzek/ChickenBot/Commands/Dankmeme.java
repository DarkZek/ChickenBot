package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import jdk.internal.util.xml.impl.Input;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.JSONObject;

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

    String[] memeSubreddits = new String[]{"prequelmemes", "dankmemes", "memes", "WholesomeMemes", "MemesIRL"};

    public Dankmeme() {
        this.description = "Gets the latest from dank memes";
        this.name = "Dankmeme";
        this.usage = ">dankmeme";
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.COMMAND), "dankmeme");
        this.trigger.messageType = MessageType.BOTH;
    }

    @Override
    public void MessageRecieved(MessageReceivedEvent event) {
        String link = "";
        int times = 0;
        while (true) {
            link = GetRandomPost(event);
            if (link != null) {
                break;
            }
        }

        Reply(link, event);

    }

    public String GetRandomPost(MessageReceivedEvent event) {

        String message = "";
        //Connect to reddit
        try {
            //Get subreddit from list
            String subreddit = memeSubreddits[new Random().nextInt(memeSubreddits.length)];

            //Ping reddit to get the memes
            URL url = new URL("https://old.reddit.com/r/" + subreddit + "/top.json");
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestProperty("User-agent", "Chicken-Bot");

            //read the output
            String line = null;
            StringBuilder tmp = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            while ((line = in.readLine()) != null) {
                tmp.append(line);
            }

            message = tmp.toString();

        } catch (IOException e) {
            Reply(Settings.getInstance().prefix + "Sorry I cant connect to reddit right now! Try again later\n```" + e.fillInStackTrace() + "```", event);
            return null;
        }

        //Convert it to a json object to easily read it
        JSONObject json = new JSONObject(message);

        JSONObject data = null;

        //Loop until we find media
        for (int i = 0; i < 10; i++) {
            data = json.getJSONObject("data").getJSONArray("children").getJSONObject(i).getJSONObject("data");

            if (!data.isNull("media")) {
                //Its media!
                break;
            }
        }

        String url = data.getString("url");

        return url;
    }
}
