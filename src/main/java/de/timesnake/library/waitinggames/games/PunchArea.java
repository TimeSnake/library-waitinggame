/*
 * workspace.library-waitinggame.main
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
import de.timesnake.basic.bukkit.util.exception.WorldNotExistException;
import de.timesnake.basic.bukkit.util.user.event.UserDamageByUserEvent;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.waitinggames.GameFile;
import de.timesnake.library.waitinggames.GameLoadException;
import de.timesnake.library.waitinggames.WaitingGameManager;
import org.bukkit.Location;

public class PunchArea extends WaitingGame {

    public static final String NAME = "punch_area";

    private static final String CENTER = "center";
    private static final String RADIUS = "radius";
    private static final String HEIGHT = "height";

    private final ExLocation center;
    private final Double radius;
    private final Double height;

    public PunchArea(ExLocation center, Double radius, Double height) {
        this.center = center;
        this.radius = radius;
        this.height = height;

        GameFile file = WaitingGameManager.getInstance().getGameFile(center.getExWorld());

        super.id = file.addGame(NAME, new Tuple<>(CENTER, center), new Tuple<>(RADIUS, radius), new Tuple<>(HEIGHT,
                height));
    }

    public PunchArea(GameFile file, int id) throws GameLoadException {
        super(id);

        if (!file.containsGame(id)) {
            throw new GameLoadException(NAME, id);
        }

        Location loc = null;

        try {
            loc = file.getLocationValue(id, CENTER);
        } catch (WorldNotExistException ignored) {
        }

        if (loc == null) {
            throw new GameLoadException(NAME, id);
        }

        this.center = new ExLocation(Server.getWorld(loc.getWorld()), loc.getX(), loc.getY(), loc.getZ());

        this.radius = file.getDoubleValue(id, RADIUS);
        this.height = file.getDoubleValue(id, HEIGHT);

        if (this.radius == null || this.height == null) {
            throw new GameLoadException(NAME, id);
        }
    }

    public boolean delete() {
        this.setEnabled(false);
        return WaitingGameManager.getInstance().getGameFile(center.getExWorld()).deleteGame(this.id);
    }

    @Override
    public boolean onUserDamageByUser(UserDamageByUserEvent e) {

        ExWorld world = e.getUser().getExWorld();

        if (!this.center.getExWorld().equals(world)) {
            return false;
        }

        Location loc = e.getUser().getLocation();
        Location damagerLoc = e.getUserDamager().getLocation();

        Location centerZero = this.center.clone().add(0, -this.center.getY(), 0);

        double distanceXZ = loc.clone().add(0, -loc.getY(), 0).distance(centerZero);
        double deltaY = loc.getY() - this.center.getY();

        double damagerDistanceXZ = damagerLoc.clone().add(0, -damagerLoc.getY(), 0).distance(centerZero);
        double damagerDeltaY = damagerLoc.getY() - this.center.getY();

        if (distanceXZ <= this.radius && deltaY >= 0 && deltaY <= this.height && damagerDistanceXZ <= this.radius && damagerDeltaY >= 0 && damagerDeltaY <= this.height) {
            e.setDamage(0);
            return true;
        }

        return false;
    }
}
