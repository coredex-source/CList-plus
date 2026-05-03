package com.coredex.gazetteer;

import eu.midnightdust.lib.config.EntryInfo;
import eu.midnightdust.lib.config.MidnightConfig;
import eu.midnightdust.lib.config.MidnightConfigListWidget;
import eu.midnightdust.lib.config.MidnightConfigScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;

public class GazetteerConfig extends MidnightConfig {
    @Entry(min=5,max=200) public static int multiplier = 10;
    @Entry(min=0) public static int renderDistance = 0;
    @Entry public static boolean waypointsToggled = true;
    @Entry public static boolean canPlaceDeathpoints = true;
    @Entry public static boolean waypointTextBackground = true;
    @Entry public static boolean squareWaypoints = false;
    @Entry public static boolean showCreateWaypointMessage = true;
    @Entry public static boolean escapeDiscardsChanges = false;
    @Entry(min=0) public static int maxDeathWaypoints = 10;

    @Override
    public void onTabInit(String tabName, MidnightConfigListWidget list, MidnightConfigScreen screen){
        if(!Objects.equals(tabName, "title") && !Objects.equals(tabName, "default")){
            return;
        }

        list.addButton(List.of(Button.builder(Component.literal("Import"), button -> {
            List<String> importedFiles = GazetteerData.importCoordinateListFiles();
            if(importedFiles == null){
                button.setMessage(Component.literal("Missing CList"));
                return;
            }

            if(affectsCurrentWorld(importedFiles)){
                GazetteerClient.reloadCurrentWorldData();
            }

            button.setMessage(Component.literal(importedFiles.isEmpty() ? "No files" : "Imported " + importedFiles.size()));
        }).bounds(screen.width - 185, 0, 150, 20).build()), Component.literal("Import CoordinateList coordinates"), new EntryInfo(null, screen.modid));
    }

    private static boolean affectsCurrentWorld(List<String> importedFiles){
        if(GazetteerClient.variables.worldName == null){
            return false;
        }

        return importedFiles.contains("gazetteer_" + GazetteerClient.variables.worldName) || importedFiles.contains("gazetteer_folders_" + GazetteerClient.variables.worldName);
    }
}
