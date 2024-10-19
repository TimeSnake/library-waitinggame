/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.waitinggames.games;

import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.SimpleFacingLocation;
import de.timesnake.library.basic.util.UserMap;

import java.util.List;

public class JumpRun extends WaitingGame {

  private final List<SimpleFacingLocation> checkpoints;

  private transient UserMap<User, Integer> checkpointIndexByUser;

  public JumpRun(String name, List<SimpleFacingLocation> checkpoints) {
    super(name);
    this.checkpoints = checkpoints;
  }

  @Override
  protected void init() {
    super.init();
    this.checkpointIndexByUser = new UserMap<>();
  }

  public SimpleFacingLocation getStart() {
    return this.checkpoints.get(0);
  }

  public SimpleFacingLocation getEnd() {
    return this.checkpoints.get(this.checkpoints.size() - 1);
  }

  public List<SimpleFacingLocation> getCheckpoints() {
    return checkpoints;
  }

  public SimpleFacingLocation getPosition(int index) {
    return this.checkpoints.get(index);
  }

  @Override
  public void addUser(User user) {
    super.addUser(user);
    this.checkpointIndexByUser.put(user, 0);
  }

  @Override
  public boolean removeUser(User user) {
    this.checkpointIndexByUser.remove(user);
    return super.removeUser(user);
  }

  public UserMap<User, Integer> getCheckpointIndexByUser() {
    return checkpointIndexByUser;
  }

  public void teleportToLastCheckpoint(User user) {
    user.teleport(this.checkpoints.get(this.checkpointIndexByUser.get(user)).toLocation(this.getWorld()));
  }
}
