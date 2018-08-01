package com.darkzek.ChickenBot;

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class ErrorReport {
    private ArrayList<Map.Entry<String, String>> objects = new ArrayList();

    public ErrorReport AddField(String name, String content) {
        objects.add(new AbstractMap.SimpleEntry(name, content));
        return this;
    }

    public ErrorReport AddStacktrace(Exception e) {
        StackTraceElement[] stackTrace = e.getStackTrace();

        String trace = "```";

        for (StackTraceElement element : stackTrace) {
            trace += "\n" + element;
        }

        trace += "```";

        objects.add(new AbstractMap.SimpleEntry("StackTrace", trace));

        return this;
    }

    private String FileReport() {
        String msg = "Hey man, we just blew a fuse! This message has been reported to DarkZek#8647\n";

        for (Map.Entry<String, String> entry : objects) {
            msg += entry.getKey() + ": " + entry.getValue() + "\n";
        }

        //Save error to file
        File file = new File("errors/");
        file.mkdirs();
        file = new File("errors/" + System.currentTimeMillis() + ".log");

        //Write to file
        try {
            Files.write(file.toPath(), msg.getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            //Continue reporting!
        }

        //Print to log
        System.out.println(msg);

        //Make sure its short enough to send to discord
        if (msg.length() > 2000) {
            msg = msg.substring(0, 1996) + "```...";
        }

        //Send message on discord
        ChickenBot.TellMe(msg);

        return msg;
    }

    public void Report() {
        FileReport();
    }

    public void Report(TextChannel channel) {
        String msg = FileReport();
        try {
            channel.sendMessage(msg).queue();
        } catch (PermissionException e) {

        }
    }
}
