package net.terrunic.shadowdrop;

// Record to track a pixel and if it should be considered a slot corner
public record CachedPixel(int x, int y, int z, boolean isSlotCorner) {}
