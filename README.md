# Sufragio
안드로이드 투표 앱인 Sufragio는 스페인어로 "투표"를 의미합니다. 로그인/회원가입을 한 후 타인이 생성한 투표에 참여하거나 자신이 직접 투표를 생성할 수 있습니다. 투표 생성 시 익명 여부, 다중 선택 허용 여부, 항목 추가 허용 여부 등을 선택할 수 있으며, 투표 상세 페이지로 들어가 제목, 설명, 항목 등 여러 정보를 확인할 수 있습니다.
## 🥰 기능/구조 소개
### 로그인/회원가입
이 앱은 FastAPI와 연동하여 투표 기능을 수행하기 때문에 이에 따라 로그인/회원가입이 필수입니다. 아이디, 비밀번호, 닉네임을 입력하여 회원가입을 할 수 있으며, 이후 아이디와 비밀번호를 이용하여 로그인하면 Sufragio를 이용할 수 있습니다.
### 5개의 메인 페이지
- 홈: **인기 투표, 최신 투표, 종료 임박 투표**를 각각 2개씩 볼 수 있으며, 클릭하면 해당 투표 상세 페이지로 진입하게 됩니다. 각 투표 item은 제목, 참여자 수, 마감일로 구성되어 있습니다.
- 검색: 상단 검색창을 이용하여 **원하는 투표를 검색**하여 찾을 수 있습니다. 다만 한글의 경우 음절 단위로 검색되지 않습니다. 검색창 아래에는 모든 투표가 홈 페이지에 있는 투표 item과 동일한 구조로 나열되어 있습니다.
- 생성: **투표 제목, 투표 설명, 익명 투표/다중 선택 허용/항목 추가 허용 여부, 항목, 마감일을 입력**하도록 구성되어 있습니다. 투표 설명은 선택이며, 항목은 최소 2개 이상이여야 합니다. 항목 추가 버튼을 눌러 3개 이상의 항목을 만들 수 있고, 모두 입력한 후 투표 생성 버튼을 눌러 투표 생성을 할 수 있습니다. 이때 생성 버튼을 누르면 마이 페이지로 이동합니다.
- 목록: **진행 중/종료됨을 구분하여 모든 투표를 볼 수 있도록 구성**되어 있습니다. 홈 및 검색 페이지와 다르게 각 투표 item은 제목, 설명, 참여자 수, 마감일로 구성되어 있습니다. 마찬가지로 클릭하면 해당 투표 상세 페이지로 진입하게 됩니다.
- 마이: 자신의 닉네임을 볼 수 있으며, 수정 및 로그아웃 버튼을 이용해 **닉네임을 수정하거나 로그아웃을 할 수 있습니다.** 아래에는 **내 투표/참여한 투표를 확인**할 수 있으며, 목록 페이지에 있는 투표 item과 동일한 구조로 나열되어 있습니다. 마찬가지로 클릭하면 해당 투표 상세 페이지로 진입하게 됩니다.
### 투표 상세 페이지
제목, 생성자, 생성일, 설명, 참여자 수, 마감일을 상단에서 확인할 수 있습니다. 그 아래에는 단일 선택/다중 선택 여부를 확인할 수 있는 block과 투표 항목이 나열되어 있는 block이 있습니다. 이를 이용해 **원하는 투표 항목을 선택**할 수 있습니다. 항목 추가가 허용된 투표의 경우, 항목 추가 버튼을 눌러 **항목을 추가**할 수 있습니다. 투표하기 버튼을 누르면 투표 항목이 나열되어 있던 block이 사라지고 **투표 결과가 노출**됩니다. 여기서 **각 항목에 투표한 인원, 비율, 투표한 사람, 그래프를 확인**할 수 있습니다. 익명 투표의 경우 투표한 사람이 비공개로 표시됩니다. 생성자의 경우 하단의 투표 삭제 버튼을 눌러 **투표 삭제를 할 수 있습니다.**
### 기타 페이지
- 스플래시 화면: 앱 실행 시 처음 표시되는 페이지입니다. 중앙에 로고가 표시되며 하단에는 제 닉네임인 Dreamer 로고가 표시됩니다.
- 온보딩 화면: 앱 설치 후 첫 실행 시 스플래시 화면 다음에 표시되는 페이지입니다. 앱에 대한 간단한 소개로 구성된 3개의 페이지로 이루어져 있습니다. 마지막 페이지에서는 로그인/회원가입 버튼을 클릭하여 로그인/회원가입을 할 수 있습니다.
### 기타 사항
- 5개 페이지 전환 애니메이션을 추가하여 부드러운 화면 전환을 구현했습니다. 홈, 검색, 목록, 마이 페이지 간의 전환은 양옆으로 슬라이딩되는 방식이며, 검색 페이지로 이동하거나 검색 페이지에서 이동하는 경우 상하로 슬라이딩되는 방식이 적용됩니다.
- 로그아웃 버튼 클릭 시 refresh_token 자체가 초기화되어 앱을 재실행해도 로그인 화면으로 이동하게 됩니다.
- 시스템 다크 모드 설정 시 모든 투표 item이 어두워집니다.
## ⚒️ 개발 환경
- IDE: Android Studio
- `Kotlin + XML`
- Backend: 제공받은 FastAPI
## 🧑‍💻 개발 기간 및 수정 내역
(2025년)
- 5/1 ~ 5/3: 앱 기본 틀 확정(구성 페이지, 로고, UI), Bottom Navigation UI 및 로직 제작
- 5/4 ~ 5/5: 로고 및 아이콘 제작, 5개의 메인페이지 UI 제작
- 5/6: Splash Activity 제작, 페이지 전환 애니메이션 추가, Onboarding Activity 제작
- 5/7: 투표 상세 페이지 UI 및 로직 제작
- 5/8 ~ 5/9: 로그인/회원가입 페이지 UI 제작, FastAPI SignUp/SignIn 연동, 다크모드 대응
- 5/10: FastAPI Get_User 연동 -> 유저 정보 연동, FastAPI Update_Nickname 연동 -> 닉네임 수정 로직 추가
- 5/11 ~ 5/12: refresh_token을 이용한 로그인 유지 기능 도입, Onboarding Activity와 로그인/회원가입 페이지 연동, 로그인 유지 오류 수정
- 5/14: FastAPI Create_Poll 연동 -> 투표 생성 로직 제작 및 UI 수정, FastAPI Get_All_Polls 연동 -> 검색 페이지에 실제 투표 item 연동, FastAPI Delete_Poll 연동 -> 투표 삭제 기능 추가
- 5/15: 목록 페이지에 진행 중/종료됨을 구분하여 실제 투표 item 연동, FastAPI Get_User_Vote 연동 -> 마이 페이지에 내 투표/참여한 투표를 구분하여 실제 투표 item 연동
- 5/18: FastAPI Vote_Poll, Get_Poll_Options, Get_Poll_Results 연동 -> 익명 여부 및 다중 투표 허용 여부를 고려하여 투표하기 기능 추가, 투표 결과 연동 및 UI 추가
- 5/19: 홈 페이지에 참여자, 생성일, 마감일을 고려하여 실제 투표 item 연동
- 5/21: 항목 추가 허용 기능 추가, Onboarding Activity UI 수정, refresh_token도 로그인할 때마다 새로 발급받도록 수정
- 5/22 ~ 5/23: 전반적인 UI 개선, 화면 짤림 이슈 및 기타 버그 수정
## 📃 To Do
- MVVM 구조 적용
- Jetpack Compose 마이그레이션
