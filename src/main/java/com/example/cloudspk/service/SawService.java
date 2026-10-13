package com.example.cloudspk.service;

import com.example.cloudspk.model.SawResult;
import com.example.cloudspk.model.Vendor;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;

@Service
public class SawService {
    /** Cost: r = min/x. Benefit: r = x/max. Nilai preferensi V = sum(w * r). */
    public SawResult calculate(double[] w, List<Vendor> vendors, boolean[] isCost) {
        int m = vendors.size(), n = w.length;
        double[] ref = new double[n];
        for (int j = 0; j < n; j++) {
            ref[j] = isCost[j] ? Double.MAX_VALUE : 0;
            for (Vendor v : vendors) ref[j] = isCost[j] ? Math.min(ref[j], v.x()[j]) : Math.max(ref[j], v.x()[j]);
        }
        double[][] r = new double[m][n];
        double[] s = new double[m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double x = vendors.get(i).x()[j];
                r[i][j] = isCost[j] ? ref[j] / x : x / ref[j];
                s[i] += w[j] * r[i][j];
            }
        }
        int[] order = IntStream.range(0, m).boxed()
                .sorted(Comparator.comparingDouble((Integer i) -> s[i]).reversed())
                .mapToInt(Integer::intValue).toArray();
        return new SawResult(r, s, order);
    }
}
