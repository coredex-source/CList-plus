package com.coredex.gazetteer.mixin;

import com.coredex.gazetteer.GazetteerConfig;
import com.coredex.gazetteer.GazetteerRenderLayers;
import com.coredex.gazetteer.GazetteerVariables;
import com.coredex.gazetteer.GazetteerWaypoint;
import com.coredex.gazetteer.GazetteerWaypointColor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static com.coredex.gazetteer.GazetteerClient.variables;

@Mixin(LevelRenderer.class)
public abstract class GazetteerWaypointRenderer{
    @Unique
    private float calculateWaypointSize(){
        return 0.5f * (GazetteerConfig.multiplier / 10.0f);
    }

    @Unique
    private float calculateTextSize(){
        return 15f * (GazetteerConfig.multiplier / 10.0f);
    }

    @Unique
    private float distanceTo(GazetteerWaypoint waypoint){
        float f = (float) (GazetteerVariables.minecraftClient.player.getX() - waypoint.x);
        float g = (float) (GazetteerVariables.minecraftClient.player.getY() - waypoint.y);
        float h = (float) (GazetteerVariables.minecraftClient.player.getZ() - waypoint.z);
        return Math.round(Mth.sqrt(f * f + g * g + h * h));
    }

    @Unique
    private Vec3 calculateRenderCoords(GazetteerWaypoint waypoint, Camera camera, float distance){
        Vec3 cameraPos = camera.position();
        float px = (float) cameraPos.x;
        float py = (float) cameraPos.y;
        float pz = (float) cameraPos.z;
        float wx = waypoint.x;
        float wy = waypoint.y;
        float wz = waypoint.z;
        float vx = wx - px;
        float vy = wy - py;
        float vz = wz - pz;
        float vectorLen = (float) Math.sqrt((vx * vx) + (vy * vy) + (vz * vz));
        float radius = 32;
        float scx = radius / vectorLen * vx;
        float scy = radius / vectorLen * vy;
        float scz = radius / vectorLen * vz;
        float prx, pry, prz;
        if(distance > 32){
            prx = scx + px;
            pry = scy + py;
            prz = scz + pz;
        }
        else{
            prx = wx;
            pry = wy;
            prz = wz;
        }
        return new Vec3(prx, pry, prz);
    }

    @Unique
    private static String getDimension(String text){
        String s = text;
        s = s.replace("minecraft:", "");
        s = s.replace("_", " ");
        s = s.replace(":", " ");
        s = StringUtils.capitalize(s);
        return s;
    }

    @Unique
    private Identifier resolveWaypointIcon(GazetteerWaypoint waypoint){
        if(waypoint.deathpoint){
            return Identifier.fromNamespaceAndPath("gazetteer", "skull.png");
        }
        if(GazetteerConfig.squareWaypoints){
            return Identifier.fromNamespaceAndPath("gazetteer", "waypoint_icon_square.png");
        }
        return Identifier.fromNamespaceAndPath("gazetteer", "waypoint_icon.png");
    }

    @Unique
    private PoseStack createWaypointPose(Camera camera, Vec3 transformedPosition, float size){
        PoseStack poseStack = new PoseStack();
        poseStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);
        poseStack.mulPose(camera.rotation());
        poseStack.scale(-size, size, size);
        return poseStack;
    }

    @Inject(method ="submitFeatures", at = @At("RETURN"))
    private void afterSubmitFeatures(LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector, boolean drawBlockOutline, CallbackInfo ci) {
        if(variables.waypoints.isEmpty() || !GazetteerConfig.waypointsToggled || GazetteerVariables.minecraftClient.player == null || variables.lastWorld == null || GazetteerVariables.minecraftClient.gui.hud.isHidden()){
            return;
        }

        Camera camera = GazetteerVariables.minecraftClient.gameRenderer.mainCamera();
        Font font = GazetteerVariables.minecraftClient.font;

        for(int i = 0; i < variables.waypoints.size(); i++){
            GazetteerWaypoint waypoint = variables.waypoints.get(i);
            int distanceWithoutDecimalPlaces = (int) distanceTo(waypoint);
            if(!Objects.equals(waypoint.getDimensionString(), getDimension(variables.lastWorld.dimension().identifier().toString())) || !waypoint.render || (GazetteerConfig.renderDistance != 0 && GazetteerConfig.renderDistance < distanceWithoutDecimalPlaces)){
                continue;
            }

            float size = calculateWaypointSize();
            Vec3 renderCoords = calculateRenderCoords(waypoint, camera, distanceWithoutDecimalPlaces);
            Vec3 targetPosition = new Vec3(renderCoords.x + 0.5, renderCoords.y + 1, renderCoords.z + 0.5);
            Vec3 transformedPosition = targetPosition.subtract(camera.position());
            PoseStack iconPose = createWaypointPose(camera, transformedPosition, size);
            GazetteerWaypointColor color = variables.colors.get(i);
            Identifier icon = resolveWaypointIcon(waypoint);

            submitNodeCollector.submitCustomGeometry(iconPose, GazetteerRenderLayers.POSITION_TEX_COLOR.apply(icon), (pose, vertices) -> {
                vertices.addVertex(pose, -0.5f, 0.5f, 0).setColor(color.r, color.g, color.b, 1f).setUv(0f, 0f);
                vertices.addVertex(pose, -0.5f, -0.5f, 0).setColor(color.r, color.g, color.b, 1f).setUv(0f, 1f);
                vertices.addVertex(pose, 0.5f, -0.5f, 0).setColor(color.r, color.g, color.b, 1f).setUv(1f, 1f);
                vertices.addVertex(pose, 0.5f, 0.5f, 0).setColor(color.r, color.g, color.b, 1f).setUv(1f, 0f);
            });

            String labelText = waypoint.name + " (" + distanceWithoutDecimalPlaces + " m)";
            int textWidth = font.width(labelText);
            PoseStack textPose = createWaypointPose(camera, transformedPosition, size);
            textPose.scale(-0.025f, -0.025f, 0.025f);
            size = calculateTextSize();
            textPose.scale((float) Math.log(size * 4), (float) Math.log(size * 4), (float) Math.log(size * 4));
            textPose.translate(0, -20, 0);

            submitNodeCollector.submitText(textPose, (float) (-textWidth / 2), 0, Component.literal(labelText).getVisualOrderText(), false, Font.DisplayMode.SEE_THROUGH, 15728880, 0xFFFFFFFF, GazetteerConfig.waypointTextBackground ? 0x90000000 : 0x00000000, 0);
        }
    }
}
