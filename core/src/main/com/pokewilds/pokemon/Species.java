package com.pokewilds.pokemon;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.pokewilds.util.TextureManager;

public class Species {
    private String name;
    public Form defaultForm;

    private static JsonValue find(JsonValue json, String key) {
        String[] pathParts = key.split("\\.");
        for (String p : pathParts) {
            if (p.startsWith("[") && p.endsWith("]")) {
                int index = Integer.parseInt(p.substring(1, p.length() - 1));
                json = json.get(index);
                continue;
            } else {
                json = json.get(p);
            }
            if (json.isNull()) {
                return null;
            }
        }
        return json;
    }

    private static Texture getTexture(TextureManager textureManager, JsonValue json, String key) {
        JsonValue jsonValue = find(json, key);
        if (jsonValue == null) {
            return null;
        }
        String iconFile = jsonValue.asString();
        return textureManager.getTexture(iconFile);
    }

    private static TextureAtlas.AtlasRegion getTextureAtlas(TextureManager textureManager, JsonValue json, String key) {
        JsonValue jsonValue = find(json, key);
        if (jsonValue == null) {
            return null;
        }
        String iconFile = jsonValue.asString();
        if (iconFile.contains("$")) {
            String[] iconFileSpl = iconFile.split("\\$");
            TextureAtlas atlas = textureManager.getAtlas(iconFileSpl[0]);
            int index = 0;
            try {
                String[] atlasNameParts = iconFileSpl[1].split("_");
                index = Integer.parseInt(atlasNameParts[atlasNameParts.length - 1]);
                StringBuilder str = new StringBuilder();
                for (int i = 0; i < atlasNameParts.length - 1; i++) {
                    str.append(atlasNameParts[i]);
                    if (i < atlasNameParts.length - 2) {
                        str.append("_");
                    }
                }
                iconFileSpl[1] = str.toString();
            } catch (NumberFormatException ignored) {
            }
            return atlas.findRegion(iconFileSpl[1], index);

        }
        return null;
    }

    public Species(TextureManager textureManager, JsonValue data) {
        JsonValue formsJson = data.get("forms");
        for (JsonValue formJson : formsJson) {
            Form form = new Form();
            if (this.defaultForm == null) {
                this.defaultForm = form;
            }
            if (Gdx.app.getType() == Application.ApplicationType.HeadlessDesktop) {
                continue;
            }
            Texture tex = getTexture(textureManager, formJson, "images.normal.front.image");
            TextureAtlas.AtlasRegion normal = getTextureAtlas(textureManager, formJson, "images.normal.icon.[0]");
            form.iconNormalOffset[0] = new Vector2(normal.offsetX, normal.offsetY);
            form.iconNormal[0] = normal;
            normal = getTextureAtlas(textureManager, formJson, "images.normal.icon.[1]");
            form.iconNormalOffset[1] = new Vector2(normal.offsetX, normal.offsetY);
            form.iconNormal[1] = normal;
        }
    }

    public String getName() {
        return this.name;
    }

    public static class Form {
        public Vector2 frontOffset, backOffset;
        public Texture front, back;
        public Vector2[] iconNormalOffset = new Vector2[2];
        public TextureRegion[] iconNormal = new TextureRegion[2], iconShiny;
    }

}
