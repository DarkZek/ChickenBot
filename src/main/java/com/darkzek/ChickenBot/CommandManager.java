package com.darkzek.ChickenBot;

import com.darkzek.ChickenBot.Events.CommandRecievedEvent;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.GenericMessageEvent;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
            new ErrorReport().AddField("Name", "Message Deleted Event").
                    AddField("Message ID", event.getMessageId()).
                    AddStacktrace(e).Report();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor() == event.getJDA().getSelfUser()) {
            return;
        }

        CommandRecievedEvent commandRecievedEvent = new CommandRecievedEvent(event);
        try {
            for(Trigger trigger : messageTriggers) {
                trigger.MessageRecieved(commandRecievedEvent);
            }

            //Exempt angry faces
            if (event.getMessage().getContentRaw().startsWith(">") && !event.getMessage().getContentRaw().startsWith(">:") && commandRecievedEvent.processed == false && !event.getAuthor().isBot()) {
                unknownCommand(event);
            }

        } catch (Exception e) {
            new ErrorReport().AddField("Name", "Message Recieved Event").
                    AddField("Message", event.getMessage().getContentDisplay()).
                    AddStacktrace(e).Report();
        }
    }

    public void unknownCommand(MessageReceivedEvent event) {
        event.getChannel().sendMessage(Settings.getInstance().prefix + "Unknown command!").queue();
    }

    public void onShutdown() {

        try {
            for(Trigger trigger : shutdownTriggers) {
                trigger.Shutdown();
            }
        } catch (Exception e) {
            new ErrorReport().AddField("Name", "Shutdown Event").AddStacktrace(e).Report();
        }
    }
}
