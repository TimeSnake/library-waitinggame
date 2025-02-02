/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.waitinggames;

import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.IncCommandListener;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.bukkit.util.world.ExPolygon;
import de.timesnake.basic.bukkit.util.world.SimpleFacingLocation;
import de.timesnake.basic.bukkit.util.world.SimpleLocation;
import de.timesnake.library.chat.Code;
import de.timesnake.library.chat.Plugin;
import de.timesnake.library.commands.PluginCommand;
import de.timesnake.library.commands.inchat.IncCommandContext;
import de.timesnake.library.commands.inchat.IncCommandOption;
import de.timesnake.library.commands.simple.Arguments;
import de.timesnake.library.waitinggames.games.*;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WaitingGameCreateCmd extends IncCommandListener {

  private final Code perm = Plugin.SERVER.createPermssionCode("waiting_game.create");

  private final WaitingGameManager waitingGameManager;

  public WaitingGameCreateCmd(WaitingGameManager waitingGameManager) {
    this.waitingGameManager = waitingGameManager;
  }

  @Override
  public IncCommandContext onCommand(Sender sender, PluginCommand pluginCommand, Arguments<Argument> args) {
    sender.hasPermissionElseExit(this.perm);
    sender.isPlayerElseExit(true);
    args.isLengthHigherEqualsElseExit(2, true);

    String name = args.get(1).toLowerCase();

    IncCommandContext context = new IncCommandContext();
    context.addOption(NAME, name);
    context.addOption(TYPE, args.get(0).toLowerCase());

    return switch (args.get(0).toLowerCase()) {
      case "jump_run" -> this.handleJumpRun(sender, args, context);
      case "punch_area" -> this.handlePunchArea(sender, args, context);
      case "build_area" -> this.handleBuildArea(sender, args, context);
      case "mlg_water" -> this.handleMlgWater(sender, args, context);
      default -> null;
    };
  }

  private IncCommandContext handleJumpRun(Sender sender, Arguments<Argument> args, IncCommandContext context) {
    this.sendSelectionTo(sender, this.createSelection(ADD_POINTS).addValues("start"));
    return context;
  }

  private IncCommandContext handlePunchArea(Sender sender, Arguments<Argument> args, IncCommandContext context) {
    args.isLengthEqualsElseExit(4, true);
    context.addOption(RADIUS, args.get(2).toBoundedDoubleOrExit(0, 16, true));
    context.addOption(HEIGHT, args.get(3).toBoundedDoubleOrExit(0, sender.getUser().getExWorld().getMaxHeight(), true));
    this.sendSelectionTo(sender, this.createSelection(ADD_POINTS).title("Center").addValues("set"));
    return context;
  }

  private IncCommandContext handleBuildArea(Sender sender, Arguments<Argument> args, IncCommandContext context) {
    args.isLengthEqualsElseExit(3, true);
    context.addOption(MATERIAL, args.get(2).toMaterialOrExit(true));
    this.sendSelectionTo(sender, this.createSelection(LOCATION).title("Start").addValues("set"));
    return context;
  }

  private IncCommandContext handleMlgWater(Sender sender, Arguments<Argument> args, IncCommandContext context) {
    this.sendSelectionTo(sender, this.createSelection(LOCATION).title("Start").addValues("set"));
    return context;
  }

  @Override
  public List<String> getTabCompletion(PluginCommand pluginCommand, Arguments<Argument> arguments) {
    return List.of();
  }

  @Override
  public <V> boolean onUpdate(Sender sender, IncCommandContext context, IncCommandOption<V> option, V v) {
    return switch (context.getOption(TYPE)) {
      case "jump_run" -> this.handleJumpRunUpdate(sender, context, option, v);
      case "punch_area" -> this.handlePunchAreaUpdate(sender, context, option, v);
      case "build_area" -> this.handleBuildAreaUpdate(sender, context, option, v);
      case "mlg_water" -> this.handleMlgWaterUpdate(sender, context, option, v);
      default -> {
        sender.sendPluginTDMessage("§wUnknown waiting game type");
        yield true;
      }
    };
  }

  private <V> boolean handleJumpRunUpdate(Sender sender, IncCommandContext context, IncCommandOption<V> option, V v) {
    if (option.equals(ADD_POINTS)) {
      if (v.equals("start")) {
        List<SimpleFacingLocation> locs = new ArrayList<>();
        locs.add(new SimpleFacingLocation(sender.getUser().getExLocation().middleHorizontalBlock().roundFacing()));
        context.addOption(POINTS, locs);
        sender.sendPluginTDMessage("§sAdded start location");
        this.sendSelectionTo(sender, this.createSelection(ADD_POINTS).addValues("checkpoint", "finish"));
        return false;
      } else if (v.equals("checkpoint")) {
        sender.sendPluginTDMessage("§sAdded checkpoint location");
        context.getOption(POINTS).add(new SimpleFacingLocation(sender.getUser().getExLocation().middleHorizontalBlock().roundFacing()));
        this.sendSelectionTo(sender, this.createSelection(ADD_POINTS).addValues("checkpoint", "finish"));
        return false;
      } else if (v.equals("finish")) {
        context.getOption(POINTS).add(new SimpleFacingLocation(sender.getUser().getExLocation().middleHorizontalBlock().roundFacing()));
        JumpRun jumpRun = new JumpRun(context.getOption(NAME), context.getOption(POINTS));
        this.waitingGameManager.getGameManager(WaitingGame.Type.JUMP_RUN).addGame(jumpRun,
            sender.getUser().getExWorld());
        sender.sendPluginTDMessage("§sCreated jump run §v" + context.getOptions().get(NAME));
        return true;
      }
    }
    return false;
  }

  private <V> boolean handlePunchAreaUpdate(Sender sender, IncCommandContext context, IncCommandOption<V> option, V v) {
    if (option.equals(ADD_POINTS)) {
      PunchArea punchArea = new PunchArea(context.getOption(NAME),
          new SimpleLocation(sender.getUser().getExLocation().middleHorizontalBlock()),
          context.getOption(RADIUS), context.getOption(HEIGHT));
      this.waitingGameManager.getGameManager(WaitingGame.Type.PUNCH_AREA).addGame(punchArea,
          sender.getUser().getExWorld());
      sender.sendPluginTDMessage("§sCreated punch area §v" + context.getOption(NAME));
      return true;
    }
    return false;
  }

  private <V> boolean handleBuildAreaUpdate(Sender sender, IncCommandContext context, IncCommandOption<V> option, V v) {
    if (option.equals(LOCATION)) {
      context.addOption(LOCATION,
          new SimpleFacingLocation(sender.getUser().getExLocation()).middleHorizontalBlock().roundFacing());
      context.addOption(POINTS, new ArrayList<>());
      this.sendSelectionTo(sender, this.createSelection(ADD_POINTS).addValues("first"));
      return false;
    } else if (option.equals(ADD_POINTS)) {
      if (v.equals("first") || v.equals("add")) {
        context.getOption(POINTS).add(new SimpleFacingLocation(sender.getUser().getLocation()));
        this.sendSelectionTo(sender, this.createSelection(ADD_POINTS).addValues("add", "done"));
      } else if (v.equals("done")) {
        BuildArea buildArea = new BuildArea(context.getOption(NAME), context.getOption(LOCATION),
            new ExPolygon(sender.getUser().getExWorld(), context.getOption(POINTS).stream()
                .map(p -> p.toLocation(sender.getUser().getExWorld())).toList()),
            context.getOption(MATERIAL));
        this.waitingGameManager.getGameManager(WaitingGame.Type.BUILD_AREA).addGame(buildArea,
            sender.getUser().getExWorld());
        sender.sendPluginTDMessage("§sCreated build area §v" + context.getOption(NAME));
        return true;
      }
    }
    return false;
  }

  private <V> boolean handleMlgWaterUpdate(Sender sender, IncCommandContext context, IncCommandOption<V> option, V v) {
    if (option.equals(LOCATION)) {
      if (v.equals("start")) {
        context.addOption(LOCATION,
            new SimpleFacingLocation(sender.getUser().getExLocation().middleHorizontalBlock().roundFacing()));
        this.sendSelectionTo(sender, this.createSelection(ADD_POINTS).addValues("jump"));
        return false;
      } else if (v.equals("jump")) {
        MlgWater mlgWater = new MlgWater(context.getOption(NAME), context.getOption(LOCATION).toBlock(),
            new SimpleFacingLocation(sender.getUser().getExLocation().middleHorizontalBlock().roundFacing()));
        this.waitingGameManager.getGameManager(WaitingGame.Type.MLG_WATER).addGame(mlgWater,
            sender.getUser().getExWorld());
        sender.sendPluginTDMessage("§sCreated mlg water §v" + context.getOption(NAME));
        return true;
      }
    }
    return false;
  }

  @Override
  public Collection<IncCommandOption<?>> getOptions() {
    return OPTIONS;
  }

  @Override
  public String getCommand() {
    return "wgc";
  }

  @Override
  public String getPermission() {
    return this.perm.getPermission();
  }

  private static final IncCommandOption<String> TYPE = new IncCommandOption.Str("type", "Type");
  private static final IncCommandOption<String> NAME = new IncCommandOption.Str("name", "Name");
  private static final IncCommandOption<String> ADD_POINTS = new IncCommandOption.Str("add_points", "Add Points");
  private static final IncCommandOption<Double> RADIUS = new IncCommandOption.Storage<>("radius");
  private static final IncCommandOption<Double> HEIGHT = new IncCommandOption.Storage<>("height");
  private static final IncCommandOption<Material> MATERIAL = new IncCommandOption.Storage<>("material");
  private static final IncCommandOption<SimpleFacingLocation> LOCATION = new IncCommandOption.Storage<>("location");
  private static final IncCommandOption<List<SimpleFacingLocation>> POINTS = new IncCommandOption.Storage<>("points");

  private static final List<IncCommandOption<?>> OPTIONS = List.of(NAME, RADIUS, HEIGHT, MATERIAL, LOCATION,
      ADD_POINTS, POINTS);
}
