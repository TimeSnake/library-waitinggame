package de.timesnake.library.waitinggames;

import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.library.basic.util.Tuple;
import org.bukkit.Location;

import java.util.Set;

public class GameFile extends ExFile {

    public static final String NAME = "waiting-games";

    private static final String GAMES = "games";

    private static final String TYPE = "type";

    public GameFile(ExWorld world) {
        super(world.getWorldFolder(), NAME + ".yml");
    }

    public int addGame(String gameType, Tuple<String, ?>... values) {
        int id = 0;

        while (super.contains(this.getGamePath(id))) {
            id++;
        }

        super.set(this.getGamePath(id) + "." + TYPE, gameType);

        for (Tuple<String, ?> value : values) {
            super.set(this.getGamePath(id) + "." + value.getA(), value.getB());
        }
        super.save();

        return id;
    }

    public boolean updateGame(int id, Tuple<String, ?> value) {
        if (!super.contains(this.getGamePath(id))) {
            return false;
        }

        super.set(this.getGamePath(id) + "." + value.getA(), value.getB());

        super.save();

        return true;
    }

    public boolean deleteGame(int id) {
        return super.remove(this.getGamePath(id));
    }

    public boolean containsGame(int id) {
        return super.contains(this.getGamePath(id));
    }

    public boolean containsGameValue(int id, String valueKey) {
        return super.contains(this.getGamePath(id) + "." + valueKey);
    }

    public Set<Integer> getGameIds() {
        return super.getPathIntegerList(GAMES);
    }

    public String getGameType(int id) {
        return super.getString(this.getGamePath(id) + "." + TYPE);
    }

    public String getStringValue(int id, String valueKey) {
        return super.getString(this.getGamePath(id) + "." + valueKey);
    }

    public Location getLocationValue(int id, String valueKey) throws WorldNotExistException {
        return super.getLocation(this.getGamePath(id) + "." + valueKey);
    }

    public Double getDoubleValue(int id, String valueKey) {
        return super.getDouble(this.getGamePath(id) + "." + valueKey);
    }

    public Integer getIntValue(int id, String valueKey) {
        return super.getInt(this.getGamePath(id) + "." + valueKey);
    }

    private String getGamePath(int id) {
        return GAMES + "." + id;
    }
}
