/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.waitinggames.games;

import de.timesnake.basic.bukkit.util.world.SimpleBlock;
import de.timesnake.basic.bukkit.util.world.SimpleFacingLocation;

public class MlgWater extends WaitingGame {

  private final SimpleBlock start;
  private final SimpleFacingLocation jumpPosition;

  public MlgWater(String name, SimpleBlock start, SimpleFacingLocation jumpPosition) {
    super(name);
    this.start = start;
    this.jumpPosition = jumpPosition;
  }

  public SimpleBlock getStart() {
    return start;
  }

  public SimpleFacingLocation getJumpPosition() {
    return jumpPosition;
  }

}
