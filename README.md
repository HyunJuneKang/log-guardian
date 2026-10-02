# 로그지킴이 (Log Guardian)

직원·협력사 계정의 접속 기록에서 수상한 행동을 찾고, AI가 한국어 보고서를 작성하는 3인 팀 프로젝트입니다. 합성 로그를 사용하며 계정 잠금은 데모 DB에서만 실행합니다.

## 프로젝트 구조

```text
log-guardian/
├── frontend/    # React 화면
├── backend/     # Spring Boot API와 DB
├── agent/       # FastAPI 이상 탐지 에이전트
└── docs/        # 실행 계획과 API 약속
```

| 담당 | 구성 요소 | 개발 포트 | 주요 책임 |
| --- | --- | --- | --- |
| A | frontend | 5173 | 로그인, 의심 목록, 보고서, 채팅, 승인 화면 |
| B | backend | 8080 | 로그인, DB, AI 호출, 승인 처리, AI 조회 기록 |
| C | agent | 8000 | 합성 로그 생성, 탐지 규칙, AI 조사와 보고서 |
| B | PostgreSQL | 5434 (PC 접속) → 5432 (컨테이너 내부) | DB `logguardian`: 계정·접속 로그·의심 건·AI 활동 기록 저장 |

실행 계획과 API 약속은 [3인 팀 실행 계획](docs/implementation-plan.md)을 참고합니다.

## 현재 상태

프론트엔드 초기 프로젝트와 백엔드 Day1 엔티티·테이블 4개, 분석 실행·목록·상세 샘플 API를 준비했습니다. 백엔드는 Java 21, Spring Boot 4.1.1, Gradle Wrapper를 사용합니다. FastAPI 실행 방법은 에이전트 프로젝트 생성 후 추가합니다.

백엔드의 샘플 API 호출 순서와 내부 응답 예시는 [Backend Day1 안내](docs/backend-day1.md)에 정리했습니다. 응답 JSON은 `contracts/`의 해당 파일을 참고합니다. 팀 전체의 PRD·API 합의와 FastAPI 등 남은 버전·실행 명령 확정은 통합 단계에서 진행합니다.

### 프로젝트 버전

현재 설정 파일 기준입니다. 프론트엔드의 설치 버전은 `package-lock.json`을 기준으로 기록하며, 팀원은 `npm ci`로 동일한 버전을 설치합니다.

| 구성 요소 | 버전 | 확인 기준 |
| --- | --- | --- |
| PostgreSQL | **17.10** | `compose.yaml`: `postgres:17.10` |
| Java | **21** | `backend/build.gradle` toolchain |
| Spring Boot | **4.1.1** | `backend/build.gradle` |
| Gradle Wrapper | **9.7.1** | `backend/gradle/wrapper/gradle-wrapper.properties` |
| Spring dependency-management 플러그인 | **1.1.7** | `backend/build.gradle` |
| React / React DOM | **19.3.0** | `frontend/package-lock.json` |
| React Router DOM | **7.18.4** | `frontend/package-lock.json` |
| Vite | **8.3.2** | `frontend/package-lock.json` |
| TypeScript | **6.0.3** | `frontend/package-lock.json` |
| FastAPI / LangGraph | 미확정 | 에이전트 프로젝트 구성 후 기록 |

### 개발 도구 설치 상태

팀에서 공유한 설치 버전입니다.

| 설치된 것 | 버전 |
| --- | --- |
| git | 2.x.x |
| java | 21.x.x |
| node | 24.x.x |
| python | 3.14.6 |
| postgresql | 17.10 |
| docker | 29.8.0 |

PostgreSQL 17.10의 기동·연결·재시작 후 데이터 유지와 테이블 4개 생성을 확인했습니다. 백엔드 테스트와 실행 방법은 아래 및 [Backend Day1 안내](docs/backend-day1.md)를 참고합니다.

## PostgreSQL 개발 환경

팀원은 각자 로컬 Docker에서 PostgreSQL 17.10을 실행합니다. 호스트 포트는 `5434`, DB 이름은 `logguardian`입니다. 다른 프로젝트의 DB와 별도 Compose 프로젝트 및 볼륨을 사용하며, DB 접속은 로컬 컴퓨터에서만 허용합니다.

Docker Desktop을 실행하고 저장소 루트에서 다음 명령을 실행합니다. `.env`가 이미 있다면 복사 단계를 건너뛰세요.

```powershell
Copy-Item .env.example .env
docker compose up -d --wait
docker compose ps
```

`.env`의 계정과 비밀번호는 로컬 개발용 샘플입니다. 실제 비밀 값은 커밋하지 않습니다. 포트 `5434`가 이미 사용 중이면 컨테이너를 중지하지 말고 충돌을 먼저 확인하세요.

### 백엔드 실행

JDK 21을 설치하고 `JAVA_HOME`을 JDK 21로 설정합니다. DB가 healthy 상태가 된 후 실행합니다.

```powershell
cd backend
.\gradlew.bat bootRun
```

IntelliJ에서는 저장소 루트를 열고 `backend/build.gradle`을 Gradle 프로젝트로 연결합니다. Project SDK와 Gradle JVM을 JDK 21로 설정하고 `BackendApplication`을 실행합니다. 작업 디렉터리는 저장소 루트 또는 `backend`여야 합니다. 두 위치 모두 루트 `.env`를 읽도록 구성했습니다. 실행 설정에 기존 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 환경변수가 있다면 `.env`보다 우선하므로 같은 값으로 맞추거나 제거하세요. `.env`는 Docker Compose와 Spring이 각각 읽으며, PowerShell 환경변수로 자동 등록되지는 않습니다.

기본 서버 포트는 `8080`입니다. 샘플 API는 `/api/analysis/run`, `/api/alerts`, `/api/alerts/{id}`입니다. `/`의 404 응답은 정상입니다. 로컬 데모 개발에서는 `ddl-auto=update`로 실행 시 엔티티에 맞춰 누락된 테이블과 칼럼을 생성합니다. `account`, `access_log`, `alert`, `agent_audit` 테이블은 PostgreSQL `public` 스키마에서 확인할 수 있습니다. 이 설정은 운영 DB의 스키마 마이그레이션 용도로 사용하지 않습니다.

DB를 실행한 상태에서 기존 Spring 컨텍스트 테스트와 빌드를 확인합니다.

```powershell
.\gradlew.bat test bootJar
```

### DB 관리

```powershell
# 저장소 루트에서 DB에 접속
 docker compose exec db psql -U logguardian -d logguardian
# 중지 후 다시 실행해도 데이터 유지
 docker compose stop
 docker compose up -d --wait
```

데이터는 `postgres_data` named volume에 저장됩니다. `docker compose down`도 볼륨을 보존합니다. **`docker compose down -v`는 데이터를 삭제하므로 초기화가 필요할 때만 사용합니다.** 최초 초기화 후 `.env`의 DB 이름·계정·비밀번호를 바꾸어도 기존 볼륨의 DB 계정이 자동 변경되지는 않습니다.

DB 시간대는 `Asia/Seoul`입니다. 계획서의 시간 약속에 따라 업무 시각은 PostgreSQL `timestamp without time zone`과 Java `LocalDateTime`으로 다룹니다.

## 요청 흐름

화면은 Spring Boot만 호출합니다. Spring Boot가 FastAPI에 분석·채팅을 요청하고, FastAPI는 Spring Boot의 내부 조회 API를 통해 로그를 읽습니다. 승인 요청은 Spring Boot에서 검증한 뒤 데모 DB에 반영합니다.

## 협업

- 각 담당자는 자기 서버 폴더에서 작업합니다.
- `main`에는 실행 가능한 코드를 유지하고, 기능 브랜치에서 작업한 뒤 PR로 합칩니다.
- API 약속을 변경할 때는 팀원과 합의하고 문서를 먼저 수정합니다.
- 비밀 값은 커밋하지 않습니다. 환경변수가 필요해지면 실제 값은 `.env`에, 변수 이름과 샘플 값은 `.env.example`에 기록합니다.

## 개발 기준

- 모든 서버와 DB의 시간대는 `Asia/Seoul`로 맞춥니다.
- 분석일은 합성 데이터의 마지막 날인 `2026-10-08`로 고정합니다.
- 조회 기간은 `from` 포함, `to` 제외로 처리합니다.
- AI는 로그 읽기만 수행하고, 계정 잠금은 사람의 승인으로 실행합니다.
