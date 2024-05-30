/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.waitinggames.games;

import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.SimpleLocation;

public class PunchArea extends WaitingGame {

  private final SimpleLocation center;
  private final double radius;
  private final double height;

  public PunchArea(String name, SimpleLocation center, double radius, double height) {
    super(name);
    this.center = center;
    this.radius = radius;
    this.height = height;
  }

  public SimpleLocation getCenter() {
    return center;
  }

  public double getRadius() {
    return radius;
  }

  public double getHeight() {
    return height;
  }

  public boolean containsLocation(User user, User userDamager) {
    return user.getExWorld().equals(this.getWorld())
           && userDamager.getExWorld().equals(this.getWorld())
           && this.center.toLocation(this.getWorld()).distanceHorizontalSquared(user.getLocation()) <= this.radius * this.radius
           && user.getLocation().getY() >= this.center.getY()
           && user.getLocation().getY() - this.center.getY() <= this.height
           && this.center.toLocation(this.getWorld()).distanceHorizontalSquared(userDamager.getLocation()) <= this.radius * this.radius
           && userDamager.getLocation().getY() >= this.center.getY()
           && userDamager.getLocation().getY() - this.center.getY() <= this.height;
  }
}
