package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.CommandType;
import com.darkzek.ChickenBot.Events.CommandRecievedEvent;
import com.darkzek.ChickenBot.Settings;
import com.darkzek.ChickenBot.Trigger;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by darkzek on 28/02/18.
 */
public class Command {
    public String description;
    public String name;
    public String usage;
    public CommandType type;
    public Trigger trigger;
    public String[] alias;
    public boolean showInHelp = true;

    public void MessageRecieved(CommandRecievedEvent event) {
        Log("[ERROR] Unhandled message received event for command " + name);
    }

    public void MessageDeleted(MessageDeleteEvent event) {
        Log("[ERROR] Unhandled message deleted event for command " + name);
    }

    public void OnShutdown() {

    }

    private static void Log(String s) {
        System.out.println(s);
    }

    public void SendMessage(String msg, MessageChannel channel) {
        channel.sendMessage(msg).queue();
    }

    public void SendMessageImage(InputStream msg, MessageChannel channel) {
        try {
            channel.sendFile(msg, "File.png").queue();
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
        }
    }

    public void Reply(String message, CommandRecievedEvent event) {
        event.processed = true;
        if (message.length() > 2000) {
            message = message.substring(0, 1993) + "```...";
        }

        Reply(message, event, false);
    }

    public void ReplyImage(InputStream message, CommandRecievedEvent event) {
        event.processed = true;
        ReplyImage(message, event, false);
    }

    public void ReplyImage(String imageUrl, CommandRecievedEvent event) {
        event.processed = true;
        InputStream stream = null;
        try {
            URL url = new URL(imageUrl);
            stream = url.openStream();
            ReplyImage(stream, event);
        } catch (IOException e) {
            Reply(Settings.getInstance().prefix + "Sorry I cant connect to the website right now! Try again later\n```" + e.fillInStackTrace() + "```", event);
            return;
        }
    }

    public void ReplyImage(InputStream message, CommandRecievedEvent event, boolean deleteMessage) {
        event.processed = true;
        if (event.getChannelType() == ChannelType.PRIVATE) {
            PrivateMessageImage(message, event.getAuthor());
        } else {
            SendMessageImage(message, event.getTextChannel());

            //Only in guild chats because you cant delete messages from PM's
            if (deleteMessage) {
                event.getMessage().delete().queue();
            }
        }
    }
    public void Reply(String message, CommandRecievedEvent event, boolean deleteMessage) {
        event.processed = true;
        if (event.getChannelType() == ChannelType.PRIVATE) {
            PrivateMessage(message, event.getAuthor());
        } else {
            SendMessage(message, event.getTextChannel());

            //Only in guild chats because you cant delete messages from PM's
            if (deleteMessage && event.getChannelType() == ChannelType.TEXT) {
                event.getMessage().delete().queue();
            }
        }
    }

    public void PrivateMessage(String message, User user) {
        if (user.hasPrivateChannel()) {
            user.openPrivateChannel().queue();
        }

        PrivateChannel channel = user.openPrivateChannel().complete();

        channel.sendMessage(message).queue();
    }
    public void PrivateMessageImage(InputStream message, User user) {
        if (user.hasPrivateChannel() && user != user.getJDA().getSelfUser()) {
            user.openPrivateChannel().queue();
        }

        PrivateChannel channel = user.openPrivateChannel().complete();

        channel.sendFile(message, "File.png").queue();
    }


}
