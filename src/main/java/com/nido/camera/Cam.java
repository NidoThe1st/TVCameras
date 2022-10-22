package com.nido.camera;

import me.makkuusen.timing.system.track.Track;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Cam {

    Location camlocation;
    Track camTrack;
    int index;

    public Cam(Location camloc, Track camTrack, int index) {

        this.camlocation = camloc;
        this.camTrack = camTrack;
        this.index = index;


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

    public int getIndex() {
        return index;
    }
}
