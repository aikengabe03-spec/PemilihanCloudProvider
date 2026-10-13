package com.example.cloudspk.model;

/** Alternatif vendor. x[0] = biaya USD/bulan (cost), x[1..4] = skala 1-10 (benefit). */
public record Vendor(String name, double[] x) {}
