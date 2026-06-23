# AI 생활 비서 MCP

LifeMate(라이프메이트)는 일상에서 반복되는 생활 문제를 짧은 대화로 해결하는 Spring Boot 기반 MCP 서버입니다.
외부 AI API 키나 데이터베이스 없이 바로 실행되며 사용자 입력을 저장하지 않습니다.

## 앱 아이콘

![LifeMate 앱 아이콘](assets/lifemate-app-icon-600.png)

이 아이콘은 LifeMate를 위해 새로 생성한 원본 이미지입니다. 말풍선은 대화형 비서, 집과 하트는 일상생활 지원, 반짝임은 AI 기능을 의미합니다.

## MVP 도구

MVP는 **Minimum Viable Product(최소 기능 제품)**의 약자입니다.
사용자가 실제로 이용할 수 있는 핵심 기능만 먼저 구현한 초기 버전을 뜻합니다.
이 프로젝트에서는 아래 네 가지 기능이 생활 비서의 가능성을 검증하기 위한 MVP에 해당합니다.

| 도구 | 역할 | 예시 |
|---|---|---|
| `suggest_meals_from_ingredients` | 냉장고 재료와 조리시간으로 메뉴 추천 | `계란, 밥, 김치로 15분 안에 뭐 해 먹지?` |
| `split_group_expenses` | 모임 지출 N분의 1 및 송금 목록 생성 | `민수 2만원, 영희 3만원, 철수 1만원 정산해줘` |
| `guide_korean_life_event` | 결혼·부고·돌잔치 금액 기준과 예절 안내 | `회사 대리님 결혼식 축의금 얼마가 무난해?` |
| `draft_message` | 연차·거절·사과·일정 변경 메시지 작성 | `팀장님께 연차를 정중하게 다시 요청하고 싶어` |

## 기술 구성

- Java 17
- Spring Boot 3.5
- Spring AI MCP Server WebMVC
- Stateless Streamable HTTP
- Gradle

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
- 모든 도구는 읽기 전용이며 외부 시스템 변경이나 결제를 수행하지 않습니다.
- `docs/playmcp-checklist.md`에서 개발가이드 반영 여부를 확인할 수 있습니다.

## 설계 원칙

- 사용자 입력과 개인정보를 저장하지 않습니다.
- 경조사 금액은 정답이 아닌 참고 범위로 안내합니다.
- 음식 알레르기와 식재료 안전은 사용자가 직접 확인해야 합니다.
- 메시지는 초안이며 보내기 전에 이름, 날짜와 사실관계를 확인해야 합니다.

## 다음 확장 후보

1. 사용자가 직접 설정하는 냉장고 재고와 유통기한 알림
2. 영수증 이미지 OCR 및 항목별 차등 정산
3. 공휴일 기반 연차·황금연휴 계산
4. 지도 기반 여행 동선 추천
5. 사용자가 명시적으로 동의한 경우에만 일정·알림 등록
