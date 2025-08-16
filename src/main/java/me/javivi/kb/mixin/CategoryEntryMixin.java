package me.javivi.kb.mixin;

import me.javivi.kb.Config;
import me.javivi.kb.Keyblocker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ControlsListWidget.CategoryEntry.class)
public class CategoryEntryMixin {
    
    @Shadow @Final Text text;
    
    @Inject(method = "method_25343", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        Config config = Keyblocker.getInstance().getConfig();
        if (config != null) {
            String categoryKey = null;
            
            // Try to get translation key if it's translatable text
            if (this.text.getContent() instanceof TranslatableTextContent translatableContent) {
                categoryKey = translatableContent.getKey();
            }
            
            // Fallback to full text if not translatable
            if (categoryKey == null) {
                categoryKey = this.text.getString();
            }
            
            if (config.isCategoryHidden(categoryKey)) {
                ci.cancel();
            }
        }
    }
}
