package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.CommandType;
import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.omg.CORBA.Environment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by darkzek on 31/03/18.
 */
public class DONG extends Command {

    String[] dongs;

    public DONG() {
        this.description = "What can you Do Online Now Guys?";
        this.name = "DONG";
        this.type = CommandType.INTERNET;
        this.usage = ">DONG";
        this.trigger = new Trigger(this, TriggerType.COMMAND, "dong");
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
        } catch (IOException e) {
        } catch (NoSuchElementException e) {
        }
        System.out.println("[ERROR] Cannot load DONG's file!");
        System.exit(1);
    }

    @Override
    public void MessageRecieved(MessageReceivedEvent event) {
        String dong = dongs[new Random().nextInt(dongs.length)];
        String[] info = dong.split("-");
        //Get message
        String message = Settings.getInstance().prefix + "<" + info[0] + ">\n```" + info[1].replaceAll("\n", System.getProperty("line.separator")) + "```";

        Reply(message, event);
    }

}
