-- priority insert문 변경 by 최효준 240925
select * from priority;

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (1,'목표폭으로 내림차순', 1, 'DESC_GOALWIDTH', 'goal_width', '1CAL', 'A');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (2,'동일값 주문폭으로 그룹핑', 2, 'GROUPING_BY_GOALWIDTH', 'goal_width', '1CAL', 'A');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (3,'코일두께로 오름차순', 3, 'ASC_THICKNESS', 'thickness', '1CAL', 'A');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (4,'코일두께로 sin곡선', 4, 'APPLY_SIN', 'thickness', '1CAL', 'A');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (5,'동일두께로 그룹핑', 5, 'GROUPING_BY_THICKNESS', 'thickness', '1CAL', 'A');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (6, '같은 소둔온도로 그룹핑', 6, 'GROUPING_BY_TEMP', 'temperature', '1CAL', 'A');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (7,'같은 품명으로 그룹핑', 7, 'GROUPING_BY_NAME', 'coil_type_code', '1CAL', 'A');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (8, '목표폭으로 내림차순', 1, 'DESC_GOALWIDTH', 'goal_width', '1CAL', 'B');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (9, '동일값 주문폭으로 그룹핑', 2, 'GROUPING_BY_GOALWIDTH', 'goal_width', '1CAL', 'B');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (10, '코일두께로 오름차순', 3, 'ASC_THICKNESS', 'thickness', '1CAL', 'B');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (11, '코일두께로 sin곡선', 4, 'APPLY_SIN', 'thickness', '1CAL', 'B');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (12, '동일두께로 그룹핑', 5, 'GROUPING_BY_THICKNESS', 'thickness', '1CAL', 'B');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (13, '같은 소둔온도로 그룹핑', 6, 'GROUPING_BY_TEMP', 'temperature', '1CAL', 'B');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (14, '같은 품명으로 그룹핑', 7, 'GROUPING_BY_NAME', 'coil_type_code', '1CAL', 'B');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (15, '목표폭으로 내림차순', 1, 'DESC_GOALWIDTH', 'goal_width', '2CAL', 'A');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (16, '동일값 주문폭으로 그룹핑', 2, 'GROUPING_BY_GOALWIDTH', 'goal_width', '2CAL', 'A');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (17, '코일두께로 오름차순', 3, 'ASC_THICKNESS', 'thickness', '2CAL', 'A');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (18, '코일두께로 sin곡선', 4, 'APPLY_SIN', 'thickness', '2CAL', 'A');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (19, '동일두께로 그룹핑', 5, 'GROUPING_BY_THICKNESS', 'thickness', '2CAL', 'A');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (20, '같은 소둔온도로 그룹핑', 6, 'GROUPING_BY_TEMP', 'temperature', '2CAL', 'A');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (21, '같은 품명으로 그룹핑', 7, 'GROUPING_BY_NAME', 'coil_type_code', '2CAL', 'A');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (22, '목표폭으로 내림차순', 1, 'DESC_GOALWIDTH', 'goal_width', '2CAL', 'B');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (23, '동일값 주문폭으로 그룹핑', 2, 'GROUPING_BY_GOALWIDTH', 'goal_width', '2CAL', 'B');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (24, '코일두께로 오름차순', 3, 'ASC_THICKNESS', 'thickness', '2CAL', 'B');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (25, '코일두께로 sin곡선', 4, 'APPLY_SIN', 'thickness', '2CAL', 'B');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (26, '동일두께로 그룹핑', 5, 'GROUPING_BY_THICKNESS', 'thickness', '2CAL', 'B');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (27, '같은 소둔온도로 그룹핑', 6, 'GROUPING_BY_TEMP', 'temperature', '2CAL', 'B');

INSERT INTO PRIORITY (id, name, priority_order, apply_method, target_column, process_code, roll_unit) VALUES (28, '같은 품명으로 그룹핑', 7, 'GROUPING_BY_NAME', 'coil_type_code', '2CAL', 'B');

-- target_column : 'goal_width'에서 width로 변경함
-- update priority set target_column='width' where priority_order=1 or priority_order=2 ;

-- constraint_insertion insert문
select * from CONSTRAINT_INSERTION;

INSERT INTO CONSTRAINT_INSERTION (id, type, target_column, target_value, process_code, roll_unit) VALUES (1, 'CONSTRAINT', 'width', 100, '1CAL', 'A');
INSERT INTO CONSTRAINT_INSERTION (id, type, target_column, target_value, process_code, roll_unit) VALUES (2, 'CONSTRAINT', 'thickness', 0.5, '1CAL', 'A');
INSERT INTO CONSTRAINT_INSERTION (id, type, target_column, target_value, process_code, roll_unit) VALUES (3, 'INSERTION', 'width', 10, '1CAL', 'A');

INSERT INTO CONSTRAINT_INSERTION (id, type, target_column, target_value, process_code, roll_unit) VALUES (4, 'CONSTRAINT', 'width', 100, '1CAL', 'B');
INSERT INTO CONSTRAINT_INSERTION (id, type, target_column, target_value, process_code, roll_unit) VALUES (5, 'CONSTRAINT', 'thickness', 0.5, '1CAL', 'B');
INSERT INTO CONSTRAINT_INSERTION (id, type, target_column, target_value, process_code, roll_unit) VALUES (6, 'INSERTION', 'width', 10, '1CAL', 'B');

INSERT INTO CONSTRAINT_INSERTION (id, type, target_column, target_value, process_code, roll_unit) VALUES (7, 'CONSTRAINT', 'width', 100, '2CAL', 'A');
INSERT INTO CONSTRAINT_INSERTION (id, type, target_column, target_value, process_code, roll_unit) VALUES (8, 'CONSTRAINT', 'thickness', 0.5, '2CAL', 'A');
INSERT INTO CONSTRAINT_INSERTION (id, type, target_column, target_value, process_code, roll_unit) VALUES (9, 'INSERTION', 'width', 10, '2CAL', 'A');

INSERT INTO CONSTRAINT_INSERTION (id, type, target_column, target_value, process_code, roll_unit) VALUES (10, 'CONSTRAINT', 'width', 100, '2CAL', 'B');
INSERT INTO CONSTRAINT_INSERTION (id, type, target_column, target_value, process_code, roll_unit) VALUES (11, 'CONSTRAINT', 'thickness', 0.5, '2CAL', 'B');
INSERT INTO CONSTRAINT_INSERTION (id, type, target_column, target_value, process_code, roll_unit) VALUES (12, 'INSERTION', 'width', 10, '2CAL', 'B');

update CONSTRAINT_INSERTION
set target_value = 50
WHERE TYPE = 'CONSTRAINT'
  AND TARGET_COLUMN = 'width';

