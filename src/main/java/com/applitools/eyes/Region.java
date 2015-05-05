package com.applitools.eyes;

import com.applitools.utils.ArgumentGuard;
import com.fasterxml.jackson.annotation.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a region.
 */
@JsonIgnoreProperties({"location" , "empty"})
public class Region {
    private int left;
    private int top;
    private int width;
    private int height;

    public static final Region EMPTY = new Region(0, 0, 0, 0);

    protected void makeEmpty() {
        left = EMPTY.getLeft();
        top = EMPTY.getTop();
        width = EMPTY.getWidth();
        height = EMPTY.getHeight();
    }

    public Region(int left, int top, int width, int height) {
        ArgumentGuard.greaterThanOrEqualToZero(width, "width");
        ArgumentGuard.greaterThanOrEqualToZero(height, "height");

        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    /**
     *
     * @return true if the region is empty, false otherwise.
     */
    public boolean isEmpty() {
        return this.getLeft() == EMPTY.getLeft()
                && this.getTop() == EMPTY .getTop()
                && this.getWidth() == EMPTY.getWidth()
                && this.getHeight() == EMPTY.getHeight();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Region)) {
            return  false;
        }
        Region other = (Region) obj;

        return (this.getLeft() == other.getLeft())
                && (this.getTop() == other.getTop())
                && (this.getWidth() == other.getWidth())
                && (this.getHeight() == other.getHeight());
    }

    @Override
    public int hashCode() {
        return (left + top + width + height);
    }

    public Region(Location location, RectangleSize size) {
        ArgumentGuard.notNull(location, "location");
        ArgumentGuard.notNull(size, "size");

        left = location.getX();
        top = location.getY();
        width = size.getWidth();
        height = size.getHeight();
    }

    public Region(Region other) {
        ArgumentGuard.notNull(other, "other");

        left = other.getLeft();
        top = other.getTop();
        width = other.getWidth();
        height = other.getHeight();
    }

    /**
     *
     * @return The (top,left) position of the current region.
     */
    public Location getLocation() {
        return new Location(left, top);
    }

    /**
     * Offsets the region's location (in place).
     *
     * @param dx The X axis offset.
     * @param dy The Y axis offset.
     */
    public void offset(int dx, int dy) {
        left += dx;
        top += dy;
    }

    /**
     *
     * @return The (top,left) position of the current region.
     */
    public RectangleSize getSize() {
        return new RectangleSize(width, height);
    }

    /**
     * Set the (top,left) position of the current region
     * @param location The (top,left) position to set.
     */
    public void setLocation(Location location) {
        ArgumentGuard.notNull(location, "location");
        left = location.getX();
        top = location.getY();
    }

    /**
     *
     * @param maxSubRegionSize The maximum size of each sub-region (some
     *                         regions might be smaller).
     * @return The sub-regions composing the current region. If
     * maxSubRegionSize is equal or greater than the current region,
     * only a single region is returned.
     */
    public Iterable<Region> getSubRegions(RectangleSize maxSubRegionSize) {
        ArgumentGuard.notNull(maxSubRegionSize, "maxSubRegionSize");

        List<Region> subRegions = new LinkedList<Region>();

        int currentTop = top;
        int bottom = top + height;
        int right = left + width;

        while (currentTop < bottom) {

            int currentBottom = currentTop + maxSubRegionSize.getHeight();
            if (currentBottom > bottom) { currentBottom = bottom; }

            int currentLeft = left;
            while (currentLeft < right) {
                int currentRight = currentLeft + maxSubRegionSize.getWidth();
                if (currentRight > right) { currentRight = right; }

                int currentHeight = currentBottom - currentTop;
                int currentWidth = currentRight - currentLeft;

                subRegions.add(new Region(currentLeft, currentTop,
                        currentWidth, currentHeight));

                currentLeft += maxSubRegionSize.getWidth();
            }
            currentTop += maxSubRegionSize.getHeight();
        }
        return subRegions;
    }

    /**
     * Check if a region is contained within the current region.
     * @param other The region to check if it is contained within the current
     *              region.
     * @return True if {@code other} is contained within the current region,
     *          false otherwise.
     */
    @SuppressWarnings("UnusedDeclaration")
    public boolean contains(Region other) {
        int right = left + width;
        int otherRight = other.getLeft() + other.getWidth();

        int bottom = top + height;
        int otherBottom = other.getTop() + other.getHeight();

        return top <= other.getTop() && left <= other.getLeft()
                && bottom >= otherBottom && right >= otherRight;
    }

    /**
     * Check if a specified location is contained within this region.
     * <p>
     * @param location The location to test.
     * @return True if the location is contained within this region,
     *          false otherwise.
     */
    public boolean contains(Location location) {
        return location.getX() >= left
                && location.getX() <= (left + width)
                && location.getY() >= top
                && location.getY() <= (top + height);
    }

    /**
     * Check if a region is intersected with the current region.
     * @param other The region to check intersection with.
     * @return True if the regions are intersected, false otherwise.
     */
    public boolean isIntersected(Region other) {
        int right = left + width;
        int bottom = top + height;

        int otherLeft = other.getLeft();
        int otherTop = other.getTop();
        int otherRight = otherLeft + other.getWidth();
        int otherBottom = otherTop + other.getHeight();

        return (((left <= otherLeft && otherLeft <= right)
                    ||  (otherLeft <= left && left <= otherRight))
                && ((top <= otherTop && otherTop <= bottom)
                    ||  (otherTop <= top && top <= otherBottom)));
    }

    /**
     * Replaces this region with the intersection of itself and
     * {@code other}
     * @param other The region with which to intersect.
     */
    public void intersect(Region other) {

        // If there's no intersection set this as the Empty region.
        if (!isIntersected(other)) {
            makeEmpty();
            return;
        }

        // The regions intersect. So let's first find the left & top values
        int otherLeft = other.getLeft();
        int otherTop = other.getTop();

        int intersectionLeft = (left >= otherLeft) ? left : otherLeft;
        int intersectionTop = (top >= otherTop) ? top : otherTop;

        // Now the width and height of the intersect
        int right = left + width;
        int otherRight = otherLeft + other.getWidth();
        int intersectionRight = (right <= otherRight) ? right : otherRight;
        int intersectionWidth = intersectionRight - intersectionLeft;

        int bottom = top + height;
        int otherBottom = otherTop + other.getHeight();
        int intersectionBottom = (bottom <= otherBottom) ? bottom : otherBottom;
        int intersectionHeight = intersectionBottom - intersectionTop;

        left = intersectionLeft;
        top = intersectionTop;
        width = intersectionWidth;
        height = intersectionHeight;

    }


    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @JsonIgnore
    public Location getMiddleOffset() {
        int middleX = width / 2;
        int middleY = height / 2;

        return new Location(middleX, middleY);
    }

    @Override
    public String toString() {
        return "(" + left + ", " + top + ") " + width + "x" + height;
    }
}
