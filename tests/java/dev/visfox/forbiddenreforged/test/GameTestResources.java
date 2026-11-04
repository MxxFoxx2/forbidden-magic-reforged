package dev.visfox.forbiddenreforged.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.gametest.framework.GameTestHelper;
import dev.visfox.forbiddenreforged.common.FmIds;

final class GameTestResources {
    private GameTestResources() {
    }

    static JsonObject readJson(GameTestHelper helper, String path) {        try (InputStream stream = ForbiddenMagic.class.getResourceAsStream(path)) {
                if (stream == null) {
                    helper.fail("missing resource " + path);
                }
                return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            } catch (IOException e) {
                helper.fail("could not read " + path + ": " + e);
                return new JsonObject();
            }
        }
}
