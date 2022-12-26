/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.library.waitinggames;

import de.timesnake.library.basic.util.LogHelper;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Plugin extends de.timesnake.basic.bukkit.util.chat.Plugin {

    public static final Plugin WAITING_GAME = new Plugin("Games", "LWG", LogHelper.getLogger("Games", Level.INFO));

    protected Plugin(String name, String code, Logger logger) {
        super(name, code, logger);
    }
}