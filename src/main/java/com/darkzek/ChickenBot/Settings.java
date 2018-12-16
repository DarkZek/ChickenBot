package com.darkzek.ChickenBot;

import java.io.*;
import java.util.Scanner;

/**
 * Created by darkzek on 28/02/18.
 */
public class Settings {

    public static String enabler = ">";
    public static String messagePrefix = "Brawk!\n";
    private static Settings settings = new Settings( );

    private Settings() {}

    /* Static 'instance' method */
    public static Settings getInstance( ) {
        return settings;
    }

    public String getToken() {
        try {
            File file = new File("token.txt");

            Scanner scanner = new Scanner(file);
            String token = scanner.nextLine();
            return token;
        } catch (FileNotFoundException e) {
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
