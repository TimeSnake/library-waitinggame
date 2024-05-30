/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.waitinggames.games;

import com.google.gson.Gson;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserDamageByUserEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDamageEvent;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.library.basic.util.GsonFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class WaitingGameManagerBasis<G extends WaitingGame> {

  public static final String FILE_PREFIX = "waiting_game_";

  protected final Logger logger;

  protected final WaitingGame.Type<G> type;
  protected final Map<ExWorld, Set<G>> gamesByWorld = new ConcurrentHashMap<>();

  public WaitingGameManagerBasis(WaitingGame.Type<G> type) {
    this.type = type;
    this.logger = LogManager.getLogger("waiting_game." + type.getName() + ".manager");
  }

  public WaitingGame.Type<G> getType() {
    return type;
  }

  public int loadGamesOfWorld(ExWorld world) {
    File gameFile = new File(this.getFilePath(world));
    if (gameFile.exists()) {
      GsonFile file = this.newGsonFile(world);

      int loadedGames = 0;
      List<G> games = file.readList(this.type.getGameClass());

      for (G game : games) {
        this.addGame(game, world);
        loadedGames++;
      }

      this.logger.info("Loaded {} waiting games in world '{}': {}", type.getName(), world.getName(), loadedGames);

      return loadedGames;
    }
    return 0;
  }

  public void saveGamesOfWorld(ExWorld world) {
    Set<G> games = this.gamesByWorld.remove(world);
    if (!games.isEmpty()) {
      this.newGsonFile(world).write(games);
      this.logger.info("Saved {} waiting games in world '{}'", type.getName(), world.getName());
    }
  }

  public void saveAllGames() {
    for (ExWorld world : this.gamesByWorld.keySet()) {
      this.saveGamesOfWorld(world);
    }
  }

  public void addGame(G game, ExWorld world) {
    game.world = world;
    game.init();
    this.gamesByWorld.computeIfAbsent(world, k -> new HashSet<>()).add(game);
  }

  protected Optional<G> getGameOfUser(User user) {
    return this.gamesByWorld.getOrDefault(user.getExWorld(), new HashSet<>()).stream()
        .filter(g -> g.containsUser(user))
        .findFirst();
  }

  protected boolean isUserPlaying(User user) {
    return this.gamesByWorld.getOrDefault(user.getExWorld(), new HashSet<>()).stream().noneMatch(g -> g.containsUser(user));
  }

  protected void removeUserFromAllGames(User user) {
    for (Set<G> games : this.gamesByWorld.values()) {
      for (G game : games) {
        game.removeUser(user, this.restoreInventory());
      }
    }
  }

  protected boolean restoreInventory() {
    return true;
  }

  private GsonFile newGsonFile(ExWorld world) {
    return new GsonFile(new File(this.getFilePath(world)), this.newGson());
  }

  private Gson newGson() {
    return Server.getDefaultGsonBuilder().create();
  }

  private String getFilePath(ExWorld world) {
    return world.getWorldFolder().getAbsolutePath() + File.separator + FILE_PREFIX + type.getName() + ".json";
  }

  public boolean onUserDamageByUser(UserDamageByUserEvent e) {
    return false;
  }

  public boolean onUserDamage(UserDamageEvent e) {
    return false;
  }
}
