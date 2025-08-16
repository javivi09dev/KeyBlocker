package me.javivi.kb.mixin;

import me.javivi.kb.Config;
import me.javivi.kb.Keyblocker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ControlsListWidget.KeyBindingEntry.class)
public class KeyBindingEntryMixin {
    
    @Shadow @Final private KeyBinding binding;
    
    @Inject(method = "method_25343", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        Config config = Keyblocker.getInstance().getConfig();
        if (config != null && config.isKeybindHidden(this.binding.getTranslationKey())) {
            ci.cancel();
        }
    }
}
