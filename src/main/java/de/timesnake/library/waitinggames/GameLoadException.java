/*
 * library-waiting-game.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
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
