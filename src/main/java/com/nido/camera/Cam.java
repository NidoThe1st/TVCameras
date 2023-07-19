package com.nido.camera;

import co.aikar.idb.DbRow;
import me.makkuusen.timing.system.track.Track;
import me.makkuusen.timing.system.track.TrackDatabase;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import static com.nido.camera.newCamCommand.plugin;

public class Cam {

    private Location camlocation;
    private String label;
    private int index;
    private Integer id;

    public Cam(Location camloc, int index, Vector min, Vector max, String label) {

        this.camlocation = camloc;
        this.index = index;
        this.label = label;

    }

    public void tpPlayer(Player cameraman) {
        CamPlayer camPlayer = plugin.getPlayer(cameraman);
        if (!camPlayer.isCameraDisabled(id)){
            cameraman.teleport(camlocation);
        }
    }
    public Location getLocation() {return camlocation;}
    public String getLabel() {return label;}
    public int getIndex() {return index;}
    public Integer getId() {return id;}

}
