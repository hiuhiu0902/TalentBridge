INSERT INTO job_posts
(employer_id, category_id, title, description, salary_min, salary_max, location, job_type, experience_level, status, posted_at, expired_at, rejection_reason)
SELECT e.id, c.id,
       'Backend Developer Java Spring Boot',
       'Tham gia phát triển REST API cho nền tảng tuyển dụng, tích hợp JWT, tối ưu hiệu năng MySQL và phối hợp với team frontend React.',
       18000000, 30000000,
       'TP. Hồ Chí Minh', 'FULL_TIME', 'MID',
       'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), NULL
FROM employers e
JOIN users u ON u.id = e.user_id
JOIN categories c ON c.name = 'Công nghệ thông tin'
WHERE u.email = 'hr@techbridge.vn'
  AND NOT EXISTS (
      SELECT 1 FROM job_posts jp
      WHERE jp.employer_id = e.id AND jp.title = 'Backend Developer Java Spring Boot'
  );

INSERT INTO job_posts
(employer_id, category_id, title, description, salary_min, salary_max, location, job_type, experience_level, status, posted_at, expired_at, rejection_reason)
SELECT e.id, c.id,
       'Frontend Developer ReactJS',
       'Xây dựng giao diện web tuyển dụng, làm việc với REST API, responsive UI, tối ưu UX cho candidate flow và employer dashboard.',
       15000000, 25000000,
       'Hà Nội', 'FULL_TIME', 'JUNIOR',
       'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 75 DAY), NULL
FROM employers e
JOIN users u ON u.id = e.user_id
JOIN categories c ON c.name = 'Công nghệ thông tin'
WHERE u.email = 'hr@techbridge.vn'
  AND NOT EXISTS (
      SELECT 1 FROM job_posts jp
      WHERE jp.employer_id = e.id AND jp.title = 'Frontend Developer ReactJS'
  );

INSERT INTO job_posts
(employer_id, category_id, title, description, salary_min, salary_max, location, job_type, experience_level, status, posted_at, expired_at, rejection_reason)
SELECT e.id, c.id,
       'Data Analyst',
       'Phân tích dữ liệu hành vi người dùng, xây dashboard BI, theo dõi funnel ứng tuyển và hỗ trợ product team ra quyết định.',
       16000000, 28000000,
       'Hà Nội', 'FULL_TIME', 'MID',
       'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), NULL
FROM employers e
JOIN users u ON u.id = e.user_id
JOIN categories c ON c.name = 'Dữ liệu & AI'
WHERE u.email = 'recruitment@datamind.vn'
  AND NOT EXISTS (
      SELECT 1 FROM job_posts jp
      WHERE jp.employer_id = e.id AND jp.title = 'Data Analyst'
  );

INSERT INTO job_posts
(employer_id, category_id, title, description, salary_min, salary_max, location, job_type, experience_level, status, posted_at, expired_at, rejection_reason)
SELECT e.id, c.id,
       'Machine Learning Engineer',
       'Xây dựng mô hình gợi ý việc làm, ranking job feed và tối ưu matching giữa hồ sơ ứng viên và job post.',
       25000000, 45000000,
       'Hà Nội', 'FULL_TIME', 'SENIOR',
       'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), NULL
FROM employers e
JOIN users u ON u.id = e.user_id
JOIN categories c ON c.name = 'Dữ liệu & AI'
WHERE u.email = 'recruitment@datamind.vn'
  AND NOT EXISTS (
      SELECT 1 FROM job_posts jp
      WHERE jp.employer_id = e.id AND jp.title = 'Machine Learning Engineer'
  );

INSERT INTO job_posts
(employer_id, category_id, title, description, salary_min, salary_max, location, job_type, experience_level, status, posted_at, expired_at, rejection_reason)
SELECT e.id, c.id,
       'Digital Marketing Specialist',
       'Lập kế hoạch và vận hành chiến dịch quảng cáo tuyển dụng, tối ưu lead, performance marketing và social media.',
       12000000, 22000000,
       'Đà Nẵng', 'FULL_TIME', 'MID',
       'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), NULL
FROM employers e
JOIN users u ON u.id = e.user_id
JOIN categories c ON c.name = 'Marketing'
WHERE u.email = 'jobs@brightmarketing.vn'
  AND NOT EXISTS (
      SELECT 1 FROM job_posts jp
      WHERE jp.employer_id = e.id AND jp.title = 'Digital Marketing Specialist'
  );

INSERT INTO job_posts
(employer_id, category_id, title, description, salary_min, salary_max, location, job_type, experience_level, status, posted_at, expired_at, rejection_reason)
SELECT e.id, c.id,
       'UI UX Designer',
       'Thiết kế giao diện và trải nghiệm người dùng cho website tuyển dụng, xây wireframe, prototype và kiểm thử usability.',
       14000000, 24000000,
       'Remote', 'FULL_TIME', 'MID',
       'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 70 DAY), NULL
FROM employers e
JOIN users u ON u.id = e.user_id
JOIN categories c ON c.name = 'Thiết kế UI/UX'
WHERE u.email = 'jobs@brightmarketing.vn'
  AND NOT EXISTS (
      SELECT 1 FROM job_posts jp
      WHERE jp.employer_id = e.id AND jp.title = 'UI UX Designer'
  );

INSERT INTO job_posts
(employer_id, category_id, title, description, salary_min, salary_max, location, job_type, experience_level, status, posted_at, expired_at, rejection_reason)
SELECT e.id, c.id,
       'Business Development Executive',
       'Tìm kiếm khách hàng doanh nghiệp có nhu cầu đăng tuyển, demo sản phẩm và phối hợp với đội vận hành để tăng hiệu quả tuyển dụng.',
       10000000, 18000000,
       'TP. Hồ Chí Minh', 'FULL_TIME', 'JUNIOR',
       'PENDING_APPROVAL', NOW(), DATE_ADD(NOW(), INTERVAL 80 DAY), NULL
FROM employers e
JOIN users u ON u.id = e.user_id
JOIN categories c ON c.name = 'Kinh doanh / Bán hàng'
WHERE u.email = 'jobs@brightmarketing.vn'
  AND NOT EXISTS (
      SELECT 1 FROM job_posts jp
      WHERE jp.employer_id = e.id AND jp.title = 'Business Development Executive'
  );

INSERT INTO job_posts
(employer_id, category_id, title, description, salary_min, salary_max, location, job_type, experience_level, status, posted_at, expired_at, rejection_reason)
SELECT e.id, c.id,
       'Technical Recruiter',
       'Phụ trách tuyển dụng các vị trí IT, phối hợp hiring manager, tối ưu JD và trải nghiệm ứng viên trong toàn bộ pipeline tuyển dụng.',
       14000000, 22000000,
       'TP. Hồ Chí Minh', 'FULL_TIME', 'MID',
       'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 65 DAY), NULL
FROM employers e
JOIN users u ON u.id = e.user_id
JOIN categories c ON c.name = 'Nhân sự / Tuyển dụng'
WHERE u.email = 'hr@techbridge.vn'
  AND NOT EXISTS (
      SELECT 1 FROM job_posts jp
      WHERE jp.employer_id = e.id AND jp.title = 'Technical Recruiter'
  );
