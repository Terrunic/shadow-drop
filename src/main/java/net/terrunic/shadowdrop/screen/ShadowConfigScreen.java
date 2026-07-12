package net.terrunic.shadowdrop.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.fml.ModList;
import net.terrunic.shadowdrop.utils.ShadowContext;
import net.terrunic.shadowdrop.ShadowDrop;
import net.terrunic.shadowdrop.ShadowDropConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// Custom config screen
public class ShadowConfigScreen extends Screen
{
    // Helper records

    private record ConfigPos(int X, int Y) {}
    private record ConfigRect(int X, int Y, int W, int H) {}

    // Texture files

    private static final ResourceLocation GUI_TEX       = new ResourceLocation("shadowdrop", "textures/gui/config.png");
    private static final ResourceLocation WIDGETS_TEX   = new ResourceLocation("minecraft",  "textures/gui/widgets.png");
    private static final ResourceLocation INVENTORY_TEX = new ResourceLocation("minecraft",  "textures/gui/container/inventory.png");

    // GUI element positions, sizes, etc

    private static final ConfigRect GUI = new ConfigRect(0, 0, 207, 213);
    private static final int GUI_T_SIZE = 256;

    private static final ConfigPos GUI_TITLE = new ConfigPos(3, 3);

    private static final ConfigRect PREVIEW_EDITBOX        = new ConfigRect(14, 33, 192, 12);
    private static final ConfigRect PREVIEW_SLOT_HOTBAR    = new ConfigRect(60, 23, 22, 22);
    private static final ConfigRect PREVIEW_SLOT_CONTAINER = new ConfigRect(7, 141, 18, 18);
    private static final ConfigPos[] PREVIEW_SLOTS = {
        new ConfigPos(12, 55),
        new ConfigPos(42, 57),
        new ConfigPos(72, 57),
        new ConfigPos(102, 57),
        new ConfigPos(-1, -1),
        new ConfigPos(-1, -1)
    };
    private static final ConfigPos[] PREVIEW_ITEMS = {
        new ConfigPos(15, 58),
        new ConfigPos(43, 58),
        new ConfigPos(73, 58),
        new ConfigPos(99, 54),
        new ConfigPos(146, 58),
        new ConfigPos(176, 58)
    };
    private static final ConfigRect PREVIEW_CURSOR = new ConfigRect(2, 223, 6, 8);
    private static final ConfigPos[] PREVIEW_CURSORS = {
        new ConfigPos(85, 69),
        new ConfigPos(107, 62)
    };
    private static final ConfigRect PREVIEW_CHECKBOX_SHADOW = new ConfigRect(1, 232, 10, 10);
    private static final ConfigRect PREVIEW_CHECKBOX_CROP   = new ConfigRect(11, 232, 10, 10);
    private static final ConfigRect PREVIEW_CHECKBOX_OFF    = new ConfigRect(21, 232, 10, 10);
    private static final ConfigRect PREVIEW_CHECKBOX_HOVER  = new ConfigRect(31, 232, 10, 10);
    private static final ConfigPos[] PREVIEW_CHECKBOXES = {
        new ConfigPos(13, 78), new ConfigPos(23, 78),
        new ConfigPos(41, 78), new ConfigPos(51, 78),
        new ConfigPos(71, 78), new ConfigPos(81, 78),
        new ConfigPos(106, 78),
        new ConfigPos(149, 78),
        new ConfigPos(179, 78)
    };
    private static final ConfigRect FIELD_COLOR     = new ConfigRect(14, 114, 51, 12);
    private static final ConfigRect FIELD_ALPHA     = new ConfigRect(14, 130, 51, 12);
    private static final ConfigPos FIELD_ALPHA_DECO = new ConfigPos(56, 132);

    private static final ConfigRect FIELD_ARROW_HOVER = new ConfigRect(13, 243, 12,12);
    private static final ConfigPos[] FIELD_ARROWS = {
        new ConfigPos(14, 162),
        new ConfigPos(27, 162),
        new ConfigPos(40, 162),
        new ConfigPos(53, 162)
    };
    private static final ConfigRect FIELD_CROPCOLOR     = new ConfigRect(103, 114, 90, 12);
    private static final ConfigRect FIELD_TRANSLUCENT   = new ConfigRect(103, 146, 90, 12);
    private static final ConfigRect FIELD_TRANSPARENT   = new ConfigRect(103, 162, 90, 12);
    private static final ConfigRect FIELD_LISTBTN_MINUS = new ConfigRect(1, 243, 12, 12);
    private static final ConfigRect FIELD_LISTBTN_HOVER = new ConfigRect(13, 243, 12, 12);
    private static final ConfigPos[] FIELD_LISTBTNS = {
        new ConfigPos(194, 114),
        new ConfigPos(194, 146),
        new ConfigPos(194, 162)
    };
    private static final ConfigPos DONE_BTN     = new ConfigPos(0, 193);
    private static final ConfigRect DONE_HOVER  = new ConfigRect( 48, 235, 186, 20);
    private static final ConfigPos DONE_TEXT    = new ConfigPos(92, 199);

    private static final ConfigPos RESET_BTN    = new ConfigPos(187, 193);
    private static final ConfigRect RESET_HOVER = new ConfigRect(235, 235, 20, 20);

    private static final ConfigRect WARNING_R_LABEL = new ConfigRect(9, 219, 17, 12);
    private static final ConfigPos[] WARNING_R_LABELS = {
        new ConfigPos(207, 1),
        new ConfigPos(207, 114),
        new ConfigPos(207, 146),
        new ConfigPos(207, 162)
    };
    private static final ConfigRect WARNING_L_LABEL = new ConfigRect(26, 219, 17, 12);
    private static final ConfigPos[] WARNING_L_LABELS = {
        new ConfigPos(-17, 130),
        new ConfigPos(-17, 130),
        new ConfigPos(-17, 162)
    };

    // Tooltip data

    private static final String[][] CHECKBOX_CONTEXTS = {
        {"shadows", "hotbar"   }, {"cropping", "hotbar"   },
        {"shadows", "slot"     }, {"cropping", "slot"     },
        {"shadows", "hover"    }, {"cropping", "hover"    },
        {"shadows", "cursor"   },
        {"shadows", "outside"  },
        {"shadows", "elsewhere"}
    };
    private static final String[] CHECKBOX_CONTEXTS_WITH_EXTRAS = {
        "slot",
        "outside",
        "elsewhere"
    };

    // Known mod ids that can cause compatibility issues without proper config

    private static final String[] WARN_MODS = {
        "emi",
        "immediatelyfast",
        "acceleratedrendering"
    };

    // Config cache

    private boolean hotbarEnabled, hotbarCropped;
    private boolean slotEnabled, slotCropped;
    private boolean hoverEnabled, hoverCropped;
    private boolean cursorEnabled, elsewhereEnabled, outsideEnabled;
    private String shadowColor;
    private int shadowAlpha, shadowOffsetX, shadowOffsetY;
    private List<String> slotBrColorsList, translucentList, transparentList;

    // Widgets

    private EditBox previewItemField, colorField, alphaField;
    private EditBox slotBrColorsField, translucentField, transparentField;

    // Misc cache

    private ItemStack previewStack = new ItemStack(Items.IRON_SWORD);
    private String lastValidItemId = "minecraft:iron_sword";
    private String lastSlotBrColor = "#FFFFFF";

    private final Screen parent;
    private int L;
    private int T;
    private int MX = 0;
    private int MY = 0;

    // Screen backend setup
    public ShadowConfigScreen(Screen parent)
    {
        super(Component.translatable("gui.shadowdrop.config.title"));
        this.parent = parent;
        loadFromConfig();
    }

    // Screen frontend setup
    @Override
    protected void init()
    {
        L = guiLeft();
        T = guiTop();
        refreshSlotBrPixelColor();

        previewItemField = new EditBox(font, L + PREVIEW_EDITBOX.X, T + PREVIEW_EDITBOX.Y, PREVIEW_EDITBOX.W, PREVIEW_EDITBOX.H, Component.empty());
        previewItemField.setMaxLength(1024);
        previewItemField.setValue(lastValidItemId);
        previewItemField.setResponder(this::onPreviewItemChanged);
        addRenderableWidget(previewItemField);

        colorField = new EditBox(font, L + FIELD_COLOR.X, T + FIELD_COLOR.Y, FIELD_COLOR.W, FIELD_COLOR.H, Component.empty());
        colorField.setMaxLength(7);
        colorField.setValue(shadowColor);
        colorField.setFilter(s -> s.matches("#?[0-9a-fA-F]{0,6}"));
        colorField.setResponder(this::onColorChanged);
        addRenderableWidget(colorField);

        alphaField = new EditBox(font, L + FIELD_ALPHA.X, T + FIELD_ALPHA.Y, FIELD_ALPHA.W, FIELD_ALPHA.H, Component.empty());
        alphaField.setMaxLength(3);
        alphaField.setValue(String.valueOf(shadowAlpha));
        alphaField.setFilter(s -> s.matches("\\d{0,3}"));
        alphaField.setResponder(this::onAlphaChanged);
        addRenderableWidget(alphaField);

        slotBrColorsField = new EditBox(font, L + FIELD_CROPCOLOR.X, T + FIELD_CROPCOLOR.Y, FIELD_CROPCOLOR.W, FIELD_CROPCOLOR.H, Component.empty());
        slotBrColorsField.setMaxLength(1024);
        slotBrColorsField.setValue(String.join(", ", slotBrColorsList));
        slotBrColorsField.setResponder(s -> { slotBrColorsList = parseList(s); pushToConfig(); });
        addRenderableWidget(slotBrColorsField);

        translucentField = new EditBox(font, L + FIELD_TRANSLUCENT.X, T + FIELD_TRANSLUCENT.Y, FIELD_TRANSLUCENT.W, FIELD_TRANSLUCENT.H, Component.empty());
        translucentField.setMaxLength(1024);
        translucentField.setValue(String.join(", ", translucentList));
        translucentField.setResponder(s -> { translucentList = parseList(s); pushToConfig(); });
        addRenderableWidget(translucentField);

        transparentField = new EditBox(font, L + FIELD_TRANSPARENT.X, T + FIELD_TRANSPARENT.Y, FIELD_TRANSPARENT.W, FIELD_TRANSPARENT.H, Component.empty());
        transparentField.setMaxLength(1024);
        transparentField.setValue(String.join(", ", transparentList));
        transparentField.setResponder(s -> { transparentList = parseList(s); pushToConfig(); });
        addRenderableWidget(transparentField);
    }

    // Handle screen close
    @Override
    public void onClose()
    {
        pushToConfig();
        Minecraft.getInstance().setScreen(parent);
    }

    // Does screen pause game
    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    // Screen render tick
    @Override
    public void render(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float delta)
    {
        renderBackground(gfx); // Dirt background
        L = guiLeft();
        T = guiTop();
        MX = mouseX;
        MY = mouseY;

        // GUI background & title
        gfx.blit(GUI_TEX, L, T, 0, 0, GUI.W, GUI.H, GUI_T_SIZE, GUI_T_SIZE);
        gfx.drawString(font, Component.translatable("gui.shadowdrop.config.title").withStyle(Style.EMPTY.withBold(true)), L + GUI_TITLE.X, T + GUI_TITLE.Y, 0xFFFFFF);

        // Slot backgrounds & item previews with blend to keep translucency
        RenderSystem.enableBlend();
        renderSlotBackgrounds(gfx);
        renderItemPreviews(gfx);

        // Preview item hover effect (translucent white square)
        gfx.fill(L + PREVIEW_ITEMS[2].X, T + PREVIEW_ITEMS[2].Y, L + PREVIEW_ITEMS[2].X + 16, T + PREVIEW_ITEMS[2].Y + 16, 200, 0x80FFFFFF);

        // Preview cursors (above other elements)
        gfx.pose().translate(0, 0, 200);
        guiTextureBlit(gfx, PREVIEW_CURSORS[0], PREVIEW_CURSOR);
        guiTextureBlit(gfx, PREVIEW_CURSORS[1], PREVIEW_CURSOR);
        gfx.pose().translate(0, 0, -200);

        // Boxes and buttons
        renderCheckboxes(gfx);
        renderListButtons(gfx);
        renderArrowButtons(gfx);
        renderDoneButton(gfx);
        renderResetButton(gfx);
        renderWarnings(gfx);
        // Editboxes etc
        super.render(gfx, mouseX, mouseY, delta);

        // Labels
        gfx.pose().translate(0, 0, 100);
        gfx.drawString(font, "%", L + FIELD_ALPHA_DECO.X, T + FIELD_ALPHA_DECO.Y, 0xE0E0E0);
        gfx.pose().translate(0, 0, -100);
        gfx.drawCenteredString(font, Component.translatable("gui.done"), L + DONE_TEXT.X, T + DONE_TEXT.Y, 0xFFFFFF);

        // Other tooltips
        renderMiscTooltips(gfx);
    }

    // Render preview slot backgrounds
    private void renderSlotBackgrounds(GuiGraphics gfx)
    {
        // Hotbar slot
        gfx.blit(WIDGETS_TEX, L + PREVIEW_SLOTS[0].X, T + PREVIEW_SLOTS[0].Y,
            PREVIEW_SLOT_HOTBAR.X, PREVIEW_SLOT_HOTBAR.Y, PREVIEW_SLOT_HOTBAR.W, PREVIEW_SLOT_HOTBAR.H,256, 256);

        // Container slots
        for (int i = 1; i <= 3; i++) {
            gfx.blit(INVENTORY_TEX, L + PREVIEW_SLOTS[i].X, T + PREVIEW_SLOTS[i].Y,
                PREVIEW_SLOT_CONTAINER.X, PREVIEW_SLOT_CONTAINER.Y, PREVIEW_SLOT_CONTAINER.W, PREVIEW_SLOT_CONTAINER.H, 256, 256);
        }
    }

    // Render preview items
    private void renderItemPreviews(GuiGraphics gfx)
    {
        for (int i = 0; i < 6; i++) {
            ShadowDrop.forcedContext = ShadowContext.values()[i];
            gfx.renderItem(previewStack, L + PREVIEW_ITEMS[i].X, T + PREVIEW_ITEMS[i].Y);
        }
    }

    // Render checkboxes for toggles
    private void renderCheckboxes(GuiGraphics gfx)
    {
        boolean[] states = {
            hotbarEnabled, hotbarCropped,
            slotEnabled, slotCropped,
            hoverEnabled, hoverCropped,
            cursorEnabled, outsideEnabled, elsewhereEnabled
        };
        for (int i = 0; i < PREVIEW_CHECKBOXES.length; i++) {
            int x = L + PREVIEW_CHECKBOXES[i].X;
            int y = T + PREVIEW_CHECKBOXES[i].Y;

            // Render box state
            if (states[i]) {
                if (CHECKBOX_CONTEXTS[i][0].equals("cropping")) guiTextureBlit(gfx, x, y, PREVIEW_CHECKBOX_CROP);
                else guiTextureBlit(gfx, x, y, PREVIEW_CHECKBOX_SHADOW);
            }
            else guiTextureBlit(gfx, x, y, PREVIEW_CHECKBOX_OFF);

            // Hover highlight
            if (isHovered(PREVIEW_CHECKBOXES[i], PREVIEW_CHECKBOX_HOVER)) {
                guiTextureBlit(gfx, x, y, PREVIEW_CHECKBOX_HOVER);

                String context = CHECKBOX_CONTEXTS[i][1];
                List<Component> tooltip = new ArrayList<>(List.of(Component.translatable("gui.shadowdrop.config.checkbox_" + CHECKBOX_CONTEXTS[i][0]),
                    Component.translatable("gui.shadowdrop.config.context_" + context).withStyle(ChatFormatting.GRAY)));

                if (Arrays.asList(CHECKBOX_CONTEXTS_WITH_EXTRAS).contains(context)) {
                    tooltip.add(Component.translatable("gui.shadowdrop.config.context_" + context + ".extra").withStyle(ChatFormatting.DARK_GRAY));
                }
                renderTooltip(tooltip, gfx);
            }
        }
    }

    // Render done button
    private void renderDoneButton(GuiGraphics gfx)
    {
        if (isHovered(DONE_BTN, DONE_HOVER)) guiTextureBlit(gfx, DONE_BTN, DONE_HOVER);
    }

    // Render reset button
    private void renderResetButton(GuiGraphics gfx)
    {
        if (isHovered(RESET_BTN, RESET_HOVER)) {
            guiTextureBlit(gfx, RESET_BTN, RESET_HOVER);
            renderTooltip("gui.shadowdrop.config.button_reset", gfx);
        }
    }

    // Render list buttons
    private void renderListButtons(GuiGraphics gfx)
    {
        boolean[] isMinus = {
            slotBrColorsList.contains(lastSlotBrColor),
            translucentList.contains(lastValidItemId),
            transparentList.contains(lastValidItemId)
        };
        List<String> listNames = List.of(
            "cropcolor",
            "alpha",
            "alpha"
        );
        for (int i = 0; i < FIELD_LISTBTNS.length; i++) {
            int x = L + FIELD_LISTBTNS[i].X;
            int y = T + FIELD_LISTBTNS[i].Y;
            String buttonAction = "add";

            // Render list button state (minus if item exists)
            if (isMinus[i]) {
                guiTextureBlit(gfx, x, y, FIELD_LISTBTN_MINUS);
                buttonAction = "remove";
            }
            // Hover highlight
            if (isHovered(FIELD_LISTBTNS[i], FIELD_LISTBTN_MINUS)) {
                guiTextureBlit(gfx, x, y, FIELD_LISTBTN_HOVER);
                renderTooltip("gui.shadowdrop.config.button_" + listNames.get(i) + "." + buttonAction, gfx);
            }
        }
    }

    // Render arrow buttons for repositioning shadows
    private void renderArrowButtons(GuiGraphics gfx)
    {
        boolean[] atEdge = {
            shadowOffsetX <= -4,
            shadowOffsetY >= 4,
            shadowOffsetY <= -4,
            shadowOffsetX >= 4,
        };
        for (int i = 0; i < 4; i++) {
            int x = L + FIELD_ARROWS[i].X;
            int y = T + FIELD_ARROWS[i].Y;

            // Render arrow button state (dark if at edge)
            if (atEdge[i]) gfx.fill(x, y, x + FIELD_ARROW_HOVER.W, y + FIELD_ARROW_HOVER.H, 0x88000000);

            // Hover highlight if not at edge
            else if (isHovered(FIELD_ARROWS[i], FIELD_ARROW_HOVER)) {
                guiTextureBlit(gfx, x, y, FIELD_ARROW_HOVER);
                renderTooltip("gui.shadowdrop.config.button_position." + i, gfx);
            }
        }
    }

    // Render warning labels
    private void renderWarnings(GuiGraphics gfx)
    {
        boolean isInGame = Minecraft.getInstance().player != null;

        List<String> loadedWarnMods = new ArrayList<>(List.of());
        for (String warnMod : WARN_MODS) {
            if (ModList.get().isLoaded(warnMod)) loadedWarnMods.add(warnMod);
        }

        boolean[] warningConditions = {
            !loadedWarnMods.isEmpty(),
            !slotBrColorsList.contains(lastSlotBrColor),
            translucentList.toString().contains("#") && !isInGame,
            transparentList.toString().contains("#") && !isInGame,
            shadowAlpha > 0 && shadowAlpha <= 10,
            shadowAlpha == 0,
            shadowOffsetX == 0 && shadowOffsetY == 0
        };

        for (int i = 0; i < 4; i++) { // right warnings
            if (warningConditions[i]) {
                guiTextureBlit(gfx, WARNING_R_LABELS[i], WARNING_R_LABEL);
                if (isHovered(WARNING_R_LABELS[i], WARNING_R_LABEL)) {
                    List<Component> tooltip = new ArrayList<>(List.of(Component.translatable("gui.shadowdrop.config.warning." + i)));

                    if (i == 0) { // unique case for mod incompats
                        tooltip.add(Component.translatable("gui.shadowdrop.config.warning.0.desc").withStyle(ChatFormatting.GRAY));
                        for (String warnMod : loadedWarnMods) tooltip.add(Component.literal(" - " + warnMod).withStyle(ChatFormatting.DARK_GRAY));
                    }
                    renderTooltip(tooltip, gfx);
                }
            }
        }
        for (int i = 0; i < 3; i++) { // left warnings
            if (warningConditions[4 + i]) {
                guiTextureBlit(gfx, WARNING_L_LABELS[i], WARNING_L_LABEL);
                if (isHovered(WARNING_L_LABELS[i], WARNING_L_LABEL)) renderTooltip("gui.shadowdrop.config.warning." + (4 + i), gfx);
            }
        }
    }

    // Render misc tooltips
    private void renderMiscTooltips(GuiGraphics gfx)
    {
        if (isHovered(PREVIEW_EDITBOX)) renderTooltip("gui.shadowdrop.config.editbox_preview", gfx);

        else if (isHovered(FIELD_COLOR)) renderTooltip(List.of(Component.translatable("gui.shadowdrop.config.editbox_color"),
            Component.translatable("gui.shadowdrop.config.editbox_color.desc").withStyle(ChatFormatting.GRAY)), gfx);

        else if (isHovered(FIELD_ALPHA)) renderTooltip(List.of(Component.translatable("gui.shadowdrop.config.editbox_alpha"),
            Component.translatable("gui.shadowdrop.config.editbox_alpha.desc").withStyle(ChatFormatting.GRAY)), gfx);

        else if (isHovered(FIELD_CROPCOLOR)) renderTooltip(List.of(Component.translatable("gui.shadowdrop.config.editbox_cropcolor"),
            Component.translatable("gui.shadowdrop.config.editbox_cropcolor.desc").withStyle(ChatFormatting.GRAY),
            Component.translatable("gui.shadowdrop.config.editbox_list.extra").withStyle(ChatFormatting.DARK_GRAY)), gfx);

        else if (isHovered(FIELD_TRANSLUCENT)) renderTooltip(List.of(Component.translatable("gui.shadowdrop.config.editbox_translucent"),
            Component.translatable("gui.shadowdrop.config.editbox_translucent.desc").withStyle(ChatFormatting.GRAY),
            Component.translatable("gui.shadowdrop.config.editbox_list.extra").withStyle(ChatFormatting.DARK_GRAY)), gfx);

        else if (isHovered(FIELD_TRANSPARENT)) renderTooltip(List.of(Component.translatable("gui.shadowdrop.config.editbox_transparent"),
            Component.translatable("gui.shadowdrop.config.editbox_transparent.desc").withStyle(ChatFormatting.GRAY),
            Component.translatable("gui.shadowdrop.config.editbox_list.extra").withStyle(ChatFormatting.DARK_GRAY)), gfx);
    }

    // Refresh slot pixel color from a currently loaded slot texture
    private void refreshSlotBrPixelColor()
    {
        var res = Minecraft.getInstance().getResourceManager().getResource(INVENTORY_TEX);
        if (res.isEmpty()) return;

        try (NativeImage img = NativeImage.read(res.get().open())) {
            int rgba = img.getPixelRGBA(PREVIEW_SLOT_CONTAINER.X + PREVIEW_SLOT_CONTAINER.W - 1, PREVIEW_SLOT_CONTAINER.Y + PREVIEW_SLOT_CONTAINER.H - 1);
            int r = rgba & 0xFF;
            int g = (rgba >> 8) & 0xFF;
            int b = (rgba >> 16) & 0xFF;
            lastSlotBrColor = String.format("#%02X%02X%02X", r, g, b);

        } catch (IOException ignored) {}
    }

    // Process mouse click
    @Override
    public boolean mouseClicked(double mx, double my, int button)
    {
        MX = (int) mx;
        MY = (int) my;

        // Only handle left clicks
        if (button != 0) return super.mouseClicked(mx, my, button);

        // Checkboxes
        for (int i = 0; i < PREVIEW_CHECKBOXES.length; i++) {
            if (isHovered(PREVIEW_CHECKBOXES[i], PREVIEW_CHECKBOX_OFF)) {
                // Flip checkbox state
                switch (i) {
                    case 0 -> hotbarEnabled    ^= true;
                    case 1 -> hotbarCropped    ^= true;
                    case 2 -> slotEnabled      ^= true;
                    case 3 -> slotCropped      ^= true;
                    case 4 -> hoverEnabled     ^= true;
                    case 5 -> hoverCropped     ^= true;
                    case 6 -> cursorEnabled    ^= true;
                    case 7 -> outsideEnabled   ^= true;
                    case 8 -> elsewhereEnabled ^= true;
                }
                pushToConfig();
                playClick(1.66f);
                return true;
            }
        }
        // Arrow buttons
        for (int i = 0; i < 4; i++) {
            if (isHovered(FIELD_ARROWS[i], FIELD_ARROW_HOVER)) {
                // Nudge shadow
                switch (i) {
                    case 0 -> shadowOffsetX = Math.max(-4, shadowOffsetX - 1);
                    case 1 -> shadowOffsetY = Math.min(4, shadowOffsetY + 1);
                    case 2 -> shadowOffsetY = Math.max(-4, shadowOffsetY - 1);
                    case 3 -> shadowOffsetX = Math.min(4, shadowOffsetX + 1);
                }
                pushToConfig();
                playClick(2.0f);
                return true;
            }
        }
        // List buttons
        for (int i = 0; i < 3; i++) {
            if (isHovered(FIELD_LISTBTNS[i], FIELD_LISTBTN_HOVER)) {
                // Toggle entry
                switch (i) {
                    case 0 -> toggleSlotBrColor();
                    case 1 -> { toggleListEntry(translucentList, lastValidItemId); transparentList.remove(lastValidItemId); }
                    case 2 -> { toggleListEntry(transparentList, lastValidItemId); translucentList.remove(lastValidItemId); }
                }
                // Sync text fields to updated lists
                slotBrColorsField.setValue(String.join(", ", slotBrColorsList));
                translucentField.setValue(String.join(", ", translucentList));
                transparentField.setValue(String.join(", ", transparentList));
                pushToConfig();
                playClick(1.33f);
                return true;
            }
        }
        // Done button
        if (isHovered(DONE_BTN, DONE_HOVER)) {
            this.onClose();
            playClick(1.0f);
            return true;
        }
        // Reset button
        else if (isHovered(RESET_BTN, RESET_HOVER)) {
            resetConfig();
            playClick(1.0f);
            return true;
        }

        return super.mouseClicked(mx, my, button);
    }

    // Toggle currently loaded slot texture color to list
    private void toggleSlotBrColor()
    {
        if (!slotBrColorsList.removeIf(s -> s.equalsIgnoreCase(lastSlotBrColor))) slotBrColorsList.add(lastSlotBrColor);
    }

    // Toggle a list entry
    private void toggleListEntry(List<String> list, String entry)
    {
        if (!list.remove(entry)) list.add(entry);
    }

    // Update previewed item from id
    private void onPreviewItemChanged(String value)
    {
        ResourceLocation loc = ResourceLocation.tryParse(value.trim());
        if (loc == null) return;

        var item = ForgeRegistries.ITEMS.getValue(loc);
        if (item != null && item != Items.AIR) {
            previewStack = new ItemStack(item);
            lastValidItemId = value.trim();
        }
    }

    // Update color from color input
    private void onColorChanged(String value)
    {
        String hex = value.startsWith("#") ? value : "#" + value;
        if (hex.matches("^#[0-9A-Fa-f]{6}$")) {
            shadowColor = hex.toUpperCase();
            pushToConfig();
        }
    }

    // Update alpha from alpha input
    private void onAlphaChanged(String value)
    {
        try {
            int v;
            if (value.isEmpty()) v = 0;
            else v = Integer.parseInt(value.trim());

            if (v < 0 || v > 100) {
                v = Math.min(100, Math.max(0, v));
                alphaField.setValue(String.valueOf(v));
            }
            shadowAlpha = v;
            pushToConfig();

        } catch (NumberFormatException ignored) {}
    }

    // Gui properties
    private int guiLeft() { return (width  - GUI.W) / 2; }
    private int guiTop()  { return (height - GUI.H) / 2; }

    // Parse list from string
    private List<String> parseList(String raw)
    {
        return Arrays.stream(raw.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toCollection(ArrayList::new));
    }

    // Blit texture at x,y using texture x,y,w,h
    private void guiTextureBlit(GuiGraphics gfx, int x, int y, ConfigRect tex)
    {
        gfx.blit(GUI_TEX, x, y, tex.X, tex.Y, tex.W, tex.H, GUI_T_SIZE, GUI_T_SIZE);
    }

    // Blit texture at offset pos x,y using texture x,y,w,h
    private void guiTextureBlit(GuiGraphics gfx, ConfigPos pos, ConfigRect tex)
    {
        gfx.blit(GUI_TEX, L + pos.X, T + pos.Y, tex.X, tex.Y, tex.W, tex.H, GUI_T_SIZE, GUI_T_SIZE);
    }

    // If mouse is hovering over a rect
    private boolean isHovered(ConfigRect rect)
    {
        return MX >= L + rect.X && MX < L + rect.X + rect.W && MY >= T + rect.Y && MY < T + rect.Y + rect.H;
    }

    // If mouse is hovering over a rect defined at x,y with w,h
    private boolean isHovered(ConfigPos xy, ConfigRect wh)
    {
        return MX >= L + xy.X && MX < L + xy.X + wh.W && MY >= T + xy.Y && MY < T + xy.Y + wh.H;
    }

    // Render tooltip from translatable string
    private void renderTooltip(String translatable, GuiGraphics gfx)
    {
        gfx.renderTooltip(font, Component.translatable(translatable), MX, MY);
    }

    // Render tooltip from a list of components together
    private void renderTooltip(List<Component> components, GuiGraphics gfx)
    {
        gfx.renderComponentTooltip(font, components, MX, MY);
    }

    // Play feedback click sound
    private void playClick(float pitch)
    {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch));
    }

    // Loads config from file into cache
    private void loadFromConfig()
    {
        ShadowDropConfig c = ShadowDropConfig.INSTANCE;
        hotbarEnabled    = c.hotbarEnabled.get();
        hotbarCropped    = c.hotbarCropped.get();
        slotEnabled      = c.slotEnabled.get();
        slotCropped      = c.slotCropped.get();
        hoverEnabled     = c.hoverEnabled.get();
        hoverCropped     = c.hoverCropped.get();
        cursorEnabled    = c.cursorEnabled.get();
        elsewhereEnabled = c.elsewhereEnabled.get();
        outsideEnabled   = c.outsideEnabled.get();
        shadowColor      = c.shadowColor.get();
        shadowAlpha      = c.shadowAlpha.get();
        shadowOffsetX    = c.shadowOffsetX.get();
        shadowOffsetY    = c.shadowOffsetY.get();
        slotBrColorsList = new ArrayList<>(c.slotBrColors.get());
        translucentList  = new ArrayList<>(c.translucentItems.get());
        transparentList  = new ArrayList<>(c.transparentItems.get());
    }

    // Pushes config cache to file
    private void pushToConfig()
    {
        ShadowDropConfig c = ShadowDropConfig.INSTANCE;
        c.hotbarEnabled   .set(hotbarEnabled);
        c.hotbarCropped   .set(hotbarCropped);
        c.slotEnabled     .set(slotEnabled);
        c.slotCropped     .set(slotCropped);
        c.hoverEnabled    .set(hoverEnabled);
        c.hoverCropped    .set(hoverCropped);
        c.cursorEnabled   .set(cursorEnabled);
        c.elsewhereEnabled.set(elsewhereEnabled);
        c.outsideEnabled  .set(outsideEnabled);
        c.shadowColor     .set(shadowColor);
        c.shadowAlpha     .set(shadowAlpha);
        c.shadowOffsetX   .set(shadowOffsetX);
        c.shadowOffsetY   .set(shadowOffsetY);
        c.slotBrColors    .set(slotBrColorsList);
        c.translucentItems.set(translucentList);
        c.transparentItems.set(transparentList);
        ShadowDropConfig.SPEC.save();
    }

    // Reset config file and screen to default
    private void resetConfig()
    {
        ShadowDropConfig c = ShadowDropConfig.INSTANCE;
        c.hotbarEnabled   .set(c.hotbarEnabled   .getDefault());
        c.hotbarCropped   .set(c.hotbarCropped   .getDefault());
        c.slotEnabled     .set(c.slotEnabled     .getDefault());
        c.slotCropped     .set(c.slotCropped     .getDefault());
        c.hoverEnabled    .set(c.hoverEnabled    .getDefault());
        c.hoverCropped    .set(c.hoverCropped    .getDefault());
        c.cursorEnabled   .set(c.cursorEnabled   .getDefault());
        c.elsewhereEnabled.set(c.elsewhereEnabled.getDefault());
        c.outsideEnabled  .set(c.outsideEnabled  .getDefault());
        c.shadowColor     .set(c.shadowColor     .getDefault());
        c.shadowAlpha     .set(c.shadowAlpha     .getDefault());
        c.shadowOffsetX   .set(c.shadowOffsetX   .getDefault());
        c.shadowOffsetY   .set(c.shadowOffsetY   .getDefault());
        c.slotBrColors    .set(c.slotBrColors    .getDefault());
        c.translucentItems.set(c.translucentItems.getDefault());
        c.transparentItems.set(c.transparentItems.getDefault());
        ShadowDropConfig.SPEC.save();
        loadFromConfig();

        clearWidgets();
        init();
    }
}