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
import java.util.*;

public class GuiLostCityConfiguration extends GuiScreen {

    private final GuiCreateWorld parent;
    private Map<Integer, Runnable> actionHandler = new HashMap<>();
    private int page = 0;
    private int numpages;
    private GuiMutableLabel pagelabel;
    private String currentProfile;

    public GuiLostCityConfiguration(GuiCreateWorld parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        JsonParser parser = new JsonParser();
        String profileName = LostCityConfiguration.DEFAULT_PROFILE;
        if (parent.chunkProviderSettingsJson != null && !parent.chunkProviderSettingsJson.trim().isEmpty()) {
            JsonElement parsed = parser.parse(parent.chunkProviderSettingsJson);
            if (parsed.getAsJsonObject().has("profile")) {
                profileName = parsed.getAsJsonObject().get("profile").getAsString();
            }
        }

        page = 0;
        numpages = (LostCityConfiguration.profiles.size() + 7) / 8;

        setupGui(profileName);
    }

    private void setupGui(String profileName) {
        currentProfile = profileName;
        actionHandler.clear();
        this.buttonList.clear();
        this.labelList.clear();
        int id = 301;
        int y = 8;
        int num = -1;
        int cnt = 0;

        List<String> profileKeys = new ArrayList<>(LostCityConfiguration.profiles.keySet());
        profileKeys.sort(new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return s.compareTo(t1);
            }
        });
        for (String key : profileKeys) {
            LostCityProfile profile = LostCityConfiguration.profiles.get(key);
            if (profile.isPublic()) {
                num++;
                if (num < page * 8) {
                    continue;
                }
                if (cnt >= 8) {
                    break;
                }
                cnt++;
                GuiButton button = new GuiButton(id, 10, y, 90, 20, key);
                if (profileName.equals(profile.getName())) {
                    button.packedFGColour = 0xffffff00;
                }
                this.buttonList.add(button);
                actionHandler.put(id, () -> setProfile(profile));
                id++;

                GuiLabel label = new GuiLabel(Minecraft.getMinecraft().fontRenderer, id++, 110, y, 230, 20, 0xffffffff);
                label.addLine(profile.getDescription());
                this.labelList.add(label);
                y += 22;
            }
        }


        y = 200;
        GuiLabel label = new GuiLabel(Minecraft.getMinecraft().fontRenderer, id++, 20, y, 340, 20, 0xffffffff);
        label.addLine("(note, you can create your own profiles and many more");
        label.addLine("configuration options in 'lostcities.cfg')");
        this.labelList.add(label);

        if (numpages > 1) {
            GuiButton prev = new GuiButton(id, 330, y, 20, 19, "<");
            this.buttonList.add(prev);
            actionHandler.put(id, () -> { page = page > 0 ? page - 1 : page; setupGui(profileName); });

            id++;
            pagelabel = new GuiMutableLabel(Minecraft.getMinecraft().fontRenderer, id++, 360, y, 30, 20, 0xffffffff);
            pagelabel.addLine("" + (page+1) + "/" + numpages);

            GuiButton next = new GuiButton(id, 390, y, 20, 19, ">");
            this.buttonList.add(next);
            actionHandler.put(id, () -> { page = page < numpages-1 ? page + 1 : page; setupGui(profileName); });
            id++;
        }
    }

    private void setProfile(LostCityProfile profile) {
        parent.chunkProviderSettingsJson = "{ \"profile\": \"" + profile.getName() + "\" }";
        this.mc.displayGuiScreen(parent);
        currentProfile = profile.getName();
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
        if (numpages > 1) {
            pagelabel.clearLines();
            pagelabel.addLine("" + (page+1) + "/" + numpages);
            pagelabel.drawLabel(Minecraft.getMinecraft(), mouseX, mouseY);
        }

        LostCityProfile profile = LostCityConfiguration.profiles.get(currentProfile);
        if (profile.getIcon() != null) {
            mc.getTextureManager().bindTexture(profile.getIcon());
            drawTexturedModalRect(0, 0, 0, 0, 64, 64);
        }
    }
}
