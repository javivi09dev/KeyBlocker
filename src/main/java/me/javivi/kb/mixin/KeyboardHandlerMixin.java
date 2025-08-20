package me.javivi.kb.mixin;

import me.javivi.kb.Config;
import me.javivi.kb.Keyblocker;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin adicional para interceptar el procesamiento de teclado a nivel más bajo
 */
@Mixin(Keyboard.class)
public class KeyboardHandlerMixin {

    /**
     * Intercepta la actualización de keybindings para resetear las bloqueadas
     */
    @Inject(method = "onKey", at = @At("TAIL"))
    public void onKeyProcessed(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        try {
            // Solo procesar en el cliente principal
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.world == null || client.player == null) {
                return;
            }

            Config config = Keyblocker.getInstance().getConfig();
            if (config == null) {
                return;
            }

            // Verificar todas las keybindings y resetear las que estén bloqueadas
            for (KeyBinding keyBinding : client.options.allKeys) {
                if (keyBinding != null && keyBinding.getTranslationKey() != null) {
                    if (config.isKeyDisabled(keyBinding.getTranslationKey())) {
                        // Forzar reset de la tecla bloqueada
                        keyBinding.setPressed(false);
                        
                        // Resetear timesPressed usando reflexión si es posible
                        try {
                            java.lang.reflect.Field timesPressedField = KeyBinding.class.getDeclaredField("timesPressed");
                            timesPressedField.setAccessible(true);
                            timesPressedField.setInt(keyBinding, 0);
                        } catch (Exception e) {
                            // Ignorar si no se puede acceder al campo
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignorar errores para no causar problemas
        }
    }
}
