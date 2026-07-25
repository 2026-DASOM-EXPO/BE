-- V1 legacy compatibility and V2 schema creation run before this seed.
-- 로컬/데모 기본 계정 5개
-- 모든 계정의 초기 비밀번호: Admin123!
INSERT INTO users (login_id, password, name, role, enabled, created_at, updated_at)
VALUES
    ('manager', '$2a$10$XCU7SF3wJEzkHuByXjT.kOyw/UMqEhHs0bntpzwpkYgvagRKRlsui', '총괄 관리자', 'MANAGER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('manager2', '$2a$10$XCU7SF3wJEzkHuByXjT.kOyw/UMqEhHs0bntpzwpkYgvagRKRlsui', '현장 관리자', 'MANAGER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('worker1', '$2a$10$XCU7SF3wJEzkHuByXjT.kOyw/UMqEhHs0bntpzwpkYgvagRKRlsui', '작업자 계정 1', 'WORKER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('worker2', '$2a$10$XCU7SF3wJEzkHuByXjT.kOyw/UMqEhHs0bntpzwpkYgvagRKRlsui', '작업자 계정 2', 'WORKER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('worker3', '$2a$10$XCU7SF3wJEzkHuByXjT.kOyw/UMqEhHs0bntpzwpkYgvagRKRlsui', '작업자 계정 3', 'WORKER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO refresh_tokens (user_id, token, expires_at, revoked, created_at, updated_at)
SELECT id, 'seed-refresh-token-manager', TIMESTAMP '2099-12-31 23:59:59', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users WHERE login_id = 'manager';

INSERT INTO refresh_tokens (user_id, token, expires_at, revoked, created_at, updated_at)
SELECT id, 'seed-refresh-token-manager2', TIMESTAMP '2099-12-31 23:59:59', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users WHERE login_id = 'manager2';

INSERT INTO refresh_tokens (user_id, token, expires_at, revoked, created_at, updated_at)
SELECT id, 'seed-refresh-token-worker1', TIMESTAMP '2099-12-31 23:59:59', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users WHERE login_id = 'worker1';

INSERT INTO refresh_tokens (user_id, token, expires_at, revoked, created_at, updated_at)
SELECT id, 'seed-refresh-token-worker2', TIMESTAMP '2099-12-31 23:59:59', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users WHERE login_id = 'worker2';

INSERT INTO refresh_tokens (user_id, token, expires_at, revoked, created_at, updated_at)
SELECT id, 'seed-refresh-token-worker3', TIMESTAMP '2099-12-31 23:59:59', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users WHERE login_id = 'worker3';

-- 작업자 5명
INSERT INTO workers (
    name, department, phone_number, rfid_tag, status,
    current_latitude, current_longitude, created_at, updated_at
)
VALUES
    ('김민수', 'A구역 철골팀', '010-1000-0001', 'RFID-WORKER-001', 'NORMAL', 37.566500, 126.978000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('이서준', 'B구역 전기팀', '010-1000-0002', 'RFID-WORKER-002', 'WARNING', 37.566620, 126.978130, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('박지훈', 'C구역 설비팀', '010-1000-0003', 'RFID-WORKER-003', 'DANGER', 37.566740, 126.978260, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('최도윤', 'D구역 배관팀', '010-1000-0004', 'RFID-WORKER-004', 'NORMAL', 37.566860, 126.978390, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('정현우', '안전관리팀', '010-1000-0005', 'RFID-WORKER-005', 'INACTIVE', 37.566980, 126.978520, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 작업자별 안전모·안전조끼·안전화 1세트, 총 15개
INSERT INTO equipment (
    worker_id, serial_number, name, type, status, wear_status,
    last_detected_at, buzzer_enabled, work_timer_enabled,
    work_timer_started_at, work_timer_ended_at,
    manual_wear_override, manual_wear_status, created_at, updated_at
)
VALUES
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-001'), 'HELMET-001', '김민수 RA18-DIY 안전모', 'HELMET', 'ASSIGNED', 'WORN', CURRENT_TIMESTAMP, FALSE, FALSE, NULL, NULL, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-001'), 'VEST-001', '김민수 HX19-B4 안전조끼', 'VEST', 'ASSIGNED', 'WORN', CURRENT_TIMESTAMP, FALSE, FALSE, NULL, NULL, TRUE, 'WORN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-001'), 'SHOES-001', '김민수 RA18-DIY 안전화', 'SHOES', 'ASSIGNED', 'WORN', CURRENT_TIMESTAMP, FALSE, FALSE, NULL, NULL, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-002'), 'HELMET-002', '이서준 RA18-DIY 안전모', 'HELMET', 'ASSIGNED', 'NOT_WORN', CURRENT_TIMESTAMP, FALSE, FALSE, NULL, NULL, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-002'), 'VEST-002', '이서준 HX19-B4 안전조끼', 'VEST', 'ASSIGNED', 'WORN', CURRENT_TIMESTAMP, TRUE, FALSE, NULL, NULL, TRUE, 'WORN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-002'), 'SHOES-002', '이서준 RA18-DIY 안전화', 'SHOES', 'ASSIGNED', 'WORN', CURRENT_TIMESTAMP, FALSE, FALSE, NULL, NULL, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-003'), 'HELMET-003', '박지훈 RA18-DIY 안전모', 'HELMET', 'ASSIGNED', 'WORN', CURRENT_TIMESTAMP, FALSE, FALSE, NULL, NULL, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-003'), 'VEST-003', '박지훈 HX19-B4 안전조끼', 'VEST', 'ASSIGNED', 'WORN', CURRENT_TIMESTAMP, FALSE, FALSE, NULL, NULL, TRUE, 'WORN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-003'), 'SHOES-003', '박지훈 RA18-DIY 안전화', 'SHOES', 'ASSIGNED', 'WORN', CURRENT_TIMESTAMP, FALSE, FALSE, NULL, NULL, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-004'), 'HELMET-004', '최도윤 RA18-DIY 안전모', 'HELMET', 'ASSIGNED', 'WORN', CURRENT_TIMESTAMP, FALSE, FALSE, NULL, NULL, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-004'), 'VEST-004', '최도윤 HX19-B4 안전조끼', 'VEST', 'ASSIGNED', 'WORN', CURRENT_TIMESTAMP, FALSE, FALSE, NULL, NULL, TRUE, 'WORN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-004'), 'SHOES-004', '최도윤 RA18-DIY 안전화', 'SHOES', 'ASSIGNED', 'WORN', CURRENT_TIMESTAMP, FALSE, FALSE, NULL, NULL, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-005'), 'HELMET-005', '정현우 RA18-DIY 안전모', 'HELMET', 'ASSIGNED', 'WORN', CURRENT_TIMESTAMP, FALSE, FALSE, NULL, NULL, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-005'), 'VEST-005', '정현우 HX19-B4 안전조끼', 'VEST', 'ASSIGNED', 'WORN', CURRENT_TIMESTAMP, FALSE, FALSE, NULL, NULL, TRUE, 'WORN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-005'), 'SHOES-005', '정현우 RA18-DIY 안전화', 'SHOES', 'ASSIGNED', 'WORN', CURRENT_TIMESTAMP, FALSE, FALSE, NULL, NULL, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 장비 불출/착용 이력 5개
INSERT INTO equipment_logs (
    worker_id, equipment_id, wear_status, issued_at, returned_at, created_at, updated_at
)
VALUES
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-001'), (SELECT id FROM equipment WHERE serial_number = 'HELMET-001'), 'WORN', CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-002'), (SELECT id FROM equipment WHERE serial_number = 'HELMET-002'), 'NOT_WORN', CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-003'), (SELECT id FROM equipment WHERE serial_number = 'VEST-003'), 'WORN', CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-004'), (SELECT id FROM equipment WHERE serial_number = 'SHOES-004'), 'WORN', CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-005'), (SELECT id FROM equipment WHERE serial_number = 'VEST-005'), 'WORN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 조끼 부저/타이머 명령 5개
INSERT INTO wearable_commands (
    worker_id, equipment_id, command_type, command_status,
    reason, requested_at, acknowledged_at, created_at, updated_at
)
VALUES
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-001'), (SELECT id FROM equipment WHERE serial_number = 'VEST-001'), 'BUZZER_OFF', 'ACKNOWLEDGED', '정상 착용 확인', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-002'), (SELECT id FROM equipment WHERE serial_number = 'VEST-002'), 'BUZZER_ON', 'REQUESTED', '안전모 미착용 LV2 경고', CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-003'), (SELECT id FROM equipment WHERE serial_number = 'VEST-003'), 'BUZZER_OFF', 'SENT', 'SOS는 조끼 부저를 추가하지 않음', CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-004'), (SELECT id FROM equipment WHERE serial_number = 'VEST-004'), 'TIMER_START', 'ACKNOWLEDGED', '작업 타이머 시작', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-005'), (SELECT id FROM equipment WHERE serial_number = 'VEST-005'), 'TIMER_STOP', 'ACKNOWLEDGED', '작업 종료', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 센서 로그 5개
INSERT INTO sensor_logs (
    worker_id, equipment_id, sensor_type,
    bpm, spo2, body_temperature,
    latitude, longitude, speed, pressure_value,
    raw_payload, wear_status, sos_pressed, risk_level,
    measured_at, created_at, updated_at
)
VALUES
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-001'), (SELECT id FROM equipment WHERE serial_number = 'HELMET-001'), 'WEAR_STATUS', NULL, NULL, NULL, NULL, NULL, NULL, 2200, '{"adc":2200}', 'WORN', FALSE, 'LV1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-002'), (SELECT id FROM equipment WHERE serial_number = 'HELMET-002'), 'WEAR_STATUS', NULL, NULL, NULL, NULL, NULL, NULL, 320, '{"adc":320}', 'NOT_WORN', FALSE, 'LV2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-003'), (SELECT id FROM equipment WHERE serial_number = 'VEST-003'), 'SOS', NULL, NULL, NULL, 37.566740, 126.978260, NULL, NULL, '{"buttonValue":1}', 'WORN', TRUE, 'LV3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-004'), (SELECT id FROM equipment WHERE serial_number = 'SHOES-004'), 'WEAR_STATUS', NULL, NULL, NULL, NULL, NULL, NULL, 1900, '{"adc":1900}', 'WORN', FALSE, 'LV1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-005'), NULL, 'BIOMETRIC', 78, 98.2, 36.6, NULL, NULL, NULL, NULL, '{"source":"demo"}', NULL, FALSE, 'LV1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 위험 이벤트 5개
INSERT INTO risk_events (
    worker_id, source_type, risk_type, risk_level, description,
    latitude, longitude, status, occurred_at, resolved_at, created_at, updated_at
)
VALUES
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-002'), 'SENSOR', 'NO_EQUIPMENT', 'LV2', '안전모 미착용 감지', 37.566620, 126.978130, 'OPEN', CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-003'), 'SOS', 'SOS_REQUEST', 'LV3', '작업자 SOS 긴급 요청', 37.566740, 126.978260, 'PROCESSING', CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-001'), 'SENSOR', 'NO_EQUIPMENT', 'LV2', '과거 안전화 미착용 복구', 37.566500, 126.978000, 'RESOLVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-004'), 'SENSOR', 'BIOMETRIC_ABNORMAL', 'LV2', '심박수 주의 범위 감지', 37.566860, 126.978390, 'OPEN', CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-005'), 'MANUAL', 'LOCATION_ABNORMAL', 'LV2', '작업 구역 이탈 확인 완료', 37.566980, 126.978520, 'RESOLVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 관리자 알림 5개
INSERT INTO alerts (
    risk_event_id, worker_id, title, message, severity,
    read_status, read_at, created_at, updated_at
)
VALUES
    ((SELECT id FROM risk_events WHERE description = '안전모 미착용 감지'), (SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-002'), 'LV2 장비 미착용', '이서준 작업자의 안전모를 확인하세요.', 'WARNING', 'UNREAD', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM risk_events WHERE description = '작업자 SOS 긴급 요청'), (SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-003'), 'LV3 SOS 요청', '박지훈 작업자가 SOS 버튼을 눌렀습니다.', 'DANGER', 'UNREAD', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM risk_events WHERE description = '과거 안전화 미착용 복구'), (SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-001'), '장비 착용 복구', '김민수 작업자의 장비 착용이 정상화되었습니다.', 'INFO', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM risk_events WHERE description = '심박수 주의 범위 감지'), (SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-004'), '생체 신호 경고', '최도윤 작업자의 심박수를 확인하세요.', 'WARNING', 'UNREAD', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM risk_events WHERE description = '작업 구역 이탈 확인 완료'), (SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-005'), '위치 확인 완료', '정현우 작업자의 위치 확인을 완료했습니다.', 'INFO', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 드론 5대
INSERT INTO drones (
    name, serial_number, model_name, status, battery_percent,
    current_latitude, current_longitude, max_flight_minutes,
    payload_mounted, created_at, updated_at
)
VALUES
    ('현장 드론 1', 'SIYI-A8-001', 'SIYI A8 Mini', 'READY', 96, 37.566400, 126.977900, 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('현장 드론 2', 'SIYI-A8-002', 'SIYI A8 Mini', 'FLYING', 82, 37.566700, 126.978200, 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('현장 드론 3', 'SIYI-A8-003', 'SIYI A8 Mini', 'CHARGING', 44, 37.566300, 126.977800, 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('현장 드론 4', 'SIYI-A8-004', 'SIYI A8 Mini', 'MAINTENANCE', 71, 37.566200, 126.977700, 30, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('현장 드론 5', 'SIYI-A8-005', 'SIYI A8 Mini', 'READY', 100, 37.566100, 126.977600, 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 드론 출동 5개, 모든 데이터는 119 미요청
INSERT INTO drone_dispatches (
    drone_id, risk_event_id, target_latitude, target_longitude,
    dispatch_reason, emergency_kit_mounted, emergency_kit_dropped,
    drop_latitude, drop_longitude, drop_method,
    emergency_call_requested, emergency_call_status, status,
    command_message, dispatched_at, arrived_at, completed_at,
    created_at, updated_at
)
VALUES
    ((SELECT id FROM drones WHERE serial_number = 'SIYI-A8-001'), (SELECT id FROM risk_events WHERE description = '안전모 미착용 감지'), 37.566620, 126.978130, '시드 데이터 확인', TRUE, FALSE, NULL, NULL, 'MANUAL', FALSE, 'NOT_REQUESTED', 'CANCELED', 'LV2에는 실제 자동 출동하지 않음', CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM drones WHERE serial_number = 'SIYI-A8-002'), (SELECT id FROM risk_events WHERE description = '작업자 SOS 긴급 요청'), 37.566740, 126.978260, 'SOS', TRUE, FALSE, NULL, NULL, 'MANUAL', FALSE, 'NOT_REQUESTED', 'DISPATCHED', 'SOS 현장으로 자동 출동', CURRENT_TIMESTAMP, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM drones WHERE serial_number = 'SIYI-A8-003'), (SELECT id FROM risk_events WHERE description = '과거 안전화 미착용 복구'), 37.566500, 126.978000, '과거 출동 이력', TRUE, TRUE, 37.566510, 126.978010, 'LIDAR_SAFE_POINT', FALSE, 'NOT_REQUESTED', 'RETURNED', '과거 데모 출동 완료', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM drones WHERE serial_number = 'SIYI-A8-004'), (SELECT id FROM risk_events WHERE description = '심박수 주의 범위 감지'), 37.566860, 126.978390, '수동 확인', FALSE, FALSE, NULL, NULL, 'MANUAL', FALSE, 'NOT_REQUESTED', 'FAILED', '정비 상태로 출동 실패', CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM drones WHERE serial_number = 'SIYI-A8-005'), (SELECT id FROM risk_events WHERE description = '작업 구역 이탈 확인 완료'), 37.566980, 126.978520, '위치 확인', TRUE, FALSE, NULL, NULL, 'YOLO_TARGET', FALSE, 'NOT_REQUESTED', 'ARRIVED', '위치 확인 데모 출동', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 드론 영상 5개, 720p HLS
INSERT INTO drone_videos (
    drone_id, dispatch_id, title, description, stream_url,
    protocol, active, stream_status, started_at, ended_at,
    last_frame_at, width, height, frame_rate, created_at, updated_at
)
VALUES
    ((SELECT id FROM drones WHERE serial_number = 'SIYI-A8-001'), (SELECT id FROM drone_dispatches WHERE command_message = 'LV2에는 실제 자동 출동하지 않음'), '드론 1 대기 영상', '시드 영상', 'http://localhost:8888/SIYI-A8-001/index.m3u8', 'HLS', FALSE, 'READY', NULL, NULL, NULL, 1280, 720, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM drones WHERE serial_number = 'SIYI-A8-002'), (SELECT id FROM drone_dispatches WHERE command_message = 'SOS 현장으로 자동 출동'), 'SOS 현장 영상', '관리자 확인 후 송출 중', 'http://localhost:8888/SIYI-A8-002/index.m3u8', 'HLS', TRUE, 'STREAMING', CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 1280, 720, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM drones WHERE serial_number = 'SIYI-A8-003'), (SELECT id FROM drone_dispatches WHERE command_message = '과거 데모 출동 완료'), '완료 영상', '과거 출동 영상', 'http://localhost:8888/SIYI-A8-003/index.m3u8', 'HLS', FALSE, 'STOPPED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1280, 720, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM drones WHERE serial_number = 'SIYI-A8-004'), (SELECT id FROM drone_dispatches WHERE command_message = '정비 상태로 출동 실패'), '연결 실패 영상', '게이트웨이 연결 실패', 'http://localhost:8888/SIYI-A8-004/index.m3u8', 'HLS', FALSE, 'FAILED', NULL, NULL, NULL, 1280, 720, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM drones WHERE serial_number = 'SIYI-A8-005'), (SELECT id FROM drone_dispatches WHERE command_message = '위치 확인 데모 출동'), '위치 확인 영상', '현장 도착 영상', 'http://localhost:8888/SIYI-A8-005/index.m3u8', 'HLS', FALSE, 'READY', NULL, NULL, NULL, 1280, 720, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 드론 물품 투하 로그 5개
INSERT INTO drone_drop_logs (
    drone_dispatch_id, drone_id, worker_id, risk_event_id,
    drop_method, target_latitude, target_longitude,
    actual_drop_latitude, actual_drop_longitude, obstacle_detected,
    lidar_front_left, lidar_front_right, lidar_back_left,
    lidar_back_right, lidar_side_left, lidar_side_right,
    drop_status, created_at, updated_at
)
VALUES
    ((SELECT id FROM drone_dispatches WHERE command_message = 'LV2에는 실제 자동 출동하지 않음'), (SELECT id FROM drones WHERE serial_number = 'SIYI-A8-001'), (SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-002'), (SELECT id FROM risk_events WHERE description = '안전모 미착용 감지'), 'MANUAL', 37.566620, 126.978130, NULL, NULL, FALSE, 4.2, 4.1, 5.0, 5.1, 3.8, 3.9, 'CANCELED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM drone_dispatches WHERE command_message = 'SOS 현장으로 자동 출동'), (SELECT id FROM drones WHERE serial_number = 'SIYI-A8-002'), (SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-003'), (SELECT id FROM risk_events WHERE description = '작업자 SOS 긴급 요청'), 'MANUAL', 37.566740, 126.978260, NULL, NULL, FALSE, 5.2, 5.0, 4.8, 4.9, 4.5, 4.6, 'READY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM drone_dispatches WHERE command_message = '과거 데모 출동 완료'), (SELECT id FROM drones WHERE serial_number = 'SIYI-A8-003'), (SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-001'), (SELECT id FROM risk_events WHERE description = '과거 안전화 미착용 복구'), 'LIDAR_SAFE_POINT', 37.566500, 126.978000, 37.566510, 126.978010, FALSE, 6.2, 6.0, 5.8, 5.9, 5.5, 5.6, 'DROPPED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM drone_dispatches WHERE command_message = '정비 상태로 출동 실패'), (SELECT id FROM drones WHERE serial_number = 'SIYI-A8-004'), (SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-004'), (SELECT id FROM risk_events WHERE description = '심박수 주의 범위 감지'), 'MANUAL', 37.566860, 126.978390, NULL, NULL, TRUE, 0.7, 0.8, 1.0, 1.1, 0.6, 0.7, 'FAILED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ((SELECT id FROM drone_dispatches WHERE command_message = '위치 확인 데모 출동'), (SELECT id FROM drones WHERE serial_number = 'SIYI-A8-005'), (SELECT id FROM workers WHERE rfid_tag = 'RFID-WORKER-005'), (SELECT id FROM risk_events WHERE description = '작업 구역 이탈 확인 완료'), 'YOLO_TARGET', 37.566980, 126.978520, NULL, NULL, FALSE, 3.2, 3.0, 3.8, 3.9, 2.5, 2.6, 'DROPPING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
