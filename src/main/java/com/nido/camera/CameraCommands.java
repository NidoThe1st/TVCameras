package com.nido.camera;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.regions.Region;
import me.makkuusen.timing.system.track.Track;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.awt.print.Paper;
import java.util.Arrays;
import java.util.Objects;


@CommandAlias("camera|cam")
public class CameraCommands extends BaseCommand {

    static CameraPlugin plugin = CameraPlugin.getInstance();

    public static void init(Plugin plugin){
        var manager = new PaperCommandManager(plugin);
        manager.enableUnstableAPI("brigadier");

        initCompletions(manager.getCommandCompletions());
        initCommands(manager);
    }

    static void initCompletions(CommandCompletions<BukkitCommandCompletionContext> completions){
        registerEnumCompletion(completions, "regionType", Camera.RegionType.class);
    }

    static void initCommands(PaperCommandManager manager){
        manager.registerCommand(new CameraCommands());
    }

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

    @CommandPermission("cameras.edit")
    @Subcommand("edit|e")
    public static void onEdit(Player player) {
        // Get player from the hashmap
        CamPlayer camPlayer = plugin.getPlayer(player);
        // If player already editing stop, if not start
        if(!camPlayer.isEditing()) {
            Track track = Utils.getClosestTrack(player);
            camPlayer.startEditing(track);
            player.sendMessage(ChatColor.AQUA + "Started editing cameras at " + track.getDisplayName());
        } else {
            camPlayer.stopEditing();
            player.sendMessage(ChatColor.DARK_AQUA + "Stopped editing cameras.");
        }
    }
    @CommandPermission("cameras.set")
    @Subcommand("set|s")
    @CommandCompletion("[regionType], <index>, [label]")
    public static void onCameraSet(Player player, String regionType ,@Optional String index, @Optional String label) throws IncompleteRegionException {
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
            if (remove) {
                if (plugin.removeCamera(regionIndex, camPlayer.getEditing())) {
                    player.sendMessage(ChatColor.DARK_AQUA + "Camera " + regionIndex + " was removed from track " + camPlayer.getEditing().getDisplayName());
                } else {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "There was an error removing the camera!");
                }
            } else {
                Region s = Objects.requireNonNull(plugin.getWorldEdit()).getSession(player).getSelection();
                Vector maxP = Utils.stringToVector(s.getMaximumPoint().toParserString());
                Vector minP = Utils.stringToVector(s.getMinimumPoint().toParserString());
                plugin.saveNewCamera(new Camera(player.getLocation(), camPlayer.getEditing(), regionIndex, minP, maxP, label, regionType));
                player.sendMessage(ChatColor.AQUA + "Camera " + regionIndex + " was set to your position on track " + camPlayer.getEditing().getDisplayName());
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

    @CommandPermission("cameras.view")
    @Subcommand("view|v")
    @CommandCompletion("<index>")
    public static void onViewCamera(Player player, int index){
        //checks if the index and the track actually exist
        Track track = Utils.getClosestTrack(player);
        if(plugin.getCamera(track, index) != null) {
            Camera camera = plugin.getCamera(track, index);
            assert camera != null;
            camera.tpPlayer(player);
            player.sendMessage(ChatColor.AQUA + "Teleported to camera number " + index + " on track " + track.getDisplayName());
        }
    }

    @CommandPermission("cameras.list")
    @Subcommand("list|l")
    public static void onListCameras(Player player){
        CamPlayer camPlayer = plugin.getPlayer(player);
        if (camPlayer.getEditing() != null){
            plugin.getTrackCameras(player);
        }
    }

    @CommandPermission("cameras.menu")
    @Subcommand("menu|m")
    public static void onMenu(Player player){
        Utils.openMenu(player);
    }

    @CommandPermission("cameras.info")
    @Subcommand("info")
    @CommandCompletion("<index>")
    public static void onInfo(Player player, int index){
        Track track = Utils.getClosestTrack(player);
        Camera camera = plugin.getCamera(track, index);
        player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Region type: " + camera.getRegionType());
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

    static void registerEnumCompletion(CommandCompletions<?> completions, String name, Class<? extends Enum<?>> enumClass) {
        completions.registerAsyncCompletion(name, ctx -> {
            return Arrays.stream(enumClass.getEnumConstants())
                    .map(Enum::name)
                    .map(String::toLowerCase)
                    .toList();
        });
    }

}
