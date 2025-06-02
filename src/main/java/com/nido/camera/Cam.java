package com.nido.camera;

import co.aikar.idb.DbRow;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.gson.BlockVectorAdapter;
import me.makkuusen.timing.system.database.TrackDatabase;
import me.makkuusen.timing.system.track.Track;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import static com.nido.camera.CameraCommand.plugin;

public class Cam {

    private Vector minp;
    private Vector maxp;
    private Location camlocation;
    private Track camTrack;
    private String label;
    private int index;
    private Integer id;

    public Cam(Location camloc, Track camTrack, int index, Vector min, Vector max, String label) {

        this.camlocation = camloc;
        this.camTrack = camTrack;
        this.index = index;
        this.label = label;
        this.minp = min;
        this.maxp = max;
    }
    public Cam(DbRow dbRow) {
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

    public Integer getId() {return id;}
    public Location getLocation() {return camlocation;}
    public Track getTrack() {return camTrack;}
    public String getLabel() {return label;}
    public int getIndex() {return index;}
    public String getMinMax() {return minp.getBlockX() + "," + minp.getBlockY() + "," + minp.getBlockZ() +
                                ":" + maxp.getBlockX() + "," + maxp.getBlockY() + "," + maxp.getBlockZ();}
    public Vector getMin() {return minp;}
    public Vector getMax() {return maxp;}

}
