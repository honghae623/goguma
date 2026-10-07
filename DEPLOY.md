# 배포 가이드 (파타주 Partage)

구성: **React → Vercel**, **Spring Boot → Render(Docker)**, **MySQL → Aiven(무료)**

> Render 무료 플랜은 15분간 요청이 없으면 서버가 잠듭니다. 첫 접속은 30초~1분 걸립니다.
> Render에는 관리형 MySQL이 없어 DB는 Aiven을 사용합니다. 이미지는 DB에 저장하므로 재배포해도 유지됩니다.

## 1. MySQL (Aiven)
1. https://aiven.io 가입 → 새 서비스 → **MySQL** → **Free** 플랜 생성
2. 서비스 개요에서 호스트, 포트, 사용자, 비밀번호, DB 이름(기본 `defaultdb`)을 확인
3. 백엔드에 넣을 JDBC URL을 만든다 (SSL 필수)
   ```
   jdbc:mysql://<호스트>:<포트>/defaultdb?sslMode=REQUIRED
   ```

## 2. 백엔드 (Render)
1. GitHub에 push
2. Render → New → **Web Service** → 이 저장소 선택
3. 설정
   - **Root Directory**: `goguma`
   - **Runtime**: Docker (`goguma/Dockerfile` 사용)
   - **Instance Type**: Free
4. 환경변수
   | 이름 | 값 |
   |---|---|
   | `DB_URL` | 위에서 만든 JDBC URL |
   | `DB_USERNAME` | Aiven 사용자 |
   | `DB_PASSWORD` | Aiven 비밀번호 |
5. 배포 후 `https://<서비스>.onrender.com/api/captures/test` 가 응답하면 성공
   (`PORT`는 Render가 자동 주입, 테이블은 첫 기동 시 자동 생성)

## 3. 프론트엔드 (Vercel)
1. Vercel → Add New → Project → 같은 저장소 선택
2. **Root Directory**: `frontend` (Vite 자동 인식)
3. 환경변수 `VITE_API_URL` = Render 주소 (예: `https://<서비스>.onrender.com`, 끝에 `/` 없이)
4. Deploy

## 로컬 개발
- 백엔드: `cd goguma && ./gradlew bootRun` (환경변수가 없으면 H2 메모리 DB)
- 프론트: `cd frontend && npm run dev` (`/api`는 Vite 프록시로 8080에 연결)

## 알아둘 점
- CORS는 전체 허용(`*`)이고 로그인이 없어 누구나 업로드·삭제할 수 있습니다.
- OCR은 브라우저에서 실행되며 첫 인식 때 한국어 데이터를 내려받습니다.
