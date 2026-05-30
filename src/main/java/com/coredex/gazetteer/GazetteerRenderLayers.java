package com.coredex.gazetteer;

import com.coredex.gazetteer.mixin.GazetteerRenderTypeAccessor;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.Optional;
import java.util.function.Function;

public class GazetteerRenderLayers{
    private static final RenderPipeline POSITION_TEX_COLOR_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                    .withLocation("pipeline/position_tex_color")
                    .withCull(false)
                    .withColorTargetState(new ColorTargetState(Optional.empty(), com.mojang.blaze3d.GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_ALL))
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                    .build()
    );

    public static final Function<Identifier, RenderType> POSITION_TEX_COLOR = Util.memoize(
            texture -> GazetteerRenderTypeAccessor.gazetteer$create(
                    "gazetteer:pos_text_color",
                    RenderSetup.builder(POSITION_TEX_COLOR_PIPELINE)
                            .setLayeringTransform(LayeringTransform.NO_LAYERING)
                            .withTexture("Sampler0", texture)
                            .createRenderSetup()
            )
    );
}