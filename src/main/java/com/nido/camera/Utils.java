package com.nido.camera;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import me.makkuusen.timing.system.api.TimingSystemAPI;
import me.makkuusen.timing.system.track.Track;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.nido.camera.CameraCommand.plugin;

public class Utils {
    public static Track getClosestTrack(Player p) {
        List<Track> tracks = TimingSystemAPI.getTracks();
        Location playerLoc = p.getLocation();
        Track closest = null;
        for (Track track: tracks) {
            if(track.getSpawnLocation().getWorld() != null) {
                if(track.getSpawnLocation().getWorld().equals(playerLoc.getWorld())) {
                    if (closest != null) {
                        if (closest.getSpawnLocation().distance(playerLoc) > track.getSpawnLocation().distance(playerLoc)) {
                            closest = track;
                        }
                    } else {
                        closest = track;
                    }
                }
            }
        }
        return closest;
    }
    public static String locationToString(Location location) {
        if (location == null) {
            return null;
        }

        return location.getWorld().getName() + " " + location.getX() + " " + location.getY() + " " + location.getZ() + " " + location.getYaw() + " " + location.getPitch();
    }

    public static Location stringToLocation(String string) {
        if (string == null || string.length() == 0) {
            return null;
        }

        String[] split = string.split(" ");
        return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]));
    }
    public static Vector stringToVector(String s) {
        String[] xyz = s.split(",");
        return new Vector(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
    }

    public static Vector stringToVectorFromBV(String s) {
        String str = s.substring(1, s.length() - 1);
        String[] xyz = str.split(",");
        return new Vector(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
    }

    //serialization of data
    public static String disabledToString(List<Integer> cameraIds) {
        List<String> cameraNames = new ArrayList<>();
        cameraIds.forEach(camera -> cameraNames.add(camera.toString()));
        return String.join(",", cameraNames);
    }
    //deserialization of data
    public static List<Integer> stringToDisabled(String string) {
        String[] cameraidsString = string.split(",");
        List<Integer> cameraids = new ArrayList<>();
        for (String cameraidString: cameraidsString) {
            try {Integer cameraid = Integer.parseInt(cameraidString);
                cameraids.add(cameraid);
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
            }

        }
        return cameraids;
    }
    public static Vector getMin(Location a, Location b) {
        return Vector.getMinimum(a.toVector(), b.toVector());
    }
    public static Vector getMax(Location a, Location b) {
        return Vector.getMaximum(a.toVector(), b.toVector());
    }

    public static void openMenu(Player player){
        CamPlayer camPlayer = plugin.getPlayer(player);
        AtomicInteger counter = new AtomicInteger(10);
        Inventory inv = Bukkit.createInventory(player,54, ChatColor.AQUA.toString() + ChatColor.BOLD + "Camera Menu");
        HashMap<Integer, Cam> cameraitems = new HashMap<>();
        SkullBuilder cameraTemplate = new SkullBuilder(Material.PLAYER_HEAD, 1)
                .setDisplayName("Camera")
                .setOwner("e43a2867-f7f1-5dd2-9f3c-6eb405548153");
        for (Cam camera: plugin.getCameras()) {
            if(camera.getTrack() != Utils.getClosestTrack(player)) {continue;}
            if(camera.getLabel() != null) {
                cameraTemplate.setDisplayName(camera.getLabel());
            }else {
                cameraTemplate.setDisplayName(String.valueOf(camera.getIndex()));
            }
            if (camPlayer.isCameraDisabled(camera.getId())){
                cameraTemplate.setLore(ChatColor.RED + "This camera is disabled!");
            }
            else {
                cameraTemplate.setLore("Click to teleport!");
            }
            cameraTemplate.setAmmount(camera.getIndex());
            if(counter.get() + 1 % 9 == 0) {counter.addAndGet(2);}
            if(counter.get() > 43) {break;}
            cameraitems.put(counter.get(), camera);
            inv.setItem(counter.getAndAdd(1), cameraTemplate.build());
        }
        camPlayer.setCameraItems(cameraitems);
        camPlayer.setInv(true);
        player.openInventory(inv);
    }
}
