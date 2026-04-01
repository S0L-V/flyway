# ✈️ Flyway
## 1. Flyway 소개

Flyway는 소비자에게 합리적인 항공권 가격을 제공하기 위해
투명한 동적 가격 모델과 가격 변동 이력 기능을 구현한
실시간 항공권 예매 시스템입니다.

또한 실제 서비스 수준에서 요구되는 핵심 문제인 **좌석 점유 동시성 제어**, **예약 상태 관리**, **결제 안정성 확보**를 중심으로 설계 및 구현했습니다.

약 20만 건 규모의 대량 데이터를 직접 생성하여
운영 환경을 가정한 기능 검증과 시스템 테스트까지 수행했습니다.

<br>

### 🚀 배포
🔗 [https://flyway.kr](https://flyway.kr)

<br>

### 🗓️ 개발 기간

📌 **2026.01.07 ~ 진행 중**

<br>


### 👥 팀원 소개
<table style="width:100%; border-collapse:collapse; table-layout:fixed; font-family:Pretendard, Arial, sans-serif; text-align:center;">
  <colgroup>
    <col style="width:33.3333%">
    <col style="width:33.3333%">
    <col style="width:33.3333%">
  </colgroup>

  <tr>
    <td><b>👩🏻‍💻 강희민</b><br> (<a href="https://github.com/kkhhmm3103">@kkhhmm3103</a>)</td>
    <td><b>👩🏻‍💻 김민서</b><br> (<a href="https://github.com/minseokim0113">@minseokim0113</a>)</td>
    <td><b>👩🏻‍💻 박수진</b><br> (<a href="https://github.com/cl-o-lc">@cl-o-lc</a>)</td>
  </tr>

  <tr>
    <td style="border:1px solid #cfd8e3; padding:18px; font-size:20px; line-height:1.5; text-align:center;">
      <ul>
        <li>
          좌석 선택 및 상태 관리
        </li>
         <li>
          데이터 생성 및 성능 테스트
        </li>
      </ul>
    </td>
    <td style="border:1px solid #cfd8e3; padding:18px; font-size:20px; line-height:1.5; text-align:center;">
      <ul>
        <li>항공편 검색 및 실시간 인기 항공편 조회</li>
        <li>데이터 생성 및 성능 테스트</li>
        <li>AWS S3 구축</li>
      </ul>
    </td>
    <td style="border:1px solid #cfd8e3; padding:18px; font-size:20px; line-height:1.5; text-align:center;">
      <ul>
        <li>
            가격 변동 배치/스케줄러 및 그래프
        </li>
         <li>
            DB 성능 테스트
        </li>
      </ul>
    </td>
  </tr>

  <tr>
    <td><b>🧑🏻‍💻 오찬혁</b><br> (<a href="https://github.com/ochanhyeok">@ochanhyeok</a>)</td>
    <td><b>👩🏻‍💻 [팀장] 이가은</b><br> (<a href="https://github.com/gaeunnlee">@gaeunnlee</a>)</td>
    <td><b>🧑🏻‍💻 한재훈</b><br> (<a href="https://github.com/hjh79gw">@hjh79gw</a>)</td>
  </tr>

  <tr>
    <td style="border:1px solid #cfd8e3; padding:18px; font-size:20px; line-height:1.5; text-align:center;">
      <ul>
        <li>
            관리자 시스템 및 실시간 대시보드
        </li>
         <li>
            인프라 구축 및 서비스 배포
        </li>
      </ul>
    </td>
    <td style="border:1px solid #cfd8e3; padding:18px; font-size:20px; line-height:1.5; text-align:center;">
      <ul>
        <li>
            JWT + Refresh Token Rotation <br>기반 회원 인증 시스템
        </li>
        <li>
            회원 정보·예약 내역 조회 및 수정
        </li>
         <li>
            Jira 기반 협업 프로세스 구축
        </li>
      </ul>
    </td>
    <td style="border:1px solid #cfd8e3; padding:18px; font-size:20px; line-height:1.5; text-align:center;">
            <ul>
        <li>
            예약 및 결제
        </li>
        <li>
            로그인 번호 인증 및 문자 발송
        </li>
         <li>
           성능 테스트
        </li>
      </ul>
    </td>
  </tr>
</table>


<br>

<details>
<summary><h2>2. 서비스 기획 배경</h2></summary>

### ✅ 문제 정의

기존 항공권 예매 시장은 가격 산정 기준이 공개되지 않아  
사용자가 **가격 변동 이유와 현재 가격의 적정성**을 판단하기 어렵습니다.  
또한 좌석 점유율과 인기 항공편 정보가 명확한 근거 없이 제공되어  
사용자 의사결정 과정에서 **정보 비대칭 문제**가 발생합니다.

<br>

### ✨ Flyway 차별점

Flyway는 항공권 가격이 불투명하게 결정되는 문제를 해결하기 위해  
**가격 산정 기준을 공개하는 투명한 예매 서비스**를 제공합니다.  
가격은 다음 요소를 기반으로 산정됩니다.

- **M_time** : 출발일까지 남은 시간  
- **M_load** : 좌석 점유율(수요)  
- **alpha** : 가격 변동 완화 계수  

또한 사용자가 합리적으로 예매할 수 있도록 다음 정보를 제공합니다.

- **가격 변동 이력 제공** (가격 추세 기반 구매 타이밍 판단)
- **실시간 인기 여행지/항공편 제공** (수요 급등 항공편 즉시 확인)
- **정확한 좌석 상태 정보 제공** (실시간 잔여 좌석 기반 의사결정)

</details>

<br>



## 3. 주요 화면
<table>
  <tr>
    <td align="center"><b>메인</b></td>
    <td align="center"><b>검색</b></td>
    <td align="center"><b>가격 변동 그래프</b></td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/ee30ecce-166c-4955-8527-cc3ab3fb543a" width="260"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/3b0a99f0-ffba-47e5-8e91-a0acb38223fa" width="260"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/a116b2a0-161f-47c2-a30e-dda2743db30b" width="260"/>
    </td>
  </tr>

  <tr>
    <td align="center"><b>좌석 선택</b></td>
    <td align="center"><b>결제</b></td>
    <td align="center"><b>관리자 대시보드 </b></td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/af00bcb4-75df-4419-abf7-2e4e39ca5632" width="260"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/f146661c-9caa-4979-b60c-c336cf81f25f" width="260"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/f09dc191-d844-4b7a-b2b0-b63574032b69" width="260"/>
    </td>
  </tr>
</table>

  
<details>
  <summary>더보기</summary>

<br>

<table>
  <tr>
    <td align="center"><b>메인</b></td>
    <td align="center"><b>메인 - 캘린더</b></td>
    <td align="center"><b>특가</b></td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/ee30ecce-166c-4955-8527-cc3ab3fb543a" width="260"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/8d7e7320-f33d-42cb-9912-8d29fe9147cd" width="260"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/c434b5fc-2209-48b5-8fdf-ae8d9c30f002" width="260"/>
    </td>
  </tr>

  <tr>
    <td align="center"><b>실시간 인기 여행지</b></td>
    <td align="center"><b>항공편 목록</b></td>
    <td align="center"><b>항공편 가격 변동 그래프</b></td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/b08dbe88-2e54-4078-bc5c-f9c4f69ec8d8" width="260"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/3b0a99f0-ffba-47e5-8e91-a0acb38223fa" width="260"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/a116b2a0-161f-47c2-a30e-dda2743db30b" width="260"/>
    </td>
  </tr>

  <tr>
    <td align="center"><b>약관 동의</b></td>
    <td align="center"><b>탑승자 정보 입력</b></td>
    <td align="center"><b>좌석 선택</b></td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/17539275-3893-4958-a45f-e914be585d06" width="260"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/8f05aece-9344-4c84-b584-b6a8f1e0b639" width="260"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/af00bcb4-75df-4419-abf7-2e4e39ca5632" width="260"/>
    </td>
  </tr>

  <tr>
    <td align="center"><b>부가서비스(1)</b></td>
    <td align="center"><b>부가서비스(2)</b></td>
    <td align="center"><b>  </b></td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/03fa04c5-fd8d-414c-94c6-eefc8b116c33" width="260" />
    </td>
    <td align="center">
     <img src="https://github.com/user-attachments/assets/d94f4d0b-b856-4c5e-8eab-a83cc715e451" width="260" />
    </td>
    <td align="center">  </td>
  </tr>



  <tr>
    <td align="center"><b>결제(1)</b></td>
    <td align="center"><b>결제(2)</b></td>
    <td align="center"><b>결제(3)</b></td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/5a24f23a-ccb5-4964-b53f-4af8e4bdd2a5" width="260"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/204cdfca-1324-4f4a-8000-3ea0fd861801" width="260"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/f146661c-9caa-4979-b60c-c336cf81f25f" width="260"/>
    </td>
  </tr>

  <tr>
    <td align="center"><b>예약 완료</b></td>
    <td align="center"><b>예약 내역</b></td>
    <td align="center"><b>관리자 대시보드</b></td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/c6297c03-56a4-4665-bc13-5a6defc7ff40" width="260"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/63598670-b4f3-49bc-9203-9c18361bcfc0" width="260"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/f09dc191-d844-4b7a-b2b0-b63574032b69" width="260"/>
    </td>
  </tr>

  <tr>
    <td align="center"><b>접속 기록</b></td>
    <td align="center"><b>시간대별 분포 그래프</b></td>
    <td align="center"><b>항공편 관리</b></td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/ec4fd136-3d52-4889-9d57-973360a7556c" width="260"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/da3c1433-e2a6-48d6-b9bc-d0cf6137b774" width="260"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/26a10c67-8658-46f4-a43f-1dbd74707b55" width="260"/>
    </td>
  </tr>

  <tr>
    <td align="center"><b>특가 관리</b></td>
    <td align="center"><b>회원</b></td>
    <td align="center"><b>결제 내역</b></td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/b890d66e-ed25-433c-ba01-1fb5310a062f" width="260"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/6058a6b7-fea8-46db-96db-313aa3a14860" width="260"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/7fa4e14b-b298-47c1-8375-567ea66e5830" width="260"/>
    </td>
  </tr>
</table>

</details>



<br>


## 4. 주요 기능
<table>
  <tr>
    <td align="center"><b>회원</b></td>
    <td align="center"><b>관리자</b></td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/d61e1ae5-6d91-4337-83f3-9808c4a7faa4" width="700"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/89fdfd04-a6fc-458f-978b-c2155376e804" width="700"/>
    </td>
  </tr>
</table>


<br>



## 5. 기술 스택 (Tech Stack)

| 구분       | 기술                                        | 구분       | 기술                                                    |
| -------- | ----------------------------------------- | -------- | ----------------------------------------------------- |
| Backend  | Spring Framework 5.3.31<br>MyBatis 3.5.13 | Security | Spring Security 5.8.13<br>JWT (jjwt 0.12.6)<br>BCrypt |
| Database | MariaDB (AWS RDS)                         | Frontend | JSP<br>Tailwind CSS<br>Chart.js                       |
| 실시간      | Spring WebSocket + SockJS                 | 결제       | 토스페이먼츠 API                                            |
| 인증       | Kakao OAuth 2.0                           | 배포       | AWS EC2<br>Tomcat                                     |

<br>



## 6. 시스템 아키텍처
<img width="1840" height="1331" alt="Image" src="https://github.com/user-attachments/assets/c103e712-61c8-4719-b16c-225995c59251" />
<br>


## 7. ERD
<table>
  <tr>
    <th align="center">항공편</th>
    <th align="center">공항</th>
    <th align="center">관리자</th>
    <th align="center">예약(1)</th>
  </tr>

  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/57ce3a42-ecc1-43d8-a3bc-a97c9b42ef74" width="320"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/bd5e6dd3-a0ed-45fa-9edb-67acc4ed2dd6" width="320"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/591d6d14-e0fa-4e95-9102-aa874c845c4d" width="320"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/d11212de-a93e-4037-8c01-bb21ed7fb503" width="320"/>
    </td>
  </tr>

  <tr>
    <th align="center">예약(2)</th>
    <th align="center">항공편 가격</th>
    <th align="center">회원</th>
    <th align="center"></th>
  </tr>
  
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/7f4aa993-303b-490d-86dd-27e3928e7c5d" width="320"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/fbd80395-2cf8-47a9-abd5-e7adca1d24c2" width="320"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/6fec5e19-5975-48f6-b9ad-1b209f62e9cc" width="320"/>
    </td>
    <td align="center">
      <!-- 빈칸 -->
    </td>
  </tr>
</table>

<br>



## 8. 개발 계획 및 협업
* **WBS 기반 일정 수립**을 통해 전체 기능을 도메인 단위로 분해하고, 스프린트 단위로 개발 계획을 구체화
* 평일 오전 9시 **데일리 스크럼** 작성 후 9시 30분 대면 회의 진행
* **Jira 이슈 기반 개발 프로세스**를 적용하여 기능 단위를 명확히 정의하고, PR 단위로 작업을 관리
* Notion을 통해 설계 문서, 트러블슈팅, 도메인 지식을 공유하며 팀 전체의 이해도를 통일
* Discord 알림 연동을 통해 PR/이슈 상황을 실시간 공유하며 협업 속도 개선
<table>
  <tr>
    <th align="center">WBS(1)</th>
    <th align="center">WBS(2)</th>
  </tr>
  <tr>
    <td align="center">
      <img width="1837" height="830" alt="image" src="https://github.com/user-attachments/assets/d9e47c1c-9601-45fe-b181-d7a6ea9b395c" />
    </td>
    <td align="center">
      <img width="1831" height="722" alt="image" src="https://github.com/user-attachments/assets/1ed4d012-503d-49b0-9118-ac7430c1a907" />
    </td>
  </tr>
</table>
<img width="900" alt="image" src="https://github.com/user-attachments/assets/e7ce7b2a-e479-4244-8210-ee77ad923a3c" />

<br>

## 9. 핵심 설계
Flyway는 항공권 예매 과정에서 발생하는 인증 보안 문제, 좌석 중복 예약 문제,  불투명한 가격 정책으로 인한 정보 비대칭 문제를 해결하기 위해 다음 4가지 핵심 설계를 중심으로 구현했습니다.


### 1) 인증/보안 설계 (JWT + Refresh + CSRF + Cookie)

Flyway는 HttpOnly Cookie 기반 JWT 인증 방식을 적용하여 토큰 탈취 위험을 줄였습니다.

또한 Refresh Token 회전(Rotation) 전략을 통해 Access Token 만료 시 자동 재발급이 가능하도록 구현하여 로그인 유지 경험을 개선했습니다.

소셜 로그인 사용자를 고려하여 OAuth 2.0 기반 로그인(Kakao) 흐름을 적용하였으며, OAuth 인증 성공 이후에도 서비스 내부에서는 JWT를 발급하여 인증 방식을 일관되게 통합했습니다.

API 요청은 JWT 기반으로 인증되며, 상태 변경 요청(POST/PUT/PATCH/DELETE)은 CSRF 토큰 검증을 통해 보호합니다.

이를 통해 브라우저 환경에서의 안정성과 보안성을 강화하고, 확장 가능한 인증 구조를 구축했습니다.

<details>
<summary>
  JWT 발급 / 회전 시퀀스 다이어그램
</summary>
  <img width="1000" height="1170" alt="auth_web" src="https://github.com/user-attachments/assets/3799fb44-c26b-4f02-a027-6b56651d1098" />
<img width="1000" height="822" alt="auth_api" src="https://github.com/user-attachments/assets/f7774967-03a4-4e31-8dab-e0c868fbbdd0" />
<img width="1000" height="1171" alt="auth_token rotation" src="https://github.com/user-attachments/assets/1af82d02-9257-4eeb-af17-ec613c623956" />
</details>
<details>
<summary>
  OAuth 시퀀스 다이어그램
</summary>
  <img width="1000" height="390" alt="kakao_oauth_3" src="https://github.com/user-attachments/assets/46be966f-2f5c-414d-a415-9a88da51c129" />
<img width="1000" height="543" alt="kakao_oauth_2" src="https://github.com/user-attachments/assets/b7baafe6-e7fd-439e-b8d4-2c35f9f7261f" />
<img width="1000" height="759" alt="kakao_oauth_1" src="https://github.com/user-attachments/assets/254558bb-a0be-4c7e-8c6b-cccbb6fc0122" />
</details>


### 2) 좌석 동시성 제어 (HOLD → PAYING → CONFIRMED)/>


Flyway는 동일 좌석에 대한 중복 예약을 방지하기 위해 좌석을 단순 조회 데이터가 아닌

공유 자원(Concurrency Resource) 으로 정의하고, 상태 기반 점유 모델을 설계했습니다.

- 좌석 선택 시 `AVAILABLE → HELD`로 변경하여 임시 점유 처리
- 결제 진행 시 `HELD → PAYING → CONFIRMED` 상태 전이로 경쟁 상태 방지
- 일정 시간(10분) 내 결제가 완료되지 않으면 `HELD → EXPIRED → AVAILABLE`로 복구

이를 통해 결제 중 경쟁 요청이 발생해도 중복 결제가 발생하지 않도록 제어했습니다.


### 3) 동적 가격 투명성 모델 (M_time, M_load, alpha)

기존 항공권 예매 서비스는 가격 변동 기준이 공개되지 않아 사용자가 가격의 적정성을 판단하기 어렵습니다.

Flyway는 이러한 정보 비대칭 문제를 해결하기 위해 **가격 산정 기준을 공개하는 동적 가격 모델**을 적용했습니다.

가격은 다음 요소를 기반으로 산정됩니다.

- **M_time** : 출발일까지 남은 시간
- **M_load** : 좌석 점유율 기반 수요 지표
- **alpha** : 가격 변동 완화 계수

또한 사용자에게 가격 변동 이력을 제공하여 합리적인 예매 판단이 가능하도록 설계했습니다.


### 4) 예약 상태 및 결제 흐름 설계 (예약 세그먼트/승객/부가서비스 + 동시성 연계)

Flyway는 항공권 예매가 단순 결제 처리로 끝나지 않고,

**예약 정보 + 탑승자 정보 + 부가서비스 + 결제 상태**가 유기적으로 연결되는 복합 도메인임을 고려하여

예약 흐름을 상태 기반으로 관리했습니다. 트랜잭션과 상태 기반 관리를 통한 동시성 제어로 오버 부킹 상황을 사전에 방지했습니다. 

- 예약 상태 변경 과정에서 **좌석 동시성 제어 로직(HOLD/PAYING/CONFIRMED)** 이 함께 적용되어
동일 좌석 중복 예약이 발생하지 않도록 설계했습니다.
- 예약은 해당 항공편을 선택하여 동의 페이지로 이동하기 전 트랜잭션과 잔여석 행에 대한 비관적 락을 통해 잔여석을 인원수 만큼 차감하고 예약자의  상태 칼럼을 `HELD`로 변경합니다. 정보 입력 후 결제를 완료하면   `HELD → CONFIRMED` 흐름으로 상태가 변경되며, 결제 확정 처리됩니다.
- 예약 세그먼트(왕복/경유) 구조를 반영하여 다구간 예약을 관리할 수 있도록 설계했습니다.
- 탑승자(Passenger) 정보와 좌석(Seat) 선택이 예약과 강하게 연결되도록 구성했습니다.
- 수하물/기내식 등 부가서비스는 Passenger 단위로 연결되어 확장 가능하도록 구현했습니다.

### 5) 관리자 실시간 대시보드 (WebSocket + SockJS)

Flyway는 관리자가 서비스 현황을 실시간으로 모니터링할 수 있도록 WebSocket 기반 실시간 대시보드를 구현했습니다.

- WebSocket + SockJS를 활용하여 서버에서 클라이언트로 실시간 데이터를 푸시합니다.
- 통계 데이터(매출, 예약, 방문자)는 15초 주기로 자동 브로드캐스트되어 새로고침 없이 최신 현황을 확인할 수 있습니다.
- WebSocket 연결 실패 시 REST API 폴백을 통해 데이터 조회가 가능하도록 설계하여 안정성을 확보했습니다.
- 알림 시스템을 통해 신규 예약, 결제 완료, 취소 요청 등 주요 이벤트를 실시간으로 전달합니다.

또한 관리자 시스템은 일반 사용자 시스템과 완전히 분리된 Security FilterChain으로 구성하여, 한쪽 시스템이 침해되더라도 다른 시스템에 영향을 주지 않도록 보안을 격리했습니다.

<br>


## 10. 회고
- **강희민**<br>
  어려운 일들이 많았지만 하나씩 해결해 가면서 많이 성장할 수 있었습니다! 이번 프로젝트를 통해 단순히 기능이 동작하는 것보다 현실적인 운영 구조를 데이터 모델에 어떻게 녹여내는지가 훨씬 중요하다는 점을 배웠습니다!
- **김민서**<br>
  다양한 변수로 일정이 지연되었으나, Jira로 계획의 우선순위를 조정하며 단계적으로 해결하였습니다. 이를 통해 계획의 유연성과 일정 관리의 중요성을 배웠습니다. 
- **박수진**<br>
  초기에 시행착오를 겪으며, 우리 팀에게 필요한 소통 방식을 찾아가던 과정이 인상 깊습니다. 매일 9:30 데일리 스크럼을 진행한 것이 큰 도움이 되었습니다.
- **오찬혁**<br>
  처음엔 막막했지만, 하나씩 해결하면서 자신감을 얻었고 개발 과정에서 겪은 문제들을 팀원들과 공유하고 함께 해결해 나갔습니다. 이를 통해 Spring Legacy 환경에서의 경험과 AWS 인프라 운영 역량을 키울 수 있었습니다.
- **이가은**<br>
  이번 프로젝트에서 설계 의도와 코드 구조를 꾸준히 기록하여, 중간에 구조 변경이 발생했을 때도 빠르게 대응할 수 있었습니다. 또한 Jira를 통해 팀원들의 작업 현황과 전체 진행도를 한눈에 파악할 수 있어 효율이 좋았고, 다음 프로젝트에서도 적극적으로 활용하고 싶습니다.
- **한재훈**<br>
  api 명세서를 작성하며 팀원과 코드를 맞춰가는 게 어려웠지만 협업과 소통하는 법을 배울 수 있었습니다. 데일리 스크럼을 통해 담당기능이 아니더라도 진행사항을 파악하고 코드 리뷰를 할 수 있어 프로젝트 진행에 많은 도움이 됐습니다.

