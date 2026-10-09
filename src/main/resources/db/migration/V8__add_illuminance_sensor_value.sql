-- 안전조끼 착용 판정용 조도 센서 ADC 값(0~4095)
ALTER TABLE sensor_logs ADD COLUMN IF NOT EXISTS light_value INTEGER;
