package me.javivi.kb.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.javivi.kb.api.KeyBlockerAPI;
import me.javivi.kb.network.NetworkHandler;
import me.javivi.kb.network.UpdateKeysPacket;
import me.javivi.kb.server.KeyBindingManager;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;

public class KeyblockerCommand {
    
    private static final SuggestionProvider<ServerCommandSource> KEY_SUGGESTIONS = (context, builder) -> {
        List<String> allKeyBindings = KeyBindingManager.getInstance().getAllKnownKeyBindings();
        
        if (!allKeyBindings.isEmpty()) {
            for (String keyName : allKeyBindings) {
                builder.suggest(keyName);
            }
        } else {
            // Fallback common keys
            builder.suggest("key.togglePerspective");
            builder.suggest("key.screenshot");
            builder.suggest("key.inventory");
            builder.suggest("key.chat");
            builder.suggest("key.forward");
            builder.suggest("key.back");
            builder.suggest("key.left");
            builder.suggest("key.right");
            builder.suggest("key.jump");
            builder.suggest("key.sneak");
            builder.suggest("key.sprint");
            builder.suggest("key.attack");
            builder.suggest("key.use");
        }
        return builder.buildFuture();
    };
    
    private static final SuggestionProvider<ServerCommandSource> CATEGORY_SUGGESTIONS = (context, builder) -> {
        List<String> allCategories = KeyBindingManager.getInstance().getAllKnownCategories();
        
        if (!allCategories.isEmpty()) {
            for (String categoryName : allCategories) {
                builder.suggest(categoryName);
            }
        } else {
            // Fallback common categories
            builder.suggest("key.categories.movement");
            builder.suggest("key.categories.gameplay");
            builder.suggest("key.categories.inventory");
            builder.suggest("key.categories.creative");
            builder.suggest("key.categories.multiplayer");
            builder.suggest("key.categories.ui");
            builder.suggest("key.categories.misc");
        }
        return builder.buildFuture();
    };
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> keyblockerCommand = CommandManager.literal("keyblocker")
                .requires(source -> source.hasPermissionLevel(2));
        
        keyblockerCommand.then(CommandManager.literal("blockkey")
                .then(CommandManager.argument("key", StringArgumentType.string())
                        .suggests(KEY_SUGGESTIONS)
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .executes(context -> executeKeyAction(context, StringArgumentType.getString(context, "key"), 
                                        EntityArgumentType.getPlayers(context, "targets"), "block")))));
        
        // Comando para desbloquear teclas
        keyblockerCommand.then(CommandManager.literal("unblockkey")
                .then(CommandManager.argument("key", StringArgumentType.string())
                        .suggests(KEY_SUGGESTIONS)
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .executes(context -> executeKeyAction(context, StringArgumentType.getString(context, "key"), 
                                        EntityArgumentType.getPlayers(context, "targets"), "unblock")))));
        
        // Comando para ocultar teclas (solo oculta del menú)
        keyblockerCommand.then(CommandManager.literal("hidekey")
                .then(CommandManager.argument("key", StringArgumentType.string())
                        .suggests(KEY_SUGGESTIONS)
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .executes(context -> executeKeyAction(context, StringArgumentType.getString(context, "key"), 
                                        EntityArgumentType.getPlayers(context, "targets"), "hide")))));
        
        // Comando para mostrar teclas
        keyblockerCommand.then(CommandManager.literal("unhidekey")
                .then(CommandManager.argument("key", StringArgumentType.string())
                        .suggests(KEY_SUGGESTIONS)
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .executes(context -> executeKeyAction(context, StringArgumentType.getString(context, "key"), 
                                        EntityArgumentType.getPlayers(context, "targets"), "unhide")))));
        
        // Comando para ocultar categorías
        keyblockerCommand.then(CommandManager.literal("hidecategory")
                .then(CommandManager.argument("category", StringArgumentType.string())
                        .suggests(CATEGORY_SUGGESTIONS)
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .executes(context -> executeCategoryAction(context, StringArgumentType.getString(context, "category"), 
                                        EntityArgumentType.getPlayers(context, "targets"), "hide")))));
        
        keyblockerCommand.then(CommandManager.literal("unhidecategory")
                .then(CommandManager.argument("category", StringArgumentType.string())
                        .suggests(CATEGORY_SUGGESTIONS)
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .executes(context -> executeCategoryAction(context, StringArgumentType.getString(context, "category"), 
                                        EntityArgumentType.getPlayers(context, "targets"), "unhide")))));
        
        // Sync server config to players
        keyblockerCommand.then(CommandManager.literal("sync")
                .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .executes(context -> executeSyncAction(context, EntityArgumentType.getPlayers(context, "targets")))));
        
        dispatcher.register(keyblockerCommand);
    }
    
    private static int executeKeyAction(CommandContext<ServerCommandSource> context, String key, 
                                      Collection<ServerPlayerEntity> targets, String action) {
        UpdateKeysPacket packet = new UpdateKeysPacket(key, action, "key");
        
        for (ServerPlayerEntity player : targets) {
            NetworkHandler.sendToClient(packet, player);
        }
        
        String actionMsg = switch (action) {
            case "hide" -> "oculta";
            case "unhide" -> "visible";
            case "block" -> "bloqueada";
            case "unblock" -> "desbloqueada";
            default -> action;
        };
        
        String playerText = targets.size() == 1 ? "jugador" : "jugadores";
        context.getSource().sendFeedback(() -> 
            Text.literal("§eLa tecla §6" + key + "§e ha sido §6" + actionMsg + "§e para §6" + targets.size() + "§e " + playerText), true);
        
        return 1;
    }
    
    private static int executeCategoryAction(CommandContext<ServerCommandSource> context, String category, 
                                           Collection<ServerPlayerEntity> targets, String action) {
        UpdateKeysPacket packet = new UpdateKeysPacket(category, action, "category");
        
        for (ServerPlayerEntity player : targets) {
            NetworkHandler.sendToClient(packet, player);
        }
        
        String actionMsg = switch (action) {
            case "hide" -> "oculta";
            case "unhide" -> "visible";
            default -> action;
        };
        
        String playerText = targets.size() == 1 ? "jugador" : "jugadores";
        context.getSource().sendFeedback(() -> 
            Text.literal("§eLa categoría §6" + category + "§e ha sido §6" + actionMsg + "§e para §6" + targets.size() + "§e " + playerText), true);
        
        return 1;
    }
    
    private static int executeSyncAction(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> targets) {
        try {
            KeyBlockerAPI api = KeyBlockerAPI.getInstance();
            boolean success = api.syncConfigToPlayers(targets);
            
            if (success) {
                String playerText = targets.size() == 1 ? "jugador" : "jugadores";
                context.getSource().sendFeedback(() -> 
                    Text.literal("§aConfiguración del servidor sincronizada con §6" + targets.size() + "§a " + playerText), true);
                return 1;
            } else {
                context.getSource().sendError(Text.literal("§cError al sincronizar la configuración"));
                return 0;
            }
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("§cError inesperado al sincronizar: " + e.getMessage()));
            return 0;
        }
    }
}
