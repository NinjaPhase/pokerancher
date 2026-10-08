package com.pokewilds.scene;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.pokewilds.Constants;
import com.pokewilds.PokeWilds;
import com.pokewilds.entity.Entity;
import com.pokewilds.entity.Player;
import com.pokewilds.gfx.OrderedRenderable;
import com.pokewilds.scene.action.Action;
import com.pokewilds.scene.action.ActionPriority;
import com.pokewilds.scene.action.ActionQueue;
import com.pokewilds.scene.battle.BattleView;
import com.pokewilds.scene.gui.Gui;
import com.pokewilds.world.Coordinate;
import com.pokewilds.world.chunk.MapChunk;
import com.pokewilds.world.tile.Tile;
import com.pokewilds.world.tile.TileEffect;
import com.pokewilds.world.tile.TileEvent;
import com.pokewilds.world.tile.TileRegistry;

import java.util.*;

/**
 * <p>
 * The {@code GameScene} contains the main game code which relate to both a Client and Host.
 * </p>
 */
public abstract class GameScene extends Scene implements ActionQueue {
    public static final int MAX_ENTITIES = 1023;
    public static final int MAX_TILE_EFFECTS = 50;
    public static final int IMASK_MOVEMENT = 0x1;
    private static final float BUILD_MODE_ZOOM = 1.25f;

    protected final PokeWilds game;

    protected final Map<Integer, MapChunk> loadedChunks;

    protected Player player;
    protected OrthographicCamera camera, uiCamera;

    protected SpriteBatch batch;
    protected Texture playerTexture, tilesetTexture, menuTexture, cursorTexture;
    protected NinePatch menuPatch;
    protected float tileAnimTimer;
    protected int tileAnim;
    protected Entity[] entities;
    protected TileEffect[] tileEffects;
    protected OrderedRenderable[] renderables, rendering;
    protected List<Gui> guis;
    protected int tileEffectIdx;
    protected Map<ActionPriority, Queue<Action>> actionQueue;
    protected BattleView battleView;
    protected boolean buildMode;
    protected Vector3 buildPos;
    protected Coordinate buildTile;

    protected boolean isDebugging;

    public static int ySort(OrderedRenderable a, OrderedRenderable b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        return (int) (a.getYIndex() - b.getYIndex());
    }

    /**
     * <p>Constructs the {@code GameScene} with a reference to the main game handler.</p>
     *
     * @param game The game handler.
     */
    public GameScene(final PokeWilds game) {
        super();

        this.entities = new Entity[MAX_ENTITIES];
        this.tileEffects = new TileEffect[MAX_TILE_EFFECTS];
        this.renderables = new OrderedRenderable[MAX_ENTITIES + MAX_TILE_EFFECTS];
        this.rendering = new OrderedRenderable[MAX_ENTITIES + MAX_TILE_EFFECTS];
        this.game = game;
        this.loadedChunks = new HashMap<>();
        this.guis = new LinkedList<>();
        this.actionQueue = new HashMap<>();
        for (ActionPriority priority : ActionPriority.values()) {
            this.actionQueue.put(priority, new ArrayDeque<>());
        }

        if (Gdx.app.getType() == Application.ApplicationType.HeadlessDesktop) {
            return;
        }

        float scale = Constants.getScale();
        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera(Gdx.graphics.getWidth() / scale, Gdx.graphics.getHeight() / scale);
        this.uiCamera = new OrthographicCamera(Gdx.graphics.getWidth() / scale, Gdx.graphics.getHeight() / scale);
        this.buildPos = new Vector3();
        this.buildTile = new Coordinate();
        this.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.playerTexture = game.getTexture("red-walking.png");
        this.tilesetTexture = game.getTexture("tileset.png", true);
        this.tilesetTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        this.menuTexture = game.getTexture("menuframe.png");
        this.cursorTexture = game.getTexture("cursor.png");
        this.menuPatch = new NinePatch(menuTexture, 8, 8, 8, 8);
        this.isDebugging = false;
    }

    @Override
    public void update(float deltaTime) {
        if (this.battleView != null) {
            this.battleView.update(deltaTime);
        }

        this.tileAnimTimer += deltaTime;
        while (this.tileAnimTimer > Constants.FRAME * 16) {
            this.tileAnim++;
            this.tileAnimTimer -= Constants.FRAME * 16;
        }

        if (this.player == null) {
            return;
        }

        Vector3 buildTarget = new Vector3(this.buildTile.x * 16, -this.buildTile.y * 16, 0.0f);
        if (!this.buildPos.epsilonEquals(buildTarget, MathUtils.FLOAT_ROUNDING_ERROR)) {
            this.buildPos.slerp(buildTarget, 0.25f);
        } else {
            this.buildPos.set(buildTarget);
        }

        Vector3 cameraPos = new Vector3(this.player.getPosition().x, -this.player.getPosition().y, 0.0f);
        cameraPos = cameraPos.add(8f, 8f, 0.0f);

        if (this.buildMode) {
            cameraPos.slerp(this.buildPos, 0.25f);
            cameraPos = cameraPos.add(8f, 8f, 0.0f);

            // zoom camera
            //            if (BUILD_MODE_ZOOM - this.camera.zoom > MathUtils.FLOAT_ROUNDING_ERROR) {
            //                this.camera.zoom = Interpolation.linear.apply(this.camera.zoom, BUILD_MODE_ZOOM, 0.2f);
            //            } else {
            //                this.camera.zoom = BUILD_MODE_ZOOM;
            //            }
        } else {
            //            if (this.camera.zoom - 1.0f > MathUtils.FLOAT_ROUNDING_ERROR) {
            //                this.camera.zoom = Interpolation.linear.apply(this.camera.zoom, 1.0f, 0.2f);
            //            } else {
            //                this.camera.zoom = 1.0f;
            //            }
        }


        if (!this.camera.position.epsilonEquals(cameraPos)) {
            float smoothing = 0.8f;
            if (this.buildMode) {
                smoothing = 0.1f;
            }
            this.camera.position.slerp(cameraPos, smoothing);
        } else {
            this.camera.position.set(cameraPos);
        }
        this.camera.position.set((float) Math.floor(this.camera.position.x), (float) Math.floor(this.camera.position.y), (float) Math.floor(this.camera.position.z));
        this.camera.update();
    }

    @Override
    public void render() {
        if (Gdx.app.getType() == Application.ApplicationType.HeadlessDesktop) {
            return;
        }

        ScreenUtils.clear(0f, 0f, 0f, 1f);
        if (this.player == null) {
            // Add loading screen here.
            return;
        }

        if (this.battleView != null) {
            batch.begin();
            this.battleView.render();
            batch.end();
            return;
        }

        batch.begin();
        batch.setProjectionMatrix(this.camera.combined);
        // Draw tiles

        for (int i = 0; i < Tile.LAYER_OOBJ; i++) {
            for (MapChunk c : this.loadedChunks.values()) {
                c.draw(batch, this.tilesetTexture, this.tileAnim, i, c.getChunkX() * Constants.CHUNK_SIZE * 16, c.getChunkY() * Constants.CHUNK_SIZE * 16);
            }
        }

        System.arraycopy(this.renderables, 0, this.rendering, 0, this.rendering.length);
        Arrays.sort(this.rendering, GameScene::ySort);
        for (int i = 0; i < this.rendering.length; i++) {
            if (this.rendering[i] == null) {
                continue;
            }
            this.rendering[i].draw(batch);
        }

        for (int i = Tile.LAYER_OOBJ; i < Tile.LAYER_COUNT; i++) {
            for (MapChunk c : this.loadedChunks.values()) {
                c.draw(batch, this.tilesetTexture, this.tileAnim, i, c.getChunkX() * Constants.CHUNK_SIZE * 16, c.getChunkY() * Constants.CHUNK_SIZE * 16);
            }
        }

        // Draw build mode cursor
        if (this.isHost() && this.buildMode) {
            batch.setColor(1.0f, 1.0f, 1.0f, 0.8f);
            batch.draw(tilesetTexture, this.buildPos.x, this.buildPos.y, 64, 16, 16, 16);
            batch.setColor(Color.WHITE);
            batch.draw(cursorTexture, this.buildPos.x, this.buildPos.y, 0, 0, 16, 16);
        }

        // Draw UI
        batch.setProjectionMatrix(uiCamera.combined);

        if (this.isDebugging) {
            MapChunk c = this.loadedChunks.get(MapChunk.getKey(this.player.getMapChunkX(), this.player.getMapChunkY()));
            if (c != null) {
                this.game.getMainFont().draw(batch, "Coords: (" + this.player.getX() + ", " + this.player.getY() + ")", 12.0f, -12.0f);
                this.game.getMainFont().draw(batch, "Chunk: (" + this.player.getMapChunkX() + ", " + this.player.getMapChunkY() + ")", 12.0f, -12.0f - (12.0f * 1));
                this.game.getMainFont().draw(batch, "Loaded Chunks: " + this.loadedChunks.size(), 12.0f, -12.0f - (12.0f * 2));
                this.game.getMainFont().draw(batch, "Entities: " + Arrays.stream(this.entities).filter(Objects::nonNull).count(), 12.0f, -12.0f - (12.0f * 3));
                this.game.getMainFont().draw(batch, "Pid: " + this.player.getId(), 12.0f, -12.0f - (12.0f * 4));
                this.game.getMainFont().draw(batch, "Type: " + (isHost() ? "Host" : "Client"), 12.0f, -12.0f - (12.0f * 5));
                this.game.getMainFont().draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 12.0f, -12.0f - (12.0f * 6));
                this.game.getMainFont().draw(batch, "Mem: " + Gdx.app.getJavaHeap() / 1024 / 1024 + "MB", 12.0f, -12.0f - (12.0f * 7));
                this.game.getMainFont().draw(batch, "PlayerPos" + this.player.getPosition(), 12.0f, -12.0f - (12.0f * 8));
                this.game.getMainFont().draw(batch, "BuildPos: " + this.buildPos, 12.0f, -12.0f - (12.0f * 9));
            }
        }

        for (Gui gui : this.guis) {
            gui.render(batch, this.uiCamera);
        }

        batch.end();
    }

    @Override
    public void dispose() {

    }

    @Override
    public void resize(int width, int height) {
        if (Gdx.app.getType() == Application.ApplicationType.HeadlessDesktop) {
            return;
        }
        float scale = Constants.getScale(height);
        float uiScale = Constants.getUIScale(height);
        float aspect = (float) height / (float) width;
        this.camera.viewportWidth = (float) Math.round(width / scale);
        this.camera.viewportHeight = (float) Math.round(height / scale);
        this.camera.update();

        this.uiCamera.viewportWidth = (float) Math.round(width / uiScale);
        this.uiCamera.viewportHeight = (float) Math.round(height / uiScale);
        this.uiCamera.position.x = Math.floorDiv((int) this.uiCamera.viewportWidth, 2);
        this.uiCamera.position.y = -Math.floorDiv((int) this.uiCamera.viewportHeight, 2);
        this.uiCamera.update();

        if (this.battleView != null) {
            this.battleView.resize(width, height);
        }
    }

    public void triggerTileEvent(TileEvent e, int x, int y, Entity entity) {
        if (!this.isHost()) {
            return;
        }
        for (int l = 0; l < Tile.LAYER_COUNT; l++) {
            int tId = this.getTileAt(l, x, y);
            if (tId == -1) {
                continue;
            }
            if (TileRegistry.get(tId).onTileEvent(e, x, y, this, entity)) {
                for (int ry = -1; ry < 2; ry++) {
                    for (int rx = -1; rx < 2; rx++) {
                        this.triggerTileEvent(e, x + rx, y + ry, entity);
                    }
                }
            }
        }
    }

    public boolean setTile(int layer, int x, int y, int id, int state, boolean transmit) {
        int cX = x / Constants.CHUNK_SIZE;
        int cY = y / Constants.CHUNK_SIZE;
        if (!loadedChunks.containsKey(MapChunk.getKey(cX, cY))) {
            return false;
        }
        MapChunk c = loadedChunks.get(MapChunk.getKey(cX, cY));
        if (c.getTileAt(layer, x % Constants.CHUNK_SIZE, y % Constants.CHUNK_SIZE) == id) {
            return this.setTileState(layer, x, y, state, transmit);
        }
        c.setTileAt(layer, x % Constants.CHUNK_SIZE, y % Constants.CHUNK_SIZE, id, state);
        return true;
    }

    public boolean setTileState(int layer, int x, int y, int state, boolean transmit) {
        int cX = x / Constants.CHUNK_SIZE;
        int cY = y / Constants.CHUNK_SIZE;
        if (!loadedChunks.containsKey(MapChunk.getKey(cX, cY))) {
            return false;
        }
        MapChunk c = loadedChunks.get(MapChunk.getKey(cX, cY));
        if (c.getTileStateAt(layer, x % Constants.CHUNK_SIZE, y % Constants.CHUNK_SIZE) == state) {
            return false;
        }
        c.setTileStateAt(layer, x % Constants.CHUNK_SIZE, y % Constants.CHUNK_SIZE, state);
        return true;
    }

    public MapChunk getChunkAt(int x, int y) {
        int cX = x / Constants.CHUNK_SIZE;
        int cY = y / Constants.CHUNK_SIZE;
        if (!loadedChunks.containsKey(MapChunk.getKey(cX, cY))) {
            return null;
        }
        return loadedChunks.get(MapChunk.getKey(cX, cY));
    }

    public int getTileAt(int layer, int x, int y) {
        int cX = x / Constants.CHUNK_SIZE;
        int cY = y / Constants.CHUNK_SIZE;
        if (!loadedChunks.containsKey(MapChunk.getKey(cX, cY))) {
            return -1;
        }
        MapChunk c = loadedChunks.get(MapChunk.getKey(cX, cY));
        return c.getTileAt(layer, x % Constants.CHUNK_SIZE, y % Constants.CHUNK_SIZE);
    }

    public int getTileFlagsAt(int x, int y) {
        int cX = Math.floorDiv(x, Constants.CHUNK_SIZE);
        int cY = Math.floorDiv(y, Constants.CHUNK_SIZE);
        if (cX < 0 || cY < 0) {
            return Tile.FLAG_SOLID;
        }
        if (!loadedChunks.containsKey(MapChunk.getKey(cX, cY))) {
            return Tile.FLAG_SOLID;
        }
        MapChunk c = loadedChunks.get(MapChunk.getKey(cX, cY));
        int flags = 0;
        for (int l = 0; l < Tile.LAYER_COUNT; l++) {
            int tile = c.getTileAt(l, x % Constants.CHUNK_SIZE, y % Constants.CHUNK_SIZE);
            if (tile == -1) {
                continue;
            }
            flags |= TileRegistry.get(tile).getFlags();
        }
        return flags;
    }

    public void createTileEffect(TileEffect e) {
        int idx = this.tileEffectIdx;

        this.renderables[MAX_ENTITIES + idx] = this.tileEffects[idx] = e;

        this.tileEffectIdx = (this.tileEffectIdx + 1) % this.tileEffects.length;
    }

    public TileEffect deleteTileEffect(Class<? extends TileEffect> type, int x, int y) {
        for (int i = 0; i < this.tileEffects.length; i++) {
            TileEffect e = this.tileEffects[i];
            if (e == null || !e.getClass().equals(type) || e.getX() != x || e.getY() != y) {
                continue;
            }
            this.tileEffects[i].setDelete(true);
            return this.tileEffects[i];
        }
        return null;
    }

    @Override
    public void queueAction(Action action) {
        this.actionQueue.get(action.getPriority()).add(action);
    }

    public Texture getTilesetTexture() {
        return this.tilesetTexture;
    }

    public void addGui(Gui gui) {
        this.guis.add(gui);
    }

    public void removeGui(Gui gui) {
        this.guis.remove(gui);
    }

    public PokeWilds getGame() {
        return this.game;
    }

    public NinePatch getMenuPatch() {
        return this.menuPatch;
    }

    public abstract boolean isHost();

    public Player getLocalPlayer() {
        return this.player;
    }
}
