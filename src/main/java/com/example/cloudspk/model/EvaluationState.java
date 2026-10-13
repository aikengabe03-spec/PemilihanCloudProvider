package com.example.cloudspk.model;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/** Data kerja per sesi pengguna (disimpan di memori, tanpa database). */
@Component
@SessionScope
public class EvaluationState {
    public static final String[] CODES = {"C1", "C2", "C3", "C4", "C5"};
    public static final String[] NAMES = {"Biaya", "Keandalan & SLA", "Keamanan & Kepatuhan", "Performa & Skalabilitas", "Dukungan & Ekosistem"};
    public static final boolean[] IS_COST = {true, false, false, false, false};

    private double[][] matrix = defaultMatrix();
    private List<Vendor> vendors = defaultVendors();

    public static double[][] defaultMatrix() {
        return new double[][]{
            {1, 1 / 2.0, 1 / 3.0, 1 / 2.0, 2},
            {2, 1, 1 / 2.0, 1, 3},
            {3, 2, 1, 2, 4},
            {2, 1, 1 / 2.0, 1, 3},
            {1 / 2.0, 1 / 3.0, 1 / 4.0, 1 / 3.0, 1}};
    }

    public static List<Vendor> defaultVendors() {
        List<Vendor> l = new ArrayList<>();
        l.add(new Vendor("AWS", new double[]{95, 9, 9, 9, 9}));
        l.add(new Vendor("Microsoft Azure", new double[]{90, 8, 9, 8, 9}));
        l.add(new Vendor("Google Cloud", new double[]{80, 9, 8, 9, 8}));
        l.add(new Vendor("Alibaba Cloud", new double[]{65, 8, 7, 7, 6}));
        l.add(new Vendor("Biznet Gio", new double[]{55, 6, 7, 5, 7}));
        return l;
    }

    public double[][] getMatrix() { return matrix; }
    public void setMatrix(double[][] m) { this.matrix = m; }
    public List<Vendor> getVendors() { return vendors; }
    public void resetMatrix() { matrix = defaultMatrix(); }
    public void resetVendors() { vendors = defaultVendors(); }
}
