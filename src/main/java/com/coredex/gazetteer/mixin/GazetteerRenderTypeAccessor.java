package com.coredex.gazetteer.mixin;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderType.class)
public interface GazetteerRenderTypeAccessor{
    @Invoker("create")
    static RenderType gazetteer$create(String name, RenderSetup renderSetup){
        throw new AssertionError();
    }
}