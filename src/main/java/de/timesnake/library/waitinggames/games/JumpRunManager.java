/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.waitinggames.games;

import de.timesnake.basic.bukkit.core.main.BasicBukkit;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.AsyncUserMoveEvent;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.chat.Plugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.time.Duration;
import java.util.HashSet;

public class JumpRunManager extends WaitingGameManagerBasis<JumpRun> implements Listener {

  private final ExItemStack startItem = new ExItemStack(Material.WHITE_DYE)
      .setDisplayName("§6Start")
      .setMoveable(false)
      .setDropable(false)
      .immutable()
      .onInteract(e -> this.getGameOfUser(e.getUser()).ifPresent(g -> g.teleportToLastCheckpoint(e.getUser())), true);

  private final ExItemStack leaveItem = new ExItemStack(8, Material.RED_DYE)
      .setDisplayName("§cLeave")
      .setMoveable(false)
      .setDropable(false)
      .immutable()
      .onInteract(e -> this.getGameOfUser(e.getUser()).ifPresent(g -> {
        g.removeUser(e.getUser());
        e.getUser().showTDTitle("", "§wLeft", Duration.ofSeconds(2));
        this.restoreUserInventory(e.getUser());
      }), true);

  public JumpRunManager() {
    super(WaitingGame.Type.JUMP_RUN);
    Server.registerListener(this, BasicBukkit.getPlugin());
  }

  @EventHandler
  public void onUserMove(AsyncUserMoveEvent e) {
    User user = e.getUser();
    ExWorld world = user.getExWorld();

    for (JumpRun jumpRun : this.gamesByWorld.getOrDefault(world, new HashSet<>())) {
      for (int index = 0; index < jumpRun.getCheckpoints().size(); index++) {
        ExLocation loc = jumpRun.getPosition(index).toLocation(world);

        if (loc.getBlock().equals(e.getTo().getBlock())) {
          if (index == 0) {
            if (jumpRun.containsUser(user)) {
              return;
            }

            Server.runTaskSynchrony(() -> {
              this.removeUserFromAllGames(user);
              jumpRun.addUser(user);
              this.storeUserInventory(user);

              user.showTitle(Component.empty(), Component.text("Jump'n Run", ExTextColor.BLUE),
                  Duration.ofSeconds(2));

              user.clearInventory();
              user.setItem(3, startItem);
              user.setItem(4, startItem);
              user.setItem(5, startItem);
              user.setItem(leaveItem);
            }, BasicBukkit.getPlugin());
          } else if (jumpRun.containsUser(user) && index == jumpRun.getCheckpoints().size() - 1) {
            Server.runTaskSynchrony(() -> {
              jumpRun.removeUser(user);
              this.restoreUserInventory(user);
              user.showTDTitle("§hFinished", "", Duration.ofSeconds(2));
              Server.broadcastTDMessage(Plugin.SERVER,
                  user.getTDChatName() + "§p finished jump'n run §v" + jumpRun.getName());
            }, BasicBukkit.getPlugin());
          } else if (jumpRun.containsUser(user)) {
            int finalIndex = index;
            Server.runTaskSynchrony(() -> {
              if (jumpRun.getCheckpointIndexByUser().getOrDefault(user, 0) >= finalIndex) {
                return;
              }
              jumpRun.getCheckpointIndexByUser().put(user, finalIndex);
              user.sendPluginTDMessage(Plugin.SERVER, "§sSaved checkpoint §v" + finalIndex);
            }, BasicBukkit.getPlugin());
          }
          return;
        }
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
}
