# WelcomeActivity 카드 스텝형 가이드 구현 계획

`WelcomeActivity`를 단순한 안내 화면에서 `ViewPager2`를 이용한 카드형 스텝 가이드로 변경합니다. 마우스 배치 방법과 센서 호환성(광학 마우스)에 대한 안내를 시각적으로 전달합니다.

## 제안된 변경 사항

### [UI/Layout]

#### [NEW] [item_welcome_page.xml](file:///Users/nicsy/AndroidStudioProjects/CheeseJiggler-main/app/src/main/res/layout/item_welcome_page.xml)
- 각 스텝(카드)을 위한 레이아웃입니다. 큰 이모지(이미지 대체), 제목, 설명을 포함합니다.

#### [MODIFY] [activity_welcome.xml](file:///Users/nicsy/AndroidStudioProjects/CheeseJiggler-main/app/src/main/res/layout/activity_welcome.xml)
- 기존 정적 레이아웃을 `ViewPager2`와 페이지 인디케이터(Dots)를 포함한 구조로 변경합니다.

#### [MODIFY] [strings.xml](file:///Users/nicsy/AndroidStudioProjects/CheeseJiggler-main/app/src/main/res/values/strings.xml)
- 스텝별 가이드 문구를 추가합니다.
    - 1단계: 환영 및 서비스 소개
    - 2단계: 사용 방법 (화면 위에 마우스 올리기)
    - 3단계: 하드웨어 호환성 (광학 센서 마우스 전용)

### [Code]

#### [MODIFY] [WelcomeActivity.kt](file:///Users/nicsy/AndroidStudioProjects/CheeseJiggler-main/app/src/main/java/com/nicsy/cheese/jiggler/WelcomeActivity.kt)
- `ViewPager2` 어댑터를 구현하여 스텝 데이터를 연결합니다.
- 마지막 페이지에서만 "시작하기" 버튼이 작동하거나, 버튼 텍스트가 "다음"에서 "시작하기"로 바뀌는 로직을 추가합니다.

## 검증 계획

### 수동 검증
1. 앱 재설치 후 시작 화면에서 카드를 좌우로 넘겨 스텝별 안내가 정상적으로 나오는지 확인합니다.
2. 각 스텝의 이미지(이모지)와 설명이 기획 의도대로 표시되는지 확인합니다.
3. 마지막 단계에서 버튼을 눌렀을 때만 메인 화면으로 진입하고 `isFirstRun` 플래그가 저장되는지 확인합니다.
