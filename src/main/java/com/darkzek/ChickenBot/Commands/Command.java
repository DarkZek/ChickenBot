package com.darkzek.ChickenBot.Commands;

import com.darkzek.ChickenBot.Enums.CommandType;
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
    String description;
    String name;
    String usage;
    CommandType type;
    Trigger trigger;
    String[] alias;
    boolean showInHelp = true;

    public void MessageRecieved(MessageReceivedEvent event) {
        Log("[ERROR] Unhandled message received event for command " + name);
    }

    public void MessageDeleted(MessageDeleteEvent event) {
        Log("[ERROR] Unhandled message deleted event for command " + name);
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

    public void Reply(String message, MessageReceivedEvent event) {
        Reply(message, event, false);
    }

    public void ReplyImage(InputStream message, MessageReceivedEvent event) {
        ReplyImage(message, event, false);
    }

    public void ReplyImage(String imageUrl, MessageReceivedEvent event) {
        InputStream stream = null;
        try {
            URL url = new URL(imageUrl);
            stream = url.openStream();
            ReplyImage(stream, event);
        } catch (IOException e) {
            Reply(Settings.prefix + "Sorry I cant connect to the website right now! Try again later\n```" + e.fillInStackTrace() + "```", event);
            return;
        }
    }

    public void ReplyImage(InputStream message, MessageReceivedEvent event, boolean deleteMessage) {
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
    public void Reply(String message, MessageReceivedEvent event, boolean deleteMessage) {
        if (event.getChannelType() == ChannelType.PRIVATE) {
            PrivateMessage(message, event.getAuthor());
        } else {
            SendMessage(message, event.getTextChannel());

            //Only in guild chats because you cant delete messages from PM's
            if (deleteMessage) {
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
        if (user.hasPrivateChannel()) {
            user.openPrivateChannel().queue();
        }

        PrivateChannel channel = user.openPrivateChannel().complete();

        channel.sendFile(message, "File.png").queue();
    }
}
