-- V3에 포함됐던 과거 자동 출동 데모 SOS만 종료한다.
-- 이후 ESP32의 새 SOS 버튼 입력은 새로운 OPEN 이벤트와 관리자 승인 모달을 만든다.
UPDATE alerts
SET read_status = 'READ',
    read_at = COALESCE(read_at, CURRENT_TIMESTAMP),
    updated_at = CURRENT_TIMESTAMP
WHERE risk_event_id IN (
    SELECT id
    FROM risk_events
    WHERE risk_type = 'SOS_REQUEST'
      AND description = '작업자 SOS 긴급 요청'
);

UPDATE risk_events
SET status = 'RESOLVED',
    resolved_at = COALESCE(resolved_at, CURRENT_TIMESTAMP),
    updated_at = CURRENT_TIMESTAMP
WHERE risk_type = 'SOS_REQUEST'
  AND description = '작업자 SOS 긴급 요청';
