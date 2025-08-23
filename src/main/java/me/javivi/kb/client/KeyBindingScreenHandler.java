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

    private void registerScreenHandlers() {
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof ControlsOptionsScreen) {
                filterKeybindingsBeforeScreenRender(client);
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof ControlsOptionsScreen) {
                handleControlsScreenAfterInit(screen);
            }
        });
    }

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

        } catch (Exception e) {
            Keyblocker.LOGGER.debug("Could not filter keybindings before screen render", e);
        }
    }

    private void handleControlsScreenAfterInit(Object screen) {
        try {
            Config config = Keyblocker.getInstance().getConfig();
            if (config == null) return;

            filterScreenKeybindingLists(screen, config);

        } catch (Exception e) {
            Keyblocker.LOGGER.debug("Could not handle controls screen after init", e);
        }
    }

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
                        field.setAccessible(true);
                        KeyBinding[] testArray = (KeyBinding[]) field.get(MinecraftClient.getInstance().options);
                        if (testArray != null && testArray.length > 50) {
                            return field;
                        }
                    }
                } catch (NoSuchFieldException e) {
                }
            }

            Field bestField = null;
            int maxSize = 0;
            
            Field[] allFields = gameOptionsClass.getDeclaredFields();
            for (Field field : allFields) {
                if (field.getType() == KeyBinding[].class) {
                    try {
                        field.setAccessible(true);
                        KeyBinding[] testArray = (KeyBinding[]) field.get(MinecraftClient.getInstance().options);
                        if (testArray != null && testArray.length > maxSize) {
                            maxSize = testArray.length;
                            bestField = field;
                        }
                    } catch (Exception e) { 
                    }
                }
            }
            
            return bestField;

        } catch (Exception e) {
            Keyblocker.LOGGER.debug("Could not find keybindings field", e);
        }

        return null;
    }

    private boolean shouldHideKeyBinding(KeyBinding keyBinding, Config config) {
        if (keyBinding == null) return false;

        String translationKey = keyBinding.getTranslationKey();
        if (translationKey == null) return false;

        return config.isKeyDisabled(translationKey) || config.isKeybindHidden(translationKey);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void refreshCurrentScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof ControlsOptionsScreen) {
            filterKeybindingsBeforeScreenRender(client);
        }
    }
}