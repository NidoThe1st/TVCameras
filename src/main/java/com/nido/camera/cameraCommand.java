package com.nido.camera;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class cameraCommand implements CommandExecutor {

    Camera plugin = Camera.getInstance();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player){

            Player player = (Player) sender;

            if (Objects.equals(args[0], "set")){

                if (args[1] != null) {

                    Location cameralocation = player.getLocation();
                    Cam camera = new Cam(cameralocation);

                    plugin.addCamera(camera, args[1]);
                    player.sendMessage(ChatColor.AQUA + "Position set for " + args[1]);
                    return true;
                }
            }else {

                if (plugin.isCamera(args[0])){

                    Cam camera = plugin.getCamera(args[0]);
                    camera.tpPlayer(player);
                    player.sendMessage(ChatColor.AQUA + "Teleported to " + args[0]);
                    return true;
                }

            }
            player.sendMessage(ChatColor.RED + "You oh deared up bro");
        }

        return false;
    }
}
