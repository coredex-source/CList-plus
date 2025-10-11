package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public record CListReverseColoredQuadGuiElementRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, int x0, int y0, int x1, int y1, int col1, int col2, @Nullable ScreenRect scissorArea, @Nullable ScreenRect bounds) implements SimpleGuiElementRenderState{
    public CListReverseColoredQuadGuiElementRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, int x0, int y0, int x1, int y1, int col1, int col2, @Nullable ScreenRect scissorArea){
        this(pipeline, textureSetup, pose, x0, y0, x1, y1, col1, col2, scissorArea, createBounds(x0, y0, x1, y1, pose, scissorArea));
    }

    @Override
    public void setupVertices(VertexConsumer vertices){
        // Convert Matrix3x2f to Matrix4f for vertex operations
        Matrix3x2f pose = this.pose();
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.set(pose.m00(), pose.m01(), 0.0f, pose.m20(),
                     pose.m10(), pose.m11(), 0.0f, pose.m21(),
                     0.0f,       0.0f,       1.0f, 0.0f,
                     0.0f,       0.0f,       0.0f, 1.0f);
        vertices.vertex(matrix4f, (float) this.x0(), (float) this.y0(), 0.0f).color(this.col1());
        vertices.vertex(matrix4f, (float) this.x0(), (float) this.y1(), 0.0f).color(this.col1());
        vertices.vertex(matrix4f, (float) this.x1(), (float) this.y1(), 0.0f).color(this.col2());
        vertices.vertex(matrix4f, (float) this.x1(), (float) this.y0(), 0.0f).color(this.col2());
    }

    @Nullable
    private static ScreenRect createBounds(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRect scissorArea){
        ScreenRect screenRect = (new ScreenRect(x0, y0, x1 - x0, y1 - y0)).transformEachVertex(pose);
        return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
    }

    public RenderPipeline pipeline(){
        return this.pipeline;
    }

    public TextureSetup textureSetup(){
        return this.textureSetup;
    }

    public Matrix3x2f pose(){
        return this.pose;
    }

    public int x0(){
        return this.x0;
    }

    public int y0(){
        return this.y0;
    }

    public int x1(){
        return this.x1;
    }

    public int y1(){
        return this.y1;
    }

    public int col1(){
        return this.col1;
    }

    public int col2(){
        return this.col2;
    }

    @Nullable
    public ScreenRect scissorArea(){
        return this.scissorArea;
    }

    @Nullable
    public ScreenRect bounds(){
        return this.bounds;
    }
}
