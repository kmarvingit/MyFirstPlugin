package me.example.myFirstPlugin.Dome;


import me.example.myFirstPlugin.enums.CombatLoot;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class DomeCommand implements CommandExecutor {

  private final JavaPlugin plugin;
  private final World world;

  private boolean running = false;
  private BukkitTask finalTask;
  private List<BukkitTask> shrinkTasks = new ArrayList<>();


  private static final long DURATION = 20L * 60 * 1;

  private final Set<UUID> participants = new HashSet<>();
  private final Set<UUID> eliminated = new HashSet<>();


  public DomeCommand(JavaPlugin plugin, World world) {
    this.plugin = plugin;
    this.world = world;

  }

  public boolean isRunning() {
    return running;
  }


  public boolean isParticipant(Player player) {
    return participants.contains(player.getUniqueId());
  }

  public boolean isEliminated(Player player) {
    return eliminated.contains(player.getUniqueId());
  }

  public void eliminate(Player player) {
    if (isParticipant(player)) {
      eliminated.add(player.getUniqueId());
    }
  }

  public List<Player> getAliveParticipants() {
    List<Player> alive = new ArrayList<>();

    for (UUID id : participants) {
      Player player = Bukkit.getPlayer(id);

      if (player != null
          && player.isOnline()
          && player.getWorld().equals(world)
          //because when player is dead, eliminate is executed
          && !eliminated.contains(id)) {

        alive.add(player);
      }
    }

    return alive;
  }


  private void spawnLootChest(Player player, int phase) {
    ThreadLocalRandom random = ThreadLocalRandom.current();

    for (int attempt = 0; attempt < 30; attempt++) {
      Location position = player.getLocation();

      int randomX = (int) (Math.random() * (20 - ((18 / 5) * phase)) / getAliveParticipants().size());
      int signX = (int) (Math.random() * 2);
      randomX = signX == 0 ? randomX * -1 : randomX;
      int totalX = (int) (position.x() + randomX);


      int randomZ = (int) (Math.random() * (20 - ((18 / 5) * phase)) / getAliveParticipants().size());
      int signZ = (int) (Math.random() * 2);
      randomZ = signZ == 0 ? randomZ * -1 : randomZ;
      int totalZ = (int) (position.z() + randomZ);

      int totalY = world.getHighestBlockYAt(
          totalX, totalZ, HeightMap.MOTION_BLOCKING_NO_LEAVES
      );


      if (totalY <= world.getMinHeight()
          || totalY >= world.getMaxHeight() - 2) {
        continue;
      }


      //Block unter der Kiste
      Block ground = world.getBlockAt(totalX, totalY - 1, totalZ);
      //Kistenblock
      Block block = world.getBlockAt(totalX, totalY, totalZ);


      if (!ground.getType().isSolid()
          || ground.isLiquid()
          || Tag.LOGS.isTagged(ground.getType())) {
        continue;
      }


      // dh mindestens ein block darüber ist noch luft
      if (!block.getType().isAir()
          || !block.getRelative(0, 1, 0).getType().isAir()) {
        continue;
      }


      // position = new Location(position.getWorld(), position.x() + randomX, position.z() + randomZ, player.getY());


      boolean nearBlocks = false;

      boolean blocked = false;

      for (int dx = -1; dx <= 1 && !blocked; dx++) {
        for (int dz = -1; dz <= 1 && !blocked; dz++) {
          for (int dy = 0; dy <= 2; dy++) {
            if(dx==0 && dz == 0 && dy == 0){
              continue;
            }
            Block nearbyBlock = world.getBlockAt(totalX + dx, totalY + dy, totalZ + dz);
            if(!nearbyBlock.getType().isAir()){
              blocked = true;
              break;
            }


          }

        }
      }

      if (blocked) {
        continue;
      }



      Block chestBlock = world.getBlockAt(block.getLocation());
      chestBlock.setType(Material.CHEST);


      Chest chest = (Chest) chestBlock.getState();

      List<CombatLoot> loot = new ArrayList<>(
          Arrays.asList(CombatLoot.values())
      );

      Collections.shuffle(loot);

      int count = ThreadLocalRandom.current().nextInt(1, 5);

      for (int i = 0; i < count; i++) {
        chest.getBlockInventory().addItem(
            loot.get(i).createItemStack()
        );
      }

      world.strikeLightningEffect(
          chestBlock.getLocation()
      );




    }
  }

  @Override
  public boolean onCommand(
      CommandSender sender,
      Command command,
      String label,
      String[] args) {

    if (args.length != 1) {
      sender.sendMessage("§e/dome start | join | leave | stop");
      return true;
    }

    if (args[0].equalsIgnoreCase("start")) {

      if (!sender.hasPermission("myfirstplugin.dome.admin")) {
        sender.sendMessage("§cKeine Berechtigung!");
        return true;
      }

      if (running) {
        sender.sendMessage("§cDie Runde läuft bereits!");
        return true;
      }

      Location center = world.getSpawnLocation();
      WorldBorder border = world.getWorldBorder();

      // Mittelpunkt exakt auf Blockraster ausrichten
      border.setCenter(
          center.getBlockX(),
          center.getBlockZ()
      );

      border.setSize(400.0);
      //no buffer
      border.setDamageBuffer(0.0);
      border.setDamageAmount(1.0);
      border.setWarningDistance(10);

      running = true;

      // WorldBorder schrumpft DURATION lang
      for (int i = 1; i <= 5; i++) {
        final int phase = i;
        shrinkTasks.add(
            Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {
                  if (!running) {
                    return;
                  }


                  border.changeSize(400 - ((360 / 5) * phase), DURATION / 5);

                  if (phase != 1) {
                    world.getPlayers().stream().forEach(player -> player.sendTitle(
                        "§6Map eingegrenzt",
                        "§cKämpft bis zum letzten Spieler!",
                        10, 70, 20
                    ));
                    // now random event occurs that
                    for (Player player : getAliveParticipants()) {
                        spawnLootChest(player, phase);

                    }
                  }


                }, DURATION / 5 * (phase - 1)
            )
        );


      }

      finalTask = Bukkit.getScheduler().runTaskLater(
          plugin,
          () -> {
            if (!running) {
              return;
            }

            border.changeSize(20, 0);

            DomeFinalArena.createCircle(world, center);

            for (Player player : world.getPlayers()) {
              player.teleport(new Location(
                  world,
                  center.getX(),
                  center.getY() + 101,
                  center.getZ()
              ));

              player.sendTitle(
                  "§6FINAL BATTLE!",
                  "§cKämpft bis zum letzten Spieler!",
                  10, 70, 20
              );
            }
          },
          DURATION
      );


      sender.sendMessage("§aDome PvP gestartet!");
      return true;
    }

    if (args[0].equalsIgnoreCase("join")) {

      if (!(sender instanceof Player player)) {
        sender.sendMessage("Nur Spieler können beitreten!");
        return true;
      }

      if (!running) {
        player.sendMessage("§cEs läuft keine PvP-Runde!");
        return true;
      }

      if (isEliminated(player)) {
        player.sendMessage("§cDu bist bereits ausgeschieden!");
        return true;
      }


      if (!participants.add(player.getUniqueId())) {
        player.sendMessage("§eDu bist bereits Teilnehmer!");
        return true;
      }

      player.setGameMode(GameMode.SURVIVAL);

      player.teleport(world.getSpawnLocation());
      player.sendMessage("§aWillkommen in der PvP-Arena!");
      return true;
    }


    if (args[0].equalsIgnoreCase("leave")) {
      if (!sender.hasPermission("myfirstplugin.dome.admin")) {
        sender.sendMessage("§cKeine Berechtigung!");
        return true;
      }

      if (!running) {
        sender.sendMessage("§cEs läuft keine Runde!");
        return true;
      }

      if (sender instanceof Player) {
        Player player = (Player) sender;
        if (world.getPlayers().contains(player) && player.isOnline()) {
          List<UUID> list = participants.stream().filter(participantId -> Bukkit.getPlayer(participantId).equals(sender)).toList();
          participants.remove(list.get(0));
          World hub = Bukkit.getWorld("world");
          player.teleport(hub.getSpawnLocation());
        } else {
          sender.sendMessage("Sender ist nicht mehr online/ nicht Teilnehmer");
        }


      } else {
        sender.sendMessage("Sender ist kein Spieler");
      }


    }

    if (args[0].equalsIgnoreCase("stop")) {

      if (!sender.hasPermission("myfirstplugin.dome.admin")) {
        sender.sendMessage("§cKeine Berechtigung!");
        return true;
      }

      if (!running) {
        sender.sendMessage("§cEs läuft keine Runde!");
        return true;
      }


      endRound(null);

      sender.sendMessage("§cPvP-Runde beendet!");
      return true;
    }

    sender.sendMessage("§e/dome start | join | stop");
    return true;
  }

  public void endRound(Player winner) {
    if (!running) return;

    running = false;

    if (finalTask != null) {
      finalTask.cancel();
      finalTask = null;
    }

    shrinkTasks.forEach(shrinkTask -> shrinkTask.cancel());
    shrinkTasks.clear();


    this.eliminated.clear();
    this.participants.clear();

    world.getWorldBorder().setSize(400.0);

    if (winner != null) {
      Bukkit.broadcastMessage(
          "§6" + winner.getName() + " hat Dome PvP gewonnen!"
      );
    }
  }


  public JavaPlugin getPlugin() {
    return plugin;
  }

  public World getLobby() {
    return Bukkit.getWorld("world");
  }


}