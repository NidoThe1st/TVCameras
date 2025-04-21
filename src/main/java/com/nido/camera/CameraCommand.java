package com.nido.camera;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;


@CommandAlias("camera|cam")
public class CameraCommand extends BaseCommand {

    static Camera plugin = Camera.getInstance();
    static Utils utils = new Utils();
    @HelpCommand
    public static void onHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Cameras 1.0 help");
        sender.sendMessage(ChatColor.AQUA + "/camera edit");
        sender.sendMessage(ChatColor.DARK_AQUA + "- Enter edit mode for the track you are currently on");
        sender.sendMessage(ChatColor.AQUA + "/camera set [index]");
        sender.sendMessage(ChatColor.DARK_AQUA + "- Places a camera on your current position and orients it where you are looking. Before that you need to make a cuboid region by taking a stick and selecting one corner with left click and the other with right");
        sender.sendMessage(ChatColor.AQUA + "/camera view <index>");
        sender.sendMessage(ChatColor.DARK_AQUA + "- Teleports you to the camera you specified");
        sender.sendMessage(ChatColor.AQUA + "/camera follow <Player>");
        sender.sendMessage(ChatColor.DARK_AQUA + "- Automatically teleports you to the camera inside a region where the target player is located");
        sender.sendMessage(ChatColor.AQUA + "/camera stopfollow");
        sender.sendMessage(ChatColor.DARK_AQUA + "- You stop following a player");
    }

    @Default
    public static void onCam(Player player){

        Player p = player;

        Inventory inv = Bukkit.createInventory(p, 54, "Camera Menu");

        ItemBuilder test = new ItemBuilder(Material.ACACIA_DOOR, 1);
        test.setDisplayName("Enter if you dare!");
        test.setLore("Only for the really brave...");
        ItemStack horrorDoor = test.build();

        inv.setItem(22, horrorDoor);

        p.openInventory(inv);

    }

    @CommandPermission("cameras.edit")
    @Subcommand("edit|e")
    public static void onEdit(Player player) {
        // Get player from the hashmap
        CamPlayer camPlayer = plugin.getPlayer(player);
        // If player already editing stop, if not start
        if(!camPlayer.isEditing()) {
            camPlayer.startEditing(Utils.getClosestTrack(player));
            player.sendMessage(ChatColor.AQUA + "Started editing cameras at " + Utils.getClosestTrack(player).getDisplayName());
        } else {
            camPlayer.stopEditing();
            player.sendMessage(ChatColor.DARK_AQUA + "Stopped editing cameras.");
        }
    }
    @CommandPermission("cameras.set")
    @Subcommand("set|s")
    @CommandCompletion("<index>, [label]")
    public static void onCameraSet(Player player,  @Optional String index, @Optional String label) throws IncompleteRegionException {
        CamPlayer camPlayer = plugin.getPlayer(player);
        if(camPlayer.isEditing()) {
            int regionIndex;
            boolean remove = false;
            //check if index exists, if not automatically make the new camera next available index
            if (index != null) {
                remove = getParsedRemoveFlag(index);
                //checks if index is a valid number
                if (getParsedIndex(index) == null) {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "This is not a valid number!");
                    return;
                }
                regionIndex = getParsedIndex(index);
            } else {
                regionIndex = plugin.getCameras().size() + 1;
            }
            //checks if index is negative, if it is it deletes the camera
            if (remove) {
                if (plugin.removeCamera(regionIndex, camPlayer.getEditing())) {
                    player.sendMessage(ChatColor.DARK_AQUA + "Camera " + regionIndex + " was removed from track " + camPlayer.getEditing().getDisplayName());
                } else {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "There was an error removing the camera!");
                }
            } else {
                //test for worldedit integration
                Region s = plugin.getWorldEdit().getSession(player).getSelection();
                Vector maxP = utils.stringToVector(s.getMaximumPoint().toParserString());
                Vector minP = utils.stringToVector(s.getMinimumPoint().toParserString());
                if (s != null){
                    plugin.saveNewCamera(new Cam(player.getLocation(), camPlayer.getEditing(), regionIndex, minP, maxP, label));
                    player.sendMessage(ChatColor.AQUA + "Camera " + regionIndex + " was set to your position on track " + camPlayer.getEditing().getDisplayName());
                } else {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Invalid or missing selection");
                }
                //checks for selection, saves the camera
                /*if(camPlayer.getSelection1() != null && camPlayer.getSelection2() != null && camPlayer.getSelection1().getWorld() == camPlayer.getSelection2().getWorld()) {
                    plugin.saveNewCamera(new Cam(player.getLocation(), camPlayer.getEditing(), regionIndex, Utils.getMin(camPlayer.getSelection1(), camPlayer.getSelection2()), Utils.getMax(camPlayer.getSelection1(), camPlayer.getSelection2()), label));
                    player.sendMessage(ChatColor.AQUA + "Camera " + regionIndex + " was set to your position on track " + camPlayer.getEditing().getDisplayName());
                } else {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Invalid or missing selection");
                }*/
            }
        } else {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Enter edit mode first!");
        }
    }
    @CommandPermission("cameras.follow")
    @Subcommand("follow|f")
    @CommandCompletion("@players")
    public static void onFollow(Player follower, OnlinePlayer followed) {
        plugin.getPlayer(followed.getPlayer()).addFollower(follower);
        follower.sendMessage(ChatColor.AQUA + "You're now following " + followed.getPlayer().getName());
    }
    @CommandPermission("cameras.stopfollow")
    @Subcommand("stopfollow|sf")
    public static void onStopFollow(Player follower){
        plugin.getPlayer(follower).stopFollowing();
        follower.sendMessage( ChatColor.DARK_AQUA + "You stopped following!");
    }
    //checks if index should be removed (returns true if index starts with -)
    private static boolean getParsedRemoveFlag(String index) {
        return index.startsWith("-");
    }
    //gives an index that should be added/removed (removes +/-)
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
    @Subcommand("view|v")
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

    @CommandPermission("cameras.list")
    @Subcommand("list|l")
    public static void onListCameras(Player p){
        if (Utils.getClosestTrack(p) != null){
            plugin.getTrackCameras(p);
        }
    }

    @CommandPermission("cameras.menu")
    @Subcommand("menu|m")
    public static void onMenu(Player player){
        Utils.openMenu(player);
    }
}
