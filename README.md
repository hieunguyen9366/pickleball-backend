# HƯỚNG DẪN SETUP BACKEND - PICKLEBALL COURT MANAGEMENT SYSTEM

## 📋 MỤC LỤC

1. [Yêu cầu hệ thống](#1-yêu-cầu-hệ-thống)
2. [Cài đặt môi trường](#2-cài-đặt-môi-trường)
3. [Clone project](#3-clone-project)
4. [Cấu hình Database](#4-cấu-hình-database)
5. [Cấu hình Application](#5-cấu-hình-application)
6. [Build và chạy project](#6-build-và-chạy-project)
7. [Setup Windows Service](#7-setup-windows-service)
8. [Kiểm tra và Test](#8-kiểm-tra-và-test)
9. [Troubleshooting](#9-troubleshooting)

---

## 1. YÊU CẦU HỆ THỐNG

### Phần mềm cần thiết:

- **Java Development Kit (JDK)**: Version 17 hoặc cao hơn
- **Maven**: Version 3.6+ (để build project)
- **MariaDB**: Version 10.5+ hoặc MySQL 8.0+
- **IDE**: IntelliJ IDEA, Eclipse, hoặc VS Code (khuyến nghị IntelliJ IDEA)
- **Git**: Để clone project

### Kiểm tra phiên bản:

```bash
java -version    # Phải hiển thị version 17 hoặc cao hơn
mvn -version     # Phải hiển thị version 3.6+
mysql --version  # Hoặc mariadb --version
```

---

## 2. CÀI ĐẶT MÔI TRƯỜNG

### 2.1. Cài đặt JDK 17

**Windows:**
1. Tải JDK 17 từ [Oracle](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) hoặc [OpenJDK](https://adoptium.net/)
2. Cài đặt và thiết lập biến môi trường:
   - `JAVA_HOME`: Đường dẫn đến thư mục JDK (VD: `C:\Program Files\Java\jdk-17`)
   - Thêm `%JAVA_HOME%\bin` vào `PATH`

**Linux/Mac:**
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# Mac (với Homebrew)
brew install openjdk@17
```

### 2.2. Cài đặt Maven

**Windows:**
1. Tải Maven từ [Apache Maven](https://maven.apache.org/download.cgi)
2. Giải nén vào thư mục (VD: `C:\Program Files\Apache\maven`)
3. Thiết lập biến môi trường:
   - `MAVEN_HOME`: Đường dẫn đến thư mục Maven
   - Thêm `%MAVEN_HOME%\bin` vào `PATH`

**Linux/Mac:**
```bash
# Ubuntu/Debian
sudo apt install maven

# Mac (với Homebrew)
brew install maven
```

### 2.3. Cài đặt MariaDB

**Windows:**
1. Tải MariaDB từ [MariaDB Downloads](https://mariadb.org/download/)
2. Cài đặt và ghi nhớ mật khẩu root
3. Đảm bảo service MariaDB đang chạy

**Linux:**
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install mariadb-server
sudo systemctl start mariadb
sudo systemctl enable mariadb
sudo mysql_secure_installation
```

**Mac:**
```bash
brew install mariadb
brew services start mariadb
```

---

## 3. CLONE PROJECT

### 3.1. Clone repository

```bash
git clone <repository-url>
cd mantis-free-angular-admin-template
cd backend
```

### 3.2. Kiểm tra cấu trúc project

Đảm bảo có các thư mục sau:
```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/pickleball/app/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── migrations/
│   ├── 002_add_time_slot_locks.sql
│   └── 003_add_time_slot_configs.sql
└── pom.xml
```

---

## 4. CẤU HÌNH DATABASE

### 4.1. Tạo database

Đăng nhập vào MariaDB/MySQL:

```bash
mysql -u root -p
```

Tạo database mới:

```sql
CREATE DATABASE pickleball_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pickleball_db;
```

### 4.2. Chạy migration scripts

Chạy các script migration theo thứ tự:

```bash
# Từ thư mục backend/migrations
mysql -u root -p pickleball_db < 002_add_time_slot_locks.sql
mysql -u root -p pickleball_db < 003_add_time_slot_configs.sql
```

**Lưu ý:** Nếu database đã có sẵn dữ liệu từ `DataInitializer`, các migration scripts sẽ tự động bỏ qua nếu bảng đã tồn tại (sử dụng `CREATE TABLE IF NOT EXISTS`).

### 4.3. Kiểm tra database

```sql
SHOW TABLES;
-- Phải thấy các bảng: users, courts, court_groups, time_slots, time_slot_locks, time_slot_configs, bookings, payments, ...
```

---

## 5. CẤU HÌNH APPLICATION

### 5.1. Cấu hình Database Connection

Mở file `backend/src/main/resources/application.properties` và cập nhật:

```properties
# Database Configuration
spring.datasource.url=jdbc:mariadb://localhost:3306/pickleball_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD_HERE
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDBDialect
```

**Lưu ý quan trọng:**
- Thay `YOUR_PASSWORD_HERE` bằng mật khẩu root của MariaDB/MySQL
- Nếu database ở server khác, thay `localhost` bằng IP/hostname
- Nếu port khác 3306, cập nhật trong URL

### 5.2. Cấu hình JWT Secret

```properties
# JWT Secret (Nên dùng biến môi trường trong production)
app.jwt.secret=YOUR_SECRET_KEY_HERE
app.jwt.expiration=86400000  # 24 giờ (milliseconds)
```

**Lưu ý:** Trong production, nên sử dụng biến môi trường thay vì hardcode secret key.

### 5.3. Cấu hình Email (Optional)

Nếu cần gửi email (verification, password reset):

```properties
# Mail Configuration (Gmail SMTP example)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

**Lưu ý:** 
- Với Gmail, cần tạo [App Password](https://support.google.com/accounts/answer/185833)
- Có thể bỏ qua nếu không cần gửi email (sẽ không ảnh hưởng đến các chức năng khác)

### 5.4. Cấu hình Frontend URL

```properties
# Frontend URL (for email links)
app.frontend.url=http://localhost:4200
```

Cập nhật nếu frontend chạy ở URL khác.

### 5.5. Cấu hình Port

```properties
server.port=8080
```

Thay đổi nếu port 8080 đã được sử dụng.

---

## 6. BUILD VÀ CHẠY PROJECT

### 6.1. Build project với Maven

Từ thư mục `backend/`:

```bash
# Clean và build
mvn clean install

# Hoặc chỉ build (bỏ qua tests)
mvn clean install -DskipTests
```

**Kết quả:** File JAR sẽ được tạo tại `backend/target/pickleball-app-0.0.1-SNAPSHOT.jar`

### 6.2. Chạy project (Development)

**Cách 1: Sử dụng Maven Spring Boot plugin**

```bash
mvn spring-boot:run
```

**Cách 2: Chạy từ JAR file**

```bash
java -jar target/pickleball-app-0.0.1-SNAPSHOT.jar
```

**Cách 3: Chạy từ IDE**

1. Mở project trong IntelliJ IDEA/Eclipse
2. Tìm class `PickleballApplication.java`
3. Click chuột phải → Run `PickleballApplication.main()`

### 6.3. Kiểm tra server đã chạy

Mở browser và truy cập:
- **API Base URL**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

### 6.4. Data Initialization

Khi chạy lần đầu, `DataInitializer` sẽ tự động:
- Tạo các user mẫu (Admin, Manager, Customer)
- Tạo cụm sân và sân mẫu
- Tạo time slot configs mặc định
- Tạo time slots cho 30 ngày đầu
- Tạo bookings và notifications mẫu

**Login credentials mặc định:**
```
Admin: admin@pickleball.com / admin123
Manager 1: manager1@pickleball.com / manager123
Manager 2: manager2@pickleball.com / manager123
Customer 1: customer1@pickleball.com / customer123
```

**Lưu ý:** Nếu database đã có dữ liệu (count > 0), DataInitializer sẽ bỏ qua.

---

## 7. SETUP WINDOWS SERVICE

### 7.1. Tạo JAR file production

```bash
mvn clean package -DskipTests
```

### 7.2. Tạo Windows Service với NSSM

**Bước 1:** Tải [NSSM (Non-Sucking Service Manager)](https://nssm.cc/download)

**Bước 2:** Giải nén và chạy NSSM:

```cmd
# Mở Command Prompt as Administrator
cd C:\path\to\nssm\win64
nssm install PickleballBackend
```

**Bước 3:** Cấu hình service:

- **Path**: `C:\Program Files\Java\jdk-17\bin\java.exe`
- **Startup directory**: `C:\path\to\backend\target`
- **Arguments**: `-jar pickleball-app-0.0.1-SNAPSHOT.jar`
- **Service name**: `PickleballBackend`

**Bước 4:** Cấu hình thêm:

- **Log on**: Chọn user để chạy service
- **Environment**: Thêm biến môi trường nếu cần (JAVA_HOME, etc.)

**Bước 5:** Khởi động service:

```cmd
nssm start PickleballBackend
```

### 7.3. Quản lý service

```cmd
# Start
nssm start PickleballBackend

# Stop
nssm stop PickleballBackend

# Restart
nssm restart PickleballBackend

# Xem status
nssm status PickleballBackend

# Xóa service
nssm remove PickleballBackend
```

### 7.4. Alternative: Sử dụng Windows Task Scheduler

1. Mở Task Scheduler
2. Create Basic Task
3. Trigger: When the computer starts
4. Action: Start a program
   - Program: `java.exe`
   - Arguments: `-jar C:\path\to\backend\target\pickleball-app-0.0.1-SNAPSHOT.jar`
   - Start in: `C:\path\to\backend\target`

---

## 8. KIỂM TRA VÀ TEST

### 8.1. Kiểm tra API endpoints

**Swagger UI:**
- Truy cập: http://localhost:8080/swagger-ui.html
- Xem tất cả API endpoints và test trực tiếp

**Test với cURL:**

```bash
# Health check (nếu có endpoint)
curl http://localhost:8080/api/health

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@pickleball.com","password":"admin123"}'
```

### 8.2. Kiểm tra Database

```sql
-- Kiểm tra số lượng records
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM courts;
SELECT COUNT(*) FROM time_slots;
SELECT COUNT(*) FROM time_slot_configs;

-- Kiểm tra time slot configs
SELECT * FROM time_slot_configs;

-- Kiểm tra scheduled tasks đã chạy
-- (Xem logs để biết scheduler đã tạo slots chưa)
```

### 8.3. Kiểm tra Logs

Logs sẽ hiển thị:
- Server đã khởi động thành công
- Database connection thành công
- DataInitializer đã chạy
- Scheduled tasks đang chạy

**Xem logs trong console hoặc file log (nếu có cấu hình logging).**

### 8.4. Test Scheduled Tasks

Scheduled tasks sẽ chạy tự động:
- **Cleanup expired locks**: Mỗi 1 phút
- **Generate time slots**: Mỗi ngày lúc 00:00 (tạo slots cho 30 ngày sau)
- **Cleanup old slots**: Mỗi ngày lúc 01:00 (xóa slots cũ hơn 30 ngày)

**Để test nhanh:** Có thể thay đổi cron expression tạm thời để test ngay.

---

## 9. TROUBLESHOOTING

### 9.1. Lỗi: "Port 8080 already in use"

**Giải pháp:**
- Thay đổi port trong `application.properties`: `server.port=8081`
- Hoặc tìm và kill process đang dùng port 8080:
  ```bash
  # Windows
  netstat -ano | findstr :8080
  taskkill /PID <PID> /F
  
  # Linux/Mac
  lsof -ti:8080 | xargs kill -9
  ```

### 9.2. Lỗi: "Cannot connect to database"

**Kiểm tra:**
1. MariaDB/MySQL service đang chạy
2. Username/password đúng trong `application.properties`
3. Database `pickleball_db` đã được tạo
4. Firewall không chặn port 3306

**Test connection:**
```bash
mysql -u root -p -h localhost -P 3306
```

### 9.3. Lỗi: "Table already exists" khi chạy migration

**Giải pháp:**
- Migration scripts sử dụng `CREATE TABLE IF NOT EXISTS`, nên an toàn
- Nếu vẫn lỗi, có thể drop và tạo lại:
  ```sql
  DROP TABLE IF EXISTS time_slot_configs;
  -- Sau đó chạy lại migration script
  ```

### 9.4. Lỗi: "JWT secret key too short"

**Giải pháp:**
- Đảm bảo JWT secret trong `application.properties` có ít nhất 64 ký tự
- Hoặc generate secret mới:
  ```bash
  # Linux/Mac
  openssl rand -hex 32
  
  # Windows (PowerShell)
  -join ((48..57) + (65..90) + (97..122) | Get-Random -Count 64 | % {[char]$_})
  ```

### 9.5. Lỗi: "DataInitializer không chạy"

**Kiểm tra:**
1. Database đã có dữ liệu chưa? (Nếu có thì sẽ bỏ qua)
2. Xem logs để biết lỗi cụ thể
3. Đảm bảo `@Component` và `@Transactional` đã được đánh dấu đúng

**Force re-initialize:**
```sql
-- Xóa tất cả dữ liệu (CẨN THẬN!)
TRUNCATE TABLE bookings;
TRUNCATE TABLE time_slots;
TRUNCATE TABLE payments;
TRUNCATE TABLE notifications;
TRUNCATE TABLE services;
TRUNCATE TABLE courts;
TRUNCATE TABLE court_groups;
TRUNCATE TABLE users;
-- Sau đó restart application
```

### 9.6. Lỗi: "Scheduled tasks không chạy"

**Kiểm tra:**
1. `@EnableScheduling` đã được thêm vào `PickleballApplication.java`
2. Xem logs để biết scheduler có khởi động không
3. Kiểm tra cron expression có đúng format không

### 9.7. Lỗi: "Time slots không được tạo tự động"

**Kiểm tra:**
1. Time slot configs đã được tạo chưa? (Kiểm tra bảng `time_slot_configs`)
2. Scheduler đã chạy chưa? (Xem logs)
3. Courts có status = AVAILABLE không?

**Tạo slots thủ công:**
- Có thể gọi API hoặc trigger scheduler thủ công

### 9.8. Lỗi: "Out of memory" khi build

**Giải pháp:**
```bash
# Tăng memory cho Maven
export MAVEN_OPTS="-Xmx2048m -Xms1024m"

# Hoặc trong Windows
set MAVEN_OPTS=-Xmx2048m -Xms1024m
```

### 9.9. Lỗi: "ClassNotFoundException" hoặc "NoClassDefFoundError"

**Giải pháp:**
```bash
# Clean và rebuild
mvn clean install

# Xóa .m2/repository và download lại dependencies
rm -rf ~/.m2/repository/com/pickleball
mvn clean install
```

### 9.10. Lỗi: "Email không gửi được"

**Kiểm tra:**
1. Email/password đúng chưa
2. App Password đã được tạo (với Gmail)
3. SMTP settings đúng
4. Firewall không chặn port 587

**Test email:**
- Có thể tạm thời bỏ qua email config nếu không cần thiết

---

## 📝 GHI CHÚ QUAN TRỌNG

1. **Security:**
   - Không commit `application.properties` với thông tin nhạy cảm lên Git
   - Sử dụng environment variables hoặc Spring Profiles cho production
   - Thay đổi JWT secret và database password trong production

2. **Performance:**
   - Trong production, nên tắt `spring.jpa.show-sql=true`
   - Cấu hình connection pool phù hợp
   - Enable caching nếu cần

3. **Monitoring:**
   - Cấu hình logging (Logback/Log4j2)
   - Setup health checks
   - Monitor scheduled tasks

4. **Backup:**
   - Backup database thường xuyên
   - Lưu trữ migration scripts
   - Document các thay đổi cấu hình

---

## 🔗 TÀI LIỆU THAM KHẢO

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MariaDB Documentation](https://mariadb.com/kb/en/documentation/)
- [Maven Documentation](https://maven.apache.org/guides/)
- [NSSM Documentation](https://nssm.cc/usage)

---

## 📞 HỖ TRỢ

Nếu gặp vấn đề, kiểm tra:
1. Logs của application
2. Logs của database
3. Network connectivity
4. Firewall settings
5. File permissions

