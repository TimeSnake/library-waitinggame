/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.waitinggames;

import de.timesnake.basic.bukkit.core.main.BasicBukkit;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.UserDamageByUserEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDamageEvent;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.ExWorldLoadEvent;
import de.timesnake.basic.bukkit.util.world.ExWorldUnloadEvent;
import de.timesnake.basic.bukkit.util.world.WorldManager;
import de.timesnake.library.waitinggames.games.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;

public class WaitingGameManager implements Listener {

  private final Logger logger = LogManager.getLogger("waiting-game.manager");

  private final HashMap<WaitingGame.Type<?>, WaitingGameManagerBasis<?>> gameManagerByType = new HashMap<>();

  public WaitingGameManager() {
    JumpRunManager jumpRunManager = new JumpRunManager();
    this.gameManagerByType.put(jumpRunManager.getType(), jumpRunManager);

    PunchAreaManager punchAreaManager = new PunchAreaManager();
    this.gameManagerByType.put(punchAreaManager.getType(), punchAreaManager);

    BuildAreaManager buildAreaManager = new BuildAreaManager();
    this.gameManagerByType.put(buildAreaManager.getType(), buildAreaManager);

    MlgWaterManager mlgWaterManager = new MlgWaterManager();
    this.gameManagerByType.put(mlgWaterManager.getType(), mlgWaterManager);

    for (ExWorld world : Server.getWorlds()) {
      this.loadGamesOfWorld(world);
    }

    Server.registerListener(this, BasicBukkit.getPlugin());
  }

  public void onDisable() {
    for (WaitingGameManagerBasis<?> manager : this.gameManagerByType.values()) {
      manager.saveAllGames();
    }
  }

  public <G extends WaitingGame> WaitingGameManagerBasis<G> getGameManager(WaitingGame.Type<G> type) {
    return (WaitingGameManagerBasis<G>) gameManagerByType.get(type);
  }

  private void loadGamesOfWorld(ExWorld world) {
    int number = 0;
    for (WaitingGameManagerBasis<?> manager : this.gameManagerByType.values()) {
      number += manager.loadGamesOfWorld(world);
    }
    this.logger.info("Loaded waiting games in world '{}': {}", world.getName(), number);
  }

  private void saveGamesOfWorld(ExWorld world) {
    for (WaitingGameManagerBasis<?> manager : this.gameManagerByType.values()) {
      manager.saveGamesOfWorld(world);
    }
    this.logger.info("Saved waiting games in world '{}'", world.getName());
  }

  public boolean onUserDamageByUser(UserDamageByUserEvent e) {
    return this.gameManagerByType.values().stream().anyMatch(manager -> manager.onUserDamageByUser(e));
  }

  public boolean onUserDamage(UserDamageEvent e) {
    return this.gameManagerByType.values().stream().anyMatch(manager -> manager.onUserDamage(e));
  }

  @EventHandler
  public void onWorldLoad(ExWorldLoadEvent e) {
    this.loadGamesOfWorld(e.getWorld());
  }

  @EventHandler
  public void onWorldUnload(ExWorldUnloadEvent e) {
    if (e.getActionType().equals(WorldManager.WorldUnloadActionType.UNLOAD)
        || e.getActionType().equals(WorldManager.WorldUnloadActionType.RELOAD))
      this.saveGamesOfWorld(e.getWorld());
  }
}
