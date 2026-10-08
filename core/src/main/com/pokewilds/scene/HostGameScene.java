package com.pokewilds.scene;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.pokewilds.Constants;
import com.pokewilds.PokeWilds;
import com.pokewilds.entity.Direction;
import com.pokewilds.entity.Entity;
import com.pokewilds.entity.Player;
import com.pokewilds.scene.battle.BattleController;
import com.pokewilds.scene.battle.BattleView;
import com.pokewilds.scene.gui.Gui;
import com.pokewilds.scene.gui.MenuGui;
import com.pokewilds.util.Settings;
import com.pokewilds.util.net.GameSocket;
import com.pokewilds.util.net.PacketHandler;
import com.pokewilds.util.net.PacketType;
import com.pokewilds.world.WorldSize;
import com.pokewilds.world.chunk.MapChunk;
import com.pokewilds.world.gen.WorldGenerator;
import com.pokewilds.world.tile.Tile;
import com.pokewilds.world.tile.TileEffect;
import com.pokewilds.world.tile.TileEvent;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * The {@code HostGameScene} is where the main processing happens,
 * </p>
 */
public class HostGameScene extends GameScene implements PacketHandler {
    public static final int MAX_PLAYERS = 8;

    protected final HashMap<Integer, MapChunk> unusedLoadedChunks;
    private final UUID sessionId;
    private final GameSocket socket;
    private final Map<SocketAddress, List<Integer>> connectedClients;
    private final Map<SocketAddress, Player> socketToPlayer;
    private final List<Integer> usedChunks;
    private final Player[] players;
    private final BattleController[] battles;
    private final ScheduledExecutorService executor;
    private final Map<File, RandomAccessFile> openFiles;
    private int entityIds;
    private String worldName;
    private WorldSize worldSize;
    private WorldGenerator generator;
    private FileHandle saveFolder;
    private boolean usingGamepad;
    private float heldTime;
    private int spawnX, spawnY;
    private long mouseDown;

    public static String toFolderName(String folderName) {
        return folderName.toLowerCase().replaceAll("[^A-Za-z0-9-_]", "_").trim();
    }

    private HostGameScene(final PokeWilds game) {
        super(game);
        this.worldSize = WorldSize.TINY;
        this.socketToPlayer = new HashMap<>();
        this.connectedClients = new HashMap<>();
        this.usedChunks = new ArrayList<>();
        this.sessionId = UUID.randomUUID();
        this.socket = new GameSocket(this);
        this.players = new Player[MAX_PLAYERS];
        this.battles = new BattleController[MAX_PLAYERS];
        this.saveFolder = Gdx.files.absolute("");
        this.unusedLoadedChunks = new HashMap<>();
        this.openFiles = new HashMap<>();
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.executor.schedule(this::save, Constants.SAVE_TIMER, TimeUnit.MINUTES);
    }

    public HostGameScene(final PokeWilds game, final String filename, final WorldSize worldSize) {
        this(game);
        this.worldSize = worldSize;
        this.worldName = filename;
        this.saveFolder = Gdx.files.local("saves").child(toFolderName(filename));
        int i = 1;
        while (this.saveFolder.exists()) {
            this.saveFolder = Gdx.files.local("saves").child(toFolderName(filename) + " (" + i++ + ")");
        }
        this.getOrCreateSave();
        this.initDesktop();
    }

    public HostGameScene(final PokeWilds game, final FileHandle saveFolder) {
        this(game);
        this.worldName = saveFolder.name();
        this.saveFolder = saveFolder;
        this.getOrCreateSave();
        this.initDesktop();
    }

    public HostGameScene(final PokeWilds game, final FileHandle saveFolder, final int port) throws IOException {
        this(game, saveFolder);

        this.socket.open(new InetSocketAddress(port));
        this.socket.start();
    }

    public HostGameScene(final PokeWilds game, final String filename, final int port) throws IOException {
        this(game, filename, WorldSize.MEDIUM);

        this.socket.open(new InetSocketAddress(port));
        this.socket.start();
    }

    public void initDesktop() {
        if (Gdx.app.getType() == Application.ApplicationType.HeadlessDesktop) {
            return;
        }

        this.renderables[0] = this.entities[0] = this.player = this.players[0] = new Player(this, 0, Settings.getPid(), 0, null, this.spawnX,
                this.spawnY);
        this.player.setTexture(this.playerTexture);
    }

    public void getOrCreateSave() {
        if (this.saveFolder.exists()) {
            ByteBuffer buffer = ByteBuffer.wrap(this.saveFolder.child("world.dat").readBytes());
            buffer.position(buffer.position() + 6);
            StringBuilder name = new StringBuilder();
            char c;
            while ((c = (char) buffer.get()) != 0) {
                name.append(c);
            }
            this.worldName = name.toString();
            long seed = buffer.getLong();
            this.worldSize = WorldSize.values()[buffer.get()];
            this.spawnX = buffer.getInt();
            this.spawnY = buffer.getInt();

            this.generator = new WorldGenerator(seed, this.worldSize);
        } else {
            this.saveFolder.mkdirs();
            this.saveFolder.child("chunks").mkdirs();

            long seed = (new Random()).nextLong();
            this.generator = new WorldGenerator(seed, this.worldSize);

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.put("PWW".getBytes(StandardCharsets.UTF_8));
            buffer.put(Constants.MAJOR_VERSION);
            buffer.put(Constants.MINOR_VERSION);
            buffer.put(Constants.PATCH_VERSION);
            buffer.put(this.worldName.getBytes(StandardCharsets.UTF_8));
            buffer.put((byte) 0);
            buffer.putLong(seed);
            buffer.put((byte) this.worldSize.ordinal());

            // Find SpawnX
            int cX = (int) (this.worldSize.width * 0.33f);
            int cY = (int) (this.worldSize.height * 0.33f);
            boolean found = false;
            ArrayDeque<MapChunk> chunks = new ArrayDeque<>();
            chunks.add(new MapChunk(this.generator, cX, cY));
            while (!found) {
                MapChunk c = chunks.poll();
                if (c == null) {
                    break;
                }
                c.generate();

                for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
                    for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
                        if (c.isTileSolid(x, y) || c.isTileWater(x, y) || found) {
                            continue;
                        }
                        found = true;
                        this.spawnX = c.getBaseX() + x;
                        this.spawnY = c.getBaseY() + y;
                    }
                }

                if (!found) {
                    chunks.add(new MapChunk(this.generator, c.getChunkX() - 1, c.getChunkY()));
                    chunks.add(new MapChunk(this.generator, c.getChunkX() + 1, c.getChunkY()));
                    chunks.add(new MapChunk(this.generator, c.getChunkX(), c.getChunkY() - 1));
                    chunks.add(new MapChunk(this.generator, c.getChunkX(), c.getChunkY() + 1));
                }
            }

            buffer.putInt(this.spawnX);
            buffer.putInt(this.spawnY);

            this.saveFolder.child("world.dat").writeBytes(buffer.array(), 0, buffer.position(), false);
            // World.saveImage(this.worldSize, this.generator, this.saveFolder);
        }
    }

    @Override
    public void update(float deltaTime) {
        this.usedChunks.clear();
        synchronized (this.loadedChunks) {
            this.usedChunks.addAll(this.loadedChunks.keySet());
        }

        for (int i = 0; i < this.players.length; i++) {
            if (this.players[i] == null) {
                continue;
            }

            if (this.players[i].hasTimedOut()) {
                this.socketToPlayer.remove(this.players[i].getSocketAddress());
                this.sendEntityRemove(i);
                this.renderables[i] = this.entities[i] = this.players[i] = null;
                Gdx.app.log("HostGameScene", "Player " + i + " has timed out.");
                // removing this player.
                continue;
            }

            for (int ry = -Constants.LOADED_CHUNKS; ry < Constants.LOADED_CHUNKS + 1; ry++) {
                for (int rx = -Constants.LOADED_CHUNKS; rx < Constants.LOADED_CHUNKS + 1; rx++) {
                    int chunkX = this.players[i].getMapChunkX() + rx;
                    int chunkY = this.players[i].getMapChunkY() + ry;
                    if (!this.worldSize.isValidChunk(chunkX, chunkY)) {
                        continue;
                    }
                    this.usedChunks.remove(MapChunk.getKey(chunkX, chunkY));
                    synchronized (this.loadedChunks) {
                        if (this.loadedChunks.containsKey(MapChunk.getKey(chunkX, chunkY))) {
                            continue;
                        }
                    }
                    MapChunk c = this.loadMapChunk(chunkX, chunkY);
                    synchronized (this.loadedChunks) {
                        this.loadedChunks.put(MapChunk.getKey(chunkX, chunkY), c);
                    }
                    c.reloadStates(this);
                    if (this.players[i].getMapChunkX() == chunkX && this.players[i].getMapChunkY() == chunkY) {
                        this.triggerTileEvent(TileEvent.TILE_ENTERED, this.players[i].getX(), this.players[i].getY(),
                                this.players[i]);
                        this.triggerTileEvent(TileEvent.TILE_FINISHED, this.players[i].getX(), this.players[i].getY(),
                                this.players[i]);
                    }
                }
            }
            this.players[i].update(deltaTime);

            if (this.players[i].hasUpdated()) {
                this.sendEntityUpdate(this.players[i]);
            }
        }

        for (int i = 0; i < this.tileEffects.length; i++) {
            TileEffect e = this.tileEffects[i];
            if (e == null) {
                continue;
            }
            e.update(deltaTime);
            if (e.isDone()) {
                this.renderables[MAX_ENTITIES + i] = this.tileEffects[i] = null;
            }
        }

        for (Integer chunkId : this.usedChunks) {
            synchronized (this.loadedChunks) {
                MapChunk c = this.loadedChunks.remove(chunkId);
                unloadMapChunk(c);
            }
        }

        List<Gui> guiOrder = new ArrayList<>(this.guis);
        Collections.reverse(guiOrder);
        for (Gui gui : guiOrder) {
            if (gui.update(deltaTime)) {
                break;
            }
        }

        if (this.mouseDown > 0) {
            if (this.buildMode) {
                boolean done = this.setTile(Tile.LAYER_GROUND, this.buildTile.x, this.buildTile.y, Tile.TILE_SAND, 0, true);
                if (done) {
                    this.triggerTileEvent(TileEvent.TILE_UPDATED, this.buildTile.x, this.buildTile.y, this.player);
                }
            }
        }

        super.update(deltaTime);
    }

    public void sendEntityRemove(int id) {
        ByteBuffer b = ByteBuffer.allocate(3);
        b.put(PacketType.ENTITY_REMOVE.id());
        b.putShort((short) id);
        this.sendToOther(b);
    }

    /**
     * <p>
     * Saves the game.
     * </p>
     */
    public void save() {
        this.savePlayers();
        this.saveAwaitingChunks();
    }

    /**
     * <p>
     * Saves the players.
     * </p>
     */
    private void savePlayers() {
        ByteBuffer save = ByteBuffer.allocate(1024);
        for (int i = 0; i < this.players.length; i++) {
            if (this.players[i] == null) {
                continue;
            }
            save.position(0);
            this.players[i].save(save);
            FileHandle playerFolder = this.saveFolder.child("players/");
            playerFolder.mkdirs();
            FileHandle f = playerFolder.child(this.players[i].getPid() + ".dat");
            f.writeBytes(save.array(), 0, save.position(), false);
        }
    }

    /**
     * <p>
     * Saves any chunks which are unloaded and waiting to be saved.
     * </p>
     */
    private void saveAwaitingChunks() {
        synchronized (this.openFiles) {
            synchronized (this.unusedLoadedChunks) {
                for (MapChunk c : this.unusedLoadedChunks.values()) {
                    try {
                        int blockX = c.getChunkX() / Constants.SAVE_SIZE;
                        int blockY = c.getChunkY() / Constants.SAVE_SIZE;
                        File saveFile = this.saveFolder.child("chunks")
                                .child("chunk_" + blockX + "_" + blockY + ".dat")
                                .file();
                        RandomAccessFile f = null;
                        if (this.openFiles.containsKey(saveFile)) {
                            f = openFiles.get(saveFile);
                        } else {
                            f = new RandomAccessFile(saveFile, "rw");
                            openFiles.put(saveFile, new RandomAccessFile(saveFile, "rw"));
                        }
                        c.save(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                this.unusedLoadedChunks.clear();
            }

            for (RandomAccessFile f : openFiles.values()) {
                try {
                    f.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            openFiles.clear();
        }

        Gdx.app.log("HostGameScene", "Saved all awaiting chunks.");
        System.gc();
    }

    @Override
    public void dispose() {
        synchronized (this.loadedChunks) {
            for (MapChunk c : this.loadedChunks.values()) {
                unloadMapChunk(c);
            }
            this.loadedChunks.clear();
        }
        this.save();
        this.executor.shutdownNow();
        try {
            this.executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        }
        if (this.socket != null) {
            this.socket.close();
            if (this.socket.isAlive()) {
                try {
                    this.socket.join();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    @Override
    public void createTileEffect(TileEffect effect) {
        super.createTileEffect(effect);

        ByteBuffer out = ByteBuffer.allocate(10);
        out.put(PacketType.TILE_EFFECT_SPAWN.id());
        out.put((byte) 0);
        out.putInt(effect.getX());
        out.putInt(effect.getY());
        out.flip();

        sendToOther(out);
    }

    @Override
    public TileEffect deleteTileEffect(Class<? extends TileEffect> effect, int tX, int tY) {
        TileEffect te = super.deleteTileEffect(effect, tX, tY);

        if (te == null) {
            return null;
        }
        ByteBuffer out = ByteBuffer.allocate(10);
        out.put(PacketType.TILE_EFFECT_REMOVE.id());
        out.put((byte) 0);
        out.putInt(tX);
        out.putInt(tY);
        out.flip();

        sendToOther(out);
        return te;
    }

    @Override
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

        if (transmit) {
            ByteBuffer out = ByteBuffer.allocateDirect(14);
            out.put(PacketType.TILE_UPDATE.id());
            out.putShort((short) id);
            out.put((byte) state);
            out.put((byte) layer);
            out.putInt(x);
            out.putInt(y);
            out.flip();

            this.sendToOther(out);
        }
        return true;
    }

    @Override
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
        int tile = c.getTileAt(layer, x % Constants.CHUNK_SIZE, y % Constants.CHUNK_SIZE);
        c.setTileStateAt(layer, x % Constants.CHUNK_SIZE, y % Constants.CHUNK_SIZE, state);

        if (transmit) {
            ByteBuffer out = ByteBuffer.allocateDirect(14);
            out.put(PacketType.TILE_UPDATE.id());
            out.putShort((short) tile);
            out.put((byte) state);
            out.put((byte) layer);
            out.putInt(x);
            out.putInt(y);
            out.flip();

            this.sendToOther(out);
        }

        return true;
    }

    public void sendEntityUpdate(Entity entity) {
        ByteBuffer out = ByteBuffer.allocate(1024);
        out.put(PacketType.ENTITY_STATE_UPDATE.id());
        out.putLong(System.currentTimeMillis());
        out.putShort((short) entity.getId());
        out.put((byte) entity.getEntityType());
        out.put((byte) entity.getLookDirection().ordinal());
        out.putInt(entity.getX());
        out.putInt(entity.getY());
        out.putFloat(entity.getPosition().x);
        out.putFloat(entity.getPosition().y);

        out.flip();

        sendToOther(out);
    }

    public MapChunk loadMapChunk(int x, int y) {
        if (this.unusedLoadedChunks.containsKey(MapChunk.getKey(x, y))) {
            return this.unusedLoadedChunks.remove(MapChunk.getKey(x, y));
        }
        boolean exists = false;
        int blockX = x / Constants.SAVE_SIZE;
        int blockY = y / Constants.SAVE_SIZE;
        File f = this.saveFolder.child("chunks").child("chunk_" + blockX + "_" + blockY + ".dat").file();

        RandomAccessFile chunkFile = null;
        if (this.openFiles.containsKey(f)) {
            chunkFile = this.openFiles.get(f);
        } else if (f.exists()) {
            try {
                chunkFile = new RandomAccessFile(f, "rw");
                this.openFiles.put(f, chunkFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (chunkFile != null) {
            int relX = x % Constants.SAVE_SIZE;
            int relY = y % Constants.SAVE_SIZE;
            long pos = (long) (relX + relY * Constants.SAVE_SIZE) * MapChunk.CHUNK_DATA_SIZE;
            try {
                if (chunkFile.length() >= pos) {
                    chunkFile.seek(pos);
                    exists = chunkFile.read() > 0;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        MapChunk c;
        if (exists) {
            c = new MapChunk(this.generator, x, y, chunkFile);
        } else {
            c = new MapChunk(this.generator, x, y);
            c.generate();
        }
        return c;
    }

    public void unloadMapChunk(MapChunk c) {
        synchronized (this.unusedLoadedChunks) {
            this.unusedLoadedChunks.put(MapChunk.getKey(c.getChunkX(), c.getChunkY()), c);
            if (this.unusedLoadedChunks.size() > 200) {
                this.executor.submit(this::saveAwaitingChunks);
            }
        }
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        this.usingGamepad = true;

        int aButton = Input.Keys.Z;
        int bButton = Input.Keys.X;
        int xButton = Input.Keys.B;
        int yButton = Input.Keys.B;
        if (controller.getName().equals("Nintendo Switch Pro Controller")) {
            aButton = Input.Keys.X;
            bButton = Input.Keys.Z;
            xButton = Input.Keys.B;
            yButton = Input.Keys.B;
        }

        List<Gui> guiOrder = new ArrayList<>(this.guis);
        Collections.reverse(guiOrder);
        for (Gui gui : guiOrder) {
            if (gui.buttonDown(controller, buttonCode)) {
                return true;
            }
        }

        switch (buttonCode) {
            case 11:
                return this.keyDown(Input.Keys.W);
            case 12:
                return this.keyDown(Input.Keys.S);
            case 13:
                return this.keyDown(Input.Keys.A);
            case 14:
                return this.keyDown(Input.Keys.D);
            case 0:
                return this.keyDown(aButton);
            case 1:
                return this.keyDown(bButton);
            case 2:
                return this.keyDown(xButton);
            case 3:
                return this.keyDown(yButton);
            case 6:
                return this.keyDown(Input.Keys.ENTER);
            default:
                System.out.println(buttonCode);
                return false;
        }
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        this.usingGamepad = true;

        int aButton = Input.Keys.Z;
        int bButton = Input.Keys.X;
        int xButton = Input.Keys.B;
        int yButton = Input.Keys.B;
        if (controller.getName().equals("Nintendo Switch Pro Controller")) {
            aButton = Input.Keys.X;
            bButton = Input.Keys.Z;
            xButton = Input.Keys.B;
            yButton = Input.Keys.B;
        }

        List<Gui> guiOrder = new ArrayList<>(this.guis);
        Collections.reverse(guiOrder);
        for (Gui gui : guiOrder) {
            if (gui.buttonUp(controller, buttonCode)) {
                return true;
            }
        }

        switch (buttonCode) {
            case 11:
                return this.keyUp(Input.Keys.W);
            case 12:
                return this.keyUp(Input.Keys.S);
            case 13:
                return this.keyUp(Input.Keys.A);
            case 14:
                return this.keyUp(Input.Keys.D);
            case 0:
                return this.keyUp(aButton);
            case 1:
                return this.keyUp(bButton);
            case 6:
                return this.keyUp(Input.Keys.ENTER);
            default:
                System.out.println(buttonCode);
                return false;
        }
    }

    @Override
    public boolean keyDown(int keyCode) {
        if (player == null) {
            return false;
        }

        List<Gui> guiOrder = new ArrayList<>(this.guis);
        Collections.reverse(guiOrder);
        for (Gui gui : guiOrder) {
            if (gui.keyDown(keyCode)) {
                return true;
            }
        }

        int minX = this.player.getX() - 7;
        int minY = this.player.getY() - 7;
        int maxX = this.player.getX() + 7;
        int maxY = this.player.getY() + 7;
        if (this.buildMode) {
            if (keyCode == Input.Keys.W) {
                this.buildTile.y -= 1;
                this.buildTile.y = Math.max(minY, this.buildTile.y);
                return true;
            } else if (keyCode == Input.Keys.A) {
                this.buildTile.x -= 1;
                this.buildTile.x = Math.max(minX, this.buildTile.x);
                return true;
            } else if (keyCode == Input.Keys.D) {
                this.buildTile.x += 1;
                this.buildTile.x = Math.min(maxX, this.buildTile.x);
                return true;
            } else if (keyCode == Input.Keys.S) {
                this.buildTile.y += 1;
                this.buildTile.y = Math.min(maxY, this.buildTile.y);
                return true;
            } else if (keyCode == Input.Keys.Z) {
                this.mouseDown = System.currentTimeMillis();
                return true;
            } else if (keyCode == Input.Keys.B) {
                this.buildMode = false;
                return true;
            }
            return true;
        }

        if (keyCode == Input.Keys.W) {
            this.player.setMoveDirection(Direction.UP);
            return true;
        } else if (keyCode == Input.Keys.A) {
            this.player.setMoveDirection(Direction.LEFT);
            return true;
        } else if (keyCode == Input.Keys.D) {
            this.player.setMoveDirection(Direction.RIGHT);
            return true;
        } else if (keyCode == Input.Keys.S) {
            this.player.setMoveDirection(Direction.DOWN);
            return true;
        } else if (keyCode == Input.Keys.ENTER) {
            this.addGui(new MenuGui(this));
            return true;
        } else if (keyCode == Input.Keys.F5) {
            try {
                this.socket.open(new InetSocketAddress(5173));
                this.socket.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (keyCode == Input.Keys.F1) {
            this.isDebugging = !this.isDebugging;
            return true;
        } else if (keyCode == Input.Keys.B) {
            this.buildMode = true;
            this.buildTile.set(this.player.getX(), this.player.getY());
            this.buildPos.set(this.player.getX() * 16.f, -this.player.getY() * 16.f, 0.0f);
            return true;
        } else if (keyCode == Input.Keys.R) {
            this.battleView = new BattleView(this, this.batch);
            return true;
        }

        return false;
    }

    @Override
    public boolean keyUp(int keyCode) {
        if (player == null) {
            return false;
        }

        List<Gui> guiOrder = new ArrayList<>(this.guis);
        Collections.reverse(guiOrder);
        for (Gui gui : guiOrder) {
            if (gui.keyUp(keyCode)) {
                return true;
            }
        }

        if (this.buildMode) {
            if (keyCode == Input.Keys.Z) {
                this.mouseDown = 0;
                return true;
            }
            return false;
        }

        if (keyCode == Input.Keys.W || keyCode == Input.Keys.A || keyCode == Input.Keys.D || keyCode == Input.Keys.S) {
            this.player.setMoveDirection(null);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if (player == null) {
            return false;
        }
        if (this.usingGamepad) {
            this.usingGamepad = false;
        }
        if (this.buildMode) {
            Vector3 pick = this.camera.getPickRay(Gdx.input.getX(), Gdx.input.getY()).origin;
            this.buildTile.set((int) Math.floor(pick.x / 16f), (int) Math.ceil(-pick.y / 16f));
            int minX = this.player.getX() - 7;
            int minY = this.player.getY() - 7;
            int maxX = this.player.getX() + 7;
            int maxY = this.player.getY() + 7;
            this.buildTile.clamp(minX, minY, maxX, maxY);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (player == null) {
            return false;
        }
        if (this.usingGamepad) {
            this.usingGamepad = false;
        }
        if (this.buildMode) {
            this.mouseMoved(screenX, screenY);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (player == null) {
            return false;
        }
        if (this.usingGamepad) {
            this.usingGamepad = false;
        }
        this.mouseDown = System.currentTimeMillis();
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (player == null) {
            return false;
        }
        if (this.usingGamepad) {
            this.usingGamepad = false;
        }
        this.mouseDown = 0;
        return false;
    }

    @Override
    public void handlePacket(final SocketAddress fromAddress, final PacketType packetType,
                             final ByteBuffer buffer) throws IOException {
        if (this.socketToPlayer.containsKey(fromAddress)) {
            this.socketToPlayer.get(fromAddress).pinged();
        }
        ByteBuffer out = ByteBuffer.allocate(1024);
        switch (packetType) {
            case PING:
                out.put(PacketType.PONG.id());
                out.flip();
                this.socket.send(fromAddress, out);
                break;
            case LAN_REQUEST_PACKET:
                out.put(PacketType.LAN_RESPONSE_PACKET.id());
                out.put((byte) 0);
                out.put(Constants.MAJOR_VERSION);
                out.put(Constants.MINOR_VERSION);
                out.put(Constants.PATCH_VERSION);
                out.putLong(sessionId.getMostSignificantBits());
                out.putLong(sessionId.getLeastSignificantBits());
                out.put(this.worldName.getBytes(StandardCharsets.UTF_8));
                out.put((byte) '\0');
                out.put(1, (byte) out.position());
                out.flip();

                this.socket.send(fromAddress, out);
                break;
            case CONNECTION_REQUEST_PACKET:
                int connectionId = -1;
                if (connectedClients.containsKey(fromAddress)) {
                    List<Integer> usedClients = connectedClients.get(fromAddress);
                    for (int i = 0; i < 5; i++) {
                        if (usedClients.contains(i)) {
                            continue;
                        }
                        connectionId = i;
                        break;
                    }
                } else {
                    connectionId = 0;
                    connectedClients.put(fromAddress, new ArrayList<Integer>());
                    connectedClients.get(fromAddress).add(connectionId);
                }
                UUID clientId = new UUID(buffer.getLong(), buffer.getLong());

                int playerId = -1;
                synchronized (this.players) {
                    for (int i = 0; i < this.players.length; i++) {
                        if (this.players[i] != null) {
                            continue;
                        }
                        playerId = i;
                        break;
                    }
                    if (playerId != -1) {
                        Player p = new Player(this, playerId, clientId, connectionId, fromAddress, this.spawnX, this.spawnY);
                        if (playerTexture != null) {
                            p.setTexture(playerTexture);
                        }
                        this.renderables[playerId] = this.entities[playerId] = this.players[playerId] = p;
                        this.socketToPlayer.put(fromAddress, p);
                    }
                }

                out.put(PacketType.CONNECTION_RESPONSE_PACKET.id());
                out.put((byte) 0);
                out.putLong(System.currentTimeMillis());
                out.putLong(this.sessionId.getMostSignificantBits());
                out.putLong(this.sessionId.getLeastSignificantBits());
                out.put((byte) this.worldSize.ordinal());
                out.put((byte) playerId);
                out.putInt(this.spawnX);
                out.putInt(this.spawnY);
                out.flip();

                this.socket.send(fromAddress, out);

                for (Player p : this.players) {
                    if (p == null) {
                        continue;
                    }
                    this.sendEntityUpdate(p);
                }
                break;
            case CHUNK_REQUEST_PACKET:
                int chunkId = buffer.getInt();
                int chunkX = (chunkId >> 16) & 0xFFFF;
                int chunkY = chunkId & 0xFFFF;

                MapChunk c;
                synchronized (this.loadedChunks) {
                    if (this.loadedChunks.containsKey(chunkId)) {
                        c = this.loadedChunks.get(chunkId);
                    } else {
                        c = this.loadMapChunk(chunkX, chunkY);
                    }
                }

                byte[] data = new byte[0xFFFF];
                out = ByteBuffer.wrap(data);
                out.put(PacketType.CHUNK_RESPONSE_PACKET.id());
                out.putInt(chunkId);
                for (int i = 0; i < Tile.LAYER_COUNT; i++) {
                    for (int j = 0; j < Constants.CHUNK_SIZE * Constants.CHUNK_SIZE; j++) {
                        out.putShort((short) c.getTileAt(i, j % Constants.CHUNK_SIZE, j / Constants.CHUNK_SIZE));
                        out.put((byte) c.getTileStateAt(i, j % Constants.CHUNK_SIZE, j / Constants.CHUNK_SIZE));
                    }
                }
                out.flip();
                socket.send(fromAddress, out);
                break;
            case MOVEMENT_REQUEST:
                int dirCode = buffer.get() & 0xFF;
                Direction d = dirCode == 0 ? null : Direction.values()[dirCode - 1];
                Player p = socketToPlayer.get(fromAddress);
                p.setMoveDirection(d);
                break;
            default:
                Gdx.app.log("PokeRancher", "got packet [" + packetType + "]");
                break;
        }
    }

    @Override
    public void onPacketError(Exception e) {
        Gdx.app.error("PokeWilds-Host", "Error while receiving packet", e);
    }

    public void sendToOther(ByteBuffer out) {
        try {
            for (int i = 0; i < players.length; i++) {
                if (players[i] == null || players[i].isLocal()) {
                    continue;
                }
                this.socket.send(players[i].getSocketAddress(), out);
                out.position(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isHost() {
        return true;
    }

}

