package me.javivi.kb.mixin;

import me.javivi.kb.Config;
import me.javivi.kb.Keyblocker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Keyboard.class)
public class KeyboardHandlerMixin {
    
    @Inject(method = "method_1466", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (window == MinecraftClient.getInstance().getWindow().getHandle()) {
            String keyName = InputUtil.fromKeyCode(key, scancode).getTranslationKey();
            Config config = Keyblocker.getInstance().getConfig();
            
            if (config != null && config.isKeyDisabled(keyName)) {
                ci.cancel();
                if (config.consoleLogs) {
                    Keyblocker.LOGGER.info("Blocked key intercepted: " + keyName);
                }
            }
        }
    }
}
