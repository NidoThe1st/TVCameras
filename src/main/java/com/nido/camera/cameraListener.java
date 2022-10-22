package com.nido.camera;

import me.makkuusen.timing.system.track.Track;
import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;

public class cameraListener implements Listener {
    Camera plugin = Camera.getInstance();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        //check if player is inside a boat
        if(p.isInsideVehicle() && p.getVehicle() instanceof Boat) {
            //check if player is followed

            if(plugin.isFollowed(p)) {
                Location followedLocation = p.getLocation();
                Cam closest = null;
                //checking for a camera closest to the followed player
                Track track = Utils.getClosestTrack(p);

                for (Cam camera : plugin.getCameras()) {
                    if (camera.getTrack() == track) {
                        if (closest != null) {
                            if (closest.getLocation().distance(followedLocation) > camera.getLocation().distance(followedLocation)) {
                                closest = camera;
                            }
                        } else {
                            closest = camera;
                        }
                    }
                }

                ArrayList<Player> followers = plugin.getFollowers(p);
                //tp all followers to the closest camera
                if (closest != null) {
                    for (Player follower : followers) {
                        if (plugin.haveCamera(follower) && plugin.getCurrentCamera(follower).equals(closest)) {
                        } else {
                            closest.tpPlayer(follower);
                            plugin.setCurrentCamera(follower, closest);
                        }
                    }

                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){

        Player p = e.getPlayer();
        plugin.onQuit(p);

    }
}
