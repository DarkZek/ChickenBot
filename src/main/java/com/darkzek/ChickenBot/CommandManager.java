package com.darkzek.ChickenBot;

import com.darkzek.ChickenBot.Events.CommandRecievedEvent;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.emote.EmoteAddedEvent;
import net.dv8tion.jda.core.events.message.GenericMessageEvent;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.exceptions.PermissionException;
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
    public List<Trigger> emoteTriggers = new ArrayList<>();

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
                try {
                    trigger.MessageDeleted(event);
                } catch (ErrorResponseException e) {

                }
            }
        } catch (Exception e) {
            new ErrorReport().AddField("Name", "Message Deleted Event").
                    AddField("Message ID", event.getMessageId()).
                    AddStacktrace(e).Report(event.getTextChannel());
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
                try {
                    trigger.MessageRecieved(commandRecievedEvent);
                } catch (ErrorResponseException e) {

                }
            }

            //Exempt angry faces
            if (event.getMessage().getContentRaw().startsWith(">") && !event.getMessage().getContentRaw().startsWith(">:") && commandRecievedEvent.processed == false && !event.getAuthor().isBot()) {
                try {
                    unknownCommand(event);
                } catch (ErrorResponseException e) {

                }
            }

        } catch (Exception e) {
            new ErrorReport().AddField("Name", commandRecievedEvent.getCommandName()).
                    AddField("Message", event.getMessage().getContentDisplay()).
                    AddField("User", event.getAuthor().getAsMention()).
                    AddStacktrace(e).
                    Report(event.getTextChannel());
        }
    }

    public void unknownCommand(MessageReceivedEvent event) {
        try {
            event.getChannel().sendMessage(Settings.messagePrefix + "Unknown command!").queue();
        } catch (InsufficientPermissionException e) {

        }
    }

    public void onShutdown() {

        try {
            for(Trigger trigger : shutdownTriggers) {
                trigger.Shutdown();
            }
        } catch (ErrorResponseException e) {
          return;
        } catch (Exception e) {
            new ErrorReport().AddField("Name", "Shutdown Event").AddStacktrace(e).Report();
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        try {
            for(Trigger trigger : emoteTriggers) {
                try {
                    trigger.Emote(event);
                } catch (ErrorResponseException e) {

                }
            }
        } catch (Exception e) {
            new ErrorReport().AddField("Name", "Shutdown Event").AddStacktrace(e).Report();
        }
    }
}
