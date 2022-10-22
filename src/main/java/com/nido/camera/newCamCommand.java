package com.nido.camera;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@CommandAlias("camera|cam")
public class newCamCommand extends BaseCommand {

    static Camera plugin;
    @HelpCommand
    public static void onHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Cameras 1.0 help");
        sender.sendMessage(ChatColor.AQUA + "/camera set (index)");
        sender.sendMessage(ChatColor.AQUA + "/camera view <index>");
        sender.sendMessage(ChatColor.AQUA + "/camera follow <Player>");
        sender.sendMessage(ChatColor.AQUA + "/camera stopfollow");
    }
    @CommandPermission("cameras.set")
    @Subcommand("set")
    @CommandCompletion("<index>")
    public static void onCameraSet(Player player,  @Optional String index) {
        int regionIndex;
        boolean remove = false;
        //check if index exists
        if (index != null) {
            remove = getParsedRemoveFlag(index);
            //checks if index is a valid number
            if (getParsedIndex(index) == null) {
                player.sendMessage("messages.error.numberException");
                return;
            }
            regionIndex = getParsedIndex(index);
        } else {
            regionIndex = plugin.getCameras().size() + 1;
        }
        if (remove) {
            if (plugin.removeCamera(regionIndex, Utils.getClosestTrack(player))) {
                player.sendMessage("messages.remove.region");
            } else {
                player.sendMessage("messages.error.remove.region");
            }
        } else {
            plugin.saveNewCamera(new Cam(player.getLocation(), Utils.getClosestTrack(player), regionIndex));
            player.sendMessage("messages.create.region");
        }
    }
    @CommandPermission("cameras.follow")
    @Subcommand("follow|f")
    @CommandCompletion("@players")
    public static void onFollow(Player follower, OnlinePlayer followed) {
        ArrayList<Player> followers = new ArrayList<>();
        followers.add(follower);
        plugin.addFollowed(followed.getPlayer(), followers);
        follower.sendMessage(ChatColor.AQUA + "You're now following " + followed.getPlayer().name());
    }
    @CommandPermission("cameras.stopfollow")
    @Subcommand("stopfollow|sf")
    public static void onStopFollow(Player follower){
        Player followed = plugin.whoFollowed(follower);
        if(followed != null){
            plugin.removeFollower(followed, follower);
        }
        follower.sendMessage( ChatColor.AQUA + "You stopped following!");
    }
    //checks if index should be removed
    private static boolean getParsedRemoveFlag(String index) {
        return index.startsWith("-");
    }
    //gives an index that should be added/removed
    private static Integer getParsedIndex(String index) {
        if (index.startsWith("-")) {
            index = index.substring(1);
        } else if (index.startsWith("+")) {
            index = index.substring(1);
        }
        try {
            return Integer.parseInt(index);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
    @CommandPermission("cameras.view")
    @Subcommand("view/v")
    @CommandCompletion("<index>")
    public static void onViewCamera(Player player, int index){
        //checks if the index and the track actually exist
        if(plugin.getCamera(Utils.getClosestTrack(player), index) != null) {
            Cam camera = plugin.getCamera(Utils.getClosestTrack(player), index);
            assert camera != null;
            camera.tpPlayer(player);
            player.sendMessage(ChatColor.AQUA + "Teleported to camera number " + index + " on track " + Utils.getClosestTrack(player).getDisplayName());
        }
    }
}
