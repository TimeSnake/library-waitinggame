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

package de.timesnake.library.waitinggames.games;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryInteractEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryInteractListener;
import de.timesnake.basic.bukkit.util.user.event.UserMoveEvent;
import de.timesnake.basic.bukkit.util.user.event.UserQuitEvent;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.waitinggames.GameFile;
import de.timesnake.library.waitinggames.GameLoadException;
import de.timesnake.library.waitinggames.Plugin;
import de.timesnake.library.waitinggames.WaitingGameManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.time.Duration;

public class JumpRun extends WaitingGame implements Listener, UserInventoryInteractListener {

    public static final String NAME = "jump_run";

    private static final String START = "start";
    private static final String FINISH = "finish";

    private final ExItemStack startItem = new ExItemStack(Material.WHITE_DYE, "§6Start");
    private final ExItemStack leaveItem = new ExItemStack(8, Material.RED_DYE, "§cLeave");

    private final ExLocation start;
    private final ExLocation finish;

    public JumpRun(ExLocation start, ExLocation finish) {
        this.start = start;
        this.finish = finish;

        GameFile file = WaitingGameManager.getInstance().getGameFile(start.getExWorld());

        super.id = file.addGame(NAME, new Tuple<>(START, start), new Tuple<>(FINISH, finish));

        Server.registerListener(this);
        Server.getInventoryEventManager().addInteractListener(this, this.startItem, this.leaveItem);
    }

    public JumpRun(GameFile file, int id) throws GameLoadException {
        super(id);

        if (!file.containsGame(id)) {
            throw new GameLoadException(NAME, id);
        }

        Location startLoc = null;
        Location finishLoc = null;

        try {
            startLoc = file.getLocationValue(id, START);
            finishLoc = file.getLocationValue(id, FINISH);
        } catch (WorldNotExistException ignored) {
        }

        if (startLoc == null || finishLoc == null) {
            throw new GameLoadException(NAME, id);
        }

        this.start = new ExLocation(Server.getWorld(startLoc.getWorld()), startLoc.getX(), startLoc.getY(),
                startLoc.getZ(), startLoc.getYaw(), startLoc.getPitch());
        this.finish = new ExLocation(Server.getWorld(finishLoc.getWorld()), finishLoc.getX(), finishLoc.getY(),
                finishLoc.getZ());

        Server.registerListener(this);
        Server.getInventoryEventManager().addInteractListener(this, this.startItem, this.leaveItem);
    }

    @EventHandler
    public void onUserMove(UserMoveEvent e) {

        User user = e.getUser();

        if (e.getTo().getBlock().equals(this.start.getBlock())) {
            if (this.users.contains(user)) {
                return;
            }

            this.users.add(user);
            user.showTitle(Component.empty(), Component.text("Jump'n Run", ExTextColor.BLUE), Duration.ofSeconds(2));

            this.inventoriesByUser.put(user, user.getInventory().getContents());
            user.clearInventory();

            user.setItem(3, startItem);
            user.setItem(4, startItem);
            user.setItem(5, startItem);
            user.setItem(leaveItem);
        }

        if (!this.users.contains(user)) {
            return;
        }

        if (e.getTo().getBlock().equals(this.finish.getBlock())) {
            this.users.remove(user);
            user.clearInventory();
            user.getInventory().setContents(this.inventoriesByUser.remove(user));
            user.showTitle(Component.text("Finished", ExTextColor.GOLD), Component.empty(), Duration.ofSeconds(2));
            Server.broadcastMessage(Plugin.WAITING_GAME, user.getChatNameComponent()
                    .append(Component.text(" finished the Jump'n Run", ExTextColor.PUBLIC)));
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {

        User user = Server.getUser(e.getPlayer());

        if (!this.users.contains(user)) {
            return;
        }

        if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) || e.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onUserLeave(UserQuitEvent e) {
        this.users.remove(e.getUser());
    }

    @Override
    public void onUserInventoryInteract(UserInventoryInteractEvent event) {
        ExItemStack clickedItem = event.getClickedItem();
        User user = event.getUser();

        if (!this.users.contains(user)) {
            return;
        }

        event.setCancelled(true);

        if (clickedItem.equals(this.startItem)) {
            user.teleport(this.start);
        } else if (clickedItem.equals(this.leaveItem)) {
            this.users.remove(user);
            user.clearInventory();
            user.getInventory().setContents(this.inventoriesByUser.remove(user));
            user.showTitle(Component.empty(), Component.text("Left", ExTextColor.WARNING), Duration.ofSeconds(2));
        }
    }
}
