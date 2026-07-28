# CheeseJiggler (치즈지글러) 🧀

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![SDK](https://img.shields.io/badge/SDK-24%2B-blue)

**CheeseJiggler**는 자리를 비우는 동안 안드로이드 기기의 화면이 잠기는 것을 방지하여, 다시 돌아왔을 때 재인증(비밀번호, 지문 등)을 거쳐야 하는 번거로움을 해결하기 위해 개발된 앱입니다.

**CheeseJiggler** is an Android application designed to prevent your device screen from locking while you're away, eliminating the inconvenience of repeated re-authentication (passwords, biometrics, etc.).

---

## 💡 개발 취지 (Motivation)

> "업무 연속성과 화면 켜짐을 안정적으로 유지해 작업 흐름이 끊기지 않도록 돕는 스마트한 유틸리티입니다. 정숙한 관리가 필요한 작업 환경에 최선의 효율을 제공합니다."

많은 사용자들이 업무나 작업 중 잠시 자리를 비울 때 화면이 잠겨 매번 다시 로그인해야 하는 상황을 겪습니다. 

화면을 모니터링 하는 경우에도 쉽게 잠길 때가 있구요. 

CheeseJiggler는 이러한 작은 불편함을 해소하고자 하는 의도에서 시작되었습니다.

Many users experience the frustration of their screen locking and having to log back in every time they step away for a moment. CheeseJiggler was born out of a simple desire to resolve this minor but recurring inconvenience.

---

## ✨ 주요 기능 (Key Features)

- **화면 켜짐 유지 (Stay Awake)**: 앱이 작동하는 동안 시스템의 화면 자동 꺼짐 설정을 무시하고 화면을 계속 켭니다.
- **다양한 움직임 패턴 (Jiggle Patterns)**: 화면 번인(Burn-in) 방지 및 활성 상태 유지를 위해 다양한 애니메이션 패턴을 제공합니다.
    - 직선 하강 (Basic), 원형 (Circle), 지그재그 (Zigzag), 미세 떨림 (Micro)
- **유연한 타이머 설정 (Flexible Timers)**:
    - **무제한**: 수동 정지 전까지 계속 작동
    - **시간 지정**: 입력한 시간(분) 동안 작동
    - **종료 시각**: 지정한 시각(예: 퇴근 시간)에 자동 종료
    - **시간 범위**: 특정 시간대(예: 업무 시간)에만 작동하도록 예약
- **스텔스 모드 (Stealth Mode)**: 일정한 주기로 작동과 휴식을 반복하여 더욱 자연스럽게 동작합니다.
- **화면 밝기 제어 (Brightness Control)**: 앱 작동 시 화면 밝기를 고정하여 배터리 소모를 최적화할 수 있습니다.
- **커스텀 배경 패턴 (Tile Patterns)**: 격자, 스트라이프, 도트 등 다양한 배경 스타일을 선택할 수 있습니다.

---

## 🛠 기술 스택 (Tech Stack)

- **Language**: Kotlin
- **UI Framework**: Android View System & Material 3 (with Compose Support enabled)
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 16 (API 36 - *Preview*)

---

## 🚀 시작하기 (Getting Started)

1. 이 저장소를 클론합니다.
   ```bash
   git clone https://github.com/your-username/CheeseJiggler.git
   ```
2. **Android Studio**에서 프로젝트를 엽니다.
3. 기기 또는 에뮬레이터에 빌드 및 설치합니다.

---

## ⚠️ 유의사항 (Disclaimer)

이 앱은 사용자의 편의를 위해 개발되었습니다. 보안 정책이 엄격한 환경에서는 사용에 주의하시기 바랍니다.

This app is developed for user convenience. Please use it responsibly and be mindful of security policies in your environment.

---

## 📄 라이선스 (License)

이 프로젝트의 라이선스는 별도로 명시되지 않은 경우 개인적인 학습 및 참조 용도로만 사용 가능합니다.

Unless otherwise specified, this project is for personal learning and reference purposes only.
