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

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.UserDamageByUserEvent;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.library.waitinggames.games.JumpRun;
import de.timesnake.library.waitinggames.games.MlgWater;
import de.timesnake.library.waitinggames.games.PunchArea;
import de.timesnake.library.waitinggames.games.WaitingGame;

import java.io.File;
import java.util.*;

public class WaitingGameManager {

    public static WaitingGameManager getInstance() {
        return instance;
    }

    private static WaitingGameManager instance;
    private final HashMap<ExWorld, GameFile> gameFilesByWorld = new HashMap<>();
    private final HashMap<ExWorld, List<WaitingGame>> gamesByWorld = new HashMap<>();

    public WaitingGameManager() {
        instance = this;

        for (ExWorld world : Server.getWorlds()) {
            File gameFile = new File(world.getWorldFolder().getAbsolutePath() + File.separator + GameFile.NAME +
                    ".yml");
            if (gameFile.exists()) {
                this.gameFilesByWorld.put(world, new GameFile(world));
            }
        }

        for (Map.Entry<ExWorld, GameFile> entry : this.gameFilesByWorld.entrySet()) {

            GameFile file = entry.getValue();

            List<WaitingGame> games = new LinkedList<>();
            List<Integer> ids = new LinkedList<>();

            for (Integer id : file.getGameIds()) {
                String type = file.getGameType(id);

                try {
                    switch (type.toLowerCase()) {
                        case PunchArea.NAME -> {
                            games.add(new PunchArea(file, id));
                            ids.add(id);
                        }
                        case JumpRun.NAME -> {
                            games.add(new JumpRun(file, id));
                            ids.add(id);
                        }
                        case MlgWater.NAME -> {
                            games.add(new MlgWater(file, id));
                            ids.add(id);
                        }
                    }
                } catch (GameLoadException e) {
                    Server.printWarning(Plugin.WAITING_GAME, e.getMessage());
                }
            }

            this.gamesByWorld.put(entry.getKey(), games);

            Server.printText(Plugin.WAITING_GAME, "Loaded waiting games in world " + entry.getKey().getName() + ": " +
                    Arrays.toString(ids.toArray()));
        }

        Server.printText(Plugin.WAITING_GAME, "Loaded waiting game manager");
    }

    public GameFile getGameFile(ExWorld world) {
        return this.gameFilesByWorld.get(world);
    }

    public boolean onUserDamage(UserDamageByUserEvent e) {
        List<WaitingGame> games = this.gamesByWorld.get(e.getUser().getExWorld());

        if (games == null) {
            return false;
        }

        boolean gameManaged = false;

        for (WaitingGame game : games) {
            gameManaged |= game.onUserDamageByUser(e);
        }

        return gameManaged;
    }
}
