package com.darkzek.ChickenBot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by darkzek on 1/04/18.
 */
public class Reactions {

    public String[] whatAreYouDoing;

    private static Reactions reactions = new Reactions();

    private Reactions() {LoadReactions();}

    /* Static 'instance' method */
    public static Reactions getInstance( ) {
        return reactions;
    }

    public void LoadReactions() {
        whatAreYouDoing = LoadFileArray("reactions/whatareyoudoing.txt");
    }

    public static String GetRandom(String[] input) {
        return input[new Random().nextInt(input.length)];
    }

    String[] LoadFileArray(String name) {
        File file = new File(name);
        ArrayList<String> fileList = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                String token = scanner.nextLine();
                fileList.add(token);
            }

            return fileList.toArray(new String[fileList.size()]);
        } catch (IOException e) {
        } catch (NoSuchElementException e) {
        }
        System.out.println("[ERROR] Cannot load " + name + "!");
        System.exit(1);
        return null;
    }
}
