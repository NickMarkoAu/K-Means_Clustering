package com;

import com.clustering.Centroid;
import com.clustering.DistanceImpl;
import com.clustering.KMeans;
import com.clustering.Record;
import com.file.CsvUtil;
import com.univocity.parsers.csv.CsvParserSettings;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class Main {

    public static void main(String[] args) {
        String fileName = "test-file.csv";
        CsvUtil csvUtil = new CsvUtil(new CsvParserSettings());
        List<String[]> rowStream = csvUtil.readStream(fileName).collect(Collectors.toList());
        List<Record> recordList = Record.fromCsv(rowStream.stream());
        Map<Centroid, List<Record>> kmeans = KMeans.fit(recordList, CsvUtil.columns(rowStream.stream()), new DistanceImpl(), 150, "test");
        kmeans.forEach((k,v) -> {
            log.info("Centroid {} contains records {}", k, v);
        });
    }
}
