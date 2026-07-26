# SIYI A8 Mini 실제 영상 송출 가이드

## 구현된 전체 흐름

```text
SIYI A8 Mini RTSP
  → BE 컨테이너의 FFmpeg (TCP 입력, H.264 재인코딩)
  → 720p/30fps 저지연 HLS(index.m3u8 + TS 조각)
  → Spring의 /streams/** 공개 전송
  → 관리자 모달의 hls.js 플레이어
```

SOS 버튼 입력 후 드론 출동 데이터는 즉시 생성되지만 영상 프로세스는 아직 시작하지 않는다. 관리자 모달에서
`확인하고 현장 영상 보기`를 누르면 위험 이벤트가 `PROCESSING`으로 바뀌고, 이때 BE가 FFmpeg를 실행한다.
HLS manifest가 실제로 생성된 경우에만 `drone_videos.stream_status`가 `STREAMING`으로 저장되고 FE에
재생 URL이 반환된다.

119 외부 신고 API는 호출하지 않는다. `emergency_call_requested=false`,
`emergency_call_status=NOT_REQUESTED`만 DB에 저장한다.

> 이 구현은 카메라 영상 연동이다. 현재 드론 "출동"은 출동 명령/상태를 DB에 생성하는 단계이며,
> 실제 비행 컨트롤러를 움직이려면 별도의 비행 제어 SDK/프로토콜 연동과 현장 안전 검증이 필요하다.

## 기본 RTSP 주소

SIYI A8 Mini 계열의 기본 메인 스트림 주소를 다음과 같이 설정했다.

```text
rtsp://192.168.144.25:8554/main.264
```

- [SIYI A8 Mini 공식 다운로드](https://www.siyi.biz/en/product/tri-axis-single-camera-gimbal/a8-mini/download/)
- [SIYI A8 Mini 공식 매뉴얼](https://siyi.biz/siyi_file/A8%20mini/A8%20mini%20v1.6.pdf)
- [SIYI MK15 공식 매뉴얼](https://siyi.biz/siyi_file/MK15/MK15%20User%20Manual%20v1.8.pdf)

카메라 펌웨어/영상 링크 설정에서 주소가 변경되었다면 `DRONE_RTSP_URL_TEMPLATE`만 실제 주소로 바꾼다.

## Docker로 한 번에 실행

필수 조건은 Docker Desktop 실행, SIYI 카메라와 영상 링크 전원 연결, PC에서 카메라 네트워크로 접근 가능한
상태이다.

```powershell
cd C:\Users\joo\Desktop\EXPO\BE
docker compose up --build
```

이 명령은 PostgreSQL, Spring BE, FFmpeg를 함께 준비한다. 호스트에 FFmpeg가 없어도 컨테이너 이미지 안에
FFmpeg와 `libx264`가 설치된다.

다른 PC/휴대기기의 브라우저에서 관리자 화면을 열 때는 `localhost` 대신 BE PC의 LAN IP를 공개 URL에
넣어야 한다.

```powershell
$env:DRONE_STREAM_PUBLIC_BASE_URL="http://192.168.0.10:8080/streams"
docker compose up --build
```

Windows 방화벽에서 TCP 8080 인바운드도 허용해야 한다. PostgreSQL 호스트 포트 기본값은 로컬 PostgreSQL과
충돌하지 않도록 5433이며, 컨테이너 내부에서는 5432를 사용한다.

## 장비 연결 전 확인

호스트에서:

```powershell
Test-NetConnection 192.168.144.25 -Port 8554
ffplay -rtsp_transport tcp rtsp://192.168.144.25:8554/main.264
```

컨테이너에서:

```powershell
docker compose exec backend ffmpeg -version
docker compose exec backend ffprobe -v error -rtsp_transport tcp `
  -show_streams rtsp://192.168.144.25:8554/main.264
```

첫 명령의 `TcpTestSucceeded`가 `False`면 애플리케이션 문제가 아니라 카메라 전원, 이더넷/영상 링크,
PC 네트워크 어댑터 IP 또는 라우팅부터 확인해야 한다.

## 실제 UI 시나리오

1. ESP32가 `POST /api/iot/sos`로 `buttonValue: 1`을 전송한다.
2. BE가 LV.3 위험 이벤트와 관리자 알림을 만들고 대기 드론 출동 데이터를 생성한다.
3. FE는 SSE/조회 결과로 SOS 모달을 띄운다. 이 시점에는 영상이 시작되지 않는다.
4. 관리자가 확인 버튼을 누르면 FE가 `PATCH /api/risk-events/{riskEventId}/status`를 호출한다.
5. BE가 SIYI RTSP에 접속하고 HLS 생성 완료를 기다린다.
6. FE가 `GET /api/events/risk?workerId={workerId}` 응답의 `droneVideo.streamUrl`을 hls.js에 연결한다.
7. 브라우저가 인증이 필요 없는 `/streams/{serialNumber}/index.m3u8`와 TS 조각을 반복 요청한다.

관리자 확인 요청:

```http
PATCH /api/risk-events/123/status
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "status": "PROCESSING"
}
```

스트림 상태를 별도로 확인할 수도 있다.

```http
GET /api/drone-streams/SIYI-A8-001/status
Authorization: Bearer {accessToken}
```

정상 응답 예시:

```json
{
  "code": "200",
  "message": "성공",
  "data": {
    "streamKey": "SIYI-A8-001",
    "enabled": true,
    "running": true,
    "manifestReady": true,
    "playlistUrl": "http://localhost:8080/streams/SIYI-A8-001/index.m3u8",
    "processId": 42,
    "sourceType": "RTSP",
    "startedAt": "2026-07-26T16:00:00",
    "lastError": null
  }
}
```

수동 시작/중지 API는 장비 점검용이다.

```http
POST   /api/drone-streams/{serialNumber}/start
GET    /api/drone-streams/{serialNumber}/status
DELETE /api/drone-streams/{serialNumber}
```

## 카메라 없이 동일 경로 검증

FFmpeg 자체 테스트 영상을 SIYI 입력 대신 사용한다. BE, HLS, 브라우저 플레이어 경로는 실제 장비 때와 같다.

```powershell
$env:DRONE_RTSP_URL_TEMPLATE="lavfi:testsrc=size=1280x720:rate=30"
docker compose up --build
```

관리자 확인 후 다음 URL이 갱신되면 송출 경로가 정상이다.

```text
http://localhost:8080/streams/{드론시리얼}/index.m3u8
```

실제 장비 모드로 되돌릴 때는 환경 변수를 삭제하고 컨테이너를 다시 만든다.

```powershell
Remove-Item Env:DRONE_RTSP_URL_TEMPLATE
docker compose up --build --force-recreate
```

## 주요 환경 변수

| 변수 | 기본값 | 용도 |
| --- | --- | --- |
| `FFMPEG_PATH` | `ffmpeg` | 실행 파일 경로 |
| `DRONE_RTSP_URL_TEMPLATE` | `rtsp://192.168.144.25:8554/main.264` | 카메라 입력; `{serialNumber}` 사용 가능 |
| `DRONE_STREAM_PUBLIC_BASE_URL` | `http://localhost:8080/streams` | 브라우저에 반환할 공개 주소 |
| `DRONE_STREAM_OUTPUT_DIR` | Docker: `/app/build/hls` | manifest/조각/FFmpeg 로그 |
| `DRONE_STREAM_STARTUP_TIMEOUT` | Docker: `20`초 | 첫 manifest 대기 시간 |
| `DRONE_STREAM_WIDTH` | `1280` | 출력 너비 |
| `DRONE_STREAM_HEIGHT` | `720` | 출력 높이 |
| `DRONE_STREAM_FRAME_RATE` | `30` | 출력 FPS |

FFmpeg 오류 상세는 스트림 볼륨의 `{serialNumber}/ffmpeg.log`와 BE 로그에 남는다.
