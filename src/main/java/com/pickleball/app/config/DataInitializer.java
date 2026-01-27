package com.pickleball.app.config;

import com.pickleball.app.entity.*;
import com.pickleball.app.enums.*;
import com.pickleball.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourtGroupRepository courtGroupRepository;
    private final CourtRepository courtRepository;
    private final ServiceRepository serviceRepository;
    private final BookingRepository bookingRepository;
    private final NotificationRepository notificationRepository;
    private final CourtPriceConfigRepository priceConfigRepository;
    private final com.pickleball.app.service.TimeSlotService timeSlotService;
    private final com.pickleball.app.repository.TimeSlotRepository timeSlotRepository;
    private final com.pickleball.app.repository.TimeSlotConfigRepository timeSlotConfigRepository;
    private final com.pickleball.app.repository.PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Chỉ chạy nếu chưa có dữ liệu
        if (userRepository.count() > 0) {
            System.out.println("Database already initialized. Skipping data initialization.");
            return;
        }

        System.out.println("Initializing database with sample data...");

        // 1. Tạo Users
        User admin = createUser("admin@pickleball.com", "admin123", "Quản trị viên", "0901234567", 
                UserRole.ADMIN, UserStatus.ACTIVE);
        
        User manager1 = createUser("manager1@pickleball.com", "manager123", "Nguyễn Văn Quản Lý", "0902345678", 
                UserRole.COURT_MANAGER, UserStatus.ACTIVE);
        
        User manager2 = createUser("manager2@pickleball.com", "manager123", "Trần Thị Quản Lý", "0903456789", 
                UserRole.COURT_MANAGER, UserStatus.ACTIVE);
        
        User customer1 = createUser("customer1@pickleball.com", "customer123", "Lê Văn Khách", "0904567890", 
                UserRole.CUSTOMER, UserStatus.ACTIVE);
        
        User customer2 = createUser("customer2@pickleball.com", "customer123", "Phạm Thị Khách", "0905678901", 
                UserRole.CUSTOMER, UserStatus.ACTIVE);
        
        User customer3 = createUser("customer3@pickleball.com", "customer123", "Hoàng Văn Khách", "0906789012", 
                UserRole.CUSTOMER, UserStatus.ACTIVE);

        System.out.println("Created users: " + userRepository.count());

        // 2. Tạo Court Groups
        CourtGroup group1 = createCourtGroup("Cụm Sân Pickleball Quận 1", 
                "123 Đường Nguyễn Huệ, Phường Bến Nghé", "Quận 1", "Hồ Chí Minh",
                "Cụm sân hiện đại với 6 sân tiêu chuẩn, có đầy đủ tiện ích", 
                "https://example.com/images/court-group-1.jpg", manager1);
        
        CourtGroup group2 = createCourtGroup("Cụm Sân Pickleball Quận 7", 
                "456 Đường Nguyễn Thị Thập, Phường Tân Phú", "Quận 7", "Hồ Chí Minh",
                "Cụm sân mới xây với 4 sân cao cấp, view đẹp", 
                "https://example.com/images/court-group-2.jpg", manager2);
        
        CourtGroup group3 = createCourtGroup("Cụm Sân Pickleball Quận 2", 
                "789 Đường Nguyễn Duy Trinh, Phường Bình Trưng Đông", "Quận 2", "Hồ Chí Minh",
                "Cụm sân rộng rãi với 8 sân, phù hợp cho giải đấu", 
                "https://example.com/images/court-group-3.jpg", null); // Chưa gán manager

        System.out.println("Created court groups: " + courtGroupRepository.count());

        // 3. Tạo Courts
        // Group 1 - 6 sân
        Court court1_1 = createCourt("Sân 1", group1, CourtStatus.AVAILABLE, new BigDecimal("200000"));
        Court court1_2 = createCourt("Sân 2", group1, CourtStatus.AVAILABLE, new BigDecimal("200000"));
        Court court1_3 = createCourt("Sân 3", group1, CourtStatus.AVAILABLE, new BigDecimal("200000"));
        Court court1_4 = createCourt("Sân 4", group1, CourtStatus.AVAILABLE, new BigDecimal("200000"));
        Court court1_5 = createCourt("Sân 5", group1, CourtStatus.MAINTENANCE, new BigDecimal("200000"));
        Court court1_6 = createCourt("Sân 6", group1, CourtStatus.AVAILABLE, new BigDecimal("200000"));

        // Group 2 - 4 sân
        Court court2_1 = createCourt("Sân VIP 1", group2, CourtStatus.AVAILABLE, new BigDecimal("300000"));
        Court court2_2 = createCourt("Sân VIP 2", group2, CourtStatus.AVAILABLE, new BigDecimal("300000"));
        Court court2_3 = createCourt("Sân VIP 3", group2, CourtStatus.AVAILABLE, new BigDecimal("300000"));
        Court court2_4 = createCourt("Sân VIP 4", group2, CourtStatus.AVAILABLE, new BigDecimal("300000"));

        // Group 3 - 8 sân
        Court court3_1 = createCourt("Sân A1", group3, CourtStatus.AVAILABLE, new BigDecimal("180000"));
        Court court3_2 = createCourt("Sân A2", group3, CourtStatus.AVAILABLE, new BigDecimal("180000"));
        Court court3_3 = createCourt("Sân A3", group3, CourtStatus.AVAILABLE, new BigDecimal("180000"));
        Court court3_4 = createCourt("Sân A4", group3, CourtStatus.AVAILABLE, new BigDecimal("180000"));
        Court court3_5 = createCourt("Sân B1", group3, CourtStatus.AVAILABLE, new BigDecimal("180000"));
        Court court3_6 = createCourt("Sân B2", group3, CourtStatus.AVAILABLE, new BigDecimal("180000"));
        Court court3_7 = createCourt("Sân B3", group3, CourtStatus.AVAILABLE, new BigDecimal("180000"));
        Court court3_8 = createCourt("Sân B4", group3, CourtStatus.AVAILABLE, new BigDecimal("180000"));

        System.out.println("Created courts: " + courtRepository.count());

        // 4. Tạo Services
        // Group 1 services
        createService("Thuê vợt", group1, "cái", new BigDecimal("50000"), ServiceStatus.AVAILABLE);
        createService("Nước uống", group1, "chai", new BigDecimal("20000"), ServiceStatus.AVAILABLE);
        createService("Khăn lạnh", group1, "gói", new BigDecimal("10000"), ServiceStatus.AVAILABLE);
        createService("Bóng Pickleball", group1, "quả", new BigDecimal("30000"), ServiceStatus.AVAILABLE);

        // Group 2 services
        createService("Thuê vợt cao cấp", group2, "cái", new BigDecimal("80000"), ServiceStatus.AVAILABLE);
        createService("Nước uống premium", group2, "chai", new BigDecimal("30000"), ServiceStatus.AVAILABLE);
        createService("Dịch vụ massage", group2, "lần", new BigDecimal("200000"), ServiceStatus.AVAILABLE);

        // Group 3 services
        createService("Thuê vợt", group3, "cái", new BigDecimal("40000"), ServiceStatus.AVAILABLE);
        createService("Nước uống", group3, "chai", new BigDecimal("15000"), ServiceStatus.AVAILABLE);
        createService("Đồ ăn nhẹ", group3, "phần", new BigDecimal("50000"), ServiceStatus.AVAILABLE);

        System.out.println("Created services: " + serviceRepository.count());

        // 5. Tạo một số Bookings mẫu
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);

        // Booking hôm nay - đã thanh toán (có services)
        Booking booking1 = createBooking(customer1, court1_1, today, LocalTime.of(17, 0), LocalTime.of(19, 0), 
                new BigDecimal("400000"), BookingStatus.PAID, PaymentStatus.PAID);

        // Booking hôm nay - chưa thanh toán
        Booking booking2 = createBooking(customer2, court1_2, today, LocalTime.of(19, 0), LocalTime.of(21, 0), 
                new BigDecimal("400000"), BookingStatus.CONFIRMED, PaymentStatus.UNPAID);

        // Booking ngày mai - đã xác nhận (có services)
        Booking booking3 = createBooking(customer1, court2_1, tomorrow, LocalTime.of(18, 0), LocalTime.of(20, 0), 
                new BigDecimal("600000"), BookingStatus.CONFIRMED, PaymentStatus.PAID);

        // Booking ngày mai - đang chờ
        Booking booking4 = createBooking(customer3, court2_2, tomorrow, LocalTime.of(20, 0), LocalTime.of(22, 0), 
                new BigDecimal("600000"), BookingStatus.PENDING, PaymentStatus.UNPAID);

        // Booking ngày kia - đã hoàn thành (có services)
        Booking booking5 = createBooking(customer2, court3_1, dayAfterTomorrow.minusDays(3), LocalTime.of(17, 0), LocalTime.of(19, 0), 
                new BigDecimal("360000"), BookingStatus.COMPLETED, PaymentStatus.PAID, 
                LocalDateTime.now().minusDays(2));

        // Booking đã hủy
        Booking booking6 = createBooking(customer3, court1_3, today.plusDays(5), LocalTime.of(18, 0), LocalTime.of(20, 0), 
                new BigDecimal("400000"), BookingStatus.CANCELLED, PaymentStatus.REFUNDED);

        // Thêm một số bookings nữa
        Booking booking7 = createBooking(customer1, court1_4, tomorrow, LocalTime.of(16, 0), LocalTime.of(18, 0), 
                new BigDecimal("400000"), BookingStatus.CONFIRMED, PaymentStatus.PAID);
        
        Booking booking8 = createBooking(customer2, court2_3, today.plusDays(3), LocalTime.of(19, 0), LocalTime.of(21, 0), 
                new BigDecimal("600000"), BookingStatus.PENDING, PaymentStatus.UNPAID);

        System.out.println("Created bookings: " + bookingRepository.count());

        // 5.1. Tạo BookingServices (link services với bookings)
        System.out.println("Creating booking services...");
        createBookingServices(booking1, group1); // Booking có services
        createBookingServices(booking3, group2); // Booking có services
        createBookingServices(booking5, group3); // Booking có services
        createBookingServices(booking7, group1); // Booking có services
        System.out.println("Created booking services");

        // 6. Tạo Payments cho các bookings đã paid
        System.out.println("Creating payments...");
        createPayment(booking1, PaymentMethod.CREDIT_CARD, "TXN-" + booking1.getBookingId() + "-001");
        createPayment(booking3, PaymentMethod.MOMO, "TXN-" + booking3.getBookingId() + "-002");
        createPayment(booking5, PaymentMethod.CASH, null); // Thanh toán tại sân
        createPayment(booking7, PaymentMethod.CREDIT_CARD, "TXN-" + booking7.getBookingId() + "-003");
        System.out.println("Created payments: " + paymentRepository.count());

        // 7. Tạo Notifications mẫu
        createNotification(customer1.getUserId(), "Chào mừng đến với Pickleball!", 
                "Cảm ơn bạn đã đăng ký tài khoản. Chúc bạn có những trải nghiệm tuyệt vời!", "SYSTEM");
        
        createNotification(customer1.getUserId(), "Đặt sân thành công", 
                "Bạn đã đặt sân thành công. Vui lòng thanh toán để hoàn tất đặt sân.", "BOOKING");
        
        createNotification(customer2.getUserId(), "Thanh toán thành công", 
                "Thanh toán cho booking #" + booking1.getBookingId() + " đã được xác nhận.", "PAYMENT");
        
        createNotification(manager1.getUserId(), "Có booking mới", 
                "Có một booking mới tại cụm sân của bạn. Vui lòng kiểm tra và xác nhận.", "BOOKING");
        
        createNotification(manager1.getUserId(), "Booking đã được thanh toán", 
                "Booking #" + booking1.getBookingId() + " đã được thanh toán thành công.", "PAYMENT");
        
        createNotification(customer3.getUserId(), "Nhắc nhở thanh toán", 
                "Bạn có booking chưa thanh toán. Vui lòng thanh toán trước 24h để giữ chỗ.", "BOOKING");

        System.out.println("Created notifications: " + notificationRepository.count());

        // 8. Tạo Dynamic Pricing Configs mẫu
        // Peak hours cho court1_1: 17:00 - 21:00, giá tăng 20%
        createPricingConfig(court1_1, LocalTime.of(17, 0), LocalTime.of(21, 0), 
                "ALL", new BigDecimal("1.2"), false);
        
        // Off-peak cho court1_1: 5:00 - 9:00, giá giảm 20%
        createPricingConfig(court1_1, LocalTime.of(5, 0), LocalTime.of(9, 0), 
                "ALL", new BigDecimal("0.8"), false);
        
        // Peak hours cho court2_1 (VIP): 18:00 - 22:00, giá tăng 30%
        createPricingConfig(court2_1, LocalTime.of(18, 0), LocalTime.of(22, 0), 
                "ALL", new BigDecimal("1.3"), false);

        System.out.println("Created pricing configs: " + priceConfigRepository.count());

        // 9. Tạo Time Slot Configs mặc định cho các cụm sân
        System.out.println("Creating default time slot configs for court groups...");
        createDefaultTimeSlotConfigs();

        // 10. Tạo Time Slots cho tất cả courts
        System.out.println("Creating time slots for courts...");
        createTimeSlotsForAllCourts();
        System.out.println("Created time slots: " + timeSlotRepository.count());

        System.out.println("Database initialization completed successfully!");
        System.out.println("\n=== Login Credentials ===");
        System.out.println("Admin: admin@pickleball.com / admin123");
        System.out.println("Manager 1: manager1@pickleball.com / manager123");
        System.out.println("Manager 2: manager2@pickleball.com / manager123");
        System.out.println("Customer 1: customer1@pickleball.com / customer123");
        System.out.println("Customer 2: customer2@pickleball.com / customer123");
        System.out.println("Customer 3: customer3@pickleball.com / customer123");
    }

    private User createUser(String email, String password, String fullName, String phone, 
                           UserRole role, UserStatus status) {
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .fullName(fullName)
                .phoneNumber(phone)
                .role(role)
                .status(status)
                .build();
        return userRepository.save(user);
    }

    private CourtGroup createCourtGroup(String groupName, String address, String district, 
                                       String city, String description, String images, User manager) {
        CourtGroup group = CourtGroup.builder()
                .groupName(groupName)
                .address(address)
                .district(district)
                .city(city)
                .description(description)
                .images(images)
                .manager(manager)
                .build();
        return courtGroupRepository.save(group);
    }

    private Court createCourt(String courtName, CourtGroup group, CourtStatus status, BigDecimal price) {
        Court court = Court.builder()
                .courtGroup(group)
                .courtName(courtName)
                .status(status)
                .basePricePerHour(price)
                .build();
        return courtRepository.save(court);
    }

    private Service createService(String serviceName, CourtGroup group, String unit, 
                                 BigDecimal price, ServiceStatus status) {
        Service service = Service.builder()
                .courtGroup(group)
                .serviceName(serviceName)
                .unit(unit)
                .price(price)
                .status(status)
                .build();
        return serviceRepository.save(service);
    }

    private Booking createBooking(User user, Court court, LocalDate date, LocalTime startTime, 
                                  LocalTime endTime, BigDecimal totalPrice, BookingStatus status, 
                                  PaymentStatus paymentStatus) {
        return createBooking(user, court, date, startTime, endTime, totalPrice, status, paymentStatus, null);
    }

    private Booking createBooking(User user, Court court, LocalDate date, LocalTime startTime, 
                                  LocalTime endTime, BigDecimal totalPrice, BookingStatus status, 
                                  PaymentStatus paymentStatus, LocalDateTime checkedInAt) {
        Booking booking = Booking.builder()
                .user(user)
                .court(court)
                .bookingDate(date)
                .startTime(startTime)
                .endTime(endTime)
                .totalPrice(totalPrice)
                .status(status)
                .paymentStatus(paymentStatus)
                .checkedInAt(checkedInAt)
                .bookingServices(new java.util.ArrayList<>()) // Initialize list
                .build();
        return bookingRepository.save(booking);
    }

    /**
     * Tạo BookingServices cho một booking
     */
    private void createBookingServices(Booking booking, CourtGroup group) {
        // Lấy các services của court group
        List<Service> services = serviceRepository.findByCourtGroup(group);
        
        if (services.isEmpty()) return;
        
        // Đảm bảo booking có list bookingServices
        if (booking.getBookingServices() == null) {
            booking.setBookingServices(new java.util.ArrayList<>());
        }
        
        // Chọn ngẫu nhiên 1-3 services
        int numServices = Math.min(1 + (int)(Math.random() * 3), services.size());
        
        BigDecimal servicesTotal = BigDecimal.ZERO;
        
        for (int i = 0; i < numServices; i++) {
            Service service = services.get(i);
            int quantity = 1 + (int)(Math.random() * 3); // 1-3 items
            
            BookingService bookingService = BookingService.builder()
                    .booking(booking)
                    .service(service)
                    .quantity(quantity)
                    .unitPrice(service.getPrice())
                    .build();
            
            // Thêm vào list (cascade sẽ tự động lưu)
            booking.getBookingServices().add(bookingService);
            
            // Tính tổng services
            servicesTotal = servicesTotal.add(
                service.getPrice().multiply(BigDecimal.valueOf(quantity))
            );
        }
        
        // Cập nhật totalPrice (court price + services price)
        booking.setTotalPrice(booking.getTotalPrice().add(servicesTotal));
        bookingRepository.save(booking);
    }

    /**
     * Tạo Payment cho một booking đã paid
     */
    private void createPayment(Booking booking, PaymentMethod method, String transactionRef) {
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalPrice())
                .paymentMethod(method)
                .transactionRef(transactionRef)
                .status(booking.getPaymentStatus() == PaymentStatus.PAID ? PaymentStatus.PAID : PaymentStatus.PENDING)
                .paymentTime(booking.getPaymentStatus() == PaymentStatus.PAID ? 
                    LocalDateTime.now().minusHours((long)(Math.random() * 24)) : null)
                .build();
        paymentRepository.save(payment);
    }

    private Notification createNotification(Long userId, String title, String message, String type) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        return notificationRepository.save(notification);
    }

    private CourtPriceConfig createPricingConfig(Court court, LocalTime timeStart, LocalTime timeEnd, 
                                                 String daysOfWeek, BigDecimal priceModifier, boolean isHoliday) {
        CourtPriceConfig config = CourtPriceConfig.builder()
                .court(court)
                .timeStart(timeStart)
                .timeEnd(timeEnd)
                .daysOfWeek(daysOfWeek)
                .priceModifier(priceModifier)
                .isHoliday(isHoliday)
                .build();
        return priceConfigRepository.save(config);
    }

    /**
     * Tạo default time slot configs cho các cụm sân
     * Mỗi cụm sân sẽ có 1 config mặc định: 5h-22h, 60 phút/slot
     */
    private void createDefaultTimeSlotConfigs() {
        List<CourtGroup> courtGroups = courtGroupRepository.findAll();
        
        for (CourtGroup group : courtGroups) {
            // Kiểm tra xem đã có config cho cụm sân này chưa
            boolean exists = timeSlotConfigRepository.existsByCourtGroupAndIsActiveTrue(group);
            
            if (!exists) {
                TimeSlotConfig config = TimeSlotConfig.builder()
                        .courtGroup(group)
                        .court(null)
                        .openTime(LocalTime.of(5, 0))
                        .closeTime(LocalTime.of(22, 0))
                        .slotDuration(60)
                        .isActive(true)
                        .build();
                
                timeSlotConfigRepository.save(config);
            }
        }
        
        System.out.println("Created default time slot configs for " + courtGroups.size() + " court groups");
    }

    /**
     * Tạo time slots cho tất cả courts trong 30 ngày tới
     * Áp dụng dynamic pricing từ CourtPriceConfig
     * Đánh dấu slots là booked cho các bookings đã có
     */
    private void createTimeSlotsForAllCourts() {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(30); // Tạo slots cho 30 ngày
        
        // Lấy tất cả courts có status = AVAILABLE
        List<Court> availableCourts = courtRepository.findAll().stream()
                .filter(c -> c.getStatus() == CourtStatus.AVAILABLE)
                .collect(Collectors.toList());
        
        int totalSlots = 0;
        
        for (Court court : availableCourts) {
            LocalDate currentDate = today;
            
            while (!currentDate.isAfter(endDate)) {
                // Kiểm tra xem đã có slots cho ngày này chưa
                List<com.pickleball.app.entity.TimeSlot> existingSlots = 
                    timeSlotRepository.findByCourt_CourtIdAndSlotDateOrderByStartTime(
                        court.getCourtId(), currentDate);
                
                // Nếu chưa có, tạo mới (sẽ tự động lấy từ config nếu có)
                if (existingSlots.isEmpty()) {
                    // Gọi với tham số 0,0,0 để tự động lấy từ config, nếu không có thì dùng default
                    List<com.pickleball.app.entity.TimeSlot> slots = 
                        timeSlotService.generateTimeSlotsForDate(
                            court.getCourtId(), 
                            currentDate, 
                            0,  // startHour (0 = auto from config)
                            0,  // endHour (0 = auto from config)
                            0   // slotDurationMinutes (0 = auto from config)
                        );
                    
                    // Áp dụng dynamic pricing cho từng slot
                    for (com.pickleball.app.entity.TimeSlot slot : slots) {
                        BigDecimal slotPrice = calculateSlotPrice(court, slot.getStartTime());
                        slot.setPrice(slotPrice);
                        timeSlotRepository.save(slot);
                    }
                    
                    totalSlots += slots.size();
                }
                
                currentDate = currentDate.plusDays(1);
            }
        }
        
        // Đánh dấu slots là booked cho các bookings đã có
        markBookedSlots();
        
        System.out.println("Generated " + totalSlots + " time slots for " + availableCourts.size() + " courts");
    }

    /**
     * Tính giá cho một slot dựa trên dynamic pricing config
     */
    private BigDecimal calculateSlotPrice(Court court, LocalTime slotTime) {
        BigDecimal basePrice = court.getBasePricePerHour();
        
        // Tìm pricing config phù hợp
        List<CourtPriceConfig> configs = priceConfigRepository.findByCourt(court);
        
        for (CourtPriceConfig config : configs) {
            // Kiểm tra xem slot time có nằm trong khoảng config không
            if (!slotTime.isBefore(config.getTimeStart()) && slotTime.isBefore(config.getTimeEnd())) {
                // Áp dụng price modifier
                return basePrice.multiply(config.getPriceModifier());
            }
        }
        
        // Nếu không có config, dùng base price
        return basePrice;
    }

    /**
     * Đánh dấu các slots là booked cho các bookings đã có
     */
    private void markBookedSlots() {
        List<Booking> bookings = bookingRepository.findAll();
        
        for (Booking booking : bookings) {
            // Chỉ đánh dấu nếu booking chưa bị hủy
            if (booking.getStatus() != BookingStatus.CANCELLED) {
                // Tìm các slots overlapping với booking
                List<com.pickleball.app.entity.TimeSlot> overlappingSlots = 
                    timeSlotRepository.findOverlappingSlots(
                        booking.getCourt().getCourtId(),
                        booking.getBookingDate(),
                        booking.getStartTime(),
                        booking.getEndTime()
                    );
                
                // Đánh dấu là booked
                for (com.pickleball.app.entity.TimeSlot slot : overlappingSlots) {
                    slot.setIsAvailable(false);
                    slot.setBooking(booking);
                    timeSlotRepository.save(slot);
                }
            }
        }
    }
}

