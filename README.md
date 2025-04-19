# Echo WebSocket 시스템 - README

## 1. 프로젝트 개요
TCP 기반 Echo 통신 시스템을 WebSocket + STOMP 구조로 구현하였다. 클라이언트는 옵션을 선택하여 메시지를 입력하고, 서버는 옵션에 따라 처리된 메시지를 다시 클라이언트로 전송한다.

## 2. 프로토콜 설계

### 2-1. 패킷 형식 설명
- ✅ 클라이언트 → 서버 패킷 (ClientPacket)
```json
{
  "username": "jungjiyu",
  "option": 2,
  "message": "hiHI"
}
```
- ✅ 서버 → 클라이언트 정상 패킷 (ServerPacket)
```json
{
  "status": 200,
  "username": "jungjiyu",
  "message": "HIHI"
}
```
- ✅ 서버 → 클라이언트 오류 패킷 (ErrorPacket)
```json
{
  "status": 403,
  "error": "option input error: integer greater than 3"
}
```

### 2-2. 패킷 송/수신 순서
1. 클라이언트는 `/pub/echo`로 메시지를 전송한다.
2. 서버는 메시지를 처리한 뒤 `/sub/echo/{username}` 또는 `/sub/error/{username}` 경로로 응답을 전송한다.

### 2-3. 패킷 송수신 시 동작
- 옵션(1~3)은 각각 일반, 대문자, 소문자 처리 후 응답.
- 옵션 4는 연결 종료.
- 잘못된 옵션 입력 시 상태 코드(401~403) 및 오류 문구 포함하여 에러 패킷 반환.
- 모든 응답은 클라이언트 화면에 실시간 출력.

## 3. 개발 환경 및 사용 기술
- ✅ 언어: Java 17
- ✅ 프레임워크: Spring Boot 3.x
- ✅ 클라이언트: HTML + JavaScript + STOMP.js
- ✅ 서버 포트: 8788
- ✅ WebSocket endpoint: `/ws`
- ✅ STOMP 발행 경로: `/pub/echo`
- ✅ STOMP 구독 경로: `/sub/echo/{username}`, `/sub/error/{username}`

## 4. 프로그램 실행 방법

### 4-1. 서버 실행
1. Spring Boot 프로젝트를 IDE 또는 CLI에서 실행
2. `http://localhost:8788/index.html`에 접속

### 4-2. 클라이언트 테스트
1. 사용자 이름 입력 → WebSocket 연결
2. 옵션 선택 + 메시지 입력 → Echo 요청 전송
3. 처리된 결과가 하단 로그 영역에 출력
4. 옵션 4 입력 시 연결 해제

### 4-3. ⚠️ 주의 사항

GitHub 리포지토리에는 보안상의 이유로 다음 파일들이 포함되어 있지 않음:

- private-key.pem, public-key.pem (JWT 인증에 사용)
- firebase-service-key.json (FCM 연동용)

정상적인 동작을 위해 위 파일들을 프로젝트 루트 또는 지정된 경로에 수동으로 추가해야 함

## 5. 실행 화면 
![image](https://github.com/user-attachments/assets/c1d64750-f88d-44b4-b97e-6979e27ed9c8)

```
Connected: CONNECTED user-name:jungjiyu heart-beat:0,0 version:1.1

요청 전송 - 옵션: 1, 메시지: hi this is msg1
[jungjiyu] Echo result: hi this is msg1

요청 전송 - 옵션: 2, 메시지: hi this is msg2
[jungjiyu] Echo result: HI THIS IS MSG2

요청 전송 - 옵션: 3, 메시지: hi this is msg3
[jungjiyu] Echo result: hi this is msg3

요청 전송 - 옵션: 4, 메시지: hi this is msg4
종료 요청됨. 연결 해제합니다.

Disconnected.
```

## 6. 디렉토리 구성 및 압축 파일 안내
```
📦 socket-echo-project
├── 📁 client
│   └── index.html
├── 📁 server
│   ├── EchoController.java
│   ├── EchoService.java
│   ├── dto/
│   │   ├── ClientPacket.java
│   │   ├── ServerPacket.java
│   │   └── ErrorPacket.java
│   └── WebSocketConfig.java
├── README.pdf
└── 실행화면캡처.png
```
