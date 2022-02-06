package de.timesnake.library.waitinggames;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.UserDamageByUserEvent;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.library.waitinggames.games.JumpRun;
import de.timesnake.library.waitinggames.games.MlgWater;
import de.timesnake.library.waitinggames.games.PunchArea;
import de.timesnake.library.waitinggames.games.WaitingGame;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaitingGameManager {

    private static WaitingGameManager instance;

    public static WaitingGameManager getInstance() {
        return instance;
    }

    private final HashMap<ExWorld, GameFile> gameFilesByWorld = new HashMap<>();
    private final HashMap<ExWorld, List<WaitingGame>> gamesByWorld = new HashMap<>();

    public WaitingGameManager() {
        instance = this;

        for (ExWorld world : Server.getWorlds()) {
            File gameFile = new File(world.getWorldFolder().getAbsolutePath() + File.separator + GameFile.NAME + ".yml");
            if (gameFile.exists()) {
                this.gameFilesByWorld.put(world, new GameFile(world));
            }
        }

        for (Map.Entry<ExWorld, GameFile> entry : this.gameFilesByWorld.entrySet()) {

            GameFile file = entry.getValue();

            List<WaitingGame> games = new ArrayList<>();

            for (Integer id : file.getGameIds()) {
                String type = file.getGameType(id);

                try {
                    switch (type.toLowerCase()) {
                        case PunchArea.NAME:
                            games.add(new PunchArea(file, id));
                            Server.printText(Plugin.WAITING_GAME, "Loaded punch area " + id);
                            break;
                        case JumpRun.NAME:
                            games.add(new JumpRun(file, id));
                            Server.printText(Plugin.WAITING_GAME, "Loaded jump run " + id);
                            break;
                        case MlgWater.NAME:
                            games.add(new MlgWater(file, id));
                            Server.printText(Plugin.WAITING_GAME, "Loaded mlg water" + id);
                    }
                } catch (GameLoadException e) {
                    Server.printWarning(Plugin.WAITING_GAME, e.getMessage());
                }
            }

            this.gamesByWorld.put(entry.getKey(), games);

            Server.printText(Plugin.WAITING_GAME, "Loaded waiting games in world " + entry.getKey().getName());
        }

        Server.printText(Plugin.WAITING_GAME, "Loaded waiting game manager");
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
