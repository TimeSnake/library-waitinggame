/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.waitinggames.games;

import de.timesnake.basic.bukkit.util.user.event.UserDamageByUserEvent;
import de.timesnake.basic.bukkit.util.world.ExWorld;

import java.util.HashSet;

public class PunchAreaManager extends WaitingGameManagerBasis<PunchArea> {

  public PunchAreaManager() {
    super(WaitingGame.Type.PUNCH_AREA);
  }

  @Override
  public boolean onUserDamageByUser(UserDamageByUserEvent e) {
    ExWorld world = e.getUser().getExWorld();

    if (this.gamesByWorld.getOrDefault(world, new HashSet<>()).stream()
        .anyMatch(g -> g.containsLocation(e.getUser(), e.getUserDamager()))) {
      e.setDamage(0);
      return true;
    }
    return false;
  }
}
