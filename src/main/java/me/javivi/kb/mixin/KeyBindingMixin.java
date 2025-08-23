package me.javivi.kb.mixin;

import me.javivi.kb.Config;
import me.javivi.kb.Keyblocker;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class KeyBindingMixin {

    @Shadow
    private String translationKey;

    @Inject(method = "wasPressed", at = @At("HEAD"), cancellable = true)
    public void blockWasPressed(CallbackInfoReturnable<Boolean> cir) {
        if (isKeyBlocked()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isPressed", at = @At("HEAD"), cancellable = true)
    public void blockIsPressed(CallbackInfoReturnable<Boolean> cir) {
        if (isKeyBlocked()) {
            cir.setReturnValue(false);
        }
    }

    private boolean isKeyBlocked() {
        try {
            if (this.translationKey == null) {
                return false;
            }

            Config config = Keyblocker.getInstance().getConfig();
            if (config == null) {
                return false;
            }

            return config.isKeyDisabled(this.translationKey);
        } catch (Exception e) {
            return false;
        }
    }
}
