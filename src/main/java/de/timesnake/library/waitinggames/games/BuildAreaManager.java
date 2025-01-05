/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.waitinggames.games;

import de.timesnake.basic.bukkit.core.main.BasicBukkit;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.AsyncUserMoveEvent;
import de.timesnake.basic.bukkit.util.user.event.CancelPriority;
import de.timesnake.basic.bukkit.util.user.event.UserBlockBreakEvent;
import de.timesnake.basic.bukkit.util.user.event.UserBlockPlaceEvent;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.library.waitinggames.Plugin;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;

public class BuildAreaManager extends WaitingGameManagerBasis<BuildArea> implements Listener {

  private static final ExItemStack BUILDING_TOOL = new ExItemStack(Material.IRON_PICKAXE)
      .unbreakable()
      .setMoveable(false)
      .setDropable(false)
      .immutable();

  private final ExItemStack startItem = new ExItemStack(Material.WHITE_DYE, "§6Start")
      .setMoveable(false)
      .setDropable(false)
      .immutable()
      .onInteract(e -> this.getGameOfUser(e.getUser())
          .ifPresent(g -> e.getUser().teleport(g.getStart().toLocation(e.getUser().getExWorld()))), true);

  private final ExItemStack leaveItem = new ExItemStack(Material.RED_DYE, "§cLeave")
      .setMoveable(false)
      .setDropable(false)
      .immutable()
      .onInteract(e -> this.getGameOfUser(e.getUser()).ifPresent(g -> {
        g.removeUser(e.getUser());
        e.getUser().showTDTitle("", "§wLeft", Duration.ofSeconds(2));
        this.restoreUserInventory(e.getUser());
      }), true);

  public BuildAreaManager() {
    super(WaitingGame.Type.BUILD_AREA);
    Server.registerListener(this, BasicBukkit.getPlugin());
  }

  @EventHandler
  public void onUserMove(AsyncUserMoveEvent e) {
    User user = e.getUser();
    ExWorld world = user.getExWorld();

    for (BuildArea buildArea : this.gamesByWorld.getOrDefault(world, new HashSet<>())) {
      if (buildArea.getStart().toLocation(world).getBlock().equals(user.getLocation().getBlock())) {
        if (buildArea.containsUser(user)) {
          user.setItem(1, buildArea.getBuildingItemStack());
          user.setItem(2, buildArea.getBuildingItemStack());
          return;
        }

        Server.runTaskSynchrony(() -> {
          this.removeUserFromAllGames(user);
          buildArea.addUser(user);
          this.storeUserInventory(user);

          user.showTDTitle("", "§5Build Area", Duration.ofSeconds(2));

          user.clearInventory();
          user.setItem(1, buildArea.getBuildingItemStack());
          user.setItem(2, buildArea.getBuildingItemStack());
          user.setItem(3, BUILDING_TOOL);
          user.setItem(5, startItem);
          user.setItem(8, leaveItem);
        }, BasicBukkit.getPlugin());
        break;
      }
    }
  }

  @EventHandler
  public void onUserBlockPlace(UserBlockPlaceEvent e) {
    Optional<BuildArea> buildAreaOptional = this.getGameOfUser(e.getUser());

    if (buildAreaOptional.isEmpty()) {
      return;
    }

    if (!buildAreaOptional.get().contains(ExLocation.fromLocation(e.getBlock().getLocation()))) {
      e.getUser().sendPluginTDMessage(Plugin.WAITING_GAME, "§wYou can not build here");
      return;
    }

    e.setCancelled(CancelPriority.HIGH, false);
  }

  @EventHandler
  public void onUserBlockBreak(UserBlockBreakEvent e) {
    Optional<BuildArea> buildAreaOptional = this.getGameOfUser(e.getUser());

    if (buildAreaOptional.isEmpty()) {
      return;
    }

    if (!buildAreaOptional.get().contains(ExLocation.fromLocation(e.getBlock().getLocation()))) {
      e.getUser().sendPluginTDMessage(Plugin.WAITING_GAME, "§wYou can not build here");
      return;
    }

    e.setDropItems(false);
    e.setCancelled(CancelPriority.HIGH, false);
  }
}
