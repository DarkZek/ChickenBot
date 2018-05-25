package com.darkzek.ChickenBot;

import net.dv8tion.jda.core.entities.ChannelType;
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
        for(Trigger trigger : deletedTriggers) {
            trigger.MessageDeleted(event);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        for(Trigger trigger : messageTriggers) {
            trigger.MessageRecieved(event);
        }
    }

    public void onShutdown() {
        for (Trigger trigger : shutdownTriggers) {
            trigger.Shutdown();
        }
    }
}
