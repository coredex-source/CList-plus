package com.coredex.gazetteer;

import java.util.ArrayList;
import java.util.List;

public class GazetteerFolder{
    public String name;
    public String colorHex;
    public boolean expanded;
    public boolean visible;
    public String parentId;
    public String id;
    public String dimension;
    public List<Integer> waypointIndices;

    GazetteerFolder(String id, String name, String colorHex, boolean expanded, boolean visible, String parentId, String dimension){
        this.id = id;
        this.name = name;
        this.colorHex = colorHex;
        this.expanded = expanded;
        this.visible = visible;
        this.parentId = parentId;
        this.dimension = dimension;
        this.waypointIndices = new ArrayList<>();
    }

    public int getColor(){
        try{
            int rgb = Integer.parseInt(colorHex, 16);
            return (255 << 24) | rgb;
        }
        catch(NumberFormatException e){
            return 0xFFFFFFFF;
        }
    }

    public void toggleVisibility(){
        this.visible = !this.visible;
        applyVisibility(this.visible);
        GazetteerClient.variables.savedSinceLastUpdate = false;
    }

    private void applyVisibility(boolean vis){
        for(int idx : waypointIndices){
            if(idx >= 0 && idx < GazetteerClient.variables.waypoints.size()){
                GazetteerClient.variables.waypoints.get(idx).render = vis;
            }
        }
        for(GazetteerFolder f : GazetteerClient.variables.folders){
            if(this.id.equals(f.parentId)){
                f.visible = vis;
                f.applyVisibility(vis);
            }
        }
    }

    public void toggleExpanded(){
        this.expanded = !this.expanded;
    }

    public static String generateId(){
        return Long.toHexString(System.nanoTime());
    }
}
