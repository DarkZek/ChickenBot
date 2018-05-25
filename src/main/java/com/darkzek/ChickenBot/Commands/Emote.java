package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

public class Emote extends Command {

    HashMap<String,String> emotes = new HashMap<>();

    public Emote() {
        this.description = "Shares an emoji with the world";
        this.name = "Emote";
        this.usage = ">emote <emote_name>";
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.COMMAND, TriggerType.BOT_SHUTDOWN), "emote");
        this.trigger.messageType = MessageType.BOTH;

        //Load emotes
        LoadEmotes();
    }

    @Override
    public void MessageRecieved(MessageReceivedEvent event) {

        String message = event.getMessage().getContentDisplay();

        //Get between all the spaces, to repmove the command bit
        String[] args = message.split(" ");

        //Check if they want to set a new emote
        if (args.length > 2 && args[1].equalsIgnoreCase("set")){
            SetEmote(args, event);
            return;
        }

        if (args.length != 2) {
            Reply(Settings.getInstance().prefix + "You forgot to add the message to emote!\nType `>help emote` for more information", event);
            return;
        }

        String emoteName = args[1].toLowerCase();

        if (!emotes.containsKey(emoteName)){
            Reply(Settings.getInstance().prefix + "There is no emoji named `" + emoteName + "`!", event);
            return;
        }

        String link = emotes.get(emoteName);

        Reply(link, event);
    }

    void SetEmote(String[] message, MessageReceivedEvent event) {

        if (message.length != 4) {
            Reply(Settings.getInstance().prefix + "Usage: `>bigtext set <Emote_Name> <Link>`", event);
            return;
        }

        String emoteName = message[2].toLowerCase();

        if (emoteName == "") {
            Reply(Settings.getInstance().prefix + "You must put in an emoji name!", event);
            return;
        }

        //Remove any other ones with that name
        emotes.remove(emoteName);

        emotes.put(emoteName, message[3]);

        Reply(Settings.getInstance().prefix + "Set the emote!", event);
    }

    @Override
    public void OnShutdown() {
        SaveEmotes();
    }

    void SaveEmotes() {

        try {
            File emotesFile = new File("emotes.txt");

            FileOutputStream f = new FileOutputStream(emotesFile);
            ObjectOutputStream s = new ObjectOutputStream(f);
            s.writeObject(emotes);
            s.flush();
        } catch (IOException e) {

        }
    }

    void LoadEmotes() {
        File emotesFile = new File("emotes.txt");

        try {
            FileInputStream f = new FileInputStream(emotesFile);
            ObjectInputStream s = new ObjectInputStream(f);
            emotes = (HashMap<String,String>)s.readObject();
            s.close();

            return;
        }catch (IOException e) {

        }catch (ClassNotFoundException e) {

        }
        System.out.println("Cannot load file emotes.txt");
    }
}
