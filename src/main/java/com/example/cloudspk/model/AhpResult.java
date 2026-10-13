package com.example.cloudspk.model;

public record AhpResult(double[] columnSums, double[][] normalized, double[] weights,
                        double[] vjt, double[] vk, double lambdaMax, double ci, double cr, boolean consistent) {}
