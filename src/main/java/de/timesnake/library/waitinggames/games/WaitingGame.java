/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.waitinggames.games;

import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.library.basic.util.UserSet;

import java.util.Set;

public abstract class WaitingGame {

  protected final String name;

  protected transient ExWorld world;

  protected transient UserSet<User> users;

  public WaitingGame(String name) {
    this.name = name;
  }

  protected void init() {
    this.users = new UserSet<>();
  }

  public String getName() {
    return name;
  }

  public ExWorld getWorld() {
    return world;
  }

  public void addUser(User user) {
    this.users.add(user);
  }

  public boolean removeUser(User user) {
    return this.users.remove(user);
  }

  public Set<User> getUsers() {
    return users;
  }

  public boolean containsUser(User user) {
    return users.contains(user);
  }

  public static class Type<G extends WaitingGame> {
    public static final Type<JumpRun> JUMP_RUN = new Type<>("jump_run", JumpRun.class);
    public static final Type<MlgWater> MLG_WATER = new Type<>("mlg_water", MlgWater.class);
    public static final Type<PunchArea> PUNCH_AREA = new Type<>("punch_area", PunchArea.class);
    public static final Type<BuildArea> BUILD_AREA = new Type<>("build_area", BuildArea.class);

    private final String name;
    private final Class<G> gameClass;

    Type(String name, Class<G> gameClass) {
      this.name = name;
      this.gameClass = gameClass;
    }

    public String getName() {
      return name;
    }

    public Class<G> getGameClass() {
      return gameClass;
    }
  }
}
