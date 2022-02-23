package com.clustering;


public interface Distance {
   int calculate(Record record, Centroid centroid);
   int calculate(String x, String y);
}
