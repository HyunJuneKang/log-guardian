# Backend Day1: 엔티티와 샘플 API

[이슈 #3](https://github.com/LogGuardianShinhan/log-guardian/issues/3)의 백엔드 완료 기준입니다. API 모양은 [실행 계획서 5장](implementation-plan.md)을 따릅니다.

## 실행과 설정

README의 PostgreSQL 실행 절차 후 `backend`에서 `./gradlew bootRun` 또는 Windows의 `.\gradlew.bat bootRun`을 실행합니다. 백엔드 포트는 `8080`, PostgreSQL 접속 포트는 `5434`입니다.

분석일은 `backend/src/main/resources/application.properties`의 `app.analysis-date=2026-10-08` 한 곳에서 지정하고 `AppProperties`로 읽습니다. JSON 날짜는 `yyyy-MM-dd`, 시각은 `yyyy-MM-dd'T'HH:mm:ss`이며 한국 시간(`Asia/Seoul`)을 사용합니다. 필드 이름은 camelCase, 값이 없으면 `null`, 빈 목록은 `[]`입니다.

## DB 테이블

`account`, `access_log`, `alert`, `agent_audit`를 `public` 스키마에 생성합니다. 로컬 개발의 `ddl-auto=update`가 엔티티 변경을 반영합니다. 계정 참조는 외래 키이며 `(run_id, account_id)`에 유일 제약을 둡니다. `alert.rules`와 `alert.evidence`는 JSONB, `agent_audit.params`는 TEXT, 업무 시각은 `timestamp without time zone`입니다.

## 화면용 샘플 API

| 방식 | 주소 | 예시 파일 |
| --- | --- | --- |
| POST | `/api/analysis/run` | [분석 결과](../contracts/analysis-run-response.json) |
| GET | `/api/alerts` | [목록](../contracts/alerts-list-response.json) |
| GET | `/api/alerts/12` | [상세](../contracts/alert-detail-response.json) |

Day1은 메모리의 가짜 결과 한 건을 반환합니다. 실제 분석이나 DB 결과 저장은 후속 일정에서 구현합니다. 결과는 HIGH 한 건이므로 candidateCount=1, highCount=1, mediumCount=0, lowCount=0입니다. `alertId=12`는 샘플 식별자입니다. 파일의 `runId`는 형식 예시이며 실행 때는 현재 한국 날짜와 UUID로 새 번호를 만듭니다. 분석일과 실행일은 다를 수 있습니다.

서버를 켠 직후 목록은 `runId=null`, `alerts=[]`입니다. 분석 실행 후 목록·상세는 같은 실행 번호를 반환하고, 다시 실행하면 최신 샘플로 갱신합니다. 서버를 재시작하면 샘플 상태는 초기화됩니다. 샘플 API는 아직 로그인 인증을 요구하지 않습니다.

```powershell
Invoke-RestMethod http://localhost:8080/api/alerts
Invoke-RestMethod -Method Post http://localhost:8080/api/analysis/run
Invoke-RestMethod http://localhost:8080/api/alerts
Invoke-RestMethod http://localhost:8080/api/alerts/12
```

오류는 공통 `code`, `message` DTO로 반환합니다. 존재하지 않는 의심 건은 404 `ALERT_NOT_FOUND`, 숫자 형식이 아닌 ID는 400 `INVALID_REQUEST`, 알 수 없는 주소는 404 `NOT_FOUND`, 예상하지 못한 서버 오류는 500 `INTERNAL_SERVER_ERROR`입니다. [오류 예시](../contracts/error.json)를 참고합니다.

## AI 담당에게 전달할 내부 응답 예시

Day1에서는 아래 파일로 응답 모양을 공유합니다. 내부 HTTP 엔드포인트의 실제 조회·인증·감사 기록은 Day3에 구현합니다.

| 요청 | 응답 예시 |
| --- | --- |
| `GET /internal/logs?accountId=partner_017&from=2026-10-08T00:00:00&to=2026-10-09T00:00:00` | [특정 계정 로그](../contracts/internal-logs-response.json) |
| `GET /internal/logs/all?from=2026-09-25T00:00:00&to=2026-10-09T00:00:00` | [전체 로그](../contracts/internal-logs-all-response.json) |
| `GET /internal/accounts/emp_011/baseline?before=2026-10-08` | [평소 패턴](../contracts/internal-baseline-response.json) |
| `GET /internal/accounts/partner_017` | [계정](../contracts/internal-account-response.json) |

로그는 시각순이며 `from` 포함·`to` 제외입니다. 특정 계정 로그에는 요청한 계정의 기록만 넣습니다. 조회 대상이 없는 로그인은 `target=null`입니다. 기준 기간은 before 이전 13일, 요일 평균에는 MON~SUN이 항상 포함됩니다. 평균·배수는 소수 한 자리, 평균이 0이면 ratio는 null입니다. 후속 서버 통신은 `X-Internal-Key`와 `X-Run-Id`를 사용합니다.

## 검증

DB가 healthy 상태일 때 `backend`에서 `.\gradlew.bat test bootJar`를 실행합니다. 엔티티 테스트는 별도의 임시 스키마에서 JSONB 왕복 저장·시간 보존·중복 제약을 검사합니다. 샘플 API 테스트는 실제 HTTP로 JSON 파일과 응답을 비교하며 분석일 설정, 빈 목록, 재실행 번호, 공통 오류 형식을 확인합니다.

프론트와 AI는 이 예시를 이용해 독립 개발을 시작할 수 있습니다. 로그인·CSV·AI 호출·내부 조회·승인 동시성은 이후 일정에서 구현합니다. PRD와 API 약속의 팀 합의 및 세 서버 공동 실행 확인은 팀 통합 단계에서 진행합니다.
