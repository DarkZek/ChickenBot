package com.darkzek.ChickenBot;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PresenceMessage {

    private static PresenceMessage _instance;

    private String name = "presences.txt";
    private String[] messages;
    private JDA jda;
    private String currentMessage;

    public static PresenceMessage getInstance(JDA jda) {
        if (_instance == null) {
            _instance = new PresenceMessage();
        }
        _instance.jda = jda;
        _instance.LoadPresences();
        _instance.ScheduleUpdater();

        return _instance;
    }

    private PresenceMessage() {}

    public void UpdatePresence() {
        int guilds = jda.getGuilds().size();

        jda.getPresence().setGame(Game.playing(currentMessage.replaceAll("\\$1", guilds + "")));
    }

    public void NewPresence() {
        int guilds = jda.getGuilds().size();

        String message = messages[new Random().nextInt(messages.length)];
        currentMessage = message;

        jda.getPresence().setGame(Game.playing(message.replaceAll("\\$1", guilds + "")));
    }

    private void LoadPresences() {
        File file = new File(name);
        ArrayList<String> fileList = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                String token = scanner.nextLine();
                fileList.add(token);
            }

            messages = fileList.toArray(new String[fileList.size()]);
            return;
        } catch (IOException e) {
        } catch (NoSuchElementException e) {
        }
        System.out.println("[ERROR] Cannot load " + name + "!");
        System.exit(1);
    }

    private void ScheduleUpdater() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                NewPresence();
            }
        }, 3600000, 3600000);
    }
}
