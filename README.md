# AI 생활 비서 MCP

LifeMate(라이프메이트)는 지역과 외출 목적을 바탕으로 최신 날씨·대기질 데이터를 조회해 외출 안전 브리핑, 대기질 확인, 날씨 위험 확인, 활동별 준비물 체크리스트를 제공하는 Spring Boot 기반 MCP 서버입니다.
사용자 입력을 저장하지 않으며, 데이터베이스 없이 외부 구조화 API를 호출해 현재 수치 기반의 생활 판단을 돕습니다.

## 앱 아이콘

![LifeMate 앱 아이콘](assets/lifemate-app-icon-600.png)

이 아이콘은 LifeMate를 위해 새로 생성한 원본 이미지입니다. 말풍선은 대화형 비서, 집과 하트는 일상생활 지원, 반짝임은 AI 기능을 의미합니다.

## MVP 도구

MVP는 **Minimum Viable Product(최소 기능 제품)**의 약자입니다.
사용자가 실제로 이용할 수 있는 핵심 기능만 먼저 구현한 초기 버전을 뜻합니다.

| 도구 | 역할 | 예시 |
|---|---|---|
| `brief_outdoor_safety` | 지역·좌표와 활동 목적을 기준으로 날씨와 대기질을 조회해 외출 안전 등급, 근거, 준비물을 안내 | `오늘 마포구에서 퇴근 후 러닝해도 괜찮아?` |
| `check_air_quality` | PM10, PM2.5, US AQI를 조회해 마스크와 야외활동 주의 여부를 안내 | `오늘 강남 미세먼지 어때? 아이 등원 괜찮아?` |
| `check_weather_risks` | 강수, 자외선, 체감온도, 바람을 조회해 우산·겉옷·자외선 차단 필요 여부를 안내 | `오늘 제주에서 빨래 밖에 널어도 될까?` |
| `recommend_outdoor_checklist` | 날씨와 대기질 수치를 바탕으로 활동별 준비물 체크리스트 생성 | `오늘 퇴근 후 러닝할 때 뭐 챙겨야 해?` |

## MCP로 구현한 이유

LLM의 일반 웹 검색 답변이 아니라 MCP 도구가 외부 구조화 데이터를 직접 조회해 판단합니다.

- 위치명 또는 좌표를 입력받아 조회 지점을 결정합니다.
- Open-Meteo Weather Forecast API에서 기온, 체감온도, 강수량, 강수확률, 자외선, 바람 정보를 조회합니다.
- Open-Meteo Air Quality API에서 PM10, PM2.5, US AQI를 조회합니다.
- 활동 목적이 러닝, 등원, 산책, 빨래 등인지에 따라 같은 수치라도 더 보수적으로 판단합니다.
- 종합 브리핑, 대기질, 날씨 위험, 체크리스트처럼 목적별 도구를 분리해 필요한 수치와 권고만 반환합니다.

## 예시 질문

- `오늘 서울 마포구에서 퇴근 후 러닝해도 괜찮아?`
- `오늘 강남 미세먼지 어때? 마스크 써야 해?`
- `오늘 제주에서 우산 챙겨야 해? 빨래 밖에 널어도 돼?`
- `오늘 아이 등원할 때 준비물 알려줘. 위치는 서울이야.`
- `부산 해운대에서 아이랑 산책해도 될까?`
- `서울 시청 근처 좌표로 아이 등원 브리핑 해줘.`

## 기술 구성

- Java 17
- Spring Boot 3.5
- Spring AI MCP Server WebMVC
- Stateless Streamable HTTP
- Gradle
- Open-Meteo Geocoding / Weather Forecast / Air Quality API

## 로컬 실행

Windows:

```powershell
.\gradlew.bat bootRun
```

macOS/Linux:

```bash
./gradlew bootRun
```

기본 포트는 `8080`, MCP 엔드포인트는 `/mcp`, 상태 확인은 `/actuator/health`입니다.

## IntelliJ HTTP Client 테스트

서버 실행 후 `http/lifestyle-assistant.http`를 열어 각 요청 왼쪽의 실행 버튼을 누릅니다.
Stateless 방식이므로 세션 ID를 복사하거나 유지할 필요가 없습니다.
IntelliJ 버전에 따른 변수 인식 문제를 피하기 위해 각 요청에는 전체 URL을 직접 표기했습니다.

## 빌드와 테스트

```powershell
.\gradlew.bat clean test bootJar
```

## Docker

```bash
./gradlew clean bootJar
docker build -t ai-lifestyle-assistant .
docker run --rm -p 8080:8080 ai-lifestyle-assistant
```

## PlayMCP 등록 메모

- 전송 방식: Stateless Streamable HTTP
- MCP URL: 배포 주소의 `/mcp`
- Health check: 배포 주소의 `/actuator/health`
- 도구 이름과 서버 설명에 특정 플랫폼명을 넣지 않습니다.
- 도구는 사용자 데이터를 저장하지 않으며, 결제·예약·외부 시스템 변경을 수행하지 않습니다.
- `docs/playmcp-checklist.md`에서 개발가이드 반영 여부를 확인할 수 있습니다.

## 안전 안내

- 이 도구는 생활 참고용 외출 브리핑입니다.
- 의료 판단, 재난 판단, 공식 기상특보를 대신하지 않습니다.
- 기상특보, 재난문자, 현지 안내, 건강 이상 증상은 공식 안내와 전문가 판단을 우선하세요.
- 민감군(영유아, 고령자, 임산부, 호흡기·심혈관 질환자)은 더 보수적으로 판단해야 합니다.
