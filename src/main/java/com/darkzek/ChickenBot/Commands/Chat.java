package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.CommandType;
import com.darkzek.ChickenBot.Enums.MessageType;
import com.darkzek.ChickenBot.Enums.TriggerType;
import com.darkzek.ChickenBot.Events.CommandRecievedEvent;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

import java.util.Arrays;
import java.util.Random;
import java.util.regex.Pattern;

public class Chat extends Command {

    private ChatterBotSession bot1session;

    public Chat() {
        this.description = "Chats with the users";
        this.name = "Chat";
        this.type = CommandType.FUN;
        this.usage = "N/A";
        this.showInHelp = false;
        this.trigger = new Trigger(this, Arrays.asList(TriggerType.MESSAGE_SENT), "");
        this.trigger.SetIgnoreCase(true);
        this.trigger.IncludeBots(false);
        this.trigger.messageType = MessageType.BOTH;

        SetupBot();
    }

    private void SetupBot() {
        try {
            ChatterBotFactory factory = new ChatterBotFactory();

            ChatterBot bot1 = factory.create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477");
            bot1session = bot1.createSession();
        } catch (Exception e) {
            System.out.println("[ERROR] Creating cleverbot chat bot");
        }
    }

    @Override
    public void MessageRecieved(CommandRecievedEvent event) {

        if (event.getMessage().mentionsEveryone()) {
            return;
        }

        if (!event.getMessage().isMentioned(event.getJDA().getSelfUser())) {
            //If they use the word chicken it has a 1/10 chance regardless
            if (!(event.getMessage().getContentDisplay().toLowerCase().contains("chicken")
                    && new Random().nextInt(10) == 1)) {
                return;
            }
        }

        String message = event.getMessage().getContentDisplay().replaceAll("@" + event.getJDA().getSelfUser().getName(), "");

        String response = "";
        try {
            response = bot1session.think(message);
        } catch (Exception e) {
            response = "Error coming up with witty response!```" + e.fillInStackTrace() + "```";
        }

        //Remove HTML tags
        response = Pattern.compile("<.+?>").matcher(response).replaceAll("");

        Reply(Settings.messagePrefix + response, event);
    }

}
