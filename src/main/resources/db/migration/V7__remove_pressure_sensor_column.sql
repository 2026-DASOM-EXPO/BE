-- 안전화 압력 센서 제거: 신규 센서 로그에는 pressure_value를 저장하지 않는다.
ALTER TABLE sensor_logs DROP COLUMN IF EXISTS pressure_value;
