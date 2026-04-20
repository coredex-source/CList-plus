package com.coredex.gazetteer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class GazetteerVariables{
    public List<GazetteerWaypoint> waypoints = Lists.newArrayList();
    public List<GazetteerWaypointColor> colors = Lists.newArrayList();
    public List<GazetteerFolder> folders = Lists.newArrayList();
    public static List<GazetteerDelayedEvent> delayedEvents = Lists.newArrayList();
    public String worldName;
    public ClientLevel lastWorld;
    public static Minecraft minecraftClient = Minecraft.getInstance();
    public boolean savedSinceLastUpdate;
    public boolean loadedLastWorld;
    public boolean hadDeathWaypointPlaced;
    public boolean isWorldError;
}