package main.java.com.darkzek.SheepBot;

import main.java.com.darkzek.SheepBot.Commands.CommandLoader;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
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
        builder.setToken(Settings.getToken());
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


        Log("Started Chicken Bot V1");
    }

    public static void Log(String s) {
        System.out.println(s);
    }
}
