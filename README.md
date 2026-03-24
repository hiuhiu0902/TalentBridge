# TalentBridge Backend

Backend cho hệ thống **TalentBridge** - nền tảng tuyển dụng kết nối **Candidate** và **Employer**.

Hệ thống cung cấp RESTful API cho các nghiệp vụ cốt lõi như:
- xác thực và phân quyền người dùng
- quản lý hồ sơ ứng viên và nhà tuyển dụng
- đăng tuyển, lưu job, ứng tuyển và xử lý application
- lịch phỏng vấn, thông báo và chat thời gian thực
- AI chat hỗ trợ đọc dữ liệu trong phạm vi cho phép

> Phạm vi tài liệu này chỉ mô tả **backend**.

---

## 1. Mục tiêu dự án

TalentBridge được xây dựng để phục vụ 3 nhóm vai trò chính:

- **CANDIDATE**: tạo hồ sơ, cập nhật kỹ năng, tìm kiếm việc làm, lưu job, ứng tuyển và theo dõi tiến trình tuyển dụng.
- **EMPLOYER**: tạo hồ sơ công ty, đăng job, quản lý ứng viên, sắp xếp phỏng vấn và trao đổi với candidate.
- **ADMIN**: quản lý tổng quan nền tảng, duyệt job và theo dõi các chỉ số hệ thống.

---

## 2. Tech stack

- **Java 17**
- **Spring Boot 3.2.5**
- **Spring Web**
- **Spring Data JPA**
- **Spring Security + JWT**
- **Spring WebSocket (STOMP)**
- **MySQL**
- **Flyway**
- **Swagger / OpenAPI** (`springdoc-openapi`)
- **Cloudinary** cho upload ảnh/tài nguyên
- **Google OAuth2 Client** cho đăng nhập Google
- **OpenAI Responses API** cho AI chat
- **Maven** để build project

---

## 3. Chức năng chính của backend

### 3.1. Authentication & Authorization
- Đăng ký / đăng nhập bằng email-password
- Đăng nhập Google
- Xác thực bằng JWT Bearer token
- Phân quyền theo role: `CANDIDATE`, `EMPLOYER`, `ADMIN`

### 3.2. Candidate
- Tạo và cập nhật hồ sơ ứng viên
- Quản lý kỹ năng, học vấn, kinh nghiệm làm việc
- Lưu job yêu thích
- Ứng tuyển vào job đang hoạt động
- Xem lịch sử application
- Nhận thông báo và chat với employer

### 3.3. Employer
- Tạo và cập nhật hồ sơ doanh nghiệp
- Tạo job post
- Quản lý danh sách job của mình
- Xem danh sách ứng viên cho từng job
- Cập nhật trạng thái application
- Sắp xếp phỏng vấn và chat với candidate

### 3.4. Admin
- Duyệt job
- Xem thống kê nền tảng
- Theo dõi số lượng candidate, employer, job, application

### 3.5. Real-time chat
- Tạo chat room giữa 2 user
- Gửi / đọc tin nhắn
- Đánh dấu đã đọc
- WebSocket push tin nhắn thời gian thực

### 3.6. AI chat (read-only assistant)
- Trả lời câu hỏi trong phạm vi dữ liệu người dùng được phép xem
- Hỗ trợ candidate hỏi về hồ sơ, đơn ứng tuyển, việc làm phù hợp
- Hỗ trợ employer hỏi về job, ứng viên, danh sách application
- Hỗ trợ admin hỏi overview hệ thống
- Không cho phép truy cập secret, token, password, raw SQL hoặc dữ liệu riêng tư ngoài phạm vi quyền hạn

---

## 4. Cấu trúc thư mục

```text
src/main/java/com/demo/talentbridge
├── config/          # cấu hình chung (security, swagger, websocket, cloudinary...)
├── controller/      # REST controllers
├── dto/             # request / response DTO
├── entity/          # JPA entities
├── enums/           # enum nghiệp vụ
├── exception/       # custom exceptions + handler
├── repository/      # Spring Data JPA repositories
├── security/        # JWT, user details, auth filter
├── service/         # service interfaces + implementations
└── websocket/       # websocket controllers / messaging

src/main/resources
├── application.properties
└── db/migration/    # Flyway migrations
```

---

## 5. Các module API chính

### Authentication
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/google`

### Candidate
- `GET /api/v1/candidates/me`
- `POST /api/v1/candidates/profile`
- `PUT /api/v1/candidates/profile`

### Employer
- `GET /api/v1/employers/me`
- `POST /api/v1/employers/profile`
- `PUT /api/v1/employers/profile`

### Job post
- `GET /api/v1/jobs`
- `GET /api/v1/jobs/{id}`
- `POST /api/v1/jobs`
- `PUT /api/v1/jobs/{id}`

### Application
- `POST /api/v1/applications`
- `GET /api/v1/applications/my`
- `PUT /api/v1/applications/{id}/status`

### Interview
- `POST /api/v1/interviews`
- `GET /api/v1/interviews/my`
- `PUT /api/v1/interviews/{id}`

### Notification
- `GET /api/v1/notifications`
- `PUT /api/v1/notifications/{id}/read`

### Chat
- `GET /api/v1/chat/rooms`
- `POST /api/v1/chat/rooms/{otherUserId}`
- `GET /api/v1/chat/rooms/{roomId}/messages`
- `POST /api/v1/chat/messages`
- `PUT /api/v1/chat/rooms/{roomId}/read`

### AI Chat
- `POST /api/v1/ai/chat`

### Health check
- `GET /api/v1/health`

> Tùy version source hiện tại, một số endpoint cụ thể có thể thay đổi nhẹ. Swagger là nguồn kiểm tra chính xác nhất khi service đang chạy.

---

## 6. Luồng nghiệp vụ cốt lõi

### 6.1. Employer đăng job
1. Employer đăng nhập
2. Tạo hồ sơ công ty nếu chưa có
3. Gửi request tạo job post
4. Job đi vào trạng thái chờ duyệt hoặc hoạt động tùy rule hệ thống
5. Admin có thể duyệt job

### 6.2. Candidate ứng tuyển
1. Candidate đăng nhập
2. Hoàn thiện hồ sơ ứng viên
3. Tìm job active
4. Gửi application
5. Hệ thống lưu application + history + notification
6. Employer xem application và cập nhật trạng thái

### 6.3. Chat giữa candidate và employer
1. Hai user tạo hoặc lấy chat room
2. Gửi message qua REST hoặc WebSocket
3. Tin nhắn được lưu DB
4. WebSocket đẩy message tới bên nhận theo thời gian thực

---

## 7. AI Chat hoạt động như thế nào

AI chat trong TalentBridge là **read-only assistant**, nghĩa là AI **không tự query database** và **không có quyền ghi dữ liệu**.

### 7.1. Luồng request
1. Client gọi `POST /api/v1/ai/chat` kèm JWT.
2. Backend xác định user hiện tại từ JWT.
3. Backend kiểm tra prompt có nằm trong nhóm bị chặn hay không.
4. Backend build system prompt + history + message hiện tại.
5. Backend gọi **OpenAI Responses API**.
6. Nếu model yêu cầu tool, backend sẽ chạy service nội bộ tương ứng để đọc dữ liệu trong hệ thống.
7. Backend trả tool output lại cho model.
8. Model sinh câu trả lời cuối và backend trả về cho frontend.

### 7.2. Tool hiện có
AI hiện chỉ được phép dùng các tool read-only sau:
- `get_platform_overview`
- `search_public_jobs`
- `get_my_candidate_profile`
- `get_my_applications`
- `get_my_employer_profile`
- `get_my_job_posts`
- `get_applications_for_my_job`
- `get_my_notifications`
- `get_admin_overview`
- `recommend_jobs_for_me`
- `analyze_my_profile_gap`
- `recommend_candidates_for_my_job`

### 7.3. Quyền hạn theo role

#### CANDIDATE có thể hỏi
- hồ sơ của chính mình
- các application của chính mình
- job public / active trong hệ thống
- job phù hợp với mình
- skill gap của bản thân
- thông báo của mình

#### EMPLOYER có thể hỏi
- hồ sơ employer của mình
- danh sách job của mình
- danh sách application cho job do mình sở hữu
- ranking ứng viên cho job do mình sở hữu
- thông báo của mình

#### ADMIN có thể hỏi
- số lượng candidate
- số lượng employer
- số lượng job active / pending / closed
- tổng số application
- overview nền tảng

### 7.4. Điều AI không được phép làm
- truy cập password, token, secret, API key
- sinh hoặc thực thi raw SQL
- dump toàn bộ database
- truy cập dữ liệu private của user khác ngoài scope hiện tại
- thực hiện hành động ghi dữ liệu thay cho người dùng

### 7.5. Dữ liệu AI trả lời có phải là dữ liệu trong hệ thống không?
**Có**, nếu câu hỏi dùng tool thì dữ liệu được lấy từ DB nội bộ của TalentBridge. Ví dụ:
- “Công việc phù hợp với tôi” → lấy từ các **job post active trong hệ thống**
- “Tôi đã apply những job nào” → lấy từ bảng application của user hiện tại
- “Ứng viên nào phù hợp nhất cho job X” → lấy từ application thuộc job của employer hiện tại

> Lưu ý: AI hiện chưa lưu conversation riêng trong backend. `history` đang do client gửi kèm mỗi lần chat.

---

## 8. Environment variables

Tạo file `.env` hoặc cấu hình biến môi trường trên môi trường deploy.

### Tối thiểu cần có

```env
PORT=8080

JWT_SECRET=replace_with_strong_base64_secret
JWT_EXPIRATION_MS=86400000

MYSQLHOST=localhost
MYSQLPORT=3306
MYSQLUSER=root
MYSQLPASSWORD=your_password
MYSQLDATABASE=talentbridge

OPENAI_API_KEY=
OPENAI_MODEL=gpt-5-mini
OPENAI_BASE_URL=https://api.openai.com/v1
AI_OUT_OF_SCOPE_MESSAGE=Xin lỗi, tôi chỉ có thể hỗ trợ với dữ liệu công khai hoặc dữ liệu thuộc phạm vi tài khoản của bạn trong TalentBridge.
AI_UNAVAILABLE_MESSAGE=Trợ lý AI hiện tạm thời chưa sẵn sàng. Vui lòng thử lại sau.

CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=

GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=

APP_CORS_ALLOWED_ORIGIN_PATTERNS=*
```

### Ghi chú
- Không commit secret thật lên Git.
- Ưu tiên dùng biến môi trường thay vì hard-code vào `application.properties`.
- Nếu dùng Railway MySQL, có thể đọc trực tiếp các biến `MYSQLHOST`, `MYSQLPORT`, `MYSQLUSER`, `MYSQLPASSWORD`, `MYSQLDATABASE` hoặc `MYSQL_URL`.

---

## 9. Chạy local

### Yêu cầu
- Java 17+
- Maven Wrapper (`./mvnw` đã có sẵn)
- MySQL đang chạy

### Các bước

1. Tạo database MySQL.
2. Cấu hình env vars hoặc chỉnh `application.properties` cho local.
3. Chạy project:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

Mặc định app chạy tại:

```text
http://localhost:8080
```

### Swagger
Khi ứng dụng chạy thành công, có thể kiểm tra API tại:

```text
http://localhost:8080/swagger-ui/index.html
```

### Health check

```text
GET /api/v1/health
```

Kết quả mong đợi:

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "status": "UP",
    "service": "TalentBridge"
  }
}
```

---

## 10. Database & migration

Project sử dụng **Flyway** để quản lý schema.

- Migration nằm trong `src/main/resources/db/migration`
- Mỗi thay đổi schema nên tạo migration mới
- Không sửa trực tiếp schema production bằng tay nếu migration chưa được cập nhật

Nguyên tắc nên dùng:
- entity thay đổi → thêm migration tương ứng
- deploy code mới → Flyway tự migrate khi app start

---

## 11. Deploy lên Railway

### Cách triển khai
1. Push source code backend lên GitHub
2. Tạo project trên Railway
3. Connect repository
4. Thêm MySQL service vào project
5. Cấu hình Variables cho backend service
6. Deploy backend

### Các biến Railway quan trọng
- `MYSQLHOST`
- `MYSQLPORT`
- `MYSQLUSER`
- `MYSQLPASSWORD`
- `MYSQLDATABASE`
- `OPENAI_API_KEY`
- `JWT_SECRET`
- `CLOUDINARY_*`
- `GOOGLE_CLIENT_*`

### Khuyến nghị
- luôn kiểm tra health endpoint sau mỗi deployment
- luôn kiểm tra Flyway migration đã chạy xong chưa
- luôn test các flow trọng yếu:
  - login
  - create profile
  - create job
  - apply job
  - chat
  - AI chat

---

## 12. Bảo mật và vận hành

### Bảo mật
- JWT secret phải đủ mạnh
- Không lưu API key trong source code
- Không expose dữ liệu nhạy cảm qua AI tools
- Các endpoint phải kiểm tra ownership theo user / role
- WebSocket / chat room cần verify membership

### Vận hành
- Dùng health endpoint để monitor service
- Dùng Swagger để smoke test API
- Ưu tiên log lỗi nghiệp vụ rõ ràng nhưng không log secret
- Theo dõi migration mismatch giữa entity và DB

---

## 13. Hạn chế hiện tại / hướng cải tiến

Một số điểm có thể mở rộng thêm trong tương lai:
- lưu conversation history riêng cho AI chat
- thêm AI conversation list / message history API
- bổ sung pagination cho messages và AI logs
- tăng cường audit log cho các thay đổi trạng thái application
- nâng cấp tool quyền hạn của admin
- tăng độ cứng role guard ở AI layer

---

## 14. Tài liệu liên quan

- `docs/AI_CHAT_FLOW.md` - mô tả chi tiết flow AI chat
- `docs/RAILWAY_DEPLOY.md` - hướng dẫn deploy Railway
- `docs/AUDIT_REPORT.md` - ghi chú audit và các điểm cần rà soát
- `.env.example` - mẫu biến môi trường

---

## 15. License / mục đích sử dụng

Tài liệu này được viết cho mục đích học tập, demo và phát triển sản phẩm TalentBridge backend.

Nếu dùng cho production, cần rà soát kỹ:
- secret management
- authorization ownership
- migration consistency
- logging / monitoring
- resilience khi gọi external AI API

---

## 16. Liên hệ / ghi chú phát triển

Nếu bạn đang tiếp tục phát triển backend này, thứ tự ưu tiên hợp lý là:
1. đảm bảo schema và entity luôn đồng bộ
2. khóa đúng quyền ở các endpoint và AI tools
3. test kỹ các flow nghiệp vụ trọng yếu
4. hoàn thiện tài liệu deploy và vận hành
5. sau đó mới mở rộng tính năng AI nâng cao

