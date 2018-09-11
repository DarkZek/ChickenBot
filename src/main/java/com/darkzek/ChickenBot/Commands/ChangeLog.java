package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.CommandType;
import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Events.CommandRecievedEvent;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import com.darkzek.ChickenBot.Version;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Scanner;

public class ChangeLog extends Command {
    private String address = "https://api.github.com/repos/DarkZek/ChickenBot/commits";
    private String changes;

    public ChangeLog() {
        this.description = "Shows the latest changes for chicken bot";
        this.name = "ChangeLog";
        this.type = CommandType.INTERNET;
        this.usage = ">changelog";
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.COMMAND), "changelog");
        this.trigger.SetIgnoreCase(true);
        this.trigger.messageType = MessageType.BOTH;

        changes = "Chicken Bot V" + Version.getVersion() + "```";

        UpdateChangelog();
    }

    private void UpdateChangelog() {
        //Get latest changelog from Github
        try {
            URLConnection url = new URL(address).openConnection();

            Scanner scanner = new Scanner(url.getInputStream());
            scanner.useDelimiter("\\Z");


            //Parse JSON
            JSONArray data = new JSONArray(scanner.nextLine());
            JSONObject latestCommit = data.getJSONObject(0).getJSONObject("commit");
            changes += latestCommit.getString("message");

        } catch (IOException e) {
            System.out.println("Error loading changelog from GitHub. Retrying in 10 seconds..");

            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            UpdateChangelog();
                        }
                    }, 10000 );
        }
    }

    @Override
    public void MessageRecieved(CommandRecievedEvent event) {
        Reply(Settings.messagePrefix + changes + "```", event);
    }
}
