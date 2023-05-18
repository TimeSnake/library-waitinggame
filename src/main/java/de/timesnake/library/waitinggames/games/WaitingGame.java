/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.waitinggames.games;

import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserDamageByUserEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDamageEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.inventory.ItemStack;

public abstract class WaitingGame {

  protected int id;

  protected boolean enabled = true;

  protected final Set<User> users = new HashSet<>();
  protected final HashMap<User, ItemStack[]> inventoriesByUser = new HashMap<>();

  public WaitingGame(int id) {
    this.id = id;
  }

  public WaitingGame() {
  }

  public int getId() {
    return id;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean onUserDamage(UserDamageEvent e) {
    return false;
  }

  public boolean onUserDamageByUser(UserDamageByUserEvent e) {
    return false;
  }

  public void addUser(User user) {
    this.users.add(user);
  }

  public void removeUser(User user) {
    this.users.remove(user);
  }
}
