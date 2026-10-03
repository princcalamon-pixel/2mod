
package com.suveren.feudal.client;

import com.suveren.feudal.FeudalMod;
import com.suveren.feudal.entity.FeudalKnightEntity;
import com.suveren.feudal.net.KnightInfo;
import com.suveren.feudal.net.SetStancePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Vojna miza: seznam vitezov + ukazi cetam. */
public class WarTableScreen extends Screen {
    public static List<KnightInfo> CACHED = new ArrayList<>();
    private List<KnightInfo> lastShown = null;
    private static final int PANEL = 360;

    public WarTableScreen() {
        super(Component.literal("Vojna miza"));
    }

    @Override
    protected void init() {
        rebuildRows();
    }

    private void rebuildRows() {
        this.clearWidgets();
        lastShown = CACHED;
        int cx = (this.width - PANEL) / 2;
        int y = 44;
        for (KnightInfo info : CACHED) {
            final KnightInfo inf = info;
            int bx = cx + PANEL - 4 * 58 - 8;
            this.addRenderableWidget(Button.builder(Component.literal("Sledi"),
                    b -> send(inf, "FOLLOW")).bounds(bx, y, 54, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Straza"),
                    b -> send(inf, "GUARD")).bounds(bx + 58, y, 54, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Ostani"),
                    b -> send(inf, "HOLD")).bounds(bx + 116, y, 54, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Patrolja"),
                    b -> send(inf, "PATROL")).bounds(bx + 174, y, 54, 20).build());
            y += 26;
        }
    }

    private void send(KnightInfo info, String stance) {
        FeudalMod.CHANNEL.sendToServer(new SetStancePacket(info.id(), stance));
    }

    private static String stanceLabel(String s) {
        try {
            return FeudalKnightEntity.stanceLabel(FeudalKnightEntity.Stance.valueOf(s));
        } catch (IllegalArgumentException ex) {
            return s;
        }
    }

    @Override
    public void tick() {
        if (CACHED != lastShown) rebuildRows();
        super.tick();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        this.renderBackground(g);
        g.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFD700);
        int cx = (this.width - PANEL) / 2;
        int y = 50;
        if (CACHED.isEmpty()) {
            g.drawCenteredString(this.font, Component.literal("Se ni vitezov. Desni klik na vascana z zezlom."),
                    this.width / 2, y + 20, 0xAAAAAA);
        }
        for (KnightInfo info : CACHED) {
            String line = info.name() + "  —  " + stanceLabel(info.stance())
                    + "  (" + (int) info.distance() + " m)";
            g.drawString(this.font, line, cx + 4, y, 0xFFFFFF);
            y += 26;
        }
        super.render(g, mouseX, mouseY, partial);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
