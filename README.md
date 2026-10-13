# CloudPick – SPK Pemilihan Vendor Cloud (AHP + SAW)

Spring Boot 3.3 (Java 17), Thymeleaf, tanpa database (data per sesi di memori).

## Menjalankan
    mvn spring-boot:run
Buka http://localhost:8080

## Tes
    mvn test

## Struktur
- model/: Vendor, AhpResult, SawResult, Scenario, EvaluationState (data per sesi)
- service/: AhpService (bobot, CI, CR), SawService (normalisasi dan V), SensitivityService
- controller/AhpController: halaman "/" dan aksi matriks, tambah/ubah/hapus vendor, reset
- templates/index.html dan static/css/style.css: tampilan bertema WANDER.ph

Nilai vendor bawaan adalah data simulasi. Ganti dengan data resmi sebelum dipakai.
