package com.nido.camera;

import co.aikar.commands.PaperCommandManager;
import co.aikar.idb.*;
import me.makkuusen.timing.system.track.Track;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Camera extends JavaPlugin {
    public FileConfiguration config = getConfig();
    HashMap<Player, CamPlayer> cameraPlayers = new HashMap<>();
    List<Cam> cameras = new ArrayList<>();
    //add a new camera with the name (addedcamera)
    public void addCamera(Cam camera){
        if(getCamera(camera.getTrack(), camera.getIndex()) == null) {
            cameras.add(camera);
        } else {
            cameras.remove(getCamera(camera.getTrack(), camera.getIndex()));
            cameras.add(camera);
        }

    }
    //get camera with for the track by the index
    public Cam getCamera(Track track, int index){
        for (Cam camera: cameras) {
            if(camera.getIndex() == index && camera.getTrack() == track) {return camera;}
        }
        return null;
    }

    //get all cameras
    public List<Cam> getCameras() {
        return cameras;
    }
    public void getTrackCameras(Player p){
        List<Integer> trackcameras = new ArrayList<>();
        StringBuilder tracks = new StringBuilder("This track has cameras with index ");
        for (Cam camera: cameras){
            if (camera.getTrack() == Utils.getClosestTrack(p)) {
                Integer camIndex = camera.getIndex();
                trackcameras.add(camIndex);
            }

            for (int index: trackcameras) {
                tracks.append(index).append(" ");
            }
            tracks.deleteCharAt(tracks.length() - 1);
        }
        p.sendMessage(ChatColor.AQUA + tracks.toString());
    }
    private static Camera instance;

    public static Camera getInstance() {
        return Camera.instance;
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
            Cam camera = new Cam(dbRow);
            addCamera(camera);
        }


    }
    public void saveNewCamera(Cam tempcamera) {
        if(getCamera(tempcamera.getTrack(), tempcamera.getIndex()) == null) {
            try {
                DB.executeInsert("INSERT INTO `Cameras` (`LABEL`, `REGION`, `INDEX`, `TRACKID`, `CAMPOSITION`) VALUES('" + tempcamera.getLabel() + "', '" + tempcamera.getMinMax() + "', '" + tempcamera.getIndex() + "', '" + tempcamera.getTrack().getId() + "', '" + Utils.locationToString(tempcamera.getLocation()) + "');");
                var camerarow = DB.getFirstRow("SELECT * FROM `Cameras` WHERE `TRACKID` = '" + tempcamera.getTrack().getId() + "' AND `INDEX` = '" + tempcamera.getIndex() + "';");
                addCamera(new Cam(camerarow));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //if camera already exists
        else {
            DB.executeUpdateAsync("UPDATE `Cameras` SET `CAMPOSITION` = '" + Utils.locationToString(tempcamera.getLocation()) + "', `REGION` = '" + tempcamera.getMinMax() + "', `LABEL` = '" + tempcamera.getLabel() + "' WHERE `TRACKID` = '" + tempcamera.getTrack().getId() + "' AND `INDEX` = '" + tempcamera.getIndex() + "';");
            addCamera(tempcamera);
        }
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        Camera.instance = this;
        config.options().copyDefaults(true);
        saveConfig();
        String username = config.getString("username");
        String password = config.getString("password");
        String database = config.getString("database");
        int port = config.getInt("port");
        String host = config.getString("ip");
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



        PaperCommandManager manager = new PaperCommandManager(this);
        // enable brigadier integration for paper servers
        manager.enableUnstableAPI("brigadier");
        manager.registerCommand(new CameraCommand());
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
                          PRIMARY KEY (`id`)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;""");
            } catch (SQLException e) {
                e.printStackTrace();
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
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        DB.close();
    }

    public boolean removeCamera(int regionIndex, Track track) {
        if (getCamera(track, regionIndex) != null) {
            Cam camera = getCamera(track, regionIndex);
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
}
