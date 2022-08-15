package de.timesnake.library.waitinggames.games;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.*;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.waitinggames.GameFile;
import de.timesnake.library.waitinggames.GameLoadException;
import de.timesnake.library.waitinggames.WaitingGameManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.time.Duration;

public class MlgWater extends WaitingGame implements Listener, UserInventoryInteractListener {

    public static final String NAME = "mlg_water";

    private static final String START = "start";
    private static final String HEIGHT = "height";

    private final ExItemStack startItem = new ExItemStack(Material.WATER_BUCKET, "§6Water");
    private final ExItemStack leaveItem = new ExItemStack(8, Material.RED_DYE, "§cLeave");

    private final ExLocation start;
    private final int height;

    public MlgWater(ExLocation start, int height) {
        this.start = start;
        this.height = height;

        GameFile file = WaitingGameManager.getInstance().getGameFile(start.getExWorld());

        super.id = file.addGame(NAME, new Tuple<>(START, start), new Tuple<>(HEIGHT, height));

        Server.registerListener(this);
        Server.getInventoryEventManager().addInteractListener(this, this.startItem, this.leaveItem);
    }

    public MlgWater(GameFile file, int id) throws GameLoadException {
        super(id);

        if (!file.containsGame(id)) {
            throw new GameLoadException(NAME, id);
        }

        Location startLoc = null;
        Integer height = null;

        try {
            startLoc = file.getLocationValue(id, START);
            height = file.getIntValue(id, HEIGHT);
        } catch (WorldNotExistException ignored) {
        }

        if (startLoc == null || height == null) {
            throw new GameLoadException(NAME, id);
        }

        this.start = new ExLocation(Server.getWorld(startLoc.getWorld()), startLoc.getX(), startLoc.getY(),
                startLoc.getZ());
        this.height = height;

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
            user.showTitle(Component.text("MLG Water", ExTextColor.BLUE), Component.empty(), Duration.ofSeconds(2));

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

        if (this.start.getY() - this.height - e.getFrom().getY() < 0.01) {
            this.users.remove(user);
            user.clearInventory();
            user.getInventory().setContents(this.inventoriesByUser.remove(user));
            user.showTitle(Component.text("Finished", ExTextColor.GOLD), Component.empty(), Duration.ofSeconds(2));
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

    @Override
    public boolean onUserDamage(UserDamageEvent e) {
        if (e.getDamageCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            User user = e.getUser();

            this.users.remove(user);
            user.clearInventory();
            user.getInventory().setContents(this.inventoriesByUser.remove(user));
            user.showTitle(Component.text("Failed", ExTextColor.WARNING), Component.empty(), Duration.ofSeconds(2));
        }
        return false;
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
            user.showTitle(Component.text("Left", ExTextColor.WARNING), Component.empty(), Duration.ofSeconds(2));
        }
    }
}
