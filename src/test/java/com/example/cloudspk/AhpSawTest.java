package com.example.cloudspk;

import static org.junit.jupiter.api.Assertions.*;

import com.example.cloudspk.model.*;
import com.example.cloudspk.service.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class AhpSawTest {
    private final AhpService ahp = new AhpService();
    private final SawService saw = new SawService();
    private final List<Vendor> vendors = EvaluationState.defaultVendors();
    private final boolean[] cost = EvaluationState.IS_COST;

    @Test
    void bobotDanKonsistensi() {
        AhpResult r = ahp.calculate(EvaluationState.defaultMatrix());
        double[] w = {0.1215, 0.2147, 0.3751, 0.2147, 0.0741};
        for (int i = 0; i < 5; i++) assertEquals(w[i], r.weights()[i], 1e-4);
        assertEquals(5.0332, r.lambdaMax(), 1e-4);
        assertEquals(0.0083, r.ci(), 1e-4);
        assertEquals(0.0074, r.cr(), 1e-4);
        assertTrue(r.consistent());
    }

    @Test
    void matriksTidakKonsistenMenghasilkanCrTinggi() {
        double[][] a = {{1, 9, 1 / 9.0}, {1 / 9.0, 1, 9}, {9, 1 / 9.0, 1}};
        assertTrue(ahp.calculate(a).cr() > 0.10);
    }

    @Test
    void peringkatSaw() {
        double[] w = ahp.calculate(EvaluationState.defaultMatrix()).weights();
        SawResult s = saw.calculate(w, vendors, cost);
        double[] v = {0.9488, 0.9050, 0.9121, 0.8017, 0.7332}; // AWS, Azure, Google, Alibaba, Biznet
        for (int i = 0; i < 5; i++) assertEquals(v[i], s.scores()[i], 1e-4);
        assertEquals(0, s.order()[0]); // AWS
        assertEquals(2, s.order()[1]); // Google Cloud
    }

    @Test
    void biayaDominanMengubahPeringkatPertama() {
        double[] w = ahp.calculate(EvaluationState.defaultMatrix()).weights();
        var sc = new SensitivityService(saw).run(w, vendors, cost).get(1);
        assertArrayEquals(new int[]{2, 0, 4, 1, 3}, sc.order()); // Google, AWS, Biznet, Azure, Alibaba
    }
}
