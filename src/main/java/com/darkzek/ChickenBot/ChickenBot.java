package com.darkzek.ChickenBot;

import com.darkzek.ChickenBot.Commands.CommandLoader;
import com.darkzek.ChickenBot.Configuration.GuildConfigurationManager;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by darkzek on 21/02/18.
 */
public class ChickenBot extends ListenerAdapter {

    //TODO: Move guild commands system to the new configuration api
    //TODO: Add better messaging system - like a language file
    //TODO: Fix rats nest of a trigger system

    public static JDA jda;
    public static boolean debug = false;

    public static void main(String[] args) {

        for (String arg:args ) {
            if (arg.equalsIgnoreCase("--debug")) {
                debug = true;
            }
        }

        //Setup logging
        if (!runningFromIntelliJ()) {
            try {
                PrintStream out = new PrintStream(new FileOutputStream("latest.txt"));
                System.setOut(out);
                System.setErr(out);
            } catch (IOException e) {

            }
        }

        //Load configs
        GuildConfigurationManager.getInstance();

        //Connect
        try {
            //Setup account
            jda = JDABuilder.createDefault(Settings.getInstance().getToken())
                    .setAutoReconnect(true)
                    .addEventListeners(CommandManager.getInstance())
                    .build();

            //Load all the commands
            CommandLoader.Load();

            jda.awaitReady();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CommandManager.getInstance().onShutdown();
            jda.shutdown();
        }));

        if (!runningFromIntelliJ()) {
            TellMe("Started ChickenBot with version " + Version.getVersion());
        }

        //Add the events listener to listen for stuff
        jda.addEventListener(new EventsListener());

        PresenceMessage.getInstance(jda).NewPresence();

        System.out.println("Started Chicken Bot V" + Version.getVersion());

    }

    public static void TellMe(String message) {
        User darkzek = jda.getUserById("130173614702985216");

        if (darkzek != null) {
            PrivateChannel pc = darkzek.openPrivateChannel().complete();
            if (message.length() > 2000) {
                message = message.substring(0, 1993) + "```...";
            }

            pc.sendMessage(message).queue();
        }

    }

    public static boolean runningFromIntelliJ()
    {
        return debug;
    }
}
