-- 설비
select * from equipment;

INSERT INTO equipment (eq_code, process, min_width_in, max_width_in, min_thickness_in, max_thickness_in, min_width_out, max_width_out, min_thickness_out, max_thickness_out, speed, max_weight, ton_for_hour) VALUES
                                                                                                                                                                                                                  ('1PCM', 'PCM', 500, 1385, 1.2, 6.0, 500, 1270, 0.15, 1.6, 350, 28, 588.0),
                                                                                                                                                                                                                  ('2PCM', 'PCM', 620, 1685, 2.0, 6.0, 620, 1670, 0.4, 2.0, 300, 35, 630.0),
                                                                                                                                                                                                                  ('1CAL', 'CAL', 495, 1278, 0.14, 1.7, 500, 1270, 0.15, 0.8, 500, 28, 840.0),
                                                                                                                                                                                                                  ('2CAL', 'CAL', 613, 1660, 0.38, 2.1, 620, 1650, 0.4, 2.0, 450, 35, 945.0),
                                                                                                                                                                                                                  ('1EGL', 'EGL', 800, 1650, 0.4, 2.3, 800, 1650, 0.4, 2.3, 200, 34, 408.0),
                                                                                                                                                                                                                  ('2EGL', 'EGL', 800, 1650, 0.4, 2.0, 800, 1650, 0.4, 2.0, 180, 34, 367.2),
                                                                                                                                                                                                                  ('1CGL', 'CGL', 800, 1860, 0.35, 2.3, 800, 1860, 0.35, 2.3, 200, 30, 360.0);


-- 계획 공정
select * from plan_process;
INSERT INTO plan_process (coil_type_code, pcm, cal, egl, cgl, packing)
VALUES ('HTS300', '2PCM', '1CAL', NULL, NULL, '101');
INSERT INTO plan_process (coil_type_code, pcm, cal, egl, cgl, packing)
VALUES ('HTS400', '2PCM', '1CAL', NULL, NULL, '101');

INSERT INTO plan_process (coil_type_code, pcm, cal, egl, cgl, packing)
VALUES ('HTS500', '2PCM', '2CAL', '2EGL', NULL, '201');

INSERT INTO plan_process (coil_type_code, pcm, cal, egl, cgl, packing)
VALUES ('HTS600', '1PCM', '2CAL', '1EGL', NULL, '101');

INSERT INTO plan_process (coil_type_code, pcm, cal, egl, cgl, packing)
VALUES ('HTS800', '1PCM', '2CAL', NULL, '1CGL', '101');

INSERT INTO plan_process (coil_type_code, pcm, cal, egl, cgl, packing)
VALUES ('HCKP', '1PCM', '2CAL', NULL, NULL, '201');

INSERT INTO plan_process (coil_type_code, pcm, cal, egl, cgl, packing)
VALUES ('HPKL', '1PCM', NULL, NULL, NULL, '101');


-- 냉연 표준 절연
INSERT INTO coil_standard_reduction (coil_type_code, process, thickness_reduction, width_reduction, temperature) VALUES
                                                                                                                     ('HTS300', '2PCM', 5.60, 15.0, NULL),
                                                                                                                     ('HTS300', '1CAL', 1.55, 8.0, 653),
                                                                                                                     ('HTS400', '2PCM', 5.60, 15.0, NULL),
                                                                                                                     ('HTS400', '1CAL', 1.55, 8.0, 788),
                                                                                                                     ('HTS500', '2PCM', 5.60, 15.0, NULL),
                                                                                                                     ('HTS500', '2CAL', 1.70, 10.0, 791),
                                                                                                                     ('HTS500', '2EGL', 1.60, 0.0, NULL),
                                                                                                                     ('HTS600', '1PCM', 5.85, 115.0, NULL),
                                                                                                                     ('HTS600', '2CAL', 1.70, 10.0, 824),
                                                                                                                     ('HTS600', '1EGL', 1.90, 0.0, NULL),
                                                                                                                     ('HTS800', '1PCM', 5.85, 115.0, NULL),
                                                                                                                     ('HTS800', '2CAL', 1.70, 10.0, 740),
                                                                                                                     ('HTS800', '1CGL', 1.95, 0.0, NULL),
                                                                                                                     ('HCKP', '1PCM', 5.85, 115.0, NULL),
                                                                                                                     ('HCKP', '2CAL', 1.70, 10.0, 689),
                                                                                                                     ('HPKL', '1PCM', 5.85, 115.0, NULL);
