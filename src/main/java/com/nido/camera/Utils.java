package com.nido.camera;

import me.makkuusen.timing.system.track.Track;
import me.makkuusen.timing.system.track.TrackDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public abstract class Utils {

    public static Track getClosestTrack(Player p) {
        List<Track> tracks = TrackDatabase.getTracks();
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
    public static Vector getMin(Location a, Location b) {
        return Vector.getMinimum(a.toVector(), b.toVector());
    }
    public static Vector getMax(Location a, Location b) {
        return Vector.getMaximum(a.toVector(), b.toVector());
    }
}
