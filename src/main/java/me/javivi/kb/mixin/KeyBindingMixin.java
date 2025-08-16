package me.javivi.kb.mixin;

import me.javivi.kb.Config;
import me.javivi.kb.Keyblocker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(KeyBinding.class)
public class KeyBindingMixin {
    
    @Shadow private String translationKey;
    @Shadow private boolean pressed;
    @Shadow private int timesPressed;
    
    @Inject(method = "method_1436", at = @At("HEAD"), cancellable = true)
    private void onWasPressed(CallbackInfoReturnable<Boolean> cir) {
        Config config = Keyblocker.getInstance().getConfig();
        if (config != null && config.isKeyDisabled(this.translationKey)) {
            this.timesPressed = 0;
            cir.setReturnValue(false);
        }
    }
    
    @Inject(method = "method_1434", at = @At("HEAD"), cancellable = true)
    private void onIsPressed(CallbackInfoReturnable<Boolean> cir) {
        Config config = Keyblocker.getInstance().getConfig();
        if (config != null && config.isKeyDisabled(this.translationKey)) {
            cir.setReturnValue(false);
        }
    }
    
    @Inject(method = "method_23481", at = @At("HEAD"), cancellable = true)
    private void onSetPressed(boolean pressed, CallbackInfo ci) {
        Config config = Keyblocker.getInstance().getConfig();
        if (config != null && config.isKeyDisabled(this.translationKey) && pressed) {
            ci.cancel();
        }
    }
}
