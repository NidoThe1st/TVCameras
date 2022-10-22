package com.nido.camera;

import me.makkuusen.timing.system.track.Track;
import me.makkuusen.timing.system.track.TrackDatabase;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class Utils {

    public static Track getClosestTrack(Player p) {
        List<Track> tracks = TrackDatabase.getTracks();
        Location playerLoc = p.getLocation();
        Track closest = null;
        for (Track track: tracks) {
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
        return closest;
    }
}
