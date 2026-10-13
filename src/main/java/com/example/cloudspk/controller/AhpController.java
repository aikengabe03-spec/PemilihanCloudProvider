package com.example.cloudspk.controller;

import com.example.cloudspk.model.*;
import com.example.cloudspk.service.*;
import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AhpController {
    record Opt(String value, String label) {}
    record Pair(int i, int j, String a, String b, String cur) {}
    record WRow(String code, String name, String pct, double bar) {}
    record Card(int rank, String name, String cost, String score, double pct, String label, String grad) {}
    record RRow(int rank, String name, String[] contrib, String score, boolean top) {}
    record NRow(String name, String[] r) {}
    record SensRow(String name, String weights, String[] top) {}

    private static final double[] SCALE = {9, 8, 7, 6, 5, 4, 3, 2, 1, 1 / 2.0, 1 / 3.0, 1 / 4.0, 1 / 5.0, 1 / 6.0, 1 / 7.0, 1 / 8.0, 1 / 9.0};
    private static final String[] GRAD = {"#0e7490,#1e3a8a", "#334155,#0f766e", "#b45309,#7c2d12", "#4338ca,#1e1b4b", "#15803d,#14532d", "#be185d,#4c1d95", "#0369a1,#164e63"};
    private static final String[] ROMAN = {"", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
    private static final Locale ID = Locale.forLanguageTag("id-ID");

    private final AhpService ahp;
    private final SawService saw;
    private final SensitivityService sens;
    private final EvaluationState state;

    public AhpController(AhpService ahp, SawService saw, SensitivityService sens, EvaluationState state) {
        this.ahp = ahp; this.saw = saw; this.sens = sens; this.state = state;
    }

    private static String f(double x, int d) { return String.format(ID, "%." + d + "f", x); }
    private static String lab(double v) { return v >= 1 ? String.valueOf((int) Math.round(v)) : "1/" + Math.round(1 / v); }
    private static String nearest(double v) {
        double best = SCALE[0];
        for (double s : SCALE) if (Math.abs(s - v) < Math.abs(best - v)) best = s;
        return lab(best);
    }
    private static boolean valid(double[] x) {
        if (x[0] <= 0) return false;
        for (int j = 1; j < x.length; j++) if (x[j] < 1 || x[j] > 10) return false;
        return true;
    }

    @GetMapping("/")
    public String index(Model m) {
        List<Vendor> vs = state.getVendors();
        String[] N = EvaluationState.NAMES, Cd = EvaluationState.CODES;
        AhpResult a = ahp.calculate(state.getMatrix());
        SawResult s = saw.calculate(a.weights(), vs, EvaluationState.IS_COST);

        List<Opt> opts = new ArrayList<>();
        for (double v : SCALE) opts.add(new Opt(Double.toString(v), lab(v)));
        List<Pair> pairs = new ArrayList<>();
        for (int i = 0; i < 5; i++) for (int j = i + 1; j < 5; j++)
            pairs.add(new Pair(i, j, N[i], N[j], nearest(state.getMatrix()[i][j])));

        List<WRow> wrows = new ArrayList<>();
        for (int i = 0; i < 5; i++) wrows.add(new WRow(Cd[i], N[i], f(a.weights()[i] * 100, 2), a.weights()[i] * 100 / 45));

        List<Card> cards = new ArrayList<>();
        List<RRow> rrows = new ArrayList<>();
        double top = s.scores()[s.order()[0]];
        for (int k = 0; k < s.order().length; k++) {
            int i = s.order()[k];
            String[] g = GRAD[i % GRAD.length].split(",");
            cards.add(new Card(k + 1, vs.get(i).name(), f(vs.get(i).x()[0], 0), f(s.scores()[i], 4), s.scores()[i] / top * 100,
                    k == 0 ? "Rekomendasi utama" : "Alternatif " + ROMAN[Math.min(k, 9)], "linear-gradient(160deg," + g[0] + "," + g[1] + ")"));
            String[] c = new String[5];
            for (int j = 0; j < 5; j++) c[j] = f(s.r()[i][j] * a.weights()[j], 4);
            rrows.add(new RRow(k + 1, vs.get(i).name(), c, f(s.scores()[i], 4), k == 0));
        }
        List<NRow> nrows = new ArrayList<>();
        for (int i = 0; i < vs.size(); i++) {
            String[] r = new String[5];
            for (int j = 0; j < 5; j++) r[j] = f(s.r()[i][j], 4);
            nrows.add(new NRow(vs.get(i).name(), r));
        }
        List<SensRow> srows = new ArrayList<>();
        List<String> changed = new ArrayList<>();
        for (Scenario sc : sens.run(a.weights(), vs, EvaluationState.IS_COST)) {
            String[] t = new String[3];
            for (int k = 0; k < 3; k++) t[k] = k < sc.order().length ? vs.get(sc.order()[k]).name() : "-";
            StringBuilder w = new StringBuilder();
            for (double x : sc.weights()) w.append(w.length() > 0 ? "; " : "").append(f(x, 2));
            if (sc.order()[0] != s.order()[0]) changed.add(sc.name());
            srows.add(new SensRow(sc.name(), w.toString(), t));
        }
        m.addAttribute("codes", Cd); m.addAttribute("names", N);
        m.addAttribute("opts", opts); m.addAttribute("pairs", pairs); m.addAttribute("wrows", wrows);
        m.addAttribute("lambda", f(a.lambdaMax(), 4)); m.addAttribute("ci", f(a.ci(), 4));
        m.addAttribute("cr", f(a.cr(), 4)); m.addAttribute("crPct", f(a.cr() * 100, 2));
        m.addAttribute("consistent", a.consistent());
        m.addAttribute("weightText", Arrays.stream(a.weights()).mapToObj(x -> f(x, 4)).toArray(String[]::new));
        m.addAttribute("vendors", vs); m.addAttribute("cards", cards);
        m.addAttribute("rrows", rrows); m.addAttribute("nrows", nrows); m.addAttribute("srows", srows);
        m.addAttribute("changed", changed.isEmpty() ? "Peringkat 1 stabil pada semua skenario."
                : "Peringkat 1 berubah pada skenario: " + String.join(", ", changed) + ". Rekomendasi bergantung pada prioritas organisasi.");
        return "index";
    }

    @PostMapping("/matriks")
    public String saveMatrix(@RequestParam Map<String, String> p, RedirectAttributes ra) {
        double[][] a = new double[5][5];
        try {
            for (int i = 0; i < 5; i++) a[i][i] = 1;
            for (int i = 0; i < 5; i++) for (int j = i + 1; j < 5; j++) {
                double v = Double.parseDouble(p.get("p_" + i + "_" + j));
                if (v < 1 / 9.0 - 1e-9 || v > 9 + 1e-9) throw new NumberFormatException();
                a[i][j] = v; a[j][i] = 1 / v;
            }
            state.setMatrix(a);
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", "Nilai perbandingan harus antara 1/9 dan 9.");
        }
        return "redirect:/#kalk";
    }

    @PostMapping("/vendor/tambah")
    public String add(@RequestParam String name, @RequestParam double k1, @RequestParam double k2, @RequestParam double k3,
                      @RequestParam double k4, @RequestParam double k5, RedirectAttributes ra) {
        double[] x = {k1, k2, k3, k4, k5};
        String n = name.trim();
        if (n.isEmpty()) ra.addFlashAttribute("error", "Isi nama vendor.");
        else if (state.getVendors().stream().anyMatch(v -> v.name().equalsIgnoreCase(n))) ra.addFlashAttribute("error", "Nama vendor sudah ada.");
        else if (!valid(x)) ra.addFlashAttribute("error", "Biaya harus lebih dari 0 dan C2 sampai C5 antara 1 dan 10.");
        else state.getVendors().add(new Vendor(n, x));
        return "redirect:/#kalk";
    }

    @PostMapping("/vendor/ubah")
    public String update(@RequestParam int idx, @RequestParam double k1, @RequestParam double k2, @RequestParam double k3,
                         @RequestParam double k4, @RequestParam double k5, RedirectAttributes ra) {
        double[] x = {k1, k2, k3, k4, k5};
        if (idx < 0 || idx >= state.getVendors().size()) return "redirect:/#kalk";
        if (!valid(x)) ra.addFlashAttribute("error", "Biaya harus lebih dari 0 dan C2 sampai C5 antara 1 dan 10.");
        else state.getVendors().set(idx, new Vendor(state.getVendors().get(idx).name(), x));
        return "redirect:/#kalk";
    }

    @PostMapping("/vendor/hapus")
    public String delete(@RequestParam int idx, RedirectAttributes ra) {
        if (state.getVendors().size() <= 2) ra.addFlashAttribute("error", "Minimal 2 vendor untuk membuat peringkat.");
        else if (idx >= 0 && idx < state.getVendors().size()) state.getVendors().remove(idx);
        return "redirect:/#kalk";
    }

    @PostMapping("/reset/matriks")
    public String resetMatrix() { state.resetMatrix(); return "redirect:/#kalk"; }

    @PostMapping("/reset/vendor")
    public String resetVendors() { state.resetVendors(); return "redirect:/#kalk"; }
}
