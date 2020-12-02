package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.AntiDeathtrap;
import com.darkzek.ChickenBot.Enums.CommandType;
import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Events.CommandRecievedEvent;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.api.entities.Guild;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by darkzek on 31/03/18.
 */
public class DONG extends Command {

    String[] dongs;

    public DONG() {
        this.description = "What can you Do Online Now Guys?";
        this.name = "DONG";
        this.type = CommandType.INTERNET;
        this.usage = ">dong";
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.COMMAND), "dong");
        this.trigger.SetIgnoreCase(true);
        this.trigger.messageType = MessageType.BOTH;

        LoadDongs();
    }

    public void LoadDongs() {
        File dongs = new File("DONG.txt");
        ArrayList<String> dongsList = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(dongs);
            while (scanner.hasNext()) {
                String token = scanner.nextLine();
                dongsList.add(token);
            }

            this.dongs = dongsList.toArray(new String[dongsList.size()]);

            return;
        } catch (IOException | NoSuchElementException ignored) {}
        System.out.println("[ERROR] Cannot load DONG's file at !");
        System.exit(1);
    }

    @Override
    public void MessageRecieved(CommandRecievedEvent event) {
        String dong = dongs[new Random().nextInt(dongs.length)];
        String[] info = dong.split("-");
        //Get message
        String message = Settings.messagePrefix + "<" + info[0] + ">\n```" + info[1].replaceAll("\n", System.getProperty("line.separator")) + "```";

        Reply(message, event);

        event.processed = true;
    }

}
