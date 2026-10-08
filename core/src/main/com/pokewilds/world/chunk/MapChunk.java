package com.pokewilds.world.chunk;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.pokewilds.Constants;
import com.pokewilds.scene.GameScene;
import com.pokewilds.world.biome.BiomeType;
import com.pokewilds.world.gen.WorldGenerator;
import com.pokewilds.world.tile.Tile;
import com.pokewilds.world.tile.TileEffect;
import com.pokewilds.world.tile.TileEvent;
import com.pokewilds.world.tile.TileRegistry;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * <p>
 * A {@code MapChunk} represents a small portion of the overall game world, used to keep the game running
 * efficiently by only sending map chunks to a client.
 * </p>
 */
public class MapChunk {
    public static final int CURRENT_VERSION = 1;
    public static final int CHUNK_DATA_SIZE = 9 + (3 * Tile.LAYER_COUNT * Constants.CHUNK_SIZE * Constants.CHUNK_SIZE);

    /**
     * A coordinate of the {@code MapChunk}.
     */
    private final int chunkX, chunkY;
    private final int[][] tiles, states, entities;
    private final TileEffect[][] tileEffects;
    private final WorldGenerator worldGenerator;
    private int usedLayers;

    /**
     * <p>
     * Gets the key of the map chunk which can span an integer.
     * </p>
     *
     * @param chunkX The x of the map chunk.
     * @param chunkY The y of the map chunk.
     * @return An 8-byte representation of the map chunk.
     */
    public static Integer getKey(int chunkX, int chunkY) {
        return (chunkX & 0xFFFF) << 16 | (chunkY & 0xFFFF);
    }

    /**
     * <p>
     * Constructs a {@code MapChunk} with empty tiles.
     * </p>
     *
     * @param worldGenerator The world generator for the map chunk.
     * @param chunkX         The chunks x coordinate.
     * @param chunkY         The chunks y coordinate.
     * @throws IllegalArgumentException Thrown if chunkX or chunkY are less than 0 or greater than 65535.
     */
    public MapChunk(final WorldGenerator worldGenerator, final int chunkX, final int chunkY) {
        if (chunkX < 0 || chunkX > 0xFFFF || chunkY < 0 || chunkY > 0xFFFF) {
            throw new IllegalArgumentException("Invalid location for MapChunk(" + chunkX + ", " + chunkY + ")");
        }
        this.worldGenerator = worldGenerator;
        this.entities = new int[Constants.CHUNK_SIZE * Constants.CHUNK_SIZE][10];
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.tiles = new int[Tile.LAYER_COUNT][Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];
        for (int[] tile : this.tiles) {
            Arrays.fill(tile, -1);
        }
        this.states = new int[Tile.LAYER_COUNT][Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];
        this.tileEffects = new TileEffect[Tile.LAYER_COUNT][Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];
    }

    /**
     * <p>
     * Constructs a new {@code MapChunk} with specific tiles.
     * </p>
     *
     * @param worldGenerator The world generator.
     * @param chunkX         The chunk x.
     * @param chunkY         The chunk y.
     * @param tiles          The tiles to copy into the tile buffer.
     * @param states         The states to copy into the state buffer.
     */
    public MapChunk(final WorldGenerator worldGenerator, final int chunkX, final int chunkY, final int[][] tiles, final int[][] states) {
        this(worldGenerator, chunkX, chunkY);
        for (int i = 0; i < Math.min(tiles.length, this.tiles.length); i++) {
            System.arraycopy(tiles[i], 0, this.tiles[i], 0, Math.min(tiles[i].length, this.tiles[i].length));
            System.arraycopy(states[i], 0, this.states[i], 0, Math.min(states[i].length, this.states[i].length));
        }
    }

    /**
     * <p>
     * Constructs a new {@code MapChunk} using tiles for a buffer.
     * </p>
     *
     * @param worldGenerator The world generator.
     * @param chunkX         The chunk x.
     * @param chunkY         The chunk y.
     * @param byteBuffer     The buffer to load tiles data from.
     */
    public MapChunk(final WorldGenerator worldGenerator, final int chunkX, final int chunkY, final ByteBuffer byteBuffer) {
        this(worldGenerator, chunkX, chunkY);
        for (int l = 0; l < Tile.LAYER_COUNT; l++) {
            for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
                for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
                    this.tiles[l][x + (y * Constants.CHUNK_SIZE)] = byteBuffer.get();
                }
            }
        }
    }

    /**
     * <p>
     * Constructs a new {@code MapChunk} using a file.
     * </p>
     *
     * @param worldGenerator The world generator.
     * @param chunkX         The chunk x.
     * @param chunkY         The chunk y.
     * @param f              The file to load the chunk from.
     */
    public MapChunk(final WorldGenerator worldGenerator, final int chunkX, final int chunkY, RandomAccessFile randomAccessFile) {
        this(worldGenerator, chunkX, chunkY);
        ByteBuffer buffer = ByteBuffer.allocate(CHUNK_DATA_SIZE);

        int relX = chunkX % Constants.SAVE_SIZE;
        int relY = chunkY % Constants.SAVE_SIZE;
        int chunkPos = (relX + relY * Constants.SAVE_SIZE) * CHUNK_DATA_SIZE;
        try {
            randomAccessFile.seek(chunkPos);
            randomAccessFile.readFully(buffer.array(), 0, CHUNK_DATA_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int version = buffer.get();
        if (version != CURRENT_VERSION) {
            throw new IllegalArgumentException("Invalid version(" + version + ") for MapChunk(" + chunkX + ", " + chunkY + ")");
        }
        int expectedX = buffer.getInt();
        int expectedY = buffer.getInt();
        if (expectedX != chunkX || expectedY != chunkY) {
            throw new IllegalArgumentException("Invalid location(" + expectedX + "," + expectedY + ") for MapChunk(" + chunkX + ", " + chunkY + ")");
        }
        for (int l = 0; l < Tile.LAYER_COUNT; l++) {
            for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
                for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
                    this.tiles[l][x + (y * Constants.CHUNK_SIZE)] = buffer.getShort();
                    this.states[l][x + (y * Constants.CHUNK_SIZE)] = buffer.get();
                }
            }
        }

        this.calculateUsedLayers();
    }

    /**
     * <p>
     * Begins world generation for this {@code MapChunk} using the underlying {@link WorldGenerator}.
     * </p>
     */
    public void generate() {
        int baseX = this.chunkX * Constants.CHUNK_SIZE;
        int baseY = this.chunkY * Constants.CHUNK_SIZE;
        for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
            for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
                BiomeType biome = this.worldGenerator.getBiome(baseX + x, baseY + y);
                this.generateTerrain(biome, baseX, baseY, x, y);
            }
        }
        for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
            for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
                BiomeType biome = this.worldGenerator.getBiome(baseX + x, baseY + y);
                this.generateTrees(biome, baseX, baseY, x, y);
            }
        }
        for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
            for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
                BiomeType biome = this.worldGenerator.getBiome(baseX + x, baseY + y);
                this.generateSpecial(biome, baseX, baseY, x, y);
            }
        }

        this.usedLayers = 0;
        for (int layer = 0; layer < Tile.LAYER_COUNT; layer++) {
            for (int i = 0; i < this.tiles[layer].length; i++) {
                if (this.tiles[layer][i] == -1) {
                    continue;
                }
                this.usedLayers |= 1 << layer;
                break;
            }
        }
    }

    /**
     * <p>
     * Calculates the used layers for this {@code MapChunk}.
     * </p>
     * <small>Used to only transmit the necessary layers to the client.</small>
     */
    private void calculateUsedLayers() {
        this.usedLayers = 0;
        for (int layer = 0; layer < Tile.LAYER_COUNT; layer++) {
            for (int i = 0; i < this.tiles[layer].length; i++) {
                if (this.tiles[layer][i] == -1) {
                    continue;
                }
                this.usedLayers |= 1 << layer;
                break;
            }
        }
    }

    /**
     * <p>
     * Generates the terrain for the {@code MapChunk}.
     * </p>
     * <small>
     * TODO: Modify this to use a WorldGeneration pipeline.
     * </small>
     *
     * @param biome The biome.
     * @param baseX The first x tile of the map chunk.
     * @param baseY The first y tile of the map chunk.
     * @param x     The x of the tile.
     * @param y     The y of the tile.
     */
    private void generateTerrain(BiomeType biome, int baseX, int baseY, int x, int y) {
        if (biome == BiomeType.FOREST) {
            double t = this.worldGenerator.get(baseX + x, baseY + y, 20000.0, 0.5, 0.2, 8, 0.4, 0.2);
            if (t > 0.45) {
                this.tiles[Tile.LAYER_GROUND][x + (y * Constants.CHUNK_SIZE)] = Tile.TILE_TALL_GRASS;
            } else {
                this.tiles[Tile.LAYER_GROUND][x + (y * Constants.CHUNK_SIZE)] = Tile.TILE_GRASS;
            }
        } else if (biome == BiomeType.SNOW) {
            this.tiles[Tile.LAYER_GROUND][x + (y * Constants.CHUNK_SIZE)] = Tile.TILE_SNOW;
        } else if (biome == BiomeType.DESERT) {
            this.tiles[Tile.LAYER_GROUND][x + (y * Constants.CHUNK_SIZE)] = Tile.TILE_SAND;
        } else if (biome == BiomeType.BEACH) {
            this.tiles[Tile.LAYER_GROUND][x + (y * Constants.CHUNK_SIZE)] = Tile.TILE_SAND;
        } else if (biome == BiomeType.OCEAN) {
            this.tiles[Tile.LAYER_GROUND][x + (y * Constants.CHUNK_SIZE)] = Tile.TILE_WATER;
        }
    }

    /**
     * <p>
     * Generates tree for the {@code MapChunk}, as trees are multi-tile
     * structures they must check all tiles are valid before placing.
     * </p>
     * <small>
     * TODO: Modify this to use a WorldGeneration pipeline.
     * </small>
     *
     * @param biome The biome.
     * @param baseX The first x tile of the map chunk.
     * @param baseY The first y tile of the map chunk.
     * @param x     The x of the tile.
     * @param y     The y of the tile.
     */
    private void generateTrees(BiomeType biome, int baseX, int baseY, int x, int y) {
        int tx = baseX + x;
        int ty = baseY + y;
        int treeCheckX = tx;
        treeCheckX -= treeCheckX % 2;
        int treeCheckY = ty;
        treeCheckY -= treeCheckY % 2;
        if (getBiomeAt(treeCheckX, treeCheckY) == BiomeType.FOREST) {
            double tree_bl = this.worldGenerator.get(treeCheckX, treeCheckY, 30000.0, 0.5, 0.2, 8, 0.4, 0.2);
            if (tree_bl > 0.5) {
                if (tx % 2 == 0) {
                    if (ty % 2 == 1) {
                        this.tiles[Tile.LAYER_GROUND][x + (y * Constants.CHUNK_SIZE)] = Tile.TILE_TREE_BOTTOM_LEFT;
                    } else if (ty % 2 == 0) {
                        this.tiles[Tile.LAYER_UOBJ][x + (y * Constants.CHUNK_SIZE)] = Tile.TILE_TREE_MID_LEFT;
                    }
                } else {
                    if (ty % 2 == 1) {
                        this.tiles[Tile.LAYER_GROUND][x + (y * Constants.CHUNK_SIZE)] = Tile.TILE_TREE_BOTTOM_RIGHT;
                    } else if (ty % 2 == 0) {
                        this.tiles[Tile.LAYER_UOBJ][x + (y * Constants.CHUNK_SIZE)] = Tile.TILE_TREE_MID_RIGHT;
                    }
                }
            }
        }

        treeCheckY += 2;
        if (getBiomeAt(treeCheckX, treeCheckY) == BiomeType.FOREST) {
            double tree_bl = this.worldGenerator.get(treeCheckX, treeCheckY, 30000.0, 0.5, 0.2, 8, 0.4, 0.2);
            if (tree_bl > 0.5) {
                if (ty % 2 == 1) {
                    if (tx % 2 == 0) {
                        this.tiles[Tile.LAYER_OOBJ][x + (y * Constants.CHUNK_SIZE)] = Tile.TILE_TREE_TOP_LEFT;
                    } else {
                        this.tiles[Tile.LAYER_OOBJ][x + (y * Constants.CHUNK_SIZE)] = Tile.TILE_TREE_TOP_RIGHT;
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Generate special structures for the {@code MapChunk}, currently there are none.
     * </p>
     * <small>
     * TODO: Modify this to use a WorldGeneration pipeline.
     * </small>
     *
     * @param biomeType The biome
     * @param baseX     The base x.
     * @param baseY     The base y.
     * @param localX    The local x.
     * @param localY    The local y.
     */
    private void generateSpecial(BiomeType biomeType, int baseX, int baseY, int localX, int localY) {

    }

    /**
     * <p>
     * Renders this chunk to the screen.
     * </p>
     *
     * @param batch          The batch to draw on.
     * @param tilesetTexture The tileset texture.
     * @param frame          The frame.
     * @param layer          The layer.
     * @param x              The absolute x position.
     * @param y              The absolute y position.
     */
    public void draw(SpriteBatch batch, Texture tilesetTexture, int frame, int layer, float x, float y) {
        for (int tY = 0; tY < Constants.CHUNK_SIZE; tY++) {
            for (int tX = 0; tX < Constants.CHUNK_SIZE; tX++) {
                int tileId = this.tiles[layer][tX + (tY * Constants.CHUNK_SIZE)];
                if (tileId == -1) {
                    continue;
                }
                final Tile t = TileRegistry.get(tileId);
                final int tileIndex = t.getTileIndex(frame, this.states[layer][tX + (tY * Constants.CHUNK_SIZE)]);
                batch.draw(tilesetTexture, (float) Math.floor(x + tX * 16f), (float) Math.floor(-((tY * 16f) + y)), 0f, 0f, 16f, 16f, 1f, 1f, 0.0f, (tileIndex % 128) * 16, (tileIndex / 128) * 16, 16, 16, false, false);
            }
        }
    }

    /**
     * <p>
     * Triggers a tile update event for every single tile, this should apply
     * the states to each tile.
     * </p>
     *
     * @param gameScene The game scene.
     */
    public void reloadStates(GameScene gameScene) {
        int baseX = this.getBaseX();
        int baseY = this.getBaseY();
        for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
            for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
                gameScene.triggerTileEvent(TileEvent.TILE_UPDATED, baseX + x, baseY + y, null);
            }
        }
    }

    private void putTile(int x, int y, int tileId) {
        if (x < 0 || x >= Constants.CHUNK_SIZE || y < 0 || y >= Constants.CHUNK_SIZE) {
            throw new IllegalArgumentException("invalid tile location (" + x + ", " + y + ") for tile " + tileId);
        }
        this.tiles[TileRegistry.get(tileId).getLayer()][x + (y * Constants.CHUNK_SIZE)] = tileId;
    }

    public int getTileAt(int layer, int x, int y) {
        if (x < 0 || x >= Constants.CHUNK_SIZE || y < 0 || y >= Constants.CHUNK_SIZE) {
            return -1;
        }
        return this.tiles[layer][x + (y * Constants.CHUNK_SIZE)];
    }

    public void setTileAt(int layer, int x, int y, int tile, int state) {
        if (x < 0 || x >= Constants.CHUNK_SIZE || y < 0 || y >= Constants.CHUNK_SIZE) {
            return;
        }
        this.tiles[layer][x + (y * Constants.CHUNK_SIZE)] = tile;
        this.states[layer][x + (y * Constants.CHUNK_SIZE)] = state;
    }

    public void setTileStateAt(int layer, int x, int y, int state) {
        if (x < 0 || x >= Constants.CHUNK_SIZE || y < 0 || y >= Constants.CHUNK_SIZE) {
            return;
        }
        this.states[layer][x + (y * Constants.CHUNK_SIZE)] = state;
    }

    public int getTileStateAt(int layer, int x, int y) {
        if (x < 0 || x >= Constants.CHUNK_SIZE || y < 0 || y >= Constants.CHUNK_SIZE) {
            return -1;
        }
        return this.states[layer][x + (y * Constants.CHUNK_SIZE)];
    }

    public void putInByteBuffer(ByteBuffer byteBuffer) {
        for (int l = 0; l < tiles.length; l++) {
            for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
                for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
                    byteBuffer.put((byte) this.tiles[l][x + (y * Constants.CHUNK_SIZE)]);
                }
            }
        }
    }

    private BiomeType getBiomeAt(int x, int y) {
        return this.worldGenerator.getBiome(x, y);
    }

    public boolean isTileSolid(int x, int y) {
        for (int l = 0; l < tiles.length; l++) {
            Tile t = TileRegistry.get(this.tiles[l][x + (y * Constants.CHUNK_SIZE)]);
            if (t == null) {
                continue;
            }
            if ((t.getFlags() & Tile.FLAG_SOLID) != 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isTileWater(int x, int y) {
        for (int l = 0; l < tiles.length; l++) {
            Tile t = TileRegistry.get(this.tiles[l][x + (y * Constants.CHUNK_SIZE)]);
            if (t == null) {
                continue;
            }
            if ((t.getFlags() & Tile.FLAG_WATER) != 0) {
                return true;
            }
        }
        return false;
    }

    public byte[] toBytes() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(CHUNK_DATA_SIZE);
        byteBuffer.put((byte) CURRENT_VERSION);
        byteBuffer.putInt(this.chunkX);
        byteBuffer.putInt(this.chunkY);
        for (int l = 0; l < this.tiles.length; l++) {
            for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
                for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
                    byteBuffer.putShort((short) this.tiles[l][x + (y * Constants.CHUNK_SIZE)]);
                    byteBuffer.put((byte) this.states[l][x + (y * Constants.CHUNK_SIZE)]);
                }
            }
        }
        byte[] data = new byte[byteBuffer.position()];
        System.arraycopy(byteBuffer.array(), 0, data, 0, data.length);
        return data;
    }

    public void save(RandomAccessFile chunkFile) {
        try {
            int relX = this.chunkX % Constants.SAVE_SIZE;
            int relY = this.chunkY % Constants.SAVE_SIZE;
            long pos = relX + (relY * Constants.SAVE_SIZE);
            chunkFile.seek(pos * CHUNK_DATA_SIZE);

            chunkFile.write(this.toBytes());
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
    }

    //    public void save(FileHandle folder) {
    //        // TODO: Save chunks in larger batches to reduce disk I/O
    //        // TODO: Also do it in a separate thread
    //        folder.child("chunks")
    //              .child("chunk_" + this.chunkX + "_" + this.chunkY + ".dat")
    //              .writeBytes(this.toBytes(), false);
    //    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MapChunk)) {
            return false;
        }
        MapChunk other = (MapChunk) o;
        return chunkX == other.chunkX && chunkY == other.chunkY;
    }

    public int getChunkX() {
        return this.chunkX;
    }

    public int getChunkY() {
        return this.chunkY;
    }

    public int getBaseX() {
        return this.chunkX * Constants.CHUNK_SIZE;
    }

    public int getBaseY() {
        return this.chunkY * Constants.CHUNK_SIZE;
    }

    public int getUsedLayers() {
        return this.usedLayers;
    }

    public Integer getId() {
        return MapChunk.getKey(this.getChunkX(), this.getChunkY());
    }

}
