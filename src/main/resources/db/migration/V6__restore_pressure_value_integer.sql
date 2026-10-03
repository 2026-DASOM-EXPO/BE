-- 운영 DB에서 float8로 바뀐 pressure_value를 엔티티(Integer)와 같은 INTEGER로 되돌린다.
-- 이미 INTEGER인 DB에서는 변화가 없고, 소수점 값은 Postgres 기본 형변환에 따라 반올림된다.
ALTER TABLE sensor_logs
    ALTER COLUMN pressure_value SET DATA TYPE INTEGER;
