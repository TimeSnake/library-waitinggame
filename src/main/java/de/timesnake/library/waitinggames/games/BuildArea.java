/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.waitinggames.games;

import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExPolygon;
import de.timesnake.basic.bukkit.util.world.SimpleFacingLocation;
import org.bukkit.Material;

public class BuildArea extends WaitingGame {

  private final SimpleFacingLocation start;
  private final ExPolygon area;
  private final Material buildingMaterial;
  private transient ExItemStack buildingItem;

  public BuildArea(String name, SimpleFacingLocation start, ExPolygon area, Material buildingMaterial) {
    super(name);
    this.start = start;
    this.area = area;
    this.buildingMaterial = buildingMaterial;
  }

  @Override
  protected void init() {
    super.init();
    this.area.setWorld(this.world);
  }

  public SimpleFacingLocation getStart() {
    return start;
  }

  public ExPolygon getArea() {
    return area;
  }

  public Material getBuildingMaterial() {
    return buildingMaterial;
  }

  public ExItemStack getBuildingItemStack() {
    if (this.buildingItem == null) {
      this.buildingItem = new ExItemStack(buildingMaterial);
    }
    return buildingItem.cloneWithId()
        .asQuantity(64)
        .setMoveable(false)
        .setDropable(false);
  }

  public boolean contains(ExLocation location) {
    return this.area.contains(location);
  }

}
