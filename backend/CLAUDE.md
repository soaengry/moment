# Moment Backend

Spring Boot 3.5.10 기반 웨딩 초대장 플랫폼 REST API.

## Skills (상세 규칙은 아래 파일 참고)

| 주제 | 파일 |
|------|------|
| 기술 스택, DDD 구조, 엔티티/서비스 패턴, 새 도메인 체크리스트, 환경변수 | [`../.claude/skills/backend-architecture/SKILL.md`](../.claude/skills/backend-architecture/SKILL.md) |
| ApiResponse, 예외 시스템, ErrorCode 네이밍, API 엔드포인트 | [`../.claude/skills/backend-api-conventions/SKILL.md`](../.claude/skills/backend-api-conventions/SKILL.md) |
| JWT, OAuth2, BCrypt, Redis 토큰 관리, S3 | [`../.claude/skills/backend-security/SKILL.md`](../.claude/skills/backend-security/SKILL.md) |
| JUnit 5, Given-When-Then, 테스트 패턴 | [`../.claude/skills/backend-testing/SKILL.md`](../.claude/skills/backend-testing/SKILL.md) |
| ErrorCode/Exception/Entity 코드 템플릿, S3·OAuth2·Redis 추가 워크플로우 | [`../.claude/skills/backend-recipes/SKILL.md`](../.claude/skills/backend-recipes/SKILL.md) |

## Quick Commands

```bash
./gradlew build          # 빌드
./gradlew bootRun        # 실행
./gradlew test           # 테스트
./gradlew clean build    # 클린 빌드
```
