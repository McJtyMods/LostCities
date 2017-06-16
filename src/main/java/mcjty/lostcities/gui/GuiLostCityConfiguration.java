package mcjty.lostcities.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GuiLostCityConfiguration extends GuiScreen {

    private final GuiCreateWorld parent;
    private Map<Integer, Runnable> actionHandler = new HashMap<>();

    public GuiLostCityConfiguration(GuiCreateWorld parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        JsonParser parser = new JsonParser();
        String profileName = "default";
        if (parent.chunkProviderSettingsJson != null && !parent.chunkProviderSettingsJson.trim().isEmpty()) {
            JsonElement parsed = parser.parse(parent.chunkProviderSettingsJson);
            if (parsed.getAsJsonObject().has("profile")) {
                profileName = parsed.getAsJsonObject().get("profile").getAsString();
            }
        }

        actionHandler.clear();
        this.buttonList.clear();
        int id = 301;
        int y = 10;
        for (Map.Entry<String, LostCityProfile> entry : LostCityConfiguration.profiles.entrySet()) {
            GuiButton button = new GuiButton(id, 20, y, 100, 20, entry.getKey());
            if (profileName.equals(entry.getValue().getName())) {
                button.packedFGColour = 0xffffff00;
            }
            this.buttonList.add(button);
            actionHandler.put(id, () -> setProfile(entry.getValue()));
            id++;

            GuiLabel label = new GuiLabel(Minecraft.getMinecraft().fontRenderer, id++, 140, y, 200, 20, 0xffffffff);
            label.addLine(entry.getValue().getDescription());
            this.labelList.add(label);
            y += 22;
        }

        y += 10;
        GuiLabel label = new GuiLabel(Minecraft.getMinecraft().fontRenderer, id++, 20, y, 340, 20, 0xffffffff);
        label.addLine("(note, you can create your own profiles and many more");
        label.addLine("configuration options in 'lostcities.cfg')");
        this.labelList.add(label);
    }

    private void setProfile(LostCityProfile profile) {
        parent.chunkProviderSettingsJson = "{ \"profile\": \"" + profile.getName() + "\" }";
        this.mc.displayGuiScreen(parent);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (actionHandler.containsKey(button.id)) {
            actionHandler.get(button.id).run();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
