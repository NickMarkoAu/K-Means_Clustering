package com.clustering;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
public class KMeans {

    private static final Random random = new Random();

    public static Map<Centroid, List<Record>> fit(List<Record> records, int k, Distance distance, int maxIterations, String baseString) {
        List<Centroid> centroids = randomCentroids(records, k, distance, baseString);
        Map<Centroid, List<Record>> clusters = new HashMap<>();
        Map<Centroid, List<Record>> lastState = new HashMap<>();

        // iterate for a pre-defined number of times
        for (int i = 0; i < maxIterations; i++) {
            boolean isLastIteration = i == maxIterations - 1;

            // in each iteration we should find the nearest centroid for each record
            int recordIndex = 0;
            for (Record record : records) {
                recordIndex++;
                log.info("Record # {}, Iteration # {}", recordIndex, i+1);
                Centroid centroid = nearestCentroid(record, centroids, distance);
                assignToCluster(clusters, record, centroid);
            }

            // if the assignments do not change, then the algorithm terminates
            boolean shouldTerminate = isLastIteration || clusters.equals(lastState);
            lastState = clusters;
            if (shouldTerminate) {
                break;
            }

            // at the end of each iteration we should relocate the centroids
            centroids = relocateCentroids(clusters);
            clusters = new HashMap<>();
        }

        return lastState;
    }

    private static List<Centroid> randomCentroids(List<Record> records, int k, Distance distance, String baseString) {
        log.info("Generating random centroids");

        List<Centroid> centroids = new ArrayList<>();
        Map<String, Integer> maxs = new HashMap<>();
        Map<String, Integer> mins = new HashMap<>();

        Record.calculateDistanceFromString(records, baseString, distance);

        for (Record record : records) {
            // compares the value with the current max and choose the bigger value between them
            maxs.compute(record.getValue(), (k1, max) -> max == null || record.getDistance() > max ? record.getDistance() : max);

            // compare the value with the current min and choose the smaller value between them
            mins.compute(record.getValue(), (k1, min) -> min == null || record.getDistance() < min ? record.getDistance() : min);
        }

        List<String> attributes = records.stream()
                .distinct()
                .map(Record::getValue)
                .collect(Collectors.toList());

        for (int i = 0; i < k; i++) {
            String attribute = attributes.get(random.nextInt((k + 1) - 1) + 1);
            int max = maxs.get(attribute);
            int min = mins.get(attribute);
            centroids.add(new Centroid(attribute, random.nextInt() * (max - min) + min));
        }
        log.info("Random centroids are {}", centroids);
        return centroids;
    }

    private static Centroid nearestCentroid(Record record, List<Centroid> centroids, Distance distance) {
        double minimumDistance = Double.MAX_VALUE;
        Centroid nearest = null;

        for (Centroid centroid : centroids) {
            double currentDistance = distance.calculate(record, centroid);

            if (currentDistance < minimumDistance) {
                minimumDistance = currentDistance;
                nearest = centroid;
            }
        }

        return nearest;
    }

    private static void assignToCluster(Map<Centroid, List<Record>> clusters, Record record, Centroid centroid) {
        clusters.compute(centroid, (key, list) -> {
            if (list == null) {
                list = new ArrayList<>();
            }

            list.add(record);
            return list;
        });
    }

    private static Centroid average(Centroid centroid, List<Record> records) {
        if (records == null || records.isEmpty()) {
            return centroid;
        }

        Centroid average = new Centroid(centroid.getValue(), 0);

        for (Record record : records) {
            average.setDistance(average.getDistance() + record.getDistance());
        }

        average.setDistance(average.getDistance() / records.size());

        return average;
    }

    private static List<Centroid> relocateCentroids(Map<Centroid, List<Record>> clusters) {
        return clusters.entrySet().stream().map(e -> average(e.getKey(), e.getValue())).collect(toList());
    }

}
