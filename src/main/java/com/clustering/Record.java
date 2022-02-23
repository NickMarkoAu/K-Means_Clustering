package com.clustering;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Record {
    private final String value;
    private Integer distance;

    public Record(String value, Integer distance) {
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

    public static List<Record> calculateDistanceFromString(List<Record> recordList, String baseString, Distance distance) {
        for (Record record : recordList) {
            record.setDistance(distance.calculate(record.getValue(), baseString));
        }
        return recordList;
    }

    public static List<Record> fromCsv(Stream<String[]> rowStream) {
        return rowStream
                .flatMap(row -> {
                    return Arrays.stream(row).map(col -> {
                        return Objects.isNull(col) ? new Record("", 0) : new Record(col, 0);
                    });
                }).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Record{" +
                ", value='" + value + '\'' +
                ", distance='" + distance + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Record record = (Record) o;
        return Objects.equals(value, record.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, distance);
    }
}
