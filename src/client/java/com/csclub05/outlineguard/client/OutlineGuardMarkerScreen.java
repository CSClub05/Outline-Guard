package com.csclub05.outlineguard.client;

import com.csclub05.outlineguard.OutlineGuard;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class OutlineGuardMarkerScreen extends Screen {
    private TextFieldWidget blockIdField;
    private Text status = Text.literal("Enter a block ID like minecraft:red_concrete. Client-side only.");
    private List<Identifier> displayedMarkers = new ArrayList<>();

    public OutlineGuardMarkerScreen() {
        super(Text.literal("Outline Guard Markers"));
    }

    @Override
    protected void init() {
        refreshDisplayedMarkers();

        int centerX = this.width / 2;
        int y = 54;

        this.blockIdField = new TextFieldWidget(this.textRenderer, centerX - 120, y, 240, 20, Text.literal("Block ID"));
        this.blockIdField.setMaxLength(128);
        this.blockIdField.setText(this.displayedMarkers.isEmpty() ? "minecraft:netherrack" : this.displayedMarkers.getFirst().toString());
        this.addDrawableChild(this.blockIdField);
        this.setInitialFocus(this.blockIdField);

        y += 32;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Set as only marker"), button -> useTypedBlock(MarkerAction.SET))
                .dimensions(centerX - 120, y, 115, 20)
                .build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Add marker"), button -> useTypedBlock(MarkerAction.ADD))
                .dimensions(centerX + 5, y, 115, 20)
                .build());

        y += 24;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Remove marker"), button -> useTypedBlock(MarkerAction.REMOVE))
                .dimensions(centerX - 120, y, 115, 20)
                .build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reload config"), button -> {
                    OutlineGuard.CONFIG.load();
                    refreshDisplayedMarkers();
                    this.status = Text.literal("Reloaded local settings from outlineguard-client.json.");
                })
                .dimensions(centerX + 5, y, 115, 20)
                .build());

        y += 30;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Close"), button -> this.close())
                .dimensions(centerX - 50, y, 100, 20)
                .build());
    }

    private void useTypedBlock(MarkerAction action) {
        Identifier id = Identifier.tryParse(this.blockIdField.getText().trim());
        if (id == null) {
            this.status = Text.literal("Invalid block id.");
            return;
        }

        if (!Registries.BLOCK.containsId(id)) {
            this.status = Text.literal("Unknown block: " + id);
            return;
        }

        apply(action, id);
    }

    private void apply(MarkerAction action, Identifier id) {
        boolean changed = switch (action) {
            case SET -> OutlineGuard.CONFIG.setOnlyMarker(id);
            case ADD -> OutlineGuard.CONFIG.addMarker(id);
            case REMOVE -> OutlineGuard.CONFIG.removeMarker(id);
        };

        // Keep the GUI's displayed marker list in sync immediately after each button press.
        // This avoids requiring the player to close/reopen the screen to see Add/Remove changes.
        refreshDisplayedMarkers();

        this.status = switch (action) {
            case SET -> Text.literal("Set local marker to " + id + ".");
            case ADD -> changed ? Text.literal("Added local marker " + id + ".") : Text.literal(id + " is already a local marker.");
            case REMOVE -> changed ? Text.literal("Removed local marker " + id + ".") : Text.literal(id + " was not in your local marker list.");
        };
    }

    private void refreshDisplayedMarkers() {
        this.displayedMarkers = OutlineGuardClient.markerBlocks();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xC0101010);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Block ID"), this.width / 2 - 120, 42, 0xA0A0A0);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("These settings only affect your own client."), this.width / 2, this.height - 82, 0xA0FFA0);
        context.drawCenteredTextWithShadow(this.textRenderer, this.status, this.width / 2, this.height - 64, 0xFFFF55);

        drawMarkerList(context);
    }

    private void drawMarkerList(DrawContext context) {
        String markerText = this.displayedMarkers.isEmpty()
                ? "Current local markers: none"
                : "Current local markers: " + this.displayedMarkers.stream().map(Identifier::toString).collect(Collectors.joining(", "));

        int maxWidth = this.width - 40;
        List<String> lines = wrap(markerText, maxWidth);
        int startY = this.height - 44;
        for (int i = 0; i < lines.size() && i < 3; i++) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(lines.get(i)), this.width / 2, startY + (i * 12), 0xFFFFFF);
        }
    }

    private List<String> wrap(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String part : text.split(", ")) {
            String candidate = current.isEmpty() ? part : current + ", " + part;
            if (this.textRenderer.getWidth(candidate) > maxWidth && !current.isEmpty()) {
                lines.add(current.toString());
                current = new StringBuilder(part);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private enum MarkerAction {
        SET,
        ADD,
        REMOVE
    }
}
