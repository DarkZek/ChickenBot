package com.darkzek.ChickenBot.Events;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;

public class CommandRecievedEvent extends MessageReceivedEvent {

    private String[] args;

    private String commandName;

    public boolean processed = false;

    public CommandRecievedEvent(MessageReceivedEvent event) {
        super(event.getJDA(), event.getResponseNumber(), event.getMessage());

        args = LoadArgs(event.getMessage().getContentRaw());

        commandName = event.getMessage().getContentRaw().split(" ")[0];

        if (commandName.length() > 0) {
            commandName = commandName.substring(1);
        }
    }

    private String[] LoadArgs(String args) {
        String[] arguments = args.split(" ");

        return Arrays.copyOfRange(arguments, 1, arguments.length);
    }

    public String[] getArgs() {
        return args;
    }

    public String getCommandName() {
        return commandName;
    }

}
