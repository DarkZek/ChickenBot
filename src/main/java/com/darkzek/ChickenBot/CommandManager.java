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

    List<Trigger> messageTriggers = new ArrayList<>();
    List<Trigger> deletedTriggers = new ArrayList<>();

    private CommandManager() {}

    /* Static 'instance' method */
    public static CommandManager getInstance( ) {
        return commandManager;
    }

    public void AddMessageListener(Trigger trigger) {
        messageTriggers.add(trigger);
    }

    public void AddMessageDeletedListener(Trigger trigger) {
        deletedTriggers.add(trigger);
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
}
