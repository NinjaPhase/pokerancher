package com.pokewilds;

import com.pokewilds.util.PanelBuilder;
import com.pokewilds.world.WorldSize;
import com.pokewilds.world.WorldUtils;
import com.pokewilds.world.biome.BiomeRegistry;
import com.pokewilds.world.gen.WorldGenerator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WorldGenerationGUI extends JFrame implements DocumentListener {
    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final JTextField seed;
    private final ImageView temperatureView, heightView, biomeView;
    private WorldGenerator worldGenerator;

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        WorldGenerationGUI gui = new WorldGenerationGUI();
        gui.setVisible(true);
    }

    public WorldGenerationGUI() {
        super("World Generation Test");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(640, 480);
        this.setLocationRelativeTo(null);

        BiomeRegistry.loadDefaults();

        this.seed = new JTextField();
        this.worldGenerator = new WorldGenerator(this.seed.getText().hashCode(), WorldSize.TINY);

        this.temperatureView = new ImageView(WorldUtils.generateTemperatureImage(this.worldGenerator));
        this.heightView = new ImageView(WorldUtils.generateHeightView(this.worldGenerator));
        this.biomeView = new ImageView(WorldUtils.generateBiomeView(this.worldGenerator));
        JPanel root = PanelBuilder.create()
                                  .withBorderLayout(8)
                                  .withDefaultBorder()
                                  .add(PanelBuilder.create()
                                                   .withBorderLayout(8)
                                                   .add(new JLabel("Seed:"), BorderLayout.WEST)
                                                   .add(this.seed, BorderLayout.CENTER)
                                                   .build(), BorderLayout.NORTH)
                                  .add(PanelBuilder.create()
                                                   .withBoxLayout(BoxLayout.Y_AXIS)
                                                   .add(new JLabel("Temperature:"))
                                                   .add(PanelBuilder.create().add(this.temperatureView).build())
                                                   .add(new JLabel("Height:"))
                                                   .add(PanelBuilder.create().add(this.heightView).build())
                                                   .add(new JLabel("Biome:"))
                                                   .add(PanelBuilder.create().add(this.biomeView).build())
                                                   .build(), BorderLayout.WEST)
                                  .build();

        this.seed.getDocument().addDocumentListener(this);

        this.getContentPane().add(root, BorderLayout.CENTER);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        if (e == null) {
            return;
        }
        if (e.getDocument() == this.seed.getDocument()) {
            this.updateSeed();
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        if (e == null) {
            return;
        }
        if (e.getDocument() == this.seed.getDocument()) {
            this.updateSeed();
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        if (e == null) {
            return;
        }
        if (e.getDocument() == this.seed.getDocument()) {
            this.updateSeed();
        }
    }

    private void updateSeed() {
        EXECUTOR.execute(() -> {
            this.worldGenerator = new WorldGenerator(this.seed.getText().hashCode(), WorldSize.TINY);
            EXECUTOR.execute(() -> {
                this.temperatureView.setImage(WorldUtils.generateTemperatureImage(this.worldGenerator));
            });
            EXECUTOR.execute(() -> {
                this.heightView.setImage(WorldUtils.generateHeightView(this.worldGenerator));
            });
            EXECUTOR.execute(() -> {
                this.biomeView.setImage(WorldUtils.generateBiomeView(this.worldGenerator));
            });
        });
    }

    public static class ImageView extends JPanel {
        private Image image;

        public ImageView(Image image) {
            this.setBorder(BorderFactory.createLoweredBevelBorder());
            this.image = image;
            this.setPreferredSize(new Dimension(256, 256));
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        }

        public void setImage(Image image) {
            this.image = image;
            this.repaint();
        }
    }

}
