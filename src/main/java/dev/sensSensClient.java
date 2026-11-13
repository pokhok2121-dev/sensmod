package dev.sens;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SensClient implements ClientModInitializer {
    // Vanilla slider: stored value 0.0-1.0 shows as 0%-200%
    private static final int STEP = 5; // percent per key press

    private static KeyBinding up;
    private static KeyBinding down;

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("sensmod", "main"));

        up = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.sensmod.up", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_BRACKET, category));
        down = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.sensmod.down", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_BRACKET, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (up.wasPressed()) setPercent(client, getPercent(client) + STEP);
            while (down.wasPressed()) setPercent(client, getPercent(client) - STEP);
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("sens")
                        .executes(ctx -> {
                            MinecraftClient c = ctx.getSource().getClient();
                            show(c, getPercent(c));
                            return 1;
                        })
                        .then(ClientCommandManager.argument("percent", IntegerArgumentType.integer(0, 200))
                                .executes(ctx -> {
                                    MinecraftClient c = ctx.getSource().getClient();
                                    setPercent(c, IntegerArgumentType.getInteger(ctx, "percent"));
                                    return 1;
                                }))));
    }

    private static int getPercent(MinecraftClient client) {
        return (int) Math.round(client.options.getMouseSensitivity().getValue() * 200.0);
    }

    private static void setPercent(MinecraftClient client, int percent) {
        percent = Math.max(0, Math.min(200, percent));
        client.options.getMouseSensitivity().setValue(percent / 200.0);
        client.options.write(); // save to options.txt
        show(client, percent);
    }

    private static void show(MinecraftClient client, int percent) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal("Sensitivity: " + percent + "%"), true);
        }
    }
          }
