package com.nido.camera;

import co.aikar.commands.PaperCommandManager;
import co.aikar.idb.*;
import me.makkuusen.timing.system.ApiUtilities;
import me.makkuusen.timing.system.track.Track;
import me.makkuusen.timing.system.track.TrackDatabase;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public final class Camera extends JavaPlugin {
    public FileConfiguration config = getConfig();
    HashMap<Player, Cam> currentCamera = new HashMap<>();
    HashMap<Player, ArrayList<Player>> followedPlayers = new HashMap<>();
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

    public boolean haveCamera(Player key){

        return currentCamera.containsKey(key);
    }
    public void setCurrentCamera(Player p, Cam cam) {
        currentCamera.put(p, cam);
    }
    public Cam getCurrentCamera(Player p) {
        return currentCamera.get(p);
    }
    //get all cameras
    public ArrayList<Cam> getCameras() {
        return cameras;
    }
    //Add followers to a player (followed)
    public void addFollowed(Player followed, ArrayList<Player> followers){
        //check if there are followers for the player (followed)
        if(followedPlayers.containsKey(followed)) {
            //add new followers and old followers together
            followers.addAll(followedPlayers.get(followed));
        }
        //update the list with the combined followers/new followers
        followedPlayers.put(followed,followers);

    }
    public void removeFollower(Player followed, Player follower) {
        // get current followers
        ArrayList<Player> followers = followedPlayers.get(followed);
        //remove follower if exists
        followers.remove(follower);
        //if no followers left remove it from the hashmap
        if(followers.isEmpty()) {followedPlayers.remove(followed);}
        //sets the new list of followers
        else { followedPlayers.put(followed, followers);}

    }
    //find who is the player (follower) following
    public Player whoFollowed(Player follower) {
        for(Player followed : followedPlayers.keySet()) {
            if(getFollowers(followed).contains(follower)) {
                return followed;
            }
        }
        return null;
    }
    //get the list of followers who are following a player (followed)
    public ArrayList<Player> getFollowers(Player followed){

        return followedPlayers.get(followed);

    }
    //check if a player is being followed
    public boolean isFollowed(Player followed){

        return followedPlayers.containsKey(followed);
    }

    private static Camera instance;

    public static Camera getInstance() {
        return Camera.instance;
    }

    public void onQuit(Player player){
        //removes player from hashmaps
        currentCamera.remove(player);
        Player followed = whoFollowed(player);
        if(followed != null){
            removeFollower(followed, player);
        }
        followedPlayers.remove(player);
    }
    private void loadCameras() throws SQLException {
        //gets the data for the cameras from the dat//abase
        var locations = DB.getResults("SELECT * FROM `ts_locations` WHERE `type` = 'CAMERA'");
        //going through every result in the database
        for (DbRow dbRow : locations) {
            //checking if a track with the given id exists
            var maybeTrack = TrackDatabase.getTrackById(dbRow.get("trackId"));
            if(maybeTrack.isPresent()) {
                //if exists make a new camera object with data from the database row
                Track track = maybeTrack.get();
                Location loc = ApiUtilities.stringToLocation(dbRow.getString("location"));
                int index = dbRow.getInt("index");
                if(loc != null) {
                    Cam camera = new Cam(loc, track, index);
                    addCamera(camera);
                }
            }
        }
    }
    public void saveNewCamera(Cam camera) {
        if(getCamera(camera.getTrack(), camera.getIndex()) == null) {
            try {
                DB.executeInsert("INSERT INTO `ts_locations` (`trackId`, `index`, `type`, `location`) VALUES(" + camera.getTrack().getId() + ", " + camera.getIndex() + ", 'CAMERA', '" + ApiUtilities.locationToString(camera.getLocation()) + "');");
                addCamera(camera);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //if camera already exists
        else {
            DB.executeUpdateAsync("UPDATE `ts_locations` SET `location` = '" + ApiUtilities.locationToString(camera.getLocation()) + "' WHERE `trackId` = " + camera.getTrack().getId() + " AND `index` = " + camera.getIndex() + " AND `type` = 'CAMERA';");
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
            DB.executeUpdateAsync("DELETE FROM `ts_locations` WHERE `trackId` = " + camera.getTrack().getId() + " AND `index` = " + camera.getIndex() + " AND `type` = 'CAMERA';");
            return true;
        }
        return false;
    }
}
