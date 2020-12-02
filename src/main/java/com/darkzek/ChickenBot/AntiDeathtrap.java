package com.darkzek.ChickenBot;

import net.dv8tion.jda.api.entities.Guild;

/**
 * This is designed to detect so called bot deathtraps and leave them
 */

public class AntiDeathtrap {
    public static boolean checkGuild(Guild guild) {
        System.out.println(guild.getName() + " - " + guild.getMembers().size());
        return false;
    }
}
