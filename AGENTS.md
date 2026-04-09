# AGENTS.md (Codex project instructions)
## 작업 로그(필수)
- Codex로 코드 수정 또는 기술 질의(Q&A)를 수행한 경우, 아래 로그 파일을 생성/업데이트한다.
- 로그 위치: docs/ai/codex/YYYY/MM/YYYY-MM-DD.md
- 규칙:
    1) 같은 날짜 파일이 있으면 "append(추가)"한다(기존 내용 유지).
    2) 각 엔트리는 타임스탬프, 작업 유형, 변경 요약, 변경 파일, 검증, 참고(질문/답변)를 포함한다.
    3) 민감정보(토큰/개인정보/내부 URL)는 기록하지 않는다.
## 반드시 준수
- 레이어 책임: Controller/Biz/Service/Mapper 규칙 준수 (TEAM_CONVENTION.md)
- 포맷/스타일: STYLE_GUIDE.md 기준. 포맷은 툴 결과가 정답.
- 리팩토링: REFACTORING_PLAYBOOK.md 절차(작게/동작보존/검증포인트)
## 변경 원칙
- 최소 변경(필요 범위만)
- PR에는 변경 목적/영향/검증 포인트를 포함
## 참고 문서
- docs/team/TEAM_CONVENTION.md
- docs/team/STYLE_GUIDE.md
- docs/team/REFACTORING_PLAYBOOK.md
## 실행(예시)
- 포맷: ./gradlew spotlessApply
- 정적분석/테스트: ./gradlew test
- (MyBatis) 필요한 경우: ./gradlew :module-name:test
## 프롬프트
- 요청이 명확하지 않을때 제대로 이해했는지 이해한 내용을 바탕으로 설명해서 제대로 이해한게 맞는지 확인 반드시 필요