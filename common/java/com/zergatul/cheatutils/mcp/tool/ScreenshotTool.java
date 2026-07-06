package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zergatul.cheatutils.configs.McpServerConfig;
import com.zergatul.cheatutils.mcp.McpItemStatus;
import com.zergatul.cheatutils.mcp.protocol.ImageContent;
import com.zergatul.cheatutils.render.ImageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ScreenshotTool implements McpTool {

    private final JsonObject inputSchema = JsonParser.parseString("""
            {
                "type": "object",
                "properties": {},
                "additionalProperties": false
            }
            """).getAsJsonObject();

    @Override
    public String getName() {
        return "take_screenshot";
    }

    @Override
    public String getTitle() {
        return "Take Screenshot";
    }

    @Override
    public String getDescription() {
        return
                "Captures the current Minecraft client screen and returns it as a PNG image. " +
                "Use only when visual inspection of game or UI state is useful and the agent can inspect returned images.";
    }

    @Override
    public JsonObject getInputSchema() {
        return inputSchema;
    }

    @Override
    public @Nullable JsonObject getOutputSchema() {
        return null;
    }

    @Override
    public McpItemStatus getStatus(McpServerConfig config) {
        McpItemStatus status = McpTool.super.getStatus(config);
        if (!status.isEnabled()) {
            return status;
        }

        if (!config.allowScreenshots) {
            return McpItemStatus.disabled("Disabled by CheatUtils MCP Server settings: allowScreenshots is false.");
        }

        return McpItemStatus.enabled();
    }

    @Override
    public McpToolCallResult invoke(JsonElement arguments) throws IOException {
        CompletableFuture<byte[]> imageFuture = new CompletableFuture<>();

        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            Screenshot.takeScreenshot(mc.gameRenderer.mainRenderTarget(), image -> {
                try (image) {
                    imageFuture.complete(ImageUtils.toPng(image));
                }
            });
        });

        try {
            byte[] image = imageFuture.get();
            String base64 = Base64.getEncoder().encodeToString(image);
            return McpToolCallResult.success(new ImageContent(base64, "image/png"));
        } catch (InterruptedException | ExecutionException e) {
            return McpToolCallResult.error(e.getMessage());
        }
    }
}