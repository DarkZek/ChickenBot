package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Set;

public class AutoTLDR extends Command {

    public AutoTLDR() {
        this.description = "Automatically makes a short TLDR; of links";
        this.name = "AutoTLDR";
        this.showInHelp = true;
        this.usage = ">tldr <LINK>";
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.COMMAND), "tldr");
    }

    @Override
    public void MessageRecieved(MessageReceivedEvent event) {

        Reply("Embeds", event);

        for (MessageEmbed embed:event.getMessage().getEmbeds()) {
            Reply(embed.getTitle(), event);
        }

        String article = "";

        try {
            //Get info
            URL url = new URL(event.getMessage().getContentRaw().split(" ")[1]);

            URLConnection urlConn = url.openConnection();

            urlConn.connect();

            InputStream out = urlConn.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(out));

            String line = "";
            while ((line = br.readLine()) != "") {
                article += line + "\n";
            }

            out.close();
        } catch (IOException e) {
            Reply(Settings.getInstance().prefix + "That didnt work! Please try again later", event);
            return;
        }

        Reply(article, event);

    }
}
