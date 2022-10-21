package com.nido.camera;

import io.papermc.paper.entity.LookAnchor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;

public class cameraListener implements Listener {
    Camera plugin = Camera.getInstance();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        //check if player is inside a boat
        if(p.isInsideVehicle() && p.getVehicle() instanceof Boat) {
            //check if player is followed
            if(plugin.isFollowed(p)){
                Location followedLocation = p.getLocation();
                Cam closest = null;
                //checking for a camera closest to the followed player
                for (Cam camera: plugin.getCameras().values()) {
                    if(closest != null) {
                        if(closest.getLocation().distance(followedLocation) > camera.getLocation().distance(followedLocation)) {
                            closest =  camera;
                        }
                    } else {closest = camera;}
                }
                ArrayList<Player> followers = plugin.getFollowers(p);
                //tp all players to closest camera, make them look at the followed player
                for (Player follower: followers) {
                    if(plugin.haveCamera(follower) && plugin.getCurrentCamera(follower).equals(closest)) {}
                    else{
                        closest.tpPlayer(follower);
                        follower.lookAt(p, LookAnchor.EYES, LookAnchor.EYES);
                        plugin.setCurrentCamera(follower, closest);
                    }
                }

            }
        }
    }

}
