package com.nido.camera;

import co.aikar.idb.DbRow;
import me.makkuusen.timing.system.track.Track;
import me.makkuusen.timing.system.track.TrackDatabase;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Cam {

    Vector minp;
    Vector maxp;
    Location camlocation;
    Track camTrack;
    String label;
    int index;

    public Cam(Location camloc, Track camTrack, int index, Vector min, Vector max, String label) {

        this.camlocation = camloc;
        this.camTrack = camTrack;
        this.index = index;
        this.label = label;
        this.minp = min;
        this.maxp = max;
    }
    public Cam(DbRow dbRow) {

        this.camlocation = Utils.stringToLocation(dbRow.get("CAMPOSITION"));
        this.camTrack = TrackDatabase.getTrackById(dbRow.getInt("TRACKID")).get();
        this.index = dbRow.getInt("INDEX");
        this.label = dbRow.getString("LABEL");
        String MinMax = dbRow.getString("REGION");
        String[] MinAndMax = MinMax.split(":");
        this.minp = Utils.stringToVector(MinAndMax[0]);
        this.maxp = Utils.stringToVector(MinAndMax[1]);
    }

    public void tpPlayer(Player cameraman) {
        cameraman.teleport(camlocation);
    }

    public Location getLocation() {
        return camlocation;
    }

    public Track getTrack() {
        return camTrack;
    }
    public String getLabel() {return label;}

    public int getIndex() {
        return index;
    }
    public String getMinMax() {
        return minp.getBlockX() +
                "," +
                minp.getBlockY() +
                "," +
                minp.getBlockZ() +
                ":" +
                maxp.getBlockX() +
                "," +
                maxp.getBlockY() +
                "," +
                maxp.getBlockZ();
    }
    public boolean isInsideRegion(Player p) {
        Vector pLoc = p.getLocation().toVector();
        return pLoc.isInAABB(minp, maxp);
    }

}
