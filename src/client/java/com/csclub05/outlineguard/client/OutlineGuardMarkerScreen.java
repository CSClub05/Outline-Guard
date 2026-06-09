package com.csclub05.outlineguard.client;

import com.csclub05.outlineguard.OutlineGuard;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Collectors;

public final class OutlineGuardMarkerScreen extends Screen {
    private TextFieldWidget blockIdField;
    private Text status = Text.literal("Enter a block ID like minecraft:red_concrete. Client-side only.");

    public OutlineGuardMarkerScreen() {
        super(Text.literal("Outline Guard Markers"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 54;

        this.blockIdField = new TextFieldWidget(this.textRenderer, centerX - 120, y, 240, 20, Text.literal("Block ID"));
        this.blockIdField.setMaxLength(128);
        List<Identifier> markers = OutlineGuardClient.markerBlocks();
        this.blockIdField.setText(markers.isEmpty() ? "minecraft:netherrack" : markers.getFirst().toString());
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
                    this.status = Text.literal("Reloaded settings from outlineguard-client.json.");
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

        this.status = switch (action) {
            case SET -> Text.literal("Set local marker to " + id + ".");
            case ADD -> changed ? Text.literal("Added local marker " + id + ".") : Text.literal(id + " is already a local marker.");
            case REMOVE -> changed ? Text.literal("Removed local marker " + id + ".") : Text.literal(id + " was not in your local marker list.");
        };
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Block ID"), this.width / 2 - 120, 42, 0xA0A0A0);
        super.render(context, mouseX, mouseY, delta);

        List<Identifier> markers = OutlineGuardClient.markerBlocks();
        String markerText = markers.isEmpty()
                ? "Current local markers: none"
                : "Current local markers: " + markers.stream().map(Identifier::toString).collect(Collectors.joining(", "));

        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("These settings only affect your own client."), this.width / 2, this.height - 66, 0xA0FFA0);
        context.drawCenteredTextWithShadow(this.textRenderer, this.status, this.width / 2, this.height - 48, 0xFFFF55);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(markerText), this.width / 2, this.height - 30, 0xFFFFFF);
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
