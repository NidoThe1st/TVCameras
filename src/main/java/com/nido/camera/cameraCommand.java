package com.nido.camera;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class cameraCommand implements CommandExecutor {

    Camera plugin = Camera.getInstance();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        //check if player did a command
        if (sender instanceof Player){

            Player player = (Player) sender;
            //check which command it is
            if (Objects.equals(args[0], "set") || Objects.equals(args[0], "s")){

                if (args[1] != null) {
                    //places a camera to players location
                    Location cameralocation = player.getLocation();
                    Cam camera = new Cam(cameralocation);
                    //adds the camera to the hashmap
                    plugin.addCamera(camera, args[1]);
                    player.sendMessage(ChatColor.AQUA + "Position set for " + args[1]);
                    return true;
                }
            }
            else if (Objects.equals(args[0], "follow") || Objects.equals(args[0], "f")){
                
                if (Bukkit.getPlayer(args[1]) != null) {
                    Player followed = Bukkit.getPlayer(args[1]);
                    ArrayList<Player> followers = new ArrayList<>();
                    followers.add(player);
                    plugin.addFollowed(followed, followers);
                }
                return true;
            }
            else if (args[0].equalsIgnoreCase("StopFollowing") || Objects.equals(args[0], "sf")){
                Player followed = plugin.whoFollowed(player);
                plugin.removeFollower(followed, player);
                return true;
            }
            else {

                if (plugin.isCamera(args[0])){

                    Cam camera = plugin.getCamera(args[0]);
                    camera.tpPlayer(player);
                    player.sendMessage(ChatColor.AQUA + "Teleported to " + args[0]);
                    return true;
                }

            }
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You oh deared up bro");
        }

        return false;
    }
}
