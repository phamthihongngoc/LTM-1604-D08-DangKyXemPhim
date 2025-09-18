<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
   NETWORK PROGRAMMING
</h2>
<div align="center">
    <p align="center">
        <img src="docs/aiotlab_logo.png" alt="AIoTLab Logo" width="170"/>
        <img src="docs/fitdnu_logo.png" alt="FIT DNU Logo" width="180"/>
        <img src="docs/dnu_logo.png" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>

---

# 📖 1. Giới thiệu
Hệ thống **Cinema Booking** là một giải pháp phần mềm hiện đại được thiết kế để hỗ trợ người dùng đặt vé xem phim thông qua mô hình **máy khách – máy chủ**. Ứng dụng cung cấp chức năng quản lý suất chiếu, ghế ngồi, combo bắp nước, khuyến mãi và tạo vé điện tử có mã QR tiện lợi.  

Đề tài tập trung xây dựng một hệ thống đặt vé theo kiến trúc client-server, trong đó:  
- **Máy chủ (Server)** chịu trách nhiệm xử lý logic nghiệp vụ, kết nối và quản lý cơ sở dữ liệu MySQL.  
- **Máy khách (Client)** cung cấp giao diện đồ họa trực quan, giúp người dùng thao tác đặt vé dễ dàng.  

📊 **Tài liệu tiêu đề**  
- Xây dựng hệ thống đặt vé xem phim theo mô hình client-server  
- Triển khai giao thức TCP cho việc truyền thông tin đặt vé đáng tin cậy  
- Phát triển giao diện người dùng bằng Java Swing  
- Kết nối cơ sở dữ liệu MySQL để lưu trữ thông tin phim, suất chiếu, ghế, đơn đặt và khuyến mãi  
- Đảm bảo tính toàn vẹn dữ liệu với khóa ngoại và ràng buộc quan hệ  
- Hỗ trợ khuyến mãi, combo bắp nước và QR Code xác nhận vé  

---

# 🔧 2. Sử dụng trình cài đặt ngôn ngữ: Java

## 🌐 Ngôn Ngữ Lập Trình  
- **Java SE 17+**: Nền tảng chính  
- **Tính năng**: Hướng đối tượng, đa luồng, kết nối mạng  
- **Ứng dụng**: Viết toàn bộ client, server và business logic  

## 🎨 Giao Diện Người Dùng  
- **Java Swing**: Thư viện GUI cho ứng dụng desktop  
- **JFrame**: Container chính cho các cửa sổ (Login, Movie List, Trailer…)  
- **JPanel**: Quản lý bố cục hiển thị phim, ghế, thanh toán  
- **Thành phần Swing**: `JButton`, `JTable`, `JTextField`, `JPasswordField`…  
- **Xử lý sự kiện**: `ActionListener`, `MouseListener` để bắt thao tác người dùng  

## 🌐 Truyền Thông Mạng  
- **Giao thức TCP/IP**: Đảm bảo truyền thông tin đặt vé ổn định  
- **Lập trình Socket**: Sử dụng `Socket` và `ServerSocket` trong Java  
- **Port**: 12345 cho kết nối client-server  
- **Luồng đối tượng**: `ObjectInputStream` / `ObjectOutputStream` để trao đổi dữ liệu  

## 🗄️ Cơ Sở Dữ Liệu  
- **MySQL 8+**: Hệ quản trị cơ sở dữ liệu  
- **Trình điều khiển JDBC**: `mysql-connector-java`  
- **Lược đồ cơ sở dữ liệu**: Quản lý bảng `Users`, `Shows`, `Bookings`, `Combos`, `Promotions`  
- **Các thao tác SQL**: `SELECT`, `INSERT`, `UPDATE`, `DELETE` kèm ràng buộc `FOREIGN KEY` và `UNIQUE` để ngăn trùng ghế  

## 🔄 Xử Lý Đa Luồng  
- **Java Multithreading**: Cho phép nhiều client đặt vé cùng lúc  
- **Thread**: Mỗi kết nối client được xử lý bởi một luồng riêng trên server  
- **Đồng bộ hóa**: Đảm bảo tránh xung đột dữ liệu khi nhiều người cùng đặt một suất chiếu  

# 3. Hình ảnh chức năng.
# 4. Câc bước cài đặt.
---

# 📞 5. Liên hệ  

Nếu bạn có bất kỳ thắc mắc hoặc cần hỗ trợ về dự án **Cinema Booking**, vui lòng liên hệ:  

- 👨‍🎓 **Sinh viên thực hiện**: Phạm Thị Hồng Ngọc 
- 🎓 **Khoa**: Công nghệ Thông tin – Đại học Đại Nam  
- 📧 **Email**: pthn2488@gmail.com




