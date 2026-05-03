package com.coredex.gazetteer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;
import org.lwjgl.glfw.GLFW;
import eu.midnightdust.lib.config.MidnightConfig;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class GazetteerClient implements ClientModInitializer{
    public static GazetteerVariables variables = new GazetteerVariables();
    static Random rand = new Random();
    KeyMapping openWaypointsKeybind;
    KeyMapping addAWaypoint;
    KeyMapping toggleVisibility;
    public static KeyMapping.Category MOD_CATEGORY = new KeyMapping.Category(Identifier.parse(Gazetteer.MOD_ID));

    @Override
    public void onInitializeClient(){
        openWaypointsKeybind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "keybinds.waypoints.menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                MOD_CATEGORY
        ));
        addAWaypoint = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "keybinds.waypoint.add",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                MOD_CATEGORY
        ));
        toggleVisibility = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "keybinds.waypoints.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                MOD_CATEGORY
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(!GazetteerVariables.delayedEvents.isEmpty()){
                for(GazetteerDelayedEvent event: GazetteerVariables.delayedEvents){
                    boolean destroy = event.update();
                    if(destroy){
                        GazetteerVariables.delayedEvents.remove(event);
                        break;
                    }
                }
            }
            while(openWaypointsKeybind.consumeClick()){
                client.gui.setScreen(new GazetteerWaypointScreen(Component.literal("Waypoints")));
            }
            while(addAWaypoint.consumeClick()){
                if(!(client.gui.screen() instanceof GazetteerWaypointScreen)){
                    Player player = GazetteerVariables.minecraftClient.player;
                    addNewWaypoint((int) Math.floor(player.getX()), (int) Math.floor(player.getY()), (int) Math.floor(player.getZ()), false, true);
                }
            }
            while(toggleVisibility.consumeClick()){
                GazetteerConfig.waypointsToggled = !GazetteerConfig.waypointsToggled;
                MidnightConfig.write(Gazetteer.MOD_ID);
            }
            if(client.level == null){
                variables.loadedLastWorld = false;
                variables.waypoints.clear();
                variables.colors.clear();
                variables.worldName = null;
                variables.lastWorld = null;
                variables.isWorldError = false;
            }
            else{
                if(!variables.isWorldError){
                    try{
                        variables.lastWorld = client.level;
                        checkForWorldChanges(variables.lastWorld);
                        checkIfSaveIsNeeded(false);
                        if(client.isLocalServer()){
                            variables.worldName = client.getSingleplayerServer().getWorldPath(LevelResource.ROOT).getParent().getFileName().toString();
                        }
                        else{
                            if(client.getCurrentServer().isRealm()){
                                variables.worldName = client.getCurrentServer().name;
                            }
                            else{
                                variables.worldName = client.getCurrentServer().ip;
                                variables.worldName = variables.worldName.replace(":", "P");
                            }
                        }
                        if(!client.player.isAlive() && !variables.hadDeathWaypointPlaced && GazetteerConfig.canPlaceDeathpoints){
                            Player player = client.player;
                            addNewWaypoint((int) Math.floor(player.getX()), (int) Math.floor(player.getY()), (int) Math.floor(player.getZ()), true, false);
                            variables.hadDeathWaypointPlaced = true;
                        }
                        else if(client.player.isAlive() && variables.hadDeathWaypointPlaced){
                            variables.hadDeathWaypointPlaced = false;
                        }
                    }
                    catch(NullPointerException e){
                        Gazetteer.LOGGER.info("Can't get the current world. Player probably uses ReplayMod and is now watching the replay");
                        variables.isWorldError = true;
                    }
                }
            }
        });
        variables.savedSinceLastUpdate = true;
        variables.loadedLastWorld = false;
    }

    public static void addNewWaypoint(int x, int y, int z, boolean death, boolean viaKeybind){
        Gazetteer.LOGGER.info("New waypoint for dimension " + variables.lastWorld.dimension().identifier());
        String waypointName;
        if(death){
            waypointName = Component.translatable("waypoint.last.death").getString();
        }
        else{
            waypointName = Component.translatable("waypoint.new.waypoint").getString();
        }
        variables.waypoints.add(new GazetteerWaypoint(x, y, z, waypointName, variables.lastWorld.dimension().identifier().toString(), true, death));
        variables.colors.add(new GazetteerWaypointColor(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
        variables.savedSinceLastUpdate = false;
        if(death && GazetteerConfig.maxDeathWaypoints > 0){
            int deathCount = 0;
            int oldestUnlockedIndex = -1;
            for(int i = 0; i < variables.waypoints.size(); i++){
                if(variables.waypoints.get(i).deathpoint){
                    deathCount++;
                    if(oldestUnlockedIndex == -1 && !variables.waypoints.get(i).locked) oldestUnlockedIndex = i;
                }
            }
            if(deathCount > GazetteerConfig.maxDeathWaypoints && oldestUnlockedIndex >= 0){
                deleteWaypoint(oldestUnlockedIndex);
            }
        }
        if(!death){
            GazetteerVariables.minecraftClient.gui.setScreen(new GazetteerWaypointConfig(Component.literal("Config"), variables.waypoints.size() - 1, viaKeybind));
        }
    }

    public static void deleteWaypoint(int position){
        try{
            variables.waypoints.remove(position);
            variables.colors.remove(position);
            variables.savedSinceLastUpdate = false;
        }
        catch(IndexOutOfBoundsException ignored){}
    }

    public static void checkForWorldChanges(ClientLevel currentWorld){
        if(!variables.loadedLastWorld && variables.worldName != null){
            Gazetteer.LOGGER.info("New world " + variables.worldName);
            variables.lastWorld = currentWorld;
            // Check for old 1.0 saves and convert them
            List<String> names = GazetteerData.loadListFromFileLegacy("gazetteer_names_" + variables.worldName);
            List<String> dimensions = GazetteerData.loadListFromFileLegacy("gazetteer_dimensions_" + variables.worldName);
            if(names != null && !names.isEmpty()){
                List<String> temp = GazetteerData.loadListFromFileLegacy("gazetteer_" + variables.worldName);
                for(int i = 0; i < names.size(); i++){
                    variables.waypoints.add(new GazetteerWaypoint(temp.get(i), names.get(i), dimensions.get(i), true, false));
                }
                for(int i = 0; i < variables.waypoints.size(); i++){
                    variables.colors.add(new GazetteerWaypointColor(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
                }
                GazetteerData.deleteLegacyFile("gazetteer_names_" + variables.worldName);
                GazetteerData.deleteLegacyFile("gazetteer_dimensions_" + variables.worldName);
                Gazetteer.LOGGER.info("Loaded old 1.0 data for world " + variables.worldName);
                // Force save converting it to a new format
                checkIfSaveIsNeeded(true);
            }
            else{
                // Check for post 1.0 saves
                if(!GazetteerVariables.minecraftClient.isLocalServer()){
                    List<GazetteerWaypoint> ways = GazetteerData.loadListFromFile("gazetteer_" + GazetteerVariables.minecraftClient.getCurrentServer().name);
                    if(ways != null && !ways.isEmpty()){
                        variables.waypoints = ways;
                        GazetteerData.deleteLegacyFile("gazetteer_" + GazetteerVariables.minecraftClient.getCurrentServer().name);
                        Gazetteer.LOGGER.info("Loaded old multiplier server data");
                        checkIfSaveIsNeeded(true);
                    }
                    else{
                        ways = GazetteerData.loadListFromFile("gazetteer_" + variables.worldName);
                        if(ways != null && !ways.isEmpty()){
                            variables.waypoints = ways;
                            Gazetteer.LOGGER.info("Loaded data for server " + variables.worldName);
                        }
                        else{
                            Gazetteer.LOGGER.info("The file for " + variables.worldName + " doesn't exist");
                        }
                    }
                }
                else{
                    List<GazetteerWaypoint> ways = GazetteerData.loadListFromFile("gazetteer_" + variables.worldName);
                    if(ways != null && !ways.isEmpty()){
                        variables.waypoints = ways;
                        Gazetteer.LOGGER.info("Loaded data for world " + variables.worldName);
                    }
                    else{
                        Gazetteer.LOGGER.info("The file for " + variables.worldName + " doesn't exist");
                    }
                }
            }
            variables.loadedLastWorld = true;
            loadFolders();
            rebuildFolderIndices();
        }
    }

    public static void loadFolders(){
        List<GazetteerFolder> folders = GazetteerData.loadFoldersFromFile("gazetteer_folders_" + variables.worldName);
        if(folders != null){
            variables.folders = folders;
        }
    }

    public static void rebuildFolderIndices(){
        for(GazetteerFolder folder : variables.folders){
            folder.waypointIndices.clear();
        }
        for(int i = 0; i < variables.waypoints.size(); i++){
            GazetteerWaypoint wp = variables.waypoints.get(i);
            if(wp.folderId != null){
                for(GazetteerFolder folder : variables.folders){
                    if(folder.id.equals(wp.folderId)){
                        folder.waypointIndices.add(i);
                        break;
                    }
                }
            }
            if(wp.globalFolderId != null){
                for(GazetteerFolder folder : variables.folders){
                    if(folder.id.equals(wp.globalFolderId)){
                        if(!folder.waypointIndices.contains(i)){
                            folder.waypointIndices.add(i);
                        }
                        break;
                    }
                }
            }
        }
    }

    public static void addRandomWaypointColor(){
        variables.colors.add(new GazetteerWaypointColor(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
    }

    public static void reloadCurrentWorldData(){
        if(variables.worldName == null){
            return;
        }

        List<GazetteerWaypoint> existingWaypoints = variables.waypoints;
        List<GazetteerWaypointColor> existingColors = variables.colors;
        List<GazetteerFolder> existingFolders = variables.folders;

        variables.waypoints = new ArrayList<>();
        variables.colors = new ArrayList<>();
        variables.folders = new ArrayList<>();

        List<GazetteerWaypoint> importedWaypoints = GazetteerData.loadListFromFile("gazetteer_" + variables.worldName);
        List<GazetteerFolder> importedFolders = GazetteerData.loadFoldersFromFile("gazetteer_folders_" + variables.worldName);

        if(importedWaypoints == null && importedFolders == null){
            variables.waypoints = existingWaypoints;
            variables.colors = existingColors;
            variables.folders = existingFolders;
            return;
        }

        if(importedWaypoints != null){
            variables.waypoints = importedWaypoints;
        }
        if(importedFolders != null){
            variables.folders = importedFolders;
        }

        rebuildFolderIndices();
        variables.savedSinceLastUpdate = true;
    }

    public static void checkIfSaveIsNeeded(boolean force){
        if(!variables.savedSinceLastUpdate || force){
            Gazetteer.LOGGER.info("Saving data for world " + variables.worldName);
            rebuildFolderIndices();
            GazetteerData.saveListToFile("gazetteer_" + variables.worldName, variables.waypoints);
            GazetteerData.saveFoldersToFile("gazetteer_folders_" + variables.worldName, variables.folders);
            variables.savedSinceLastUpdate = true;
        }
    }
}