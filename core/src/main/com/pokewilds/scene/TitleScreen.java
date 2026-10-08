package com.pokewilds.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.pokewilds.Constants;
import com.pokewilds.PokeWilds;
import com.pokewilds.api.world.gen.WorldType;
import com.pokewilds.util.GameMode;
import com.pokewilds.util.ShadowFont;
import com.pokewilds.util.net.PacketType;
import com.pokewilds.world.WorldSize;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class TitleScreen extends Scene {
    public static int MAIN_MENU = 0, WORLD_SELECT = 1, NEW_WORLD = 2, MULTIPLAYER_MENU = 3, LAN_MENU = 4, DIRECT_CONNECT = 5, PROFILE_MENU = 6;

    private final PokeWilds game;
    private final Texture bgTexture, btnTex, btnSelTex, texTitles;
    private final TextureRegion texOption, texOptionSel, texBack, texBackSel, texCont, texContSel;
    private final TextureRegion texProfile, texProfileSel;
    private final NinePatch btn, btnSel, npInfo;
    private final GlyphLayout glyphLayout;
    private final List<String> options;
    private final List<FileHandle> worldHandles;
    private final List<UUID> uuids;
    private final List<WorldType> worldTypes;
    private final Map<UUID, GameSession> gameSessions;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final ShadowFont font;
    private int index;
    private int menuIdx;
    private boolean isCheckingLAN;
    private float responseTimer;
    private boolean usingGamepad;
    private float cursor;

    private String worldName;
    private WorldSize worldSize;
    private GameMode gameMode;

    public TitleScreen(final PokeWilds game) {
        this.game = game;
        this.font = this.game.getMainFont();
        this.glyphLayout = this.font.createLayout("NEW WORLD");
        this.bgTexture = game.getTexture("titlebg.png");
        this.bgTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        this.btnTex = game.getTexture("titlebox.png");
        this.btnSelTex = game.getTexture("titleboxsel.png");
        this.texTitles = game.getTexture("title.png");
        this.texOption = new TextureRegion(this.texTitles, 0, 22, 240, 22);
        this.texOptionSel = new TextureRegion(this.texTitles, 0, 0, 240, 22);
        this.texBack = new TextureRegion(this.texTitles, 0, 68, 70, 24);
        this.texBackSel = new TextureRegion(this.texTitles, 70, 68, 70, 24);
        this.texCont = new TextureRegion(this.texTitles, 0, 44, 70, 24);
        this.texContSel = new TextureRegion(this.texTitles, 70, 44, 70, 24);
        this.texProfile = new TextureRegion(this.texTitles, 223, 44, 16, 16);
        this.texProfileSel = new TextureRegion(this.texTitles, 223, 60, 16, 16);
        this.btn = new NinePatch(this.btnTex, 13, 13, 13, 13);
        this.btnSel = new NinePatch(this.btnSelTex, 13, 13, 13, 13);
        this.npInfo = new NinePatch(new TextureRegion(this.texTitles, 204, 44, 18, 18), 6, 6, 6, 6);
        float scale = Constants.getScale(Gdx.graphics.getHeight());
        this.camera = new OrthographicCamera(Gdx.graphics.getWidth() / scale, Gdx.graphics.getHeight() / scale);
        this.camera.position.x = this.camera.viewportWidth * 0.5f;
        this.camera.position.y = this.camera.viewportHeight * 0.5f;
        this.camera.update();
        this.batch = new SpriteBatch();

        this.worldTypes = WorldType.getWorldTypes();
        this.gameSessions = new HashMap<>();
        this.options = new ArrayList<>();
        this.worldHandles = new ArrayList<>();
        this.uuids = new ArrayList<>();
        this.buildMainMenu();
        this.usingGamepad = true;
        this.worldName = "";
        this.worldSize = WorldSize.TINY;
        this.gameMode = GameMode.ORIGIN;
    }

    @Override
    public void update(float deltaTime) {
        cursor += deltaTime;
        while (cursor >= 1.0f) {
            cursor -= 1.0f;
        }
        if (this.isCheckingLAN && this.menuIdx == LAN_MENU) {
            try {
                DatagramSocket s = PokeWilds.getLANSocket();
                if (s == null) {
                    this.isCheckingLAN = false;
                    this.buildMultiplayerMenu();
                    return;
                }
                DatagramPacket p = new DatagramPacket(new byte[1024], 1024);
                s.receive(p);
                ByteBuffer data = ByteBuffer.wrap(p.getData(), 0, p.getLength());
                if (p.getData()[0] == PacketType.LAN_RESPONSE_PACKET.id() && p.getData()[1] == p.getLength()) {
                    data.position(5);
                    UUID id = new UUID(data.getLong(), data.getLong());
                    if (!this.gameSessions.containsKey(id)) {
                        String name = "";
                        char c;
                        int i = 21;
                        while ((c = ((char) p.getData()[i++])) != '\0') {
                            name += c;
                        }
                        GameSession session = new GameSession();
                        session.address = p.getAddress();
                        this.uuids.add(id);
                        this.gameSessions.put(id, session);
                        this.options.add(this.options.size() - 2, name);
                    }
                }
            } catch (SocketTimeoutException ignored) {
            } catch (IOException e) {
                Gdx.app.error("PokeRancher", "Unable to read LAN socket: " + e.getMessage());
            }

            responseTimer += deltaTime;
            if (responseTimer >= 5.0f) {
                isCheckingLAN = false;
                PokeWilds.closeLANSocket();
            }
        }
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1.0f);

        this.camera.update();
        batch.begin();
        batch.setProjectionMatrix(camera.combined);
        for (int x = 0; x < camera.viewportWidth / 240; x++) {
            for (int y = 0; y < camera.viewportHeight / 240; y++) {
                batch.draw(this.bgTexture, x * 240f, y * 240f);
            }
        }
        if (this.menuIdx == NEW_WORLD) {
            this.drawWorldOption("WORLD NAME", worldName + (cursor > 0.5f && index == 0 ? "|" : ""), (camera.viewportWidth - texOption.getRegionWidth()) / 2f, 0, index == 0);
            this.drawWorldOption("WORLD SIZE", this.worldSize.getName(), (camera.viewportWidth - texOption.getRegionWidth()) / 2f, 22f, index == 1);
            this.drawWorldOption("WORLD TYPE", this.worldTypes.get(0).getName(), (camera.viewportWidth - texOption.getRegionWidth()) / 2f, 44f, index == 2);
            this.drawWorldOption("GAME MODE", this.gameMode.toString(), (camera.viewportWidth - texOption.getRegionWidth()) / 2f, 66f, index == 3);
            this.drawWorldOption("CHARACTER LOCK", "ON", (camera.viewportWidth - texOption.getRegionWidth()) / 2f, 88f, index == 4);
            batch.draw(this.index == 5 ? this.texContSel : this.texCont, (camera.viewportWidth - 40.0f) / 2f, camera.viewportHeight - 134.0f);
            batch.draw(this.index == 6 ? this.texBackSel : this.texBack, (camera.viewportWidth + 100.0f) / 2f, camera.viewportHeight - 134.0f);
            this.font.draw(batch, "CANCEL", (camera.viewportWidth + 118.0f) / 2f, camera.viewportHeight - 118.0f);
            this.font.draw(batch, "CREATE", (camera.viewportWidth - 22.0f) / 2f, camera.viewportHeight - 118.0f);
            this.npInfo.draw(batch, (camera.viewportWidth - texOption.getRegionWidth()) / 2f, camera.viewportHeight - 46.0f - 134.0f, texOption.getRegionWidth(), 46.0f);
            String text = "";
            if (index == 0) {
                text = "The name of your world.";
            } else if (index == 1) {
                text = "The size of your world.\nWorlds are finite.";
            } else if (index == 2) {
                text = "The type of world to generate.";
            } else if (index == 3) {
                text = this.gameMode.getDescription();
            } else if (index == 4) {
                // text = "Character's can be used from other worlds.";
                text = "The character in this world will be only for\nthis world.";
            }
            this.font.draw(batch, text, (camera.viewportWidth - 228.0f) / 2f, camera.viewportHeight - 146.0f);
        } else {
            if (this.menuIdx == TitleScreen.MAIN_MENU) {
                if (this.index == this.options.size()) {
                    batch.draw(this.texProfileSel, camera.viewportWidth - 22.f, camera.viewportHeight - 22f);
                } else {
                    batch.draw(this.texProfile, camera.viewportWidth - 22.f, camera.viewportHeight - 22f);
                }
            }
            float y = camera.viewportHeight - 32.0f;
            for (int i = 0; i < options.size(); i++) {
                this.drawButton(options.get(i), camera.viewportWidth / 2f, y, index == i);
                y -= 32.0f;
            }
        }
        batch.end();
    }

    @Override
    public void resize(final int width, final int height) {
        float scale = Constants.getScale(height);
        this.camera.viewportWidth = width / scale;
        this.camera.viewportHeight = height / scale;
        this.camera.position.x = this.camera.viewportWidth * 0.5f;
        this.camera.position.y = this.camera.viewportHeight * 0.5f;
        this.camera.update();
    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean keyTyped(char character) {
        if (menuIdx == NEW_WORLD && index == 0) {
            if (this.worldName.length() > 0 && character == '\b') {
                this.worldName = this.worldName.substring(0, this.worldName.length() - 1);
                return true;
            }
            if (!Character.isLetterOrDigit(character) && character != ' ') {
                return false;
            }
            if (this.worldName.length() < 12) {
                this.worldName += character;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyDown(int keyCode) {
        if (menuIdx == NEW_WORLD) {
            if ((keyCode == Input.Keys.UP) && this.index > 0) {
                if (this.index == 6) {
                    this.index--;
                }
                this.index--;
                return true;
            } else if ((keyCode == Input.Keys.DOWN) && this.index < 5) {
                this.index++;
                return true;
            } else if ((keyCode == Input.Keys.RIGHT) && this.index == 5) {
                this.index++;
                return true;
            } else if ((keyCode == Input.Keys.LEFT) && this.index == 6) {
                this.index--;
                return true;
            } else if ((keyCode == Input.Keys.Z)) {
                if (this.index == 1) {
                    this.worldSize = WorldSize.values()[(this.worldSize.ordinal() + 1) % WorldSize.values().length];
                    return true;
                } else if (this.index == 3) {
                    this.gameMode = GameMode.values()[(this.gameMode.ordinal() + 1) % GameMode.values().length];
                    return true;
                } else if (this.index == 5 && this.worldName.length() > 0) {
                    this.game.setScreen(new HostGameScene(this.game, this.worldName, this.worldSize));
                } else if (this.index == 6) {
                    this.buildWorldSelectMenu();
                    return true;
                }
            }
            return false;
        }
        if ((keyCode == Input.Keys.W || keyCode == Input.Keys.UP) && this.index > 0) {
            this.index--;
            return true;
        } else if ((keyCode == Input.Keys.S || keyCode == Input.Keys.DOWN) && this.index < options.size() - 1) {
            this.index++;
            return true;
        } else if ((keyCode == Input.Keys.D || keyCode == Input.Keys.RIGHT) && this.index >= 0 && this.index < options.size()) {
            if (this.menuIdx == TitleScreen.MAIN_MENU) {
                this.index = options.size();
            }
            return true;
        } else if ((keyCode == Input.Keys.A || keyCode == Input.Keys.LEFT) && this.index == options.size()) {
            this.index = 0;
            return true;
        } else if (keyCode == Input.Keys.ENTER || keyCode == Input.Keys.E || keyCode == Input.Keys.Z) {
            if (this.index < 0 || this.index >= this.options.size() + 1) {
                return true;
            }
            String opt = null;
            if (this.index < this.options.size()) {
                opt = this.options.get(this.index);
            }
            if (menuIdx == MAIN_MENU) {
                if (this.index == this.options.size()) {
                    System.out.println("PROFILES!");
                    return true;
                }
                if (opt == null) {
                    return true;
                }
                if (opt.equalsIgnoreCase("SINGLEPLAYER")) {
                    this.buildWorldSelectMenu();
                    return true;
                } else if (opt.equalsIgnoreCase("MULTIPLAYER")) {
                    this.index = 0;
                    this.buildMultiplayerMenu();
                    return true;
                } else if (opt.equalsIgnoreCase("QUIT")) {
                    Gdx.app.exit();
                    return true;
                }
            } else if (menuIdx == WORLD_SELECT) {
                if (this.index == this.options.size() - 2) {
                    this.worldName = "";
                    this.worldSize = WorldSize.TINY;
                    this.gameMode = GameMode.ORIGIN;
                    this.menuIdx = NEW_WORLD;
                    this.index = 0;
                    this.options.clear();
                    return true;
                } else if (this.index == this.options.size() - 1) {
                    this.index = 0;
                    this.buildMainMenu();
                    return true;
                } else {
                    game.setScreen(new HostGameScene(game, this.worldHandles.get(this.index)));
                }
            } else if (menuIdx == MULTIPLAYER_MENU) {
                if (opt.equalsIgnoreCase("LAN")) {
                    this.menuIdx = LAN_MENU;
                    this.startLANCheck();
                } else if (opt.equalsIgnoreCase("BACK")) {
                    this.index = 1;
                    this.buildMainMenu();
                    return true;
                }
            } else if (menuIdx == LAN_MENU) {
                if (opt.equalsIgnoreCase("REFRESH")) {
                    if (PokeWilds.getLANSocket() != null) {
                        return true;
                    }
                    this.startLANCheck();
                } else if (opt.equalsIgnoreCase("BACK")) {
                    this.index = 0;
                    PokeWilds.closeLANSocket();
                    this.buildMultiplayerMenu();
                } else {
                    try {
                        PokeWilds.closeLANSocket();
                        game.setScreen(new ClientGameScene(this.game, this.gameSessions.get(this.uuids.get(this.index)).address));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        this.usingGamepad = true;
        int aButton = Input.Keys.Z;
        int bButton = Input.Keys.X;
        if (controller.getName().equals("Nintendo Switch Pro Controller")) {
            aButton = Input.Keys.X;
            bButton = Input.Keys.Z;
        }
        switch (buttonCode) {
            case 11:
                return this.keyDown(Input.Keys.UP);
            case 12:
                return this.keyDown(Input.Keys.DOWN);
            case 13:
                return this.keyDown(Input.Keys.LEFT);
            case 14:
                return this.keyDown(Input.Keys.RIGHT);
            case 0:
                return this.keyDown(aButton);
            case 1:
                return this.keyDown(bButton);
            default:
                System.out.println(buttonCode);
                return false;
        }
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        this.usingGamepad = false;
        float scaling = Constants.getScale(Gdx.graphics.getHeight());
        screenX = (int) ((float) screenX / scaling);
        screenY = (int) ((float) screenY / scaling);
        final Rectangle rect = new Rectangle(0.0f, 0.0f, 160.f, 26.0f);
        if (this.menuIdx == NEW_WORLD) {
            rect.set((camera.viewportWidth - texOption.getRegionWidth()) / 2f, 0.0f, texOption.getRegionWidth(), texOption.getRegionHeight());
            if (rect.contains(screenX, screenY)) {
                this.index = 0;
                return true;
            }
            rect.set((camera.viewportWidth - texOption.getRegionWidth()) / 2f, 22.0f, texOption.getRegionWidth(), texOption.getRegionHeight());
            if (rect.contains(screenX, screenY)) {
                this.index = 1;
                return true;
            }
            rect.set((camera.viewportWidth - texOption.getRegionWidth()) / 2f, 44.0f, texOption.getRegionWidth(), texOption.getRegionHeight());
            if (rect.contains(screenX, screenY)) {
                this.index = 2;
                return true;
            }
            rect.set((camera.viewportWidth - texOption.getRegionWidth()) / 2f, 66.0f, texOption.getRegionWidth(), texOption.getRegionHeight());
            if (rect.contains(screenX, screenY)) {
                this.index = 3;
                return true;
            }
            rect.set((camera.viewportWidth - texOption.getRegionWidth()) / 2f, 88.0f, texOption.getRegionWidth(), texOption.getRegionHeight());
            if (rect.contains(screenX, screenY)) {
                this.index = 4;
                return true;
            }
            rect.set((camera.viewportWidth - 40.0f) / 2f, 110.0f, texCont.getRegionWidth(), texCont.getRegionHeight());
            if (rect.contains(screenX, screenY)) {
                this.index = 5;
                return true;
            }
            rect.set((camera.viewportWidth + 98.0f) / 2f, 110.0f, texCont.getRegionWidth(), texCont.getRegionHeight());
            if (rect.contains(screenX, screenY)) {
                this.index = 6;
                return true;
            }
            this.index = -1;
            return true;
        }
        this.index = -1;
        for (int i = 0; i < this.options.size(); i++) {
            rect.x = (camera.viewportWidth / 2f) - 80.0f;
            rect.y = (32.0f * (i + 1)) - 13.0f;
            if (rect.contains(screenX, screenY)) {
                this.index = i;
            }
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float scaling = Constants.getScale(Gdx.graphics.getHeight());
        screenX = (int) ((float) screenX / scaling);
        screenY = (int) ((float) screenY / scaling);
        final Rectangle rect = new Rectangle(0.0f, 0.0f, 160.f, 26.0f);
        if (this.menuIdx == NEW_WORLD) {
            rect.set((camera.viewportWidth - texOption.getRegionWidth()) / 2f, 0.0f, texOption.getRegionWidth(), texOption.getRegionHeight());
            if (rect.contains(screenX, screenY)) {
                this.index = 0;
                return true;
            }
            rect.set((camera.viewportWidth - texOption.getRegionWidth()) / 2f, 22.0f, texOption.getRegionWidth(), texOption.getRegionHeight());
            if (rect.contains(screenX, screenY)) {
                this.index = 1;
                this.keyDown(Input.Keys.Z);
                return true;
            }
            rect.set((camera.viewportWidth - texOption.getRegionWidth()) / 2f, 66.0f, texOption.getRegionWidth(), texOption.getRegionHeight());
            if (rect.contains(screenX, screenY)) {
                this.index = 3;
                this.keyDown(Input.Keys.Z);
                return true;
            }
            rect.set((camera.viewportWidth - texOption.getRegionWidth()) / 2f, 88.0f, texOption.getRegionWidth(), texOption.getRegionHeight());
            if (rect.contains(screenX, screenY)) {
                this.index = 4;
                return true;
            }
            rect.set((camera.viewportWidth - 40.0f) / 2f, 110.0f, texCont.getRegionWidth(), texCont.getRegionHeight());
            if (rect.contains(screenX, screenY) && this.worldName.length() > 0) {
                this.game.setScreen(new HostGameScene(this.game, this.worldName, this.worldSize));
                return true;
            }
            rect.set((camera.viewportWidth + 98.0f) / 2f, 110.0f, texCont.getRegionWidth(), texCont.getRegionHeight());
            if (rect.contains(screenX, screenY)) {
                this.buildWorldSelectMenu();
                return true;
            }
            return true;
        }
        this.index = -1;
        for (int i = 0; i < this.options.size(); i++) {
            rect.x = (camera.viewportWidth / 2f) - 80.0f;
            rect.y = (32.0f * (i + 1)) - 13.0f;
            if (rect.contains(screenX, screenY)) {
                this.index = i;
                this.keyDown(Input.Keys.ENTER);
                return true;
            }
        }
        return false;
    }

    private void drawButton(String text, float x, float y, boolean isActive) {
        this.glyphLayout.setText(this.font.getPrimaryFont(), text);
        NinePatch np = isActive ? this.btnSel : this.btn;
        np.draw(batch, x - 80.0f, y - 13f, 160.0f, 26.0f);
        font.draw(batch, text, x - (this.glyphLayout.width / 2f), y + 4f);
    }

    private void drawWorldOption(String key, String val, float x, float y, boolean isActive) {
        batch.draw(isActive ? this.texOptionSel : this.texOption, x, camera.viewportHeight - y - 22f);
        font.draw(batch, key, x + 6f, camera.viewportHeight - y - 9f);
        font.draw(batch, val, x + 114, camera.viewportHeight - y - 9f);
    }

    private void startLANCheck() {
        this.gameSessions.clear();
        this.uuids.clear();
        this.options.clear();
        this.options.add("REFRESH");
        this.options.add("BACK");
        new Thread(this::checkLan).start();
        this.isCheckingLAN = true;
        this.responseTimer = 0.0f;
    }

    private void checkLan() {
        try {
            PokeWilds.createLANSocket(0);
            DatagramSocket s = PokeWilds.getLANSocket();
            s.setSoTimeout(1000);
            Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
            while (ifs.hasMoreElements()) {
                NetworkInterface ni = ifs.nextElement();
                if (ni.isLoopback()) {
                    continue;
                }
                for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
                    if (interfaceAddress.getBroadcast() == null) {
                        continue;
                    }
                    try {
                        DatagramPacket p = new DatagramPacket(new byte[]{PacketType.LAN_REQUEST_PACKET.id()}, 1);
                        p.setAddress(interfaceAddress.getBroadcast());
                        p.setPort(5173);
                        s.send(p);
                    } catch (IOException e) {
                        Gdx.app.error("PokeRancher", "Unable to create LAN socket(" + ni.getName() + "): " + e.getMessage());
                    }
                }
            }
            s.setSoTimeout(10);
        } catch (IOException e) {
            Gdx.app.error("PokeRancher", "Unable to create LAN socket: " + e.getMessage());
        }
    }

    private void buildMainMenu() {
        this.menuIdx = MAIN_MENU;
        this.options.clear();
        this.options.add("SINGLEPLAYER");
        this.options.add("MULTIPLAYER");
        this.options.add("SETTINGS");
        this.options.add("QUIT");
    }

    private void buildWorldSelectMenu() {
        this.index = 0;
        this.menuIdx = WORLD_SELECT;
        this.options.clear();

        for (FileHandle f : Gdx.files.local("saves").list()) {
            if (!f.isDirectory() || !f.child("world.dat").exists()) {
                continue;
            }
            try {
                FileHandle worldDat = f.child("world.dat");
                byte[] b = worldDat.readBytes();
                StringBuilder name = new StringBuilder();
                char c;
                int i = 6;
                while ((c = (char) b[i++]) != '\0') {
                    name.append(c);
                }
                this.worldHandles.add(f);
                this.options.add(name.toString());
            } catch (Exception e) {
                Gdx.app.log("PokeRancher", "Unable to load world file for", e);
            }
        }

        this.options.add("NEW WORLD");
        this.options.add("BACK");
    }

    private void buildMultiplayerMenu() {
        this.menuIdx = MULTIPLAYER_MENU;
        this.options.clear();
        this.options.add("LAN");
        this.options.add("DIRECT");
        this.options.add("BACK");
    }

    private static class GameSession {
        public InetAddress address;
        public String name;
    }
}
