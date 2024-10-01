# Traffic-Warning-Navigation
💡 [ Protfolio Project 009] 교통 장애 경고 내비게이션

## 📌 프로젝트 소개
이 프로젝트는 운전자에게 실시간으로 도로 장애물에 대한 경고를 제공하는 모바일 내비게이션 앱입니다. 시스템은 열화상 카메라와 광각 카메라를 사용하여 도로 상태를 감지하고, 공사 현장, 사고, 장애물, 낙석, 포트홀, 블랙아이스와 같은 위험 요소를 탐지합니다. 탐지된 장애물은 지도 위에 표시되며, 운전자가 장애물에 접근할 때 음성 알림을 제공합니다.  

## 📌 폴더 구조
    📂 src/main
    ┣ 📂 ../java/com/ubit/blackice
    ┃ ┣ 📜 MainActivity.java          # 메인 화면 구성 및 교통 장애 경고 내비게이션 기능 구현
    ┃ ┣ 📜 blackiceData.java          # 블랙아이스 데이터 처리 및 관리
    ┃ ┣ 📜 introActivity.java         # 시작 화면 및 앱 소개 화면 처리
    ┃ ┣ 📜 openapiData.java           # 교통 장애물 오픈 API 데이터 연동
    ┣ 📂 ../res/layout
    ┃ ┣ 📜 activity_main.xml          # 메인 내비게이션 화면 레이아웃 설정
    ┃ ┣ 📜 startmenu.xml              # 시작 메뉴 레이아웃 설정


## 📌 데이터베이스 구조
 - id: 라벨  
 - datetime: 날짜  
 - latitude: 위도  
 - longtitude: 경도  
 - type: 장애물 유형  
 - detail_type: 유형 별 세부 정보
 - message: 추가 정보  
![db](https://github.com/user-attachments/assets/326cab66-5874-42a0-82bd-532a014c508c)

## 📌 주요 기능
### - 실시간 장애물 탐지:
열화상 카메라와 광각 카메라를 통해 포트홀, 사고, 도로 공사와 같은 장애물을 실시간으로 탐지합니다.  
Jetson 보드에서 센서 데이터를 처리하여 해당 좌표와 객체 유형을 데이터베이스에 저장합니다.  

### - 지도 표시:
탐지된 장애물은 카카오맵 API를 사용해 지도에 아이콘으로 표시됩니다.  
각 장애물 유형에 따라 다른 아이콘이 사용되어, 포트홀, 공사 현장, 사고 등 다양한 장애물을 구분할 수 있습니다.  

### - 음성 알림:
차량이 장애물에 70미터 이내로 접근할 때 음성 알림을 통해 운전자에게 장애물 정보를 전달합니다.  
데이터베이스에서 장애물 정보를 가져와 장애물의 유형과 차량까지의 거리를 계산하여 알림을 제공합니다.  

### - 데이터베이스 연동:
장애물 데이터는 원격 데이터베이스에 저장되며, 실시간으로 앱에서 해당 데이터를 가져와 최신 장애물 정보를 지도에 표시합니다.  
데이터베이스는 장애물이 새로 탐지되거나 변경될 때마다 업데이트됩니다.  

## 📌 구현 상세
### - 지도 통합:
카카오맵 API를 사용하여 지도에 장애물 아이콘을 표시하며, 각 장애물 유형에 따라 다른 아이콘을 사용하여 시각적으로 구분 가능하도록 했습니다. 새로운 장애물이 탐지되면 지도에 해당 위치가 실시간으로 업데이트됩니다.  

### - 음성 알림:
텍스트-음성 변환 기능을 사용하여 차량이 장애물에 가까워질 때 경고음을 생성합니다. 실시간으로 운전자가 장애물에 70미터 이내로 접근하면 음성 알림이 자동으로 활성화됩니다.  

### - 데이터 동기화:
장애물 데이터는 일정 간격으로 원격 데이터베이스와 동기화됩니다. 데이터베이스가 실시간으로 갱신되어 사용자가 항상 최신 정보를 확인할 수 있도록 구현되었습니다.  

## 📌 개발 환경
- Java
- Android Studio
- Kakao Map API
- MariaDB, phpMyAdmin 
- Text-to-Speech (TTS)

 
