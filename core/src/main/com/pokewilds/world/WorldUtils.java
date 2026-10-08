package com.pokewilds.world;

import com.badlogic.gdx.files.FileHandle;
import com.pokewilds.Constants;
import com.pokewilds.world.biome.Biome;
import com.pokewilds.world.chunk.MapChunk;
import com.pokewilds.world.gen.WorldGenerator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class WorldUtils {

    public static Image generateTemperatureImage(WorldGenerator generator) {
        WorldSize size = generator.getWorldSize();
        BufferedImage tempImg = new BufferedImage(size.width * Constants.CHUNK_SIZE, size.height * Constants.CHUNK_SIZE,
                                                  BufferedImage.TYPE_INT_ARGB);

        for (int cY = 0; cY < size.height; cY++) {
            for (int cX = 0; cX < size.width; cX++) {
                MapChunk c = new MapChunk(generator, cX, cY);

                for (int tY = 0; tY < Constants.CHUNK_SIZE; tY++) {
                    for (int tX = 0; tX < Constants.CHUNK_SIZE; tX++) {
                        double temp = generator.getTemperature((cX * Constants.CHUNK_SIZE) + tX,
                                                               (cY * Constants.CHUNK_SIZE) + tY);

                        tempImg.setRGB((cX * Constants.CHUNK_SIZE) + tX, (cY * Constants.CHUNK_SIZE) + tY,
                                       0xff000000 | (int) (0xff * temp) << 16 | (int) (0xff * temp) << 8 |
                                       (int) (0xff * temp));
                    }
                }
            }
        }
        return tempImg;
    }

    public static Image generateHeightView(WorldGenerator generator) {
        WorldSize size = generator.getWorldSize();
        BufferedImage heightImg = new BufferedImage(size.width * Constants.CHUNK_SIZE,
                                                    size.height * Constants.CHUNK_SIZE, BufferedImage.TYPE_INT_ARGB);

        for (int cY = 0; cY < size.height; cY++) {
            for (int cX = 0; cX < size.width; cX++) {
                MapChunk c = new MapChunk(generator, cX, cY);

                for (int tY = 0; tY < Constants.CHUNK_SIZE; tY++) {
                    for (int tX = 0; tX < Constants.CHUNK_SIZE; tX++) {
                        double h = generator.getHeight((cX * Constants.CHUNK_SIZE) + tX,
                                                       (cY * Constants.CHUNK_SIZE) + tY);

                        heightImg.setRGB((cX * Constants.CHUNK_SIZE) + tX, (cY * Constants.CHUNK_SIZE) + tY,
                                         0xff000000 | (int) (0xff * h) << 16 | (int) (0xff * h) << 8 |
                                         (int) (0xff * h));
                    }
                }
            }
        }
        return heightImg;
    }

    public static Image generateBiomeView(WorldGenerator generator) {
        WorldSize size = generator.getWorldSize();
        BufferedImage biomeImg = new BufferedImage(size.width * Constants.CHUNK_SIZE,
                                                   size.height * Constants.CHUNK_SIZE, BufferedImage.TYPE_INT_ARGB);

        for (int cY = 0; cY < size.height; cY++) {
            for (int cX = 0; cX < size.width; cX++) {
                MapChunk c = new MapChunk(generator, cX, cY);

                for (int tY = 0; tY < Constants.CHUNK_SIZE; tY++) {
                    for (int tX = 0; tX < Constants.CHUNK_SIZE; tX++) {
                        Biome b = generator.getBiomeNew((cX * Constants.CHUNK_SIZE) + tX,
                                                        (cY * Constants.CHUNK_SIZE) + tY);

                        biomeImg.setRGB((cX * Constants.CHUNK_SIZE) + tX, (cY * Constants.CHUNK_SIZE) + tY,
                                        0xff000000 | b.getTestColor().getRGB());
                    }
                }
            }
        }
        return biomeImg;
    }

    public static void saveImage(WorldGenerator generator, FileHandle f) {
        WorldSize size = generator.getWorldSize();
        f.mkdirs();
        try {
            BufferedImage biomeImg = new BufferedImage(size.width * Constants.CHUNK_SIZE,
                                                       size.height * Constants.CHUNK_SIZE, BufferedImage.TYPE_INT_ARGB);
            BufferedImage heightImg = new BufferedImage(size.width * Constants.CHUNK_SIZE,
                                                        size.height * Constants.CHUNK_SIZE,
                                                        BufferedImage.TYPE_INT_ARGB);
            BufferedImage tempImg = new BufferedImage(size.width * Constants.CHUNK_SIZE,
                                                      size.height * Constants.CHUNK_SIZE, BufferedImage.TYPE_INT_ARGB);

            for (int cY = 0; cY < size.height; cY++) {
                for (int cX = 0; cX < size.width; cX++) {
                    MapChunk c = new MapChunk(generator, cX, cY);

                    for (int tY = 0; tY < Constants.CHUNK_SIZE; tY++) {
                        for (int tX = 0; tX < Constants.CHUNK_SIZE; tX++) {

                            Biome b = generator.getBiomeNew((cX * Constants.CHUNK_SIZE) + tX,
                                                            (cY * Constants.CHUNK_SIZE) + tY);
                            double temp = generator.getTemperature((cX * Constants.CHUNK_SIZE) + tX,
                                                                   (cY * Constants.CHUNK_SIZE) + tY);
                            double h = generator.getHeight((cX * Constants.CHUNK_SIZE) + tX,
                                                           (cY * Constants.CHUNK_SIZE) + tY);

                            tempImg.setRGB((cX * Constants.CHUNK_SIZE) + tX, (cY * Constants.CHUNK_SIZE) + tY,
                                           0xff000000 | (int) (0xff * temp) << 16 | (int) (0xff * temp) << 8 |
                                           (int) (0xff * temp));

                            heightImg.setRGB((cX * Constants.CHUNK_SIZE) + tX, (cY * Constants.CHUNK_SIZE) + tY,
                                             0xff000000 | (int) (0xff * h) << 16 | (int) (0xff * h) << 8 |
                                             (int) (0xff * h));

                            biomeImg.setRGB((cX * Constants.CHUNK_SIZE) + tX, (cY * Constants.CHUNK_SIZE) + tY,
                                            0xff000000 | b.getTestColor().getRGB());

                        }
                    }
                }
            }

            ImageIO.write(biomeImg, "PNG", f.child("biome.png").file());
            ImageIO.write(heightImg, "PNG", f.child("height.png").file());
            ImageIO.write(tempImg, "PNG", f.child("temperature.png").file());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private WorldUtils() {

    }
}

