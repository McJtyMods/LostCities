package mcjty.lostcities.mixin;

import mcjty.lostcities.LostCities;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws the Lost Cities decoration next to the world-creation configuration button. */
@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {

    private static final Identifier LOSTCITIES_CONFIG_ICON =
            Identifier.fromNamespaceAndPath(LostCities.MODID, "textures/gui/configicon.png");

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void lostcities$extractConfigIcon(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                              float partialTick, CallbackInfo ci) {
        CreateWorldScreen screen = (CreateWorldScreen) (Object) this;
        if (screen.tabManager.getCurrentTab() instanceof CreateWorldScreen.MoreTab) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, LOSTCITIES_CONFIG_ICON,
                    screen.width - 100, 60, 70, 70, 256, 256, 256, 256, 256, 256);
        }
    }
}
