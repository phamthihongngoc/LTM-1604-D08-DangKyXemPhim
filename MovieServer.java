import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class MovieServer {
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("🎬 MovieServer đang chạy tại cổng " + PORT);
            while (true) {
                Socket client = serverSocket.accept();
                new Thread(new ClientHandler(client)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/* ------------ Client Handler ------------ */
class ClientHandler implements Runnable {
    private Socket socket;

    ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"))) {

            String line = in.readLine();
            if (line == null) return;

            if (line.startsWith("REGISTER")) {
                handleRegister(out, line.split(" "));
            } else if (line.startsWith("LOGIN")) {
                handleLogin(out, line.split(" "));
            } else if (line.equals("LIST")) {
                handleListShows(out);
            } else if (line.startsWith("SEATS")) {
                handleSeats(out, line.split(" ")[1]);
            } else if (line.startsWith("BOOKM")) {
                handleBooking(out, line.substring(6));
            } else if (line.startsWith("MYBOOKINGS")) {
                handleMyBookings(out, line.split(" ")[1]);
            } else if (line.equals("LISTCOMBOS")) {
                handleListCombos(out);
            } else if (line.equals("LISTPROMOS")) {
                handleListPromos(out);
            } else {
                out.write("ERR Lệnh không hợp lệ\n"); out.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* ------------ REGISTER ------------ */
    private void handleRegister(BufferedWriter out, String[] parts) throws IOException {
        if (parts.length < 3) {
            out.write("ERR Thiếu email hoặc mật khẩu\n"); out.flush(); return;
        }
        String email = parts[1];
        String pass = parts[2];

        try (Connection conn = DB.getConnection()) {
            PreparedStatement check = conn.prepareStatement("SELECT email FROM Users WHERE email=?");
            check.setString(1, email);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                out.write("ERR Email đã tồn tại\n"); out.flush(); return;
            }

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Users(email, password, fullname) VALUES(?,?,?)");
            ps.setString(1, email);
            ps.setString(2, pass);
            ps.setString(3, email); // fullname mặc định = email
            ps.executeUpdate();

            out.write("OK Đăng ký thành công\n"); out.flush();
        } catch (SQLException e) {
            out.write("ERR Lỗi SQL khi đăng ký\n"); out.flush();
            e.printStackTrace();
        }
    }

    /* ------------ LOGIN ------------ */
    private void handleLogin(BufferedWriter out, String[] parts) throws IOException {
        if (parts.length < 3) {
            out.write("ERR Thiếu email hoặc mật khẩu\n"); out.flush(); return;
        }
        String email = parts[1];
        String pass = parts[2];

        try (Connection conn = DB.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT email FROM Users WHERE email=? AND password=?");
            ps.setString(1, email);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                out.write("OK Đăng nhập thành công\n");
            } else {
                out.write("ERR Sai email hoặc mật khẩu\n");
            }
            out.flush();
        } catch (SQLException e) {
            out.write("ERR Lỗi SQL khi đăng nhập\n"); out.flush();
            e.printStackTrace();
        }
    }

    /* ------------ LIST SHOWS ------------ */
    private void handleListShows(BufferedWriter out) throws IOException {
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, title, showtime, total_rows, total_cols, poster, trailer FROM Shows")) {
            ResultSet rs = ps.executeQuery();
            out.write("OK\n");
            while (rs.next()) {
                out.write(rs.getString("id") + "|" + rs.getString("title") + "|" + rs.getString("showtime") + "|" +
                          rs.getInt("total_rows") + "|" + rs.getInt("total_cols") + "|" +
                          rs.getString("poster") + "|" + rs.getString("trailer") + "\n");
            }
            out.write("END\n"); out.flush();
        } catch (SQLException e) {
            out.write("ERR Lỗi SQL khi lấy danh sách suất chiếu\n"); out.flush();
            e.printStackTrace();
        }
    }

    /* ------------ SEATS ------------ */
    private void handleSeats(BufferedWriter out, String showId) throws IOException {
        try (Connection conn = DB.getConnection()) {
            int rows=0, cols=0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT total_rows, total_cols FROM Shows WHERE id=?")) {
                ps.setString(1, showId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) { rows = rs.getInt("total_rows"); cols = rs.getInt("total_cols"); }
            }
            boolean[][] seats = new boolean[rows][cols];
            try (PreparedStatement ps = conn.prepareStatement("SELECT seat_row, seat_col FROM Bookings WHERE showId=?")) {
                ps.setString(1, showId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int r = rs.getInt("seat_row"), c = rs.getInt("seat_col");
                    seats[r][c] = true;
                }
            }
            out.write("OK\n");
            for (int r=0;r<rows;r++) {
                for (int c=0;c<cols;c++) out.write(seats[r][c] ? "1" : "0");
                out.write("\n");
            }
            out.write("END\n"); out.flush();
        } catch (SQLException e) {
            out.write("ERR Lỗi SQL khi lấy ghế\n"); out.flush();
            e.printStackTrace();
        }
    }

    /* ------------ BOOKM ------------ */
    // format: showId|email|row-col,row-col|comboId:qty,comboId:qty|promoCode
    private void handleBooking(BufferedWriter out, String data) throws IOException {
        String[] parts = data.split("\\|");
        if (parts.length < 3) { out.write("ERR Thiếu tham số BOOKM\n"); out.flush(); return; }

        String showId = parts[0];
        String email = parts[1];
        String[] seatList = parts[2].split(",");
        String comboStr = parts.length >= 4 ? parts[3] : "";
        String promoCode = parts.length >= 5 ? parts[4] : "";

        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // insert bookings
                for (String s : seatList) {
                    if (s.isEmpty()) continue;
                    String[] rc = s.split("-");
                    int row = Integer.parseInt(rc[0]);
                    int col = Integer.parseInt(rc[1]);
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO Bookings(showId, email, seat_row, seat_col) VALUES (?,?,?,?)")) {
                        ps.setString(1, showId);
                        ps.setString(2, email);
                        ps.setInt(3, row);
                        ps.setInt(4, col);
                        ps.executeUpdate();
                    }
                }

                // insert combos nếu có
                if (!comboStr.isEmpty()) {
                    for (String c : comboStr.split(",")) {
                        if (c.isEmpty()) continue;
                        String[] pq = c.split(":");
                        int comboId = Integer.parseInt(pq[0]);
                        int qty = Integer.parseInt(pq[1]);
                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO OrderCombos(showId, email, comboId, quantity) VALUES (?,?,?,?)")) {
                            ps.setString(1, showId);
                            ps.setString(2, email);
                            ps.setInt(3, comboId);
                            ps.setInt(4, qty);
                            ps.executeUpdate();
                        }
                    }
                }

                // lưu promo nếu có
                if (!promoCode.isEmpty()) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO BookingPromos(showId, email, promoCode) VALUES (?,?,?)")) {
                        ps.setString(1, showId);
                        ps.setString(2, email);
                        ps.setString(3, promoCode);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                out.write("OK Đặt vé thành công\n"); out.flush();
            } catch (SQLException ex) {
                conn.rollback();
                out.write("ERR Lỗi SQL khi đặt vé\n"); out.flush();
                ex.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            out.write("ERR Lỗi kết nối DB khi BOOKM\n"); out.flush();
            e.printStackTrace();
        }
    }

    /* ------------ MYBOOKINGS ------------ */
    private void handleMyBookings(BufferedWriter out, String email) throws IOException {
        try (Connection conn = DB.getConnection()) {
            // Lấy show + ghế
            String sql = "SELECT s.id, s.title, s.showtime, s.poster, b.seat_row, b.seat_col, " +
                         "bp.promoCode " +
                         "FROM Bookings b " +
                         "JOIN Shows s ON b.showId=s.id " +
                         "LEFT JOIN BookingPromos bp ON b.showId=bp.showId AND b.email=bp.email " +
                         "WHERE b.email=? " +
                         "ORDER BY s.showtime";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            // Map để group
            Map<String, TicketInfo> tickets = new LinkedHashMap<>();

            while (rs.next()) {
                String key = rs.getString("id") + "|" + rs.getString("showtime");
                TicketInfo info = tickets.getOrDefault(key,
                        new TicketInfo(rs.getString("title"),
                                       rs.getString("showtime"),
                                       rs.getString("poster"),
                                       rs.getString("promoCode")));
                // Thêm ghế
                String seat = (char)('A' + rs.getInt("seat_row")) + String.valueOf(rs.getInt("seat_col") + 1);
                info.seats.add(seat);
                tickets.put(key, info);
            }

            // Lấy combos
            String comboSql = "SELECT oc.showId, c.name, oc.quantity " +
                              "FROM OrderCombos oc JOIN Combos c ON oc.comboId=c.id " +
                              "WHERE oc.email=?";
            PreparedStatement ps2 = conn.prepareStatement(comboSql);
            ps2.setString(1, email);
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) {
                String key = rs2.getString("showId"); // match với group theo showId
                for (String k : tickets.keySet()) {
                    if (k.startsWith(key + "|")) {
                        tickets.get(k).combos.add(rs2.getString("name") + " x" + rs2.getInt("quantity"));
                    }
                }
            }

            // Xuất kết quả
            out.write("OK\n");
            for (TicketInfo t : tickets.values()) {
                out.write("Title: " + t.title + " - " + t.time + "\n");
                out.write("Seats: " + String.join(", ", t.seats) + "\n");
                if (!t.combos.isEmpty()) out.write("Combos: " + String.join(", ", t.combos) + "\n");
                if (t.promo != null) out.write("KM: " + t.promo + "\n");
                out.write("Poster: " + t.poster + "\n");
                out.write("---\n"); // phân cách
            }
            out.write("END\n");
            out.flush();

        } catch (SQLException e) {
            out.write("ERR Lỗi SQL khi lấy vé\n"); out.flush();
            e.printStackTrace();
        }
    }

    // class phụ
    class TicketInfo {
        String title, time, poster, promo;
        List<String> seats = new ArrayList<>();
        List<String> combos = new ArrayList<>();
        TicketInfo(String title, String time, String poster, String promo) {
            this.title = title; this.time = time; this.poster = poster; this.promo = promo;
        }
    }



    /* ------------ LISTCOMBOS ------------ */
    private void handleListCombos(BufferedWriter out) throws IOException {
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, name, price FROM Combos")) {
            ResultSet rs = ps.executeQuery();
            out.write("OK\n");
            while (rs.next()) {
                out.write(rs.getInt("id") + "|" + rs.getString("name") + "|" + rs.getInt("price") + "\n");
            }
            out.write("END\n"); out.flush();
        } catch (SQLException e) {
            out.write("ERR Lỗi SQL khi lấy combo\n"); out.flush();
            e.printStackTrace();
        }
    }

    /* ------------ LISTPROMOS ------------ */
    private void handleListPromos(BufferedWriter out) throws IOException {
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT code, discount FROM Promotions WHERE expiry>=CURRENT_DATE")) {
            ResultSet rs = ps.executeQuery();
            out.write("OK\n");
            while (rs.next()) {
                out.write(rs.getString("code") + "|" + rs.getInt("discount") + "\n");
            }
            out.write("END\n"); out.flush();
        } catch (SQLException e) {
            out.write("ERR Lỗi SQL khi lấy khuyến mãi\n"); out.flush();
            e.printStackTrace();
        }
    }
}

/* ------------ DB Utility ------------ */
class DB {
    private static final String URL = "jdbc:mysql://localhost:3306/cinema?useUnicode=true&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASS = "12062004";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
