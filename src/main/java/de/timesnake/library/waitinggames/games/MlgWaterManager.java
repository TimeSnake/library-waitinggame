/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.waitinggames.games;

import de.timesnake.basic.bukkit.core.main.BasicBukkit;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.AsyncUserMoveEvent;
import de.timesnake.basic.bukkit.util.user.event.CancelPriority;
import de.timesnake.basic.bukkit.util.user.event.UserBlockPlaceEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDamageEvent;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;

public class MlgWaterManager extends WaitingGameManagerBasis<MlgWater> implements Listener {

  private final ExItemStack waterItem = new ExItemStack(Material.WATER_BUCKET, "§6Water");
  private final ExItemStack startItem = new ExItemStack(Material.WHITE_DYE, "§6Start")
      .setMoveable(false)
      .setDropable(false)
      .immutable()
      .onInteract(e -> this.getGameOfUser(e.getUser())
              .ifPresent(g -> e.getUser().teleport(g.getJumpPosition().toLocation(e.getUser().getExWorld()))),
          true);
  private final ExItemStack leaveItem = new ExItemStack(Material.RED_DYE, "§cLeave")
      .setMoveable(false)
      .setDropable(false)
      .immutable()
      .onInteract(e -> this.getGameOfUser(e.getUser()).ifPresent(g -> {
        g.removeUser(e.getUser());
        e.getUser().showTDTitle("", "§wLeft", Duration.ofSeconds(2));
        this.restoreUserInventory(e.getUser());
      }), true);

  public MlgWaterManager() {
    super(WaitingGame.Type.MLG_WATER);

    Server.registerListener(this, BasicBukkit.getPlugin());
  }

  @EventHandler
  public void onUserMove(AsyncUserMoveEvent e) {

    User user = e.getUser();
    ExWorld world = user.getExWorld();

    for (MlgWater mlgWater : this.gamesByWorld.getOrDefault(world, new HashSet<>())) {
      if (mlgWater.getStart().toBlock(world).equals(e.getTo().getBlock())) {
        if (mlgWater.containsUser(user)) {
          return;
        }

        Server.runTaskSynchrony(() -> {
          this.removeUserFromAllGames(user);
          mlgWater.addUser(user);
          this.storeUserInventory(user);

          user.showTDTitle("", "§wMLG Water", Duration.ofSeconds(2));

          user.clearInventory();
          user.setItem(1, waterItem);
          user.setItem(7, startItem);
          user.setItem(8, leaveItem);
        }, BasicBukkit.getPlugin());
        break;
      }
    }
  }

  @EventHandler
  public void onPlayerTeleport(PlayerTeleportEvent e) {
    if (this.isUserPlaying(Server.getUser(e.getPlayer()))) {
      return;
    }

    if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT)
        || e.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
      e.setCancelled(true);
    }
  }

  @Override
  public boolean onUserDamage(UserDamageEvent e) {
    if (e.getDamageCause().equals(EntityDamageEvent.DamageCause.FALL)) {
      User user = e.getUser();

      Optional<MlgWater> game = this.getGameOfUser(user);

      if (game.isPresent()) {
        user.showTDTitle("", "§wFailed", Duration.ofSeconds(2));
        Server.runTaskLaterSynchrony(() -> {
              user.teleport(game.get().getJumpPosition().toLocation(user.getExWorld()));
              user.setItem(1, waterItem);
            },
            20, BasicBukkit.getPlugin());
        return true;
      }
    }
    return false;
  }

  @EventHandler
  public void onUserBlockPlacer(UserBlockPlaceEvent e) {
    if (this.isUserPlaying(e.getUser())) {
      if (e.getBlock().getType() == Material.WATER) {
        e.setCancelled(CancelPriority.HIGH, false);
        Server.runTaskLaterSynchrony(() -> e.getBlock().setType(Material.AIR), 10, BasicBukkit.getPlugin());
      }
    }
  }
}
