package me.javivi.kb.client;

import me.javivi.kb.Config;
import me.javivi.kb.Keyblocker;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.option.KeyBinding;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * New approach: Instead of trying to filter keybindings after they're displayed,
 * we modify the keybinding list before the screen is rendered.
 * This uses a technique similar to what key-binding-hider-mod does.
 */
public class KeyBindingScreenHandler {
    private static KeyBindingScreenHandler instance;
    private static boolean initialized = false;

    private KeyBindingScreenHandler() {}

    public static KeyBindingScreenHandler getInstance() {
        if (instance == null) {
            instance = new KeyBindingScreenHandler();
        }
        return instance;
    }

    /**
     * Initialize the screen handling system
     */
    public void initialize() {
        if (initialized) return;

        try {
            registerScreenHandlers();
            initialized = true;
            Keyblocker.LOGGER.info("KeyBindingScreenHandler initialized with pre-render filtering");
        } catch (Exception e) {
            Keyblocker.LOGGER.error("Failed to initialize KeyBindingScreenHandler", e);
        }
    }

    /**
     * Register handlers to intercept controls screen before it renders
     */
    private void registerScreenHandlers() {
        // Intercept when the controls screen is about to be opened
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof ControlsOptionsScreen) {
                filterKeybindingsBeforeScreenRender(client);
            }
        });

        // Also intercept after init in case the first attempt fails
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof ControlsOptionsScreen) {
                handleControlsScreenAfterInit(screen);
            }
        });
    }

    /**
     * Filter keybindings before the screen renders by modifying the global keybinding array
     */
    private void filterKeybindingsBeforeScreenRender(MinecraftClient client) {
        try {
            Config config = Keyblocker.getInstance().getConfig();
            if (config == null) return;

            if (client.options == null) return;

            Field allKeyBindingsField = findKeybindingsField(client.options.getClass());
            if (allKeyBindingsField == null) return;

            allKeyBindingsField.setAccessible(true);
            KeyBinding[] allKeyBindings = (KeyBinding[]) allKeyBindingsField.get(client.options);

            if (allKeyBindings == null) return;

            List<KeyBinding> filteredList = new ArrayList<>();
            int hiddenCount = 0;

            for (KeyBinding keyBinding : allKeyBindings) {
                if (keyBinding != null && !shouldHideKeyBinding(keyBinding, config)) {
                    filteredList.add(keyBinding);
                } else if (keyBinding != null) {
                    hiddenCount++;
                }
            }

            KeyBinding[] filteredArray = filteredList.toArray(new KeyBinding[0]);

            allKeyBindingsField.set(client.options, filteredArray);

            if (hiddenCount > 0) {
                Keyblocker.LOGGER.info("Filtered {} keybindings from controls screen", hiddenCount);
            }

        } catch (Exception e) {
            Keyblocker.LOGGER.debug("Could not filter keybindings before screen render", e);
        }
    }

    /**
     * Handle controls screen after initialization
     */
    private void handleControlsScreenAfterInit(Object screen) {
        try {
            Config config = Keyblocker.getInstance().getConfig();
            if (config == null) return;

            filterScreenKeybindingLists(screen, config);

        } catch (Exception e) {
            Keyblocker.LOGGER.debug("Could not handle controls screen after init", e);
        }
    }

    /**
     * Filter keybinding lists found in the screen object
     */
    private void filterScreenKeybindingLists(Object screen, Config config) {
        try {
            Class<?> currentClass = screen.getClass();

            while (currentClass != null && currentClass != Object.class) {
                Field[] fields = currentClass.getDeclaredFields();

                for (Field field : fields) {
                    field.setAccessible(true);

                    try {
                        Object value = field.get(screen);

                        if (value instanceof KeyBinding[] keyArray) {
                            filterKeyBindingArray(keyArray, config, field, screen);
                        }
                        else if (value instanceof List<?> list && !list.isEmpty()) {
                            if (list.get(0) instanceof KeyBinding) {
                                filterKeyBindingList(list, config);
                            }
                        }

                    } catch (Exception e) {
                    }
                }

                currentClass = currentClass.getSuperclass();
            }

        } catch (Exception e) {
            Keyblocker.LOGGER.debug("Could not filter screen keybinding lists", e);
        }
    }

    /**
     * Filter a KeyBinding array
     */
    private void filterKeyBindingArray(KeyBinding[] keyArray, Config config, Field field, Object screen) {
        try {
            List<KeyBinding> filteredList = new ArrayList<>();
            int hiddenCount = 0;

            for (KeyBinding keyBinding : keyArray) {
                if (keyBinding != null && !shouldHideKeyBinding(keyBinding, config)) {
                    filteredList.add(keyBinding);
                } else if (keyBinding != null) {
                    hiddenCount++;
                }
            }

            if (hiddenCount > 0) {
                KeyBinding[] filteredArray = filteredList.toArray(new KeyBinding[0]);
                field.set(screen, filteredArray);

                Keyblocker.LOGGER.info("Filtered {} keybindings from screen array field: {}", 
                    hiddenCount, field.getName());
            }

        } catch (Exception e) {
            Keyblocker.LOGGER.debug("Could not filter keybinding array", e);
        }
    }

    /**
     * Filter a KeyBinding list
     */
    @SuppressWarnings("unchecked")
    private void filterKeyBindingList(List<?> list, Config config) {
        try {
            List<KeyBinding> keyBindingList = (List<KeyBinding>) list;
            int originalSize = keyBindingList.size();

            keyBindingList.removeIf(keyBinding -> shouldHideKeyBinding(keyBinding, config));

            int hiddenCount = originalSize - keyBindingList.size();
            if (hiddenCount > 0) {
                Keyblocker.LOGGER.info("Filtered {} keybindings from screen list", hiddenCount);
            }

        } catch (Exception e) {
            Keyblocker.LOGGER.debug("Could not filter keybinding list", e);
        }
    }

    /**
     * Find the keybindings field in GameOptions class
     */
    private Field findKeybindingsField(Class<?> gameOptionsClass) {
        try {
            String[] possibleFieldNames = {
                "allKeys",
                "keyBindings", 
                "allKeyBindings",
                "keys"
            };

            for (String fieldName : possibleFieldNames) {
                try {
                    Field field = gameOptionsClass.getDeclaredField(fieldName);
                    if (field.getType() == KeyBinding[].class) {
                        return field;
                    }
                } catch (NoSuchFieldException e) {
                    // Try next field name
                }
            }

            // If field names don't work, search by type
            Field[] fields = gameOptionsClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.getType() == KeyBinding[].class) {
                    return field;
                }
            }

        } catch (Exception e) {
            Keyblocker.LOGGER.debug("Could not find keybindings field", e);
        }

        return null;
    }

    /**
     * Determine if a keybinding should be hidden
     */
    private boolean shouldHideKeyBinding(KeyBinding keyBinding, Config config) {
        if (keyBinding == null) return false;

        String translationKey = keyBinding.getTranslationKey();
        if (translationKey == null) return false;

        // Hide if it's disabled (blocked) or explicitly hidden
        return config.isKeyDisabled(translationKey) || config.isKeybindHidden(translationKey);
    }

    /**
     * Check if the system is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Force refresh the current controls screen if open
     */
    public void refreshCurrentScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof ControlsOptionsScreen) {
            filterKeybindingsBeforeScreenRender(client);
        }
    }
}