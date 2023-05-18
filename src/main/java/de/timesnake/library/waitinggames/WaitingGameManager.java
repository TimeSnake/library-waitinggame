/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.waitinggames;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.UserDamageByUserEvent;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.library.basic.util.Loggers;
import de.timesnake.library.waitinggames.games.JumpRun;
import de.timesnake.library.waitinggames.games.MlgWater;
import de.timesnake.library.waitinggames.games.PunchArea;
import de.timesnake.library.waitinggames.games.WaitingGame;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
      File gameFile = new File(
          world.getWorldFolder().getAbsolutePath() + File.separator + GameFile.NAME +
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
          Loggers.GAME.warning(e.getMessage());
        }
      }

      this.gamesByWorld.put(entry.getKey(), games);

      Loggers.GAME.info("Loaded waiting games in world " + entry.getKey().getName() + ": " +
          Arrays.toString(ids.toArray()));
    }

    Loggers.GAME.info("Loaded waiting game manager");
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
