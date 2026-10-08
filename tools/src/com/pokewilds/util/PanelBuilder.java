package com.pokewilds.util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * <p>
 * A class that builds JPanels.
 * </p>
 */
public class PanelBuilder {

    private final JPanel panel;

    public static PanelBuilder create() {
        return new PanelBuilder();
    }

    private PanelBuilder() {
        this.panel = new JPanel();
    }

    public PanelBuilder withLayout(LayoutManager layout) {
        this.panel.setLayout(layout);
        return this;
    }

    public PanelBuilder withBoxLayout(int axis) {
        this.panel.setLayout(new BoxLayout(this.panel, axis));
        return this;
    }

    public PanelBuilder withBorderLayout(int padding) {
        this.panel.setLayout(new BorderLayout(padding, padding));
        return this;
    }

    public PanelBuilder withBorder(Border border) {
        this.panel.setBorder(border);
        return this;
    }

    public PanelBuilder withDefaultBorder() {
        this.panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return this;
    }

    public PanelBuilder add(Component component) {
        this.panel.add(component);
        return this;
    }

    public PanelBuilder add(Component component, Object constraints) {
        this.panel.add(component, constraints);
        return this;
    }

    public JPanel build() {
        return this.panel;
    }

}
