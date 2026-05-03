package com.coredex.gazetteer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

final class GazetteerSpriteButton extends Button{
    static final Identifier CHANGE_ICON = Identifier.fromNamespaceAndPath("gazetteer", "icon/change");
    private static final Identifier VISIBLE_ICON = Identifier.fromNamespaceAndPath("gazetteer", "icon/visible");
    private static final Identifier NOT_VISIBLE_ICON = Identifier.fromNamespaceAndPath("gazetteer", "icon/not_visible");

    private final Supplier<Identifier> iconSupplier;

    GazetteerSpriteButton(int x, int y, int width, int height, OnPress onPress, Supplier<Identifier> iconSupplier){
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.iconSupplier = iconSupplier;
    }

    static Identifier visibilityIcon(boolean visible){
        return visible ? VISIBLE_ICON : NOT_VISIBLE_ICON;
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta){
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, iconSupplier.get(), getX(), getY(), width, height);
    }
}