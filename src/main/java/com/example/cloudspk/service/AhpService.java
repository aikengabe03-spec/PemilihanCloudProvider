package com.example.cloudspk.service;

import com.example.cloudspk.model.AhpResult;
import org.springframework.stereotype.Service;

@Service
public class AhpService {
    private static final double[] RI = {0.0, 0.0, 0.58, 0.90, 1.12, 1.24, 1.32, 1.41, 1.45, 1.49};

    public AhpResult calculate(double[][] a) {
        int n = a.length;
        double[] cs = new double[n];
        for (int j = 0; j < n; j++) for (int i = 0; i < n; i++) cs[j] += a[i][j];

        double[][] norm = new double[n][n];
        double[] w = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                norm[i][j] = a[i][j] / cs[j];
                w[i] += norm[i][j] / n;
            }
        }
        double[] vjt = new double[n], vk = new double[n];
        double lambda = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) vjt[i] += a[i][j] * w[j];
            vk[i] = vjt[i] / w[i];
            lambda += vk[i] / n;
        }
        double ci = (lambda - n) / (n - 1);
        double cr = RI[n - 1] == 0 ? 0 : ci / RI[n - 1];
        return new AhpResult(cs, norm, w, vjt, vk, lambda, ci, cr, cr <= 0.10);
    }
}
