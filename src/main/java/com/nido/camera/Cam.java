package com.nido.camera;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Cam {

    Location camlocation;

    public Cam(Location camloc){

        this.camlocation = camloc;

    }

    public void tpPlayer(Player cameraman){

        cameraman.teleport(camlocation);

    }

}
