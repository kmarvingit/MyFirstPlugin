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
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import javax.print.attribute.standard.Destination;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class DomeCommand implements CommandExecutor {

  private final JavaPlugin plugin;
  private World world;

  private boolean running = false;
  private BukkitTask finalTask;
  private List<BukkitTask> shrinkTasks = new ArrayList<>();


  private static final long DURATION = 20L * 60 * 100;

  private final Set<UUID> participants = new HashSet<>();
  private final Set<UUID> eliminated = new HashSet<>();


  public DomeCommand(JavaPlugin plugin) {
    this.plugin = plugin;
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

  private final Set<Location> spawnedChests = new HashSet<>();


  public boolean coversAccessible(Location center, int radius, int baseY) {
    World world = center.getWorld();

    int centerX = center.getBlockX();
    int centerZ = center.getBlockZ();

    int numberOfWalkableSides = 0;

    // 0 = Norden, 1 = Süden, 2 = Westen, 3 = Osten
    for (int side = 0; side < 4; side++) {

      int numberOfWalkableBlocks = 0;
      int totalBlocks = 2 * radius + 1;

      for (int i = -radius; i <= radius; i++) {

        int x;
        int z;

        // north coordinate is negative (z)
        switch (side) {
          case 0 -> { // North
            x = centerX + i;
            z = centerZ - radius - 1;
          }
          case 1 -> { // South
            x = centerX + i;
            z = centerZ + radius + 1;
          }
          case 2 -> { // West
            x = centerX - radius - 1;
            z = centerZ + i;
          }
          default -> { // East
            x = centerX + radius + 1;
            z = centerZ + i;
          }
        }

        int groundY = world.getHighestBlockYAt(
            x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES
        );

        int diff = groundY - baseY;

        // maximum: 3 blocks (bottom); 1 block (top)
        if (diff < -3 || diff > 1) {
          continue;
        }

        Block ground = world.getBlockAt(x, groundY, z);
        Block feet = world.getBlockAt(x, groundY + 1, z);
        Block head = world.getBlockAt(x, groundY + 2, z);

        if (ground.getType().isSolid()
            && feet.isPassable()
            && head.isPassable()
            && !feet.isLiquid()
            && !head.isLiquid()) {
          numberOfWalkableBlocks++;
        }
      }

      double percentage =
          (double) numberOfWalkableBlocks / totalBlocks;

      if (percentage >= 0.3) {
        numberOfWalkableSides++;
      }
    }

    return numberOfWalkableSides >= 2;
  }

  /*
  tries at baseY
   */
  private void flattenArea(World world, Location center, int radius, int baseY) {
    int centerX = center.getBlockX();
    int centerZ = center.getBlockZ();

    for (int x = centerX - radius; x <= centerX + radius; x++) {
      for (int z = centerZ - radius; z <= centerZ + radius; z++) {


        int highestY = world.getHighestBlockYAt(
            x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES
        );

        // Wenn Gelände höher als baseY ist -> nach unten abschneiden
        if (highestY > baseY) {
          for (int y = highestY; y > baseY; y--) {
            world.getBlockAt(x, y, z).setType(Material.AIR, false);
          }
        }

        // Wenn Gelände tiefer als baseY ist -> auffüllen
        if (highestY < baseY) {
          for (int y = highestY + 1; y < baseY; y++) {
            world.getBlockAt(x, y, z).setType(Material.DIRT, false);
          }
        }

        // Oberfläche setzen
        world.getBlockAt(x, baseY - 2, z).setType(Material.DIRT, false);
        world.getBlockAt(x, baseY - 1, z).setType(Material.DIRT, false);


        world.getBlockAt(x, baseY, z).setType(Material.GRASS_BLOCK, false);


        // bisschen Luftraum freiräumen
        for (int y = baseY + 1; y <= baseY + 300; y++) {
          world.getBlockAt(x, y, z).setType(Material.AIR, false);
        }
      }
    }


    DomeFinalArena.createCircle(new Location(world, center.x(), baseY, center.z()));
  }


  private void spawnLootChest(Player player, int phase) {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    Location playerPosition = player.getLocation();

    // Spaetere Phasen: Kisten erscheinen naeher beim Spieler.
    int radius = Math.max(15, Math.min(400, 100) - (Math.min(400, 100) / (5) * phase));

    for (int attempt = 0; attempt < 300; attempt++) {
      int x = playerPosition.getBlockX() + random.nextInt(-radius, radius + 1);
      int z = playerPosition.getBlockZ() + random.nextInt(-radius, radius + 1);

      // Block über dem höchsten Block
      int y = world.getHighestBlockYAt(
          x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES) + 1;


      if (!world.getBlockAt(x, y, z).getType().isAir()) {
        continue;
      }

      if (y <= world.getMinHeight() || y >= world.getMaxHeight() - 1) {
        continue;
      }

      if (!isChestPositionSuitable(x, y, z)) {
        continue;
      }

      Block block = world.getBlockAt(x, y, z);
      for (Player playerInside : world.getPlayers()) {
        plugin.getLogger().info("Spieler teleportiert");
        playerInside.teleport(new Location(world, x - 3, y + 2, z - 3));
      }


      block.setType(Material.CHEST);
      Chest chest = (Chest) block.getState();

      // 1 bis 4 verschiedene zufaellige Kampfitems.
      List<CombatLoot> loot = new ArrayList<>(Arrays.asList(CombatLoot.values()));
      Collections.shuffle(loot);
      int count = random.nextInt(1, Math.min(4, loot.size()) + 1);

      for (int i = 0; i < count; i++) {
        Random randomGen = new Random();
        Integer randomNumb;
        do {
          randomNumb = randomGen.nextInt(0, chest.getBlockInventory().getSize());
        } while (chest.getBlockInventory().getItem(randomNumb) != null);

        chest.getBlockInventory().setItem(randomNumb, loot.get(i).createItemStack());
      }

      spawnedChests.add(block.getLocation());
      //world.strikeLightningEffect(block.getLocation().add(0.5, 0, 0.5));
      return; // Erfolgreich: nur eine Kiste pro Spieler/Phase.
    }

    plugin.getLogger().info("Kein geeigneter Platz fuer eine Loot-Kiste bei "
        + player.getName() + " (Phase " + phase + ") gefunden.");
  }

  private boolean isChestPositionSuitable(int x, int y, int z) {
    Block chestBlock = world.getBlockAt(x, y, z);
    Block ground = world.getBlockAt(x, y - 1, z);

    // Kiste muss auf festem Boden stehen, nicht auf einem Baum.
    Material groundType = ground.getType();
    if (!groundType.isSolid()
        || ground.isLiquid()
        || Tag.LOGS.isTagged(groundType)
        || Tag.LEAVES.isTagged(groundType)) {
      return false;
    }

    // An der Kistenposition und direkt darueber muss Luft sein.
    if (!chestBlock.getType().isAir()
        || !chestBlock.getRelative(0, 1, 0).getType().isAir()) {
      return false;
    }

    WorldBorder border = world.getWorldBorder();

    double halfSize = border.getSize() / 2.0;

    double minX = border.getCenter().getX() - halfSize;
    double maxX = border.getCenter().getX() + halfSize;

    double minZ = border.getCenter().getZ() - halfSize;
    double maxZ = border.getCenter().getZ() + halfSize;

    //muss genau reinpassen
    if (x < minX || x + 1 > maxX
        || z < minZ || z + 1 > maxZ) {
      return false;
    }


    // Nur die vier direkten Nachbarn, statt eines 3x3x3-Luftwuerfels.
    int[][] directions = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };
    int freeSides = 0;

    // iterieren über {1,0}, {-1,0} usw; passable und block darüber
    for (int[] direction : directions) {
      Block side = world.getBlockAt(
          x + direction[0], y, z + direction[1]);
      Block aboveSide = side.getRelative(0, 1, 0);

      // Auch Gras und Blumen sind passierbar; Wasser zaehlt nicht, obwohl auch passable
      if (side.isPassable() && !side.isLiquid()
          && aboveSide.isPassable() && !aboveSide.isLiquid()) {
        freeSides++;
      }
    }

    return freeSides >= 3;
  }

  public World getArenaWorld() {
    return world;
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

      if (world != null) {
        sender.sendMessage("§cDie vorherige Arena ist noch geladen!");
        return true;
      }

// Jede Runde erhält einen neuen Namen


// Neuen zufälligen Seed erzeugen
      WorldCreator creator;
      int baseY;
      boolean foundWorld = false;


      Map<World, Integer> attemptedWorldsMap = new HashMap<>();
      do {

          String worldName = "dome_" + UUID.randomUUID();
          long seed = ThreadLocalRandom.current().nextLong();
          creator = new WorldCreator(worldName);
          creator.seed(seed);
          creator.environment(World.Environment.NORMAL);
          world = creator.createWorld();
          baseY =  world.getHighestBlockYAt(
              world.getSpawnLocation().getBlockX(),
              world.getSpawnLocation().getBlockZ(),
              HeightMap.MOTION_BLOCKING_NO_LEAVES
          );

          foundWorld = this.coversAccessible(world.getSpawnLocation(), 40, baseY);
          if(!foundWorld){
            Bukkit.unloadWorld(worldName, false);
          }

      } while (!foundWorld);

      final World finalWorld = world;
      baseY = world.getHighestBlockYAt(
          world.getSpawnLocation().getBlockX(),
          world.getSpawnLocation().getBlockZ(),
          HeightMap.MOTION_BLOCKING_NO_LEAVES
      );

      flattenArea(finalWorld, world.getSpawnLocation(), 40, baseY);

      world.setSpawnLocation(world.getSpawnLocation().getBlockX(), baseY + 1, world.getSpawnLocation().getBlockZ());


      WorldBorder border = world.getWorldBorder();

      // Mittelpunkt exakt auf Blockraster ausrichten
      border.setCenter(
          world.getSpawnLocation().getBlockX(),
          world.getSpawnLocation().getBlockZ()
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
                    finalWorld.getPlayers().stream().forEach(player -> player.sendTitle(
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

            //DomeFinalArena.createCircle(world, center);

            for (Player player : finalWorld.getPlayers()) {
              player.teleport(new Location(
                  finalWorld,
                  finalWorld.getSpawnLocation().getX(),
                  finalWorld.getSpawnLocation().getY() + 1,
                  finalWorld.getSpawnLocation().getZ()
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

  public void tryUnloadArena() {
    if (running || world == null) return;

    // Noch Spieler in der Arena?
    if (!world.getPlayers().isEmpty()) return;

    if (Bukkit.unloadWorld(world, false)) {
      world = null;
      plugin.getLogger().info("Arena entladen!");
    } else {
      plugin.getLogger().warning("Entladen fehlgeschlagen!");
    }
  }


  public void endRound(Player winner) {
    if (!running) return;

    world.getPlayers().stream().forEach(player -> player.teleport(getLobby().getSpawnLocation()));

    running = false;

    if (finalTask != null) {
      finalTask.cancel();
      finalTask = null;
    }

    shrinkTasks.forEach(shrinkTask -> shrinkTask.cancel());
    shrinkTasks.clear();


    for (Location location : spawnedChests) {
      Block block = location.getBlock();
      if (block.getType() == Material.CHEST) {
        block.setType(Material.AIR);
      }
    }


    spawnedChests.clear();
    eliminated.clear();
    participants.clear();

    world.getWorldBorder().setSize(400.0);

    Bukkit.getScheduler().runTask(plugin,
        () -> {
          if (Bukkit.unloadWorld(world, false)) {
            world = null;
          } else {
            plugin.getLogger().warning("Arena konnte nicht entladen werden!");
          }
        });


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