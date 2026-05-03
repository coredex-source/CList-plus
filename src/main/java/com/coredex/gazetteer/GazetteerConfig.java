package com.coredex.gazetteer;

import eu.midnightdust.lib.config.EntryInfo;
import eu.midnightdust.lib.config.MidnightConfig;
import eu.midnightdust.lib.config.MidnightConfigListWidget;
import eu.midnightdust.lib.config.MidnightConfigScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GazetteerConfig extends MidnightConfig {
    public static final String DEFAULT_SECTION_ALL = "ALL";
    public static final String DEFAULT_SECTION_CURRENT_REGION = "CURRENT_REGION";
    public static final String DEFAULT_SECTION_OVERWORLD = "minecraft:overworld";
    public static final String DEFAULT_SECTION_NETHER = "minecraft:the_nether";
    public static final String DEFAULT_SECTION_END = "minecraft:the_end";

    @Entry(min=5,max=200) public static int multiplier = 10;
    @Entry(min=0) public static int renderDistance = 0;
    @Hidden @Entry public static String defaultSection = DEFAULT_SECTION_CURRENT_REGION;
    @Entry public static boolean waypointsToggled = true;
    @Entry public static boolean canPlaceDeathpoints = true;
    @Entry public static boolean waypointTextBackground = true;
    @Entry public static boolean squareWaypoints = false;
    @Entry public static boolean showCreateWaypointMessage = true;
    @Entry public static boolean escapeDiscardsChanges = false;
    @Entry(min=0) public static int maxDeathWaypoints = 10;
    @Entry public static boolean legacyCoordinateSorting = false;

    public static String resolveCurrentDimension(){
        return GazetteerClient.variables.lastWorld != null ? GazetteerClient.variables.lastWorld.dimension().identifier().toString() : null;
    }

    public static String resolveDefaultCategory(){
        if(legacyCoordinateSorting){
            return null;
        }

        String section = normalizeDefaultSection(defaultSection);
        if(Objects.equals(section, DEFAULT_SECTION_ALL)){
            return null;
        }
        if(Objects.equals(section, DEFAULT_SECTION_CURRENT_REGION)){
            return resolveCurrentDimension();
        }
        return section;
    }

    public static List<String> resolveDynamicDimensions(){
        Set<String> dims = new LinkedHashSet<>();
        String currentDimension = resolveCurrentDimension();

        var connection = GazetteerVariables.minecraftClient.getConnection();
        if(connection != null){
            for(var level : connection.levels()){
                dims.add(level.identifier().toString());
            }
        }

        if(currentDimension != null){
            dims.add(currentDimension);
        }
        for(GazetteerWaypoint wp : GazetteerClient.variables.waypoints){
            dims.add(wp.dimension);
        }

        List<String> orderedDims = new ArrayList<>();
        addOrderedDimension(orderedDims, dims, currentDimension);
        addOrderedDimension(orderedDims, dims, DEFAULT_SECTION_OVERWORLD);
        addOrderedDimension(orderedDims, dims, DEFAULT_SECTION_NETHER);
        addOrderedDimension(orderedDims, dims, DEFAULT_SECTION_END);
        dims.stream().sorted().forEach(dimension -> addOrderedDimension(orderedDims, dims, dimension));
        return orderedDims;
    }

    public static Component getDimensionLabel(String dimension){
        if(dimension == null){
            return Component.translatable("gazetteer.midnightconfig.enum.DefaultSection.ALL");
        }
        return Component.literal(formatDimension(dimension));
    }

    @Override
    public void onTabInit(String tabName, MidnightConfigListWidget list, MidnightConfigScreen screen){
        if(!Objects.equals(tabName, "title") && !Objects.equals(tabName, "default")){
            return;
        }

        if(!legacyCoordinateSorting){
            list.addButton(List.of(Button.builder(getDefaultSectionMessage(), button -> {
                List<String> sections = resolveConfigSections();
                String currentSection = normalizeDefaultSection(defaultSection);
                int index = sections.indexOf(currentSection);
                if(index < 0){
                    index = 0;
                }
                defaultSection = sections.get((index + 1) % sections.size());
                button.setMessage(getDefaultSectionMessage());
            }).bounds(screen.width - 185, 0, 150, 20).build()), Component.translatable("gazetteer.midnightconfig.defaultSection"), new EntryInfo(null, screen.modid));
        }

        list.addButton(List.of(Button.builder(Component.translatable("gazetteer.midnightconfig.import"), button -> {
            List<String> importedFiles = GazetteerData.importCoordinateListFiles();
            if(importedFiles == null){
                button.setMessage(Component.translatable("gazetteer.midnightconfig.import.missing"));
                return;
            }

            if(affectsCurrentWorld(importedFiles)){
                GazetteerClient.reloadCurrentWorldData();
            }

            button.setMessage(importedFiles.isEmpty() ? Component.translatable("gazetteer.midnightconfig.import.none") : Component.translatable("gazetteer.midnightconfig.import.imported", importedFiles.size()));
        }).bounds(screen.width - 185, 0, 150, 20).build()), Component.translatable("gazetteer.midnightconfig.importCoordinateList"), new EntryInfo(null, screen.modid));
    }

    private static boolean affectsCurrentWorld(List<String> importedFiles){
        if(GazetteerClient.variables.worldName == null){
            return false;
        }

        return importedFiles.contains("gazetteer_" + GazetteerClient.variables.worldName) || importedFiles.contains("gazetteer_folders_" + GazetteerClient.variables.worldName);
    }

    private static List<String> resolveConfigSections(){
        Set<String> sections = new LinkedHashSet<>();
        sections.add(DEFAULT_SECTION_ALL);
        sections.add(DEFAULT_SECTION_CURRENT_REGION);
        sections.addAll(resolveDynamicDimensions());

        String currentSection = normalizeDefaultSection(defaultSection);
        if(currentSection != null && !currentSection.isBlank()){
            sections.add(currentSection);
        }
        return new ArrayList<>(sections);
    }

    private static void addOrderedDimension(List<String> orderedDims, Set<String> availableDims, String dimension){
        if(dimension != null && availableDims.contains(dimension) && !orderedDims.contains(dimension)){
            orderedDims.add(dimension);
        }
    }

    private static Component getDefaultSectionMessage(){
        String section = normalizeDefaultSection(defaultSection);
        return switch(section){
            case DEFAULT_SECTION_ALL -> getDimensionLabel(null);
            case DEFAULT_SECTION_CURRENT_REGION -> Component.translatable("gazetteer.midnightconfig.enum.DefaultSection.CURRENT_REGION");
            default -> getDimensionLabel(section);
        };
    }

    private static String normalizeDefaultSection(String section){
        if(section == null || section.isBlank()){
            return DEFAULT_SECTION_CURRENT_REGION;
        }

        return switch(section){
            case DEFAULT_SECTION_ALL, DEFAULT_SECTION_CURRENT_REGION, DEFAULT_SECTION_OVERWORLD, DEFAULT_SECTION_NETHER, DEFAULT_SECTION_END -> section;
            case "OVERWORLD" -> DEFAULT_SECTION_OVERWORLD;
            case "NETHER" -> DEFAULT_SECTION_NETHER;
            case "END" -> DEFAULT_SECTION_END;
            default -> section;
        };
    }

    public static String formatDimension(String raw){
        String formatted = raw;
        formatted = formatted.replace("minecraft:", "");
        formatted = formatted.replace("_", " ");
        formatted = formatted.replace(":", " ");
        return org.apache.commons.lang3.StringUtils.capitalize(formatted);
    }
}
