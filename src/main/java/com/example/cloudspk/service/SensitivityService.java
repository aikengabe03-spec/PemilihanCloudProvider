package com.example.cloudspk.service;

import com.example.cloudspk.model.Scenario;
import com.example.cloudspk.model.Vendor;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SensitivityService {
    private final SawService saw;

    public SensitivityService(SawService saw) { this.saw = saw; }

    public List<Scenario> run(double[] base, List<Vendor> vendors, boolean[] isCost) {
        Object[][] defs = {
            {"Dasar (hasil AHP)", base},
            {"Biaya dominan", new double[]{.40, .15, .20, .15, .10}},
            {"Bobot sama rata", new double[]{.2, .2, .2, .2, .2}},
            {"Keandalan & performa dominan", new double[]{.10, .30, .20, .30, .10}}};
        List<Scenario> out = new ArrayList<>();
        for (Object[] d : defs) {
            double[] w = (double[]) d[1];
            out.add(new Scenario((String) d[0], w, saw.calculate(w, vendors, isCost).order()));
        }
        return out;
    }
}
