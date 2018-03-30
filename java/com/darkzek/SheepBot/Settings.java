package main.java.com.darkzek.SheepBot;

import javafx.application.Application;

import java.io.*;
import java.util.Scanner;

/**
 * Created by darkzek on 28/02/18.
 */
public class Settings {

    //TODO: Convert to singleton

    public static String enabler = ">";
    public static String prefix = "Brawk!\n";

    public static String getToken() {
        try {
            File file = new File("token.txt");

            Scanner scanner = new Scanner(file);
            String token = scanner.nextLine();
            return token;
        } catch (FileNotFoundException e) {
            NoToken();
        } catch (IOException e) {
            NoToken();
        }
        return "";
    }

    static void NoToken() {
        String path = new File("token.txt").getAbsolutePath();
        System.out.println("[ERROR] Cannot read token file " + path);
        System.exit(1);
    }
}
