package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
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

    public Dankmeme() {
        this.description = "Gets the latest from /r/dankmemes";
        this.name = "Dankmeme";
        this.usage = ">dankmeme";
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.COMMAND), "dankmeme");
        this.trigger.messageType = MessageType.BOTH;
    }

    @Override
    public void MessageRecieved(MessageReceivedEvent event) {
        String link = "";
        int times = 0;
        while (times < 4) {
            link = GetRandomPost(event);
            if (link == null) {
                break;
            }
            times++;
        }

        if (link == "") {
            Reply(Settings.getInstance().prefix + "I couldnt find any memes sorry", event);
            return;
        }


        if (link.endsWith("jpg") || link.endsWith("png")) {
            //its an image
            ReplyImage(link, event);
        } else {
            Reply(link, event);
        }

    }

    public String GetRandomPost(MessageReceivedEvent event) {

        String message = "";
        //Connect to reddit
        try {
            URL url = new URL("https://www.reddit.com/r/dankmeme/random.json");
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
            Reply(Settings.getInstance().prefix + "Sorry I cant connect to reddit right now! Try again later\n```" + e.fillInStackTrace() + "```", event);
            return null;
        }

        //Fix json because reddit dosent like json objects
        message = "{\"data\":" + message + "}";

        JSONObject json = new JSONObject(message);

        String url = json.getJSONArray("data").getJSONObject(0).getJSONObject("data").getJSONArray("children").getJSONObject(0).getJSONObject("data").getString("url");

        return url;
    }
}
