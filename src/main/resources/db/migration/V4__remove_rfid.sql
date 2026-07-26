-- 기존 데모의 SOS 자동 출동 흔적은 과거 이력으로 분리한다.
-- 같은 SOS 위험 이벤트를 관리자가 승인하면 새 수동 출동이 생성될 수 있어야 한다.
UPDATE drone_videos
SET active = FALSE,
    stream_status = 'STOPPED',
    ended_at = COALESCE(ended_at, CURRENT_TIMESTAMP),
    updated_at = CURRENT_TIMESTAMP
WHERE dispatch_id IN (
    SELECT dd.id
    FROM drone_dispatches dd
    JOIN risk_events re ON re.id = dd.risk_event_id
    WHERE re.risk_type = 'SOS_REQUEST'
);

UPDATE drone_drop_logs
SET risk_event_id = NULL,
    updated_at = CURRENT_TIMESTAMP
WHERE risk_event_id IN (
    SELECT id FROM risk_events WHERE risk_type = 'SOS_REQUEST'
);

UPDATE drone_dispatches
SET risk_event_id = NULL,
    status = 'RETURNED',
    command_message = '과거 SOS 출동 이력',
    completed_at = COALESCE(completed_at, CURRENT_TIMESTAMP),
    updated_at = CURRENT_TIMESTAMP
WHERE risk_event_id IN (
    SELECT id FROM risk_events WHERE risk_type = 'SOS_REQUEST'
);

UPDATE risk_events
SET status = 'OPEN',
    resolved_at = NULL,
    updated_at = CURRENT_TIMESTAMP
WHERE risk_type = 'SOS_REQUEST'
  AND status = 'PROCESSING';

UPDATE drones
SET status = 'READY',
    updated_at = CURRENT_TIMESTAMP
WHERE serial_number = 'SIYI-A8-002';

-- RFID 센서 이력과 작업자 식별 컬럼을 최종 스키마에서 제거한다.
DELETE FROM sensor_logs
WHERE sensor_type = 'RFID';

ALTER TABLE workers
    DROP COLUMN IF EXISTS rfid_tag;
