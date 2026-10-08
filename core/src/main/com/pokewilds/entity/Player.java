package com.pokewilds.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.pokewilds.Constants;
import com.pokewilds.pokemon.Pokemon;
import com.pokewilds.scene.GameScene;
import com.pokewilds.world.tile.Tile;
import com.pokewilds.world.tile.TileEvent;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * <p>
 * The {@code Player} class handles both Local and Networked players.
 * </p>
 */
public class Player extends Entity {
    public static final int PLAYER_ENTITY_TYPE = 1;
    public static final int MAX_PARTY_SIZE = 6;
    public static final int STATE_MAP = 0, STATE_BATTLE = 2;

    private final SocketAddress socketAddress;
    private final float moveSpeed = 64.0f;
    private final Pokemon[] party;
    private boolean hasUpdated;
    private Animation<TextureRegion>[] walkingAnimations;
    private float moveTimer;
    private int state;
    private long lastPing;
    private UUID pid;
    private int pidIdx;

    /**
     * <p>
     * Constructs a new local Player at a default X and Y.
     * </p>
     *
     * @param gameScene The game scene.
     * @param entityId  The entity id.
     * @param pid       The player id.
     * @param pidIdx    The player index.
     */
    public Player(GameScene gameScene, int entityId, UUID pid, int pidIdx) {
        this(gameScene, entityId, pid, pidIdx, null);
    }

    /**
     * <p>
     * Constructs a new Player at a default X and Y.
     * </p>
     *
     * @param gameScene     The game scene.
     * @param entityId      The entity id.
     * @param pid           The player id.
     * @param pidIdx        The player index.
     * @param socketAddress The socket address if connected from elsewhere.
     */
    public Player(GameScene gameScene, int entityId, UUID pid, int pidIdx, SocketAddress socketAddress) {
        this(gameScene, entityId, pid, pidIdx, socketAddress, 64 * Constants.CHUNK_SIZE, 64 * Constants.CHUNK_SIZE);
    }

    /**
     * <p>Constructs a new {@code Player} at a specific X and Y.</p>
     *
     * @param gameScene     The game scene.
     * @param entityId      The entity id.
     * @param pid           The player id.
     * @param pidIdx        The player index.
     * @param socketAddress The socket address.
     * @param x             The x position.
     * @param y             The y position.
     */
    public Player(GameScene gameScene, int entityId, UUID pid, int pidIdx, SocketAddress socketAddress, final int x, final int y) {
        super(gameScene, entityId);
        this.pid = pid;
        this.pidIdx = pidIdx;
        this.socketAddress = socketAddress;
        this.tileX = x;
        this.tileY = y;
        this.lookDirection = Direction.DOWN;
        this.position = new Vector2(x * 16.0f, y * 16.0f);
        this.party = new Pokemon[Player.MAX_PARTY_SIZE];
        this.party[0] = new Pokemon("bulbasaur", 5);
        this.lastPing = System.currentTimeMillis();
        this.pid = pid;
        this.pidIdx = pidIdx;
    }

    /**
     * <p>Sets the texture of the player.</p>
     * <p>The dedicated server won't have a graphics pipeline, so set here if not headless.</p>
     *
     * @param texture The texture to give the player.
     */
    @SuppressWarnings("unchecked")
    public void setTexture(Texture texture) {
        this.walkingAnimations = new Animation[]{
                new Animation<>(Constants.FRAME * 8, new TextureRegion(texture, 16, 64, 16, 32),
                        new TextureRegion(texture, 0, 64, 16, 32), new TextureRegion(texture, 16, 64, 16, 32),
                        new TextureRegion(texture, 32, 64, 16, 32)),
                new Animation<>(Constants.FRAME * 8, new TextureRegion(texture, 32, 64, -16, 32),
                        new TextureRegion(texture, 16, 64, -16, 32),
                        new TextureRegion(texture, 32, 64, -16, 32),
                        new TextureRegion(texture, 48, 64, -16, 32)),
                new Animation<>(Constants.FRAME * 8, new TextureRegion(texture, 16, 32, 16, 32),
                        new TextureRegion(texture, 0, 32, 16, 32), new TextureRegion(texture, 16, 32, 16, 32),
                        new TextureRegion(texture, 32, 32, 16, 32)),
                new Animation<>(Constants.FRAME * 8, new TextureRegion(texture, 16, 0, 16, 32),
                        new TextureRegion(texture, 0, 0, 16, 32), new TextureRegion(texture, 16, 0, 16, 32),
                        new TextureRegion(texture, 32, 0, 16, 32)),
        };
    }

    @Override
    public void update(final float deltaTime) {
        super.update(deltaTime);
        if (this.moveDirection == null && this.position.epsilonEquals(tileX * 16.0f, tileY * 16.0f)) {
            if (this.animationTimer < Constants.FRAME * 8) {
                this.animationTimer = 0.0f;
            } else if (this.animationTimer >= Constants.FRAME * 16 && this.animationTimer < Constants.FRAME * 24) {
                this.animationTimer = Constants.FRAME * 16;
            } else {
                this.animationTimer += deltaTime;
                while (this.animationTimer > Constants.FRAME * 32) {
                    this.animationTimer -= Constants.FRAME * 32;
                }
            }
            this.moveTimer = 0.0f;
            this.hasUpdated = false;
            return;
        }

        if (this.moveDirection != null && this.position.epsilonEquals(tileX * 16.0f, tileY * 16.0f)) {
            this.moveTimer += deltaTime;
            if (this.lookDirection != this.moveDirection) {
                this.animationTimer += deltaTime;
                this.lookDirection = this.moveDirection;
                this.moveTimer = -Constants.FRAME * 2;
                this.hasUpdated = true;
            } else if (moveTimer >= Constants.FRAME * 3) {
                int flags = gameScene.getTileFlagsAt(this.tileX + this.moveDirection.x,
                        this.tileY + this.moveDirection.y);
                if (!Tile.isSolid(flags) && !Tile.isWater(flags)) {
                    gameScene.triggerTileEvent(TileEvent.TILE_LEFT, this.tileX, this.tileY, this);
                    this.prevTileX = this.tileX;
                    this.prevTileY = this.tileY;
                    this.tileX = this.tileX + this.moveDirection.x;
                    this.tileY = this.tileY + this.moveDirection.y;
                    gameScene.triggerTileEvent(TileEvent.TILE_ENTERED, this.tileX, this.tileY, this);
                } else {
                    this.animationTimer += deltaTime;
                }
            }
        }

        if (!this.position.epsilonEquals(tileX * 16.0f, tileY * 16.0f)) {
            this.hasUpdated = true;
            this.animationTimer += deltaTime;
            while (this.animationTimer > Constants.FRAME * 32) {
                this.animationTimer -= Constants.FRAME * 32;
            }
            if (this.position.x < tileX * 16.0f) {
                this.position.x += moveSpeed * deltaTime;
                if (this.position.x > tileX * 16.0f) {
                    this.position.x = tileX * 16.0f;
                }
            } else if (this.position.x > tileX * 16.0f) {
                this.position.x -= moveSpeed * deltaTime;
                if (this.position.x < tileX * 16.0f) {
                    this.position.x = tileX * 16.0f;
                }
            }

            if (this.position.y < tileY * 16.0f) {
                this.position.y += moveSpeed * deltaTime;
                if (this.position.y > tileY * 16.0f) {
                    this.position.y = tileY * 16.0f;
                }
            } else if (this.position.y > tileY * 16.0f) {
                this.position.y -= moveSpeed * deltaTime;
                if (this.position.y < tileY * 16.0f) {
                    this.position.y = tileY * 16.0f;
                }
            }
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(this.walkingAnimations[this.lookDirection.ordinal()].getKeyFrame(this.animationTimer, true),
                position.x, -position.y);
    }

    public void setMoveDirection(Direction d) {
        if (this.moveDirection == null && d != lookDirection) {
            this.animationTimer += Constants.FRAME * 11 % Constants.FRAME * 24;
        }
        this.moveDirection = d;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public int getEntityType() {
        return Player.PLAYER_ENTITY_TYPE;
    }

    @Override
    public boolean hasUpdated() {
        return this.hasUpdated;
    }

    public boolean isLocal() {
        return this.socketAddress == null;
    }

    public SocketAddress getSocketAddress() {
        return this.socketAddress;
    }

    public Pokemon[] getParty() {
        return this.party;
    }

    public int getPartyCount() {
        int count = 0;
        for (Pokemon p : this.party) {
            if (p == null) {
                continue;
            }
            count++;
        }
        return count;
    }

    public void pinged() {
        this.lastPing = System.currentTimeMillis();
    }

    public boolean hasTimedOut() {
        if (this.isLocal()) {
            return false;
        }
        return System.currentTimeMillis() - this.lastPing > Constants.PING_TIMEOUT;
    }

    public void load(ByteBuffer buffer) {
        this.tileX = buffer.getInt();
        this.tileY = buffer.getInt();
        this.lookDirection = Direction.values()[buffer.get()];
        this.position.set(this.tileX * 16.0f, this.tileY * 16.0f);
    }

    public void save(ByteBuffer buffer) {
        buffer.putInt(this.tileX).putInt(this.tileY).put((byte) this.lookDirection.ordinal());
    }

    public UUID getPid() {
        return this.pid;
    }

    public int getPidIdx() {
        return this.pidIdx;
    }
}
