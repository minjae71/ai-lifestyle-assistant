# PlayMCP 개발가이드 점검

기준 문서: PlayMCP 서버 개발가이드, 2026년 6월 12일 업데이트

## 코드에 반영된 항목

- MCP 프로토콜 `2025-03-26` 이상을 지원하는 공식 MCP SDK 기반
- Stateless Streamable HTTP 방식과 `/mcp` 엔드포인트 사용
- 서버명과 Tool Name에 금지어 `kakao` 미사용
- Tool Name은 영문 소문자, 숫자와 underscore만 사용
- 도구는 1개이며 외부 구조화 데이터 조회 목적을 명확히 함
- 모든 도구에 `name`, `description`, `inputSchema`, `annotations` 제공
- 모든 annotation에 `title`, `readOnlyHint`, `destructiveHint`, `idempotentHint`, `openWorldHint` 지정
- description마다 서비스 고유명사 `LifeMate(라이프메이트)` 병기
- description은 1,024자 이내이며 한국어로 간결하게 작성
- Open-Meteo 외부 API 원문 대신 필요한 필드만 담은 작은 결과 모델 반환
- 사용자 인증과 개인정보 저장 없음
- 광고 노출이나 광고 클릭 유도 없음

## 배포·등록 단계에서 확인할 항목

- 공개 HTTPS 도메인으로 배포
- MCP Inspector로 초기화, 도구 목록과 도구 호출 점검
- 외부 API 호출 지연을 고려해 평균 응답과 p99 응답 시간 점검
- PlayMCP 개발자 콘솔의 MCP 식별자와 서비스명 확정
