-- Password cho toàn bộ employer account bên dưới: 123456
-- BCrypt hash tương ứng: $2y$10$qggxpETkGWBe3Uk2bTbjZO.N3il9HzNj9LNc4xaxkdjHsFFFQUwRi

INSERT INTO users (username, email, full_name, password, role, active, avatar_url, provider, provider_id, created_at)
SELECT 'techbridge_hr', 'hr@techbridge.vn', 'Nguyễn Hoàng Minh',
       '$2y$10$qggxpETkGWBe3Uk2bTbjZO.N3il9HzNj9LNc4xaxkdjHsFFFQUwRi',
       'EMPLOYER', TRUE,
       'https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&w=300&q=80',
       'local', NULL, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'hr@techbridge.vn'
);

INSERT INTO users (username, email, full_name, password, role, active, avatar_url, provider, provider_id, created_at)
SELECT 'datamind_recruiter', 'recruitment@datamind.vn', 'Trần Thu Hà',
       '$2y$10$qggxpETkGWBe3Uk2bTbjZO.N3il9HzNj9LNc4xaxkdjHsFFFQUwRi',
       'EMPLOYER', TRUE,
       'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=300&q=80',
       'local', NULL, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'recruitment@datamind.vn'
);

INSERT INTO users (username, email, full_name, password, role, active, avatar_url, provider, provider_id, created_at)
SELECT 'brightmarketing_jobs', 'jobs@brightmarketing.vn', 'Lê Thanh Phương',
       '$2y$10$qggxpETkGWBe3Uk2bTbjZO.N3il9HzNj9LNc4xaxkdjHsFFFQUwRi',
       'EMPLOYER', TRUE,
       'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=300&q=80',
       'local', NULL, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'jobs@brightmarketing.vn'
);

INSERT INTO employers (user_id, company_name, website, description, logo_url, industry, company_size, address)
SELECT u.id,
       'TechBridge Solutions',
       'https://techbridge.vn',
       'Công ty công nghệ chuyên xây dựng nền tảng tuyển dụng, HRM và các sản phẩm số cho doanh nghiệp Việt Nam.',
       'https://logo.clearbit.com/example.com',
       'Technology',
       '51-200 nhân viên',
       'Quận 1, TP. Hồ Chí Minh'
FROM users u
WHERE u.email = 'hr@techbridge.vn'
  AND NOT EXISTS (
      SELECT 1 FROM employers e WHERE e.user_id = u.id
  );

INSERT INTO employers (user_id, company_name, website, description, logo_url, industry, company_size, address)
SELECT u.id,
       'DataMind Analytics',
       'https://datamind.vn',
       'Doanh nghiệp chuyên về dữ liệu, AI, dashboard phân tích và hệ thống gợi ý cho thương mại điện tử và HR Tech.',
       'https://logo.clearbit.com/example.org',
       'Data & AI',
       '11-50 nhân viên',
       'Cầu Giấy, Hà Nội'
FROM users u
WHERE u.email = 'recruitment@datamind.vn'
  AND NOT EXISTS (
      SELECT 1 FROM employers e WHERE e.user_id = u.id
  );

INSERT INTO employers (user_id, company_name, website, description, logo_url, industry, company_size, address)
SELECT u.id,
       'Bright Marketing Agency',
       'https://brightmarketing.vn',
       'Agency cung cấp dịch vụ branding, performance marketing, social media và tuyển dụng cho các startup tăng trưởng nhanh.',
       'https://logo.clearbit.com/example.net',
       'Marketing Agency',
       '51-200 nhân viên',
       'Hải Châu, Đà Nẵng'
FROM users u
WHERE u.email = 'jobs@brightmarketing.vn'
  AND NOT EXISTS (
      SELECT 1 FROM employers e WHERE e.user_id = u.id
  );
