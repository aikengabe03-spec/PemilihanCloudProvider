package com.example.cloudspk.model;

/** r = matriks ternormalisasi, scores = nilai V, order = indeks vendor dari peringkat 1 ke bawah. */
public record SawResult(double[][] r, double[] scores, int[] order) {}
