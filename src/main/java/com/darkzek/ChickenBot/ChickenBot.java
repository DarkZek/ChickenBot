package com.darkzek.ChickenBot;

import com.darkzek.ChickenBot.Commands.CommandLoader;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;

/**
 * Created by darkzek on 21/02/18.
 */
public class ChickenBot extends ListenerAdapter{

    public static JDA jda;

    public static void main(String[] args) {

        //Setup account
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(Settings.getInstance().getToken());
        builder.setAutoReconnect(true);
        builder.setStatus(OnlineStatus.ONLINE);

        //Setup the command manager so it can listen to events
        builder.addEventListener(CommandManager.getInstance());

        //Load all the commands
        CommandLoader.Load();

        //Connect
        try {
            jda = builder.buildBlocking();
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            //Disconnect
            jda.shutdown();
        }));

        if (!runningFromIntelliJ()) {
            TellMe();
        }

        Log("Started Chicken Bot V1");
    }

    public static void TellMe() {
        User darkzek = jda.getUserById("130173614702985216");

        PrivateChannel pc = darkzek.openPrivateChannel().complete();
        System.out.println("Sent message");

        pc.sendMessage("Started ChickenBot with version " + new Version().getVersion()).queue();
    }

    public static void Log(String s) {
        System.out.println(s);
    }

    public static boolean runningFromIntelliJ()
    {
        String classPath = System.getProperty("java.class.path");
        return classPath.contains("idea_rt.jar");
    }
}
