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
    ArrayList<Cam> cameras = new ArrayList<>();
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
    public ArrayList<Cam> getCameras() {
        return cameras;
    }
    public void getTrackCameras(Player p){
        List<Integer> trackcameras = new ArrayList<>();
        for (Cam camera: cameras){
            if (camera.getTrack() == Utils.getClosestTrack(p)) {
                Integer camIndex = camera.getIndex();
                trackcameras.add(camIndex);
            }
            StringBuilder tracks = new StringBuilder("This track has cameras with index ");
            for (int index: trackcameras) {
                tracks.append(index).append(" ");
            }
            tracks.deleteCharAt(tracks.length() - 1);
            p.sendMessage(ChatColor.AQUA + tracks.toString());
        }
    }
    private static Camera instance;

    public static Camera getInstance() {
        return Camera.instance;
    }

    public void onQuit(Player player){
        //removes player from hashmaps
        CamPlayer camPlayer = getPlayer(player);
        camPlayer.stopFollowing();
        ArrayList<Player> followers = camPlayer.getFollowers();
        for (Player follower: followers) {
            getPlayer(follower).stopFollowing();
        }
        cameraPlayers.remove(player);
    }
    private void loadCameras() throws SQLException {
        //gets the data for the cameras from the dat//abase
        var locations = DB.getResults("SELECT * FROM `Cameras`;");
        //going through every result in the database
        for (DbRow dbRow : locations) {
            Cam camera = new Cam(dbRow);
            addCamera(camera);
        }


    }
    public void saveNewCamera(Cam camera) {
        if(getCamera(camera.getTrack(), camera.getIndex()) == null) {
            try {
                DB.executeInsert("INSERT INTO `Cameras` (`LABEL`, `REGION`, `INDEX`, `TRACKID`, `CAMPOSITION`) VALUES(" + camera.getLabel() + ", " + camera.getMinMax() + ", " + camera.getIndex() + ", " + camera.getTrack().getId() + ", " + Utils.locationToString(camera.getLocation()) + "');");
                addCamera(camera);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //if camera already exists
        else {
            DB.executeUpdateAsync("UPDATE `Cameras` SET `CAMPOSITION` = '" + Utils.locationToString(camera.getLocation()) + "', `REGION` = '" + camera.getMinMax() + "', `LABEL` = '" + camera.getLabel() + "' WHERE `TRACKID` = " + camera.getTrack().getId() + " AND `INDEX` = " + camera.getIndex() + ";");
            addCamera(camera);
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
        getServer().getPluginManager().registerEvents(new cameraListener(), this);



        PaperCommandManager manager = new PaperCommandManager(this);
        // enable brigadier integration for paper servers
        manager.enableUnstableAPI("brigadier");
        manager.registerCommand(new newCamCommand());
        //load data from db
        try {
            try {
                DB.executeUpdate("CREATE TABLE IF NOT EXISTS Cameras " +
                        "(ID MEDIUMINT NOT NULL UNIQUE AUTO_INCREMENT," +
                        " LABEL VARCHAR(100)," +
                        " REGION VARCHAR(100) NOT NULL," +
                        " TRACKID MEDIUMINT NOT NULL," +
                        " CAMPOSITION VARCHAR(255) NOT NULL," +
                        " INDEX MEDIUMINT NOT NULL);");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            loadCameras();
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
        if(!cameraPlayers.containsKey(p)) {
            cameraPlayers.put(p, new CamPlayer(p));
        }
    }

}
