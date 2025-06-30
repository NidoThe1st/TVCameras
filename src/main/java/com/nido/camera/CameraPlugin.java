package com.nido.camera;

import co.aikar.commands.PaperCommandManager;
import co.aikar.idb.*;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import lombok.Getter;
import me.makkuusen.timing.system.track.Track;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class CameraPlugin extends JavaPlugin {
    static CameraPlugin plugin = CameraPlugin.getInstance();
    public FileConfiguration config = getConfig();
    HashMap<Player, CamPlayer> cameraPlayers = new HashMap<>();
    //get all cameras
    @Getter
    List<Camera> cameras = new ArrayList<>();
    //add a new camera with the name (addedcamera)
    public void addCamera(Camera camera){
        if(getCamera(camera.getTrack(), camera.getIndex()) == null) {
            cameras.add(camera);
        } else {
            cameras.remove(getCamera(camera.getTrack(), camera.getIndex()));
            cameras.add(camera);
        }

    }
    //get camera with for the track by the index
    public Camera getCamera(Track track, int index){
        for (Camera camera: cameras) {
            if(camera.getIndex() == index && camera.getTrack() == track) {return camera;}
        }
        return null;
    }

    // FIX FIX FIX
    public void getTrackCameras(Player player){
        List<Integer> trackcameras = new ArrayList<>();
        StringBuilder tracks = new StringBuilder("This track has cameras with index ");
        CamPlayer camPlayer = plugin.getPlayer(player);
        for (Camera camera: cameras){
            if (camera.getTrack() == camPlayer.getEditing()) {
                Integer camIndex = camera.getIndex();
                trackcameras.add(camIndex);
            }
        }
        for (int index : trackcameras) {
            tracks.append(index).append(" ");
        }
        tracks.deleteCharAt(tracks.length() - 1);
        player.sendMessage(ChatColor.AQUA + tracks.toString());
    }

    public static CameraPlugin getInstance() {
        return plugin;
    }

    public void onQuit(Player player){
        //removes player from hashmaps
        CamPlayer camPlayer = getPlayer(player);
        camPlayer.stopFollowing();
        List<Player> followers = new ArrayList<>(camPlayer.getFollowers());
        if(!followers.isEmpty()) {
            for (Player follower : followers) {
                getPlayer(follower).stopFollowing();
            }
        }
        DB.executeUpdateAsync("UPDATE `Camera_Players` set `DISABLED` = ? WHERE `UUID` = ?;", Utils.disabledToString(camPlayer.getDisabledCameras()), player.getUniqueId());
        cameraPlayers.remove(player);
    }
    private void loadCameras() throws SQLException {
        //gets the data for the cameras from the database
        var locations = DB.getResults("SELECT * FROM `Cameras`;");
        //going through every result in the database
        for (DbRow dbRow : locations) {
            Camera camera = new Camera(dbRow);
            addCamera(camera);
        }


    }
    public void saveNewCamera(Camera tempcamera) {
        if(getCamera(tempcamera.getTrack(), tempcamera.getIndex()) == null) {
            try {
                DB.executeInsert("INSERT INTO `Cameras` (`LABEL`, `REGION`, `INDEX`, `TRACKID`, `CAMPOSITION`, `REGIONTYPE`) VALUES('" + tempcamera.getLabel() + "', '" + tempcamera.getMinMax() + "', '" + tempcamera.getIndex() + "', '" + tempcamera.getTrack().getId() + "', '" + Utils.locationToString(tempcamera.getLocation()) + "', '" + tempcamera.getRegionType() + "');");
                var camerarow = DB.getFirstRow("SELECT * FROM `Cameras` WHERE `TRACKID` = '" + tempcamera.getTrack().getId() + "' AND `INDEX` = '" + tempcamera.getIndex() + "';");
                addCamera(new Camera(camerarow));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //if camera already exists
        else {
            DB.executeUpdateAsync("UPDATE `Cameras` SET `CAMPOSITION` = '" + Utils.locationToString(tempcamera.getLocation()) + "', `REGION` = '" + tempcamera.getMinMax() + "', `LABEL` = '" + tempcamera.getLabel() + "', `REGIONTYPE` = '" + tempcamera.getRegionType() + "' WHERE `TRACKID` = '" + tempcamera.getTrack().getId() + "' AND `INDEX` = '" + tempcamera.getIndex() + "';");
            addCamera(tempcamera);
        }
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        config.options().copyDefaults(true);
        saveConfig();
        String username = config.getString("username");
        String password = config.getString("password");
        String database = config.getString("database");
        int port = config.getInt("port");
        String host = config.getString("host");
        assert username != null;
        assert password != null;
        assert database != null;
        PooledDatabaseOptions options = BukkitDB.getRecommendedOptions(this, username, password, database, host + ":" + port);
        options.setDataSourceProperties(new HashMap<>() {{
            put("useSSL", false);
        }});
        options.setMinIdleConnections(5);
        options.setMaxConnections(10);
        Database db = new HikariPooledDatabase(options);
        DB.setGlobalDatabase(db);
        getServer().getPluginManager().registerEvents(new CameraListener(), this);
        CameraCommands.init(plugin);
        //load data from db
        try {
            try {
                DB.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS `Cameras` (
                          `ID` int(11) NOT NULL AUTO_INCREMENT,
                          `TRACKID` int(11) NOT NULL,
                          `INDEX` int(11) NOT NULL,
                          `REGION` varchar(255) NOT NULL,
                          `CAMPOSITION` varchar(255) NOT NULL,
                          `LABEL` varchar(255) DEFAULT NULL,
                          `REGIONTYPE` varchar(255) DEFAULT NULL,
                          PRIMARY KEY (`id`)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;""");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                DB.executeUpdate("""
                    ALTER TABLE `Cameras`
                    ADD COLUMN `REGIONTYPE` varchar(255) DEFAULT NULL;
                """);
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate column name")) {
                    e.printStackTrace();
                }
            }
            loadCameras();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // --NEW--
        try {
            DB.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS `Camera_Players` (
                          `ID` int(11) NOT NULL AUTO_INCREMENT,
                          `UUID` varchar(255) NOT NULL,
                          `DISABLED` MEDIUMTEXT DEFAULT NULL,
                          PRIMARY KEY (`id`)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;""");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        CameraEditor cameraEditor = new CameraEditor();
        cameraEditor.startParticleSpawner(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        DB.close();
    }

    public boolean removeCamera(int regionIndex, Track track) {
        if (getCamera(track, regionIndex) != null) {
            Camera camera = getCamera(track, regionIndex);
            cameras.remove(camera);
            assert camera != null;
            DB.executeUpdateAsync("DELETE FROM `Cameras` WHERE `TRACKID` = " + camera.getTrack().getId() + " AND `INDEX` = " + camera.getIndex() + ";");
            return true;
        }
        return false;
    }
    public CamPlayer getPlayer(Player p) {
        return cameraPlayers.get(p);
    }
    public void newCamPlayer(Player p) {
        DB.executeUpdateAsync("INSERT INTO `Camera_Players` (`UUID`) VALUES(?);", p.getUniqueId());
        if(!cameraPlayers.containsKey(p)) {
            cameraPlayers.put(p, new CamPlayer(p));
        }
    }
    public void addCamPlayer(Player p, DbRow row) {
        List<Integer> disabled = Utils.stringToDisabled(row.getString("DISABLED"));
        cameraPlayers.put(p, new CamPlayer(p, disabled));
    }
    public WorldEditPlugin getWorldEdit(){
        Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");

        if (p instanceof WorldEditPlugin){
            return (WorldEditPlugin) p;
        }else {
            return null;
        }
    }
}
