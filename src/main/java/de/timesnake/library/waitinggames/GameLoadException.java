/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.waitinggames;

public class GameLoadException extends Exception {

    private final String game;
    private final int id;

    public GameLoadException(String game, int id) {
        super("Can not load " + game + " with id " + id);
        this.game = game;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getGame() {
        return game;
    }
}
