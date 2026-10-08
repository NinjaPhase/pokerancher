package com.pokewilds.world;

/**
 * <p>
 * A simple class to represent a coordinate in the world.
 * </p>
 */
public class Coordinate {
    public int x, y;

    public Coordinate() {
        this(0, 0);
    }

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void min(int minX, int minY) {
        this.x = Math.min(this.x, minX);
        this.y = Math.min(this.y, minY);
    }

    public void max(int maxX, int maxY) {
        this.x = Math.max(this.x, maxX);
        this.y = Math.max(this.y, maxY);
    }

    public void clamp(int minX, int minY, int maxX, int maxY) {
        this.x = Math.min(Math.max(this.x, minX), maxX);
        this.y = Math.min(Math.max(this.y, minY), maxY);
    }
}
