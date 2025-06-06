package com.nido.camera;

import co.aikar.idb.DbRow;
import lombok.Getter;
import me.makkuusen.timing.system.database.TrackDatabase;
import me.makkuusen.timing.system.track.Track;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import static com.nido.camera.CameraCommands.plugin;

public class Camera {

    private Vector minp;
    private Vector maxp;
    private Location camlocation;
    private Track camTrack;
    @Getter
    private String label;
    @Getter
    private int index;
    @Getter
    private Integer id;
    @Getter
    private String regionType;

    public Camera(Location camloc, Track camTrack, int index, Vector min, Vector max, String label, String regionType) {

        this.camlocation = camloc;
        this.camTrack = camTrack;
        this.index = index;
        this.label = label;
        this.minp = min;
        this.maxp = max;
        this.regionType = regionType;
    }
    public Camera(DbRow dbRow) {
        this.id = dbRow.getInt("ID");
        this.camlocation = Utils.stringToLocation(dbRow.get("CAMPOSITION"));
        this.camTrack = TrackDatabase.getTrackById(dbRow.getInt("TRACKID")).get();
        this.index = dbRow.getInt("INDEX");
        if(dbRow.getString("LABEL").contentEquals("null")) {this.label = null;}
        else {this.label = dbRow.getString("LABEL");}
        String MinMax = dbRow.getString("REGION");
        String[] MinAndMax = MinMax.split(":");
        this.minp = Utils.stringToVector(MinAndMax[0]);
        this.maxp = Utils.stringToVector(MinAndMax[1]);
        this.regionType = dbRow.getString("REGIONTYPE");
    }

    // Teleport player to camera
    public void tpPlayer(Player cameraman) {
        CamPlayer camPlayer = plugin.getPlayer(cameraman);
        // Checks if camera is active, if yes executes
        if (!camPlayer.isCameraDisabled(id)){
            cameraman.teleport(camlocation);
        }
    }

    public boolean isInsideRegion(Player p) {
        Vector pLoc = p.getLocation().toVector();
        return pLoc.isInAABB(minp, maxp);
    }

    public Location getLocation() {return camlocation;}
    public Track getTrack() {return camTrack;}
    public String getMinMax() {return minp.getBlockX() + "," + minp.getBlockY() + "," + minp.getBlockZ() +
                                ":" + maxp.getBlockX() + "," + maxp.getBlockY() + "," + maxp.getBlockZ();}
    public Vector getMin() {return minp;}
    public Vector getMax() {return maxp;}

    public enum RegionType{
        STATIC, ONBOARD
    }

}
