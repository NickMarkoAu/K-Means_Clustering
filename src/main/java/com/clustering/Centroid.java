package com.clustering;

import java.util.Objects;

public class Centroid {
    private final String value;
    private Integer distance;

    public Centroid(String value, Integer distance) {
        this.value = value;
        this.distance = distance;
    }

    public String getValue() {
        return value;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "Centroid{" +
                "value='" + value + '\'' +
                ", distance=" + distance +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Centroid centroid = (Centroid) o;
        return Objects.equals(value, centroid.value) && Objects.equals(distance, centroid.distance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, distance);
    }
}
