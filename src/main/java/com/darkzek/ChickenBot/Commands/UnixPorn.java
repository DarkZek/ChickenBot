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
import java.util.Random;

/**
 * Created by darkzek on 28/02/18.
 */
public class UnixPorn extends Command {

    public UnixPorn() {
        this.description = "Gets the latest from /r/unixporn";
        this.name = "UnixPorn";
        this.usage = ">unixporn";
        this.trigger = new Trigger(this, TriggerType.MESSAGE_SENT_CONTAINS, "unixporn");
        this.trigger.messageType = MessageType.BOTH;
    }

    @Override
    public void MessageRecieved(MessageReceivedEvent event) {
        String image = "";
        int times = 0;
        while (times < 4) {
            image = GetRandomPost(event);
            if (image == null) {
                break;
            }

            if (image.endsWith("jpg") || image.endsWith("png")) {
                break;
            }
            times++;
        }

        if (image == "") {
            Reply(Settings.prefix + "I couldnt find any images sorry", event);
            return;
        }

        ReplyImage(image, event);
    }

    public String GetRandomPost(MessageReceivedEvent event) {

        String message = "";
        //Connect to reddit
        try {
            URL url = new URL("https://www.reddit.com/r/unixporn/.json");
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
            Reply(Settings.prefix + "Sorry I cant connect to reddit right now! Try again later\n```" + e.fillInStackTrace() + "```", event);
            return null;
        }

        JSONObject json = new JSONObject(message);

        int postNumber = new Random().nextInt(10);

        String url = json.getJSONObject("data").getJSONArray("children").getJSONObject(postNumber).getJSONObject("data").getString("url");

        return url;
    }
}
