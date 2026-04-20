package com.coredex.gazetteer;

import com.google.common.collect.Lists;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class GazetteerData{
    public static void saveListToFile(String fileName, List<GazetteerWaypoint> waypointList){
        if(!Files.exists(FabricLoader.getInstance().getConfigDir().resolve("gazetteer"))){
            try{
                Files.createDirectories(FabricLoader.getInstance().getConfigDir().resolve("gazetteer"));
            }
            catch(IOException ignored){}
        }
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("gazetteer").toFile();
        File file = new File(dataDir, fileName);
        try(PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)))){
            for(int i = 0; i < waypointList.size(); i++){
                String folderId = GazetteerClient.variables.waypoints.get(i).folderId;
                if(folderId == null) folderId = "";
                String globalFolderId = GazetteerClient.variables.waypoints.get(i).globalFolderId;
                if(globalFolderId == null) globalFolderId = "";
                writer.println(GazetteerClient.variables.waypoints.get(i).getCoordinates() + "~" + GazetteerClient.variables.waypoints.get(i).name.replaceAll("~", "") + "~" + GazetteerClient.variables.waypoints.get(i).dimension + "~" + GazetteerClient.variables.colors.get(i).getHexNoAlpha() + "~" + GazetteerClient.variables.waypoints.get(i).render + "~" + GazetteerClient.variables.waypoints.get(i).deathpoint + "~" + folderId + "~" + globalFolderId + "~" + GazetteerClient.variables.waypoints.get(i).locked);
            }
        }
        catch(IOException ignored){}
    }

    public static List<GazetteerWaypoint> loadListFromFile(String fileName){
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("gazetteer").toFile();
        File file = new File(dataDir, fileName);
        if(!file.exists()){
            return null;
        }
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))){
            List<GazetteerWaypoint> waypointList = Lists.newArrayList();
            String line;
            while((line = reader.readLine()) != null){
                String[] segments = line.split("~");
                if(segments.length >= 3){
                    String coords = segments[0];
                    String name = segments[1];
                    String dimension = segments[2];
                    String color = null, bool = null, deathpoint = null, folderId = null, globalFolderId = null, locked = null;
                    try{
                        color = segments[3];
                        bool = segments[4];
                        deathpoint = segments[5];
                        folderId = segments[6];
                        globalFolderId = segments[7];
                        locked = segments[8];
                    }
                    catch(IndexOutOfBoundsException ignored){}
                    GazetteerWaypoint waypoint = new GazetteerWaypoint(coords, name, dimension, Boolean.parseBoolean(bool), Boolean.parseBoolean(deathpoint));
                    if(folderId != null && !folderId.isEmpty()){
                        waypoint.folderId = folderId;
                    }
                    if(globalFolderId != null && !globalFolderId.isEmpty()){
                        waypoint.globalFolderId = globalFolderId;
                    }
                    if(locked != null){
                        waypoint.locked = Boolean.parseBoolean(locked);
                    }
                    if(color == null){
                        GazetteerClient.addRandomWaypointColor();
                    }
                    else{
                        GazetteerWaypointColor color_class = new GazetteerWaypointColor(0, 0, 0);
                        color_class.set(color);
                        GazetteerClient.variables.colors.add(color_class);
                    }
                    waypointList.add(waypoint);
                }
            }
            return waypointList;
        }
        catch(IOException ignored){}
        return null;
    }

    public static void deleteLegacyFile(String fileName){
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("gazetteer").toFile();
        File file = new File(dataDir, fileName);
        if(file.exists()){
            boolean ignored = file.delete();
        }
    }

    public static void saveFoldersToFile(String fileName, List<GazetteerFolder> folders){
        if(!Files.exists(FabricLoader.getInstance().getConfigDir().resolve("gazetteer"))){
            try{
                Files.createDirectories(FabricLoader.getInstance().getConfigDir().resolve("gazetteer"));
            }
            catch(IOException ignored){}
        }
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("gazetteer").toFile();
        File file = new File(dataDir, fileName);
        try(PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)))){
            for(GazetteerFolder folder : folders){
                String parentId = folder.parentId == null ? "" : folder.parentId;
                String dim = folder.dimension == null ? "" : folder.dimension;
                writer.println(folder.id + "~" + folder.name.replaceAll("~", "") + "~" + folder.colorHex + "~" + folder.expanded + "~" + folder.visible + "~" + parentId + "~" + dim);
            }
        }
        catch(IOException ignored){}
    }

    public static List<GazetteerFolder> loadFoldersFromFile(String fileName){
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("gazetteer").toFile();
        File file = new File(dataDir, fileName);
        if(!file.exists()){
            return null;
        }
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))){
            List<GazetteerFolder> folderList = com.google.common.collect.Lists.newArrayList();
            String line;
            while((line = reader.readLine()) != null){
                String[] segments = line.split("~");
                if(segments.length >= 5){
                    String id = segments[0];
                    String name = segments[1];
                    String colorHex = segments[2];
                    boolean expanded = Boolean.parseBoolean(segments[3]);
                    boolean visible = Boolean.parseBoolean(segments[4]);
                    String parentId = null;
                    String dimension = null;
                    try{
                        parentId = segments[5];
                        if(parentId.isEmpty()) parentId = null;
                        dimension = segments[6];
                        if(dimension.isEmpty()) dimension = null;
                    }
                    catch(IndexOutOfBoundsException ignored){}
                    folderList.add(new GazetteerFolder(id, name, colorHex, expanded, visible, parentId, dimension));
                }
            }
            return folderList;
        }
        catch(IOException ignored){}
        return null;
    }

    public static List<String> loadListFromFileLegacy(String fileName){
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("gazetteer").toFile();
        File file = new File(dataDir, fileName);
        if(!file.exists()){
            return null;
        }
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))){
            List<String> stringList = Lists.newArrayList();
            String line;
            while((line = reader.readLine()) != null){
                stringList.add(line);
            }
            return stringList;
        }
        catch(IOException ignored){}
        return null;
    }
}
