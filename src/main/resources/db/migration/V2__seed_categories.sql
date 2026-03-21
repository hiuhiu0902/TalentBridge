INSERT INTO categories (name, description)
SELECT 'Công nghệ thông tin', 'Lập trình viên, Tester, QA, BA, DevOps, quản trị hệ thống'
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE name = 'Công nghệ thông tin'
);

INSERT INTO categories (name, description)
SELECT 'Dữ liệu & AI', 'Data Analyst, Data Engineer, Data Scientist, Machine Learning, AI Engineer'
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE name = 'Dữ liệu & AI'
);

INSERT INTO categories (name, description)
SELECT 'Thiết kế UI/UX', 'UI Designer, UX Designer, Product Designer, Graphic Designer'
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE name = 'Thiết kế UI/UX'
);

INSERT INTO categories (name, description)
SELECT 'Marketing', 'Digital Marketing, SEO, Content Marketing, Performance Marketing'
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE name = 'Marketing'
);

INSERT INTO categories (name, description)
SELECT 'Kinh doanh / Bán hàng', 'Business Development, Sales Executive, Account Manager'
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE name = 'Kinh doanh / Bán hàng'
);

INSERT INTO categories (name, description)
SELECT 'Nhân sự / Tuyển dụng', 'Talent Acquisition, HR Executive, Recruiter, HRBP'
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE name = 'Nhân sự / Tuyển dụng'
);

INSERT INTO categories (name, description)
SELECT 'Kế toán / Tài chính', 'Kế toán, kiểm toán, tài chính doanh nghiệp, phân tích tài chính'
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE name = 'Kế toán / Tài chính'
);

INSERT INTO categories (name, description)
SELECT 'Vận hành / Hỗ trợ khách hàng', 'Operation Executive, Customer Support, Customer Success'
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE name = 'Vận hành / Hỗ trợ khách hàng'
);
