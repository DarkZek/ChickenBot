package com.darkzek.ChickenBot;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.GenericMessageEvent;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by darkzek on 28/02/18.
 */
public class CommandManager extends ListenerAdapter {

    private static CommandManager commandManager = new CommandManager( );

    public List<Trigger> messageTriggers = new ArrayList<>();
    public List<Trigger> deletedTriggers = new ArrayList<>();
    public List<Trigger> shutdownTriggers = new ArrayList<>();

    private CommandManager() {}

    /* Static 'instance' method */
    public static CommandManager getInstance( ) {
        return commandManager;
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event)
    {
        try {
            for(Trigger trigger : deletedTriggers) {
                trigger.MessageDeleted(event);
            }
        } catch (Exception e) {
            ShowError("Message Delete Event", "Message Id - " + event.getMessageId() , e);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        try {
            for(Trigger trigger : messageTriggers) {
                trigger.MessageRecieved(event);
            }
        } catch (Exception e) {
            ShowError("Message Recieved Event", event.getMessage().getContentDisplay(), e);
        }

    }

    public void onShutdown() {
        try {
            for(Trigger trigger : messageTriggers) {
                trigger.Shutdown();
            }
        } catch (Exception e) {
            ShowError("Shutdown Event", "None", e);
        }
    }

    public void ShowError(String name, String message, Exception e) {
        StackTraceElement[] stackTrace = e.getStackTrace();

        String trace = "";

        for (StackTraceElement element : stackTrace) {
            trace += element + "\n";
        }

        ChickenBot.TellMe("Hey man, we just blew a fuse!" +
                "\nName: `" + name + "`" +
                "\nMessage: `" + message + "`" +
                "\nError: `" + e.getClass().getCanonicalName() + "`" +
                "\nStacktrace:```" + trace + "```");
    }
}
