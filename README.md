# 첫길메이트 (FirstRoad Mate) · 초기 앱 v0.1

초보 운전자를 위한 Pleos Connect 차량용 앱의 초기 버전입니다. 기획서: [../기획서_첫길메이트_초안.md](../기획서_첫길메이트_초안.md)

| 화면 | 와이어프레임 | 구현 |
|---|---|---|
| A. 홈 / 경로 빌더 | 기획서 6.3-A | `ui/home/HomeScreen.kt` |
| B. 주행: 다음 행동 카드 | 6.3-B | `ui/drive/DriveScreen.kt`, `component/NextActionCard.kt` |
| C. 경고등 통역사 카드 | 6.3-C | `ui/drive/component/WarningCard.kt` |
| D. 하차 체크 + 성장 리포트 | 6.3-D | `ui/park/ParkScreen.kt` |

렌더링 결과는 [../docs/images/screens/](../docs/images/screens/)에 있습니다. `WireframeScreenshotTest`가 생성합니다.

## 빌드와 실행

필요한 도구는 `C:\sdk`에 설치되어 있습니다: JDK 21(`C:\sdk\jdk-21`), Android SDK(`C:\sdk\android`), Gradle 8.14.6.

```powershell
$env:JAVA_HOME = 'C:\sdk\jdk-21'
.\gradlew.bat :app:assembleDebug        # APK: app\build\outputs\apk\debug\app-debug.apk
.\gradlew.bat :app:testDebugUnitTest    # 단위 테스트 + 와이어프레임 스냅숏
```

Android Studio에서는 이 폴더를 열면 됩니다. `local.properties`의 `sdk.dir`이 `C:\sdk\android`를 가리킵니다.

## 동작 모드

| 모드 | 언제 | 데이터 |
|---|---|---|
| 실차 | Pleos Connect 기기·에뮬레이터 (Vehicle Service 연결됨) | Pleos Vehicle · NaviHelper · FusedLocation · TTS/STT + AAOS 차속·기어·주행 제한 |
| 데모 | 그 밖의 환경 (일반 에뮬레이터, 심사 PC) | '이준호의 두 번째 출근' 120초 리플레이 (`data/datasource/demo/DemoTripScript.kt`) |

- 기본값은 자동 선택(AUTO)입니다. 데모일 때는 화면 위에 '데모 모드' 배지가 보입니다.
- 음성 인식이 없는 환경에서는 화면 아래 '음성 시뮬레이션' 버튼으로 음성 명령 12종을 대신 말할 수 있습니다.
- 데모 시나리오 0~120초: 브리핑 · 5초 뒷좌석 창문 열림 · 14초 합류 1km 전 차선 준비 · 26초 진정 모드 · 34.5초 깜빡이 알림 → 칭찬 · 37초 긴장 구간 통과(실내 복원) · 39초 판교IC 1km 전 차선 준비 · 45초 타이어 경고(휴게소 경유 질문) · 55초 판교IC 깜빡이 미리 켬 → 칭찬 · 72초 급제동 · 91초 좌회전 깜빡이 알림(놓침) · 112초 도착 · 114초 P단 → 하차 체크.

## 장소 검색 API 키 (선택)

경유지 후보(주차장·주유소·휴게소·정비소) 검색에는 카카오 로컬 REST API를 씁니다. 키가 없으면 샘플 장소로 동작합니다.

```properties
# local.properties (저장소에 올리지 않음)
PLACE_API_KEY=발급받은_REST_API_키
```

## 구조 (팀 Android 개발 표준 적용)

```
com.firstroad.mate
├── data/        datasource(pleos · demo · voice · settings) · local(Room) · network(Retrofit + dto + ApiResult) · repository(...Impl)
├── domain/      model · repository(인터페이스) · usecase(순수 업무 규칙)
├── di/          Hilt 모듈 (@Binds / @Provides / @Qualifier @PleosSource @DemoSource)
├── ui/          theme · common · navigation · home · drive · park (Screen + ViewModel + UiState)
└── utils/       GeoMath
```

- 의존 방향: `ui → domain ← data`. Pleos SDK 타입은 `data/datasource/*/pleos` 밖으로 나가지 않습니다.
- 주행 중 판단(코칭, 급제동, 경고, 긴장, 도착)은 `domain/usecase/DriveTickUseCase`가 순수 함수로 수행합니다. ViewModel은 그 결과 이벤트를 화면·음성·Repository 호출로 옮기기만 합니다.
- 표준과 다르게 적용한 부분: minSdk 28(Pleos 요구사항, 에뮬레이터 API 34), 외부 API DTO는 BaseResponse 대신 원래 응답 형식, 로그인·자동 업데이트·WebSocket 규정은 해당 없음.

## 검증

| 검증 | 방법 | 결과 (2026-10-26) |
|---|---|---|
| 컴파일 | `assembleDebug` (Pleos SDK 실제 바이너리와 함께) | 성공, 경고 0 |
| 업무 규칙 | 코칭 타이밍, 음성 명령 12종 해석, 경고 안전 분류, 하차 점검, 레벨, 경로 추천 | 통과 |
| 시나리오 | `DriveTickScenarioTest`: 데모 120초 전체를 리듀서에 흘려 기획서 5장 사건 확인 + 급제동 오탐 회귀 | 통과 |
| 음성 흐름 | `GuidanceSpeakerTest`: 경고 뒤 질문 대기열, 질문 뒤 듣기, 다시 듣기 강제 재생, 대답 보존 | 통과 |
| 화면 | `WireframeScreenshotTest`: 화면 A~D 렌더링 (Robolectric + Roborazzi) | 통과, 이미지 확인 |
| 합계 | 테스트 클래스 9개 | 44개 전부 통과 |
| 레이어 규칙 | import 검사 (ui↛data, data↛ui, domain 순수, SDK 격리, 금지 비동기 API, 파일당 public 클래스 1개) | 위반 0 |

### 교차 검토로 고친 것

독립 검토(코드 표준·버그 / 기획서 설득력) 두 건의 지적을 반영했습니다.

- 음성: 질문 뒤 자동 듣기가 자기 작업을 취소하던 문제, Pleos TTS의 이전 발화 이벤트가 새 발화를 끝내던 문제, STT 리스너 누수, SDK 콜백 무한 대기(→ 3초 timeout), 경고 직후 질문이 묵살되던 문제, '다시 듣기'가 간격 규칙에 막히던 문제
- 실차 데이터: 첫 스냅숏(값 없음)으로 "모두 괜찮아요"라고 말하던 문제, 다른 신호 틱 때문에 생기던 급제동 오탐, 실내 조절 누적(→ 복원), 주행 종료 이중 저장, 지난 주차 메모가 보이던 문제, 내비 상태 경합
- 안전: 모르는 경고등을 '참고'로 안내하던 분류, 타이어 경고의 "계속 가셔도 돼요", 바로 정차 등급에 정비소 경유 제안 → 보수적 분류와 쉴 곳 제안으로 변경
- 코칭: 차선 준비 안내가 일반 내비보다 늦던 타이밍(15초 → 25초/350m, 고속도로 1km), 문구를 제안형으로
- 범위: 문 잠금 점검 제거(차량 기본 기능과 중복), 주행 화면 스크롤 제거·마이크 하단 고정

## 아직 확인하지 못한 것

- **Pleos Connect 에뮬레이터·실차 동작**: 실차 소스(`data/datasource/*/pleos`)는 SDK 바이너리로 컴파일만 검증했습니다. 이벤트 순서, 권한 승인, `poiId` 생략 가능 여부는 에뮬레이터에서 확인해야 합니다.
- **차속·기어**: Pleos Vehicle SDK에 API가 없어 AAOS `CarPropertyManager`로 읽습니다. Pleos 에뮬레이터에서 값이 들어오는지 확인이 필요합니다.
- **제어 권한**: 창문 닫기·공조 조절은 App Market 승인 범위에 따라 거부될 수 있고, 그 경우 '직접 확인해 주세요' 안내로 대체됩니다.
- **실제 기기 화면·음성**: 화면은 Robolectric 렌더링으로, 음성 흐름은 가짜 음성 엔진으로 검증했습니다. 실제 기기에서 TTS 재생과 마이크 인식은 아직 확인하지 않았습니다.
