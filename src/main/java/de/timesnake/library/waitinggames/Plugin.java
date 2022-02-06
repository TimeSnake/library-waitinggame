package de.timesnake.library.waitinggames;

public class Plugin extends de.timesnake.basic.bukkit.util.chat.Plugin {

    public static final Plugin WAITING_GAME = new Plugin("Games", "LWG");

    protected Plugin(String name, String code) {
        super(name, code);
    }
}