import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import javax.swing.Timer;


public class MovieClient extends JFrame {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 5000;

    // Theme Colors
    private static final Color PRIMARY_COLOR     = new Color(59, 130, 246);
    private static final Color SECONDARY_COLOR   = new Color(147, 197, 253);
    private static final Color SUCCESS_COLOR     = new Color(187, 247, 208); // ghế trống
    private static final Color ERROR_COLOR       = new Color(254, 202, 202); // ghế đã đặt
    private static final Color WARNING_COLOR     = new Color(254, 240, 138); // ghế đang chọn
    private static final Color BACKGROUND_COLOR  = new Color(250, 250, 250);
    private static final Color CARD_COLOR        = Color.WHITE;
    private static final Color TEXT_PRIMARY      = new Color(23, 23, 23);
    private static final Color TEXT_SECONDARY    = new Color(82, 82, 82);

    private final String userEmail;
    private JComboBox<Show> showCombo;
    private JPanel seatPanel;
    private JLabel statusLabel;
    private JLabel movieInfoLabel;
    private JLabel selectedSeatsLabel;
    private JLabel posterLabel;
    private JButton trailerBtn;

    // Quản lý combo + promo
    private JButton comboBtn, promoBtn, bookButton;
    private JLabel totalPriceLabel;
    private JTextField promoField;

    private java.util.List<JToggleButton> seatButtons = new ArrayList<>();
    private java.util.List<int[]> selectedSeats = new ArrayList<>();
    private java.util.List<ComboItem> selectedCombos = new ArrayList<>();
    private int seatPrice = 70000; // giá vé mặc định 70k
    private int promoDiscount = 0; // %

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new MovieClient("demo@example.com").setVisible(true));
    }

    public MovieClient(String email) {
        super("Cinema Booking System");
        this.userEmail = email;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1250, 800);
        setLocationRelativeTo(null);
        setBackground(BACKGROUND_COLOR);
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadShows();
    }

    /* ---------- UI Components ---------- */
    private void initializeComponents() {
        showCombo = createStyledComboBox();
        statusLabel = createStyledLabel("Đang kết nối server...", TEXT_SECONDARY, 12);
        movieInfoLabel = createStyledLabel("Chọn suất chiếu để xem thông tin", TEXT_PRIMARY, 14);
        selectedSeatsLabel = createStyledLabel("Chưa chọn ghế nào", TEXT_SECONDARY, 12);
        seatPanel = new JPanel();
        seatPanel.setBackground(CARD_COLOR);

        posterLabel = new JLabel();
        posterLabel.setHorizontalAlignment(SwingConstants.CENTER);

        trailerBtn = createStyledButton("Xem Trailer", SECONDARY_COLOR);

        comboBtn = createStyledButton("Chọn Combo", SECONDARY_COLOR);
        promoBtn = createStyledButton("Áp dụng KM", SECONDARY_COLOR);
        bookButton = createStyledButton("Đặt vé", PRIMARY_COLOR);

        totalPriceLabel = createStyledLabel("Tổng tiền: 0đ", TEXT_PRIMARY, 16);
        promoField = new JTextField();
        promoField.setPreferredSize(new Dimension(150, 30));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
        getContentPane().setBackground(BACKGROUND_COLOR);
    }
    
    private void showPaymentDialog(Show sh, String seatStr, String comboStr, String promoCode, int total, String seatNames) {
        JDialog dialog = new JDialog(this, "Thanh toán", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        // Thông tin vé
        JTextArea infoArea = new JTextArea(
        	"Mail: " + userEmail + "\n" +
            "Phim: " + sh.title + "\n" +
            "Suất: " + sh.time + "\n" +
            "Ghế: " + seatNames + "\n" +
            "Tổng tiền: " + total + "đ\n"
        );
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoArea.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));


        JLabel qrLabel = new JLabel("", SwingConstants.CENTER);
        qrLabel.setPreferredSize(new Dimension(200, 200));

        try {
            // Nội dung QR: có thể là thông tin giao dịch, ghế, tổng tiền...
            String data = "Phim:" + sh.title + "|Ghế:" + seatStr + "|Tổng:" + total;
            qrLabel.setIcon(QRCodeUtil.generateQRCodeImage(data, 200, 200));
        } catch (Exception e) {
            qrLabel.setText("Không thể tạo QR");
        }


        JButton payBtn = new JButton("Thanh toán");
        JButton cancelBtn = new JButton("Hủy");

        payBtn.addActionListener(ev -> {
            String cmd = String.format("BOOKM %s|%s|%s|%s|%s",
                    sh.id, userEmail, seatStr, comboStr, promoCode);
            String resp = sendCmd(cmd);
            if (resp.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, "Đặt vé thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadSeats(sh);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(this, resp, "Lỗi đặt vé", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(ev -> dialog.dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.add(payBtn);
        btnPanel.add(cancelBtn);

        dialog.add(infoArea, BorderLayout.NORTH);
        dialog.add(qrLabel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Cinema Booking System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel userLabel = new JLabel("Xin chào, " + userEmail);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        JButton refreshBtn = createStyledButton("Làm mới", SECONDARY_COLOR);
        JButton myTicketsBtn = createStyledButton("Vé của tôi", SUCCESS_COLOR);
        JButton logoutBtn = createStyledButton("Đăng xuất", ERROR_COLOR);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(myTicketsBtn);
        buttonPanel.add(logoutBtn);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(titleLabel, BorderLayout.NORTH);
        leftPanel.add(userLabel, BorderLayout.CENTER);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        refreshBtn.addActionListener(e -> loadShows());
        myTicketsBtn.addActionListener(e -> showMyTickets());
        logoutBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn đăng xuất?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) dispose();
        });
        return headerPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 0, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);

        JPanel showPanel = createShowSelectionPanel();
        showPanel.setBorder(new EmptyBorder(0, 0, 0, 15)); // cách ra bên phải

        JPanel seatPanel = createSeatSelectionPanel();
        seatPanel.setBorder(new EmptyBorder(0, 15, 0, 0)); // cách ra bên trái

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, showPanel, seatPanel);
        splitPane.setDividerLocation(420);
        splitPane.setResizeWeight(0.35);
        splitPane.setDividerSize(12); // tạo khoảng ngăn rõ hơn
        splitPane.setBorder(null);

        mainPanel.add(splitPane, BorderLayout.CENTER);
        return mainPanel;
    }


    private JPanel createShowSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BACKGROUND_COLOR);

        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(CARD_COLOR);
        cardPanel.setBorder(createCardBorder());

        JLabel titleLabel = createStyledLabel("Chọn suất chiếu", TEXT_PRIMARY, 16);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        showCombo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel comboPanel = new JPanel();
        comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.Y_AXIS));
        comboPanel.setOpaque(false);
        comboPanel.add(titleLabel);
        comboPanel.add(Box.createVerticalStrut(8)); // khoảng cách nhỏ
        comboPanel.add(showCombo);


        // Poster + Trailer
        JPanel posterPanel = new JPanel();
        posterPanel.setLayout(new BoxLayout(posterPanel, BoxLayout.Y_AXIS));
        posterPanel.setOpaque(false);

        posterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        trailerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        posterPanel.add(posterLabel);
        posterPanel.add(Box.createVerticalStrut(8)); // 👈 khoảng cách 8px
        posterPanel.add(trailerBtn);

        // Info
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(10, 5, 0, 5));
        infoPanel.add(movieInfoLabel, BorderLayout.CENTER);

        cardPanel.add(titleLabel);
        cardPanel.add(comboPanel);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(posterPanel);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(infoPanel);

        panel.add(cardPanel, BorderLayout.NORTH);
        return panel;
    }


    private JPanel createSeatSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = createStyledLabel("Chọn ghế ngồi", TEXT_PRIMARY, 16);
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(selectedSeatsLabel, BorderLayout.EAST);

        JPanel seatContainer = new JPanel(new BorderLayout());
        seatContainer.setBackground(CARD_COLOR);
        seatContainer.setBorder(createCardBorder());

        JLabel screenLabel = new JLabel("MÀN HÌNH", SwingConstants.CENTER);
        screenLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        screenLabel.setForeground(TEXT_SECONDARY);
        screenLabel.setBorder(new EmptyBorder(15, 0, 20, 0));
        seatContainer.add(screenLabel, BorderLayout.NORTH);

        JScrollPane seatScrollPane = new JScrollPane(seatPanel);
        seatScrollPane.setBorder(null);
        seatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        seatContainer.add(seatScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        bottomPanel.setOpaque(false);

        JPanel promoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        promoPanel.setOpaque(false);
        promoPanel.add(new JLabel("Mã KM:"));
        promoPanel.add(promoField);
        promoPanel.add(promoBtn);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        actionPanel.setOpaque(false);
        actionPanel.add(comboBtn);
        actionPanel.add(bookButton);

        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        pricePanel.setOpaque(false);
        pricePanel.add(totalPriceLabel);

        bottomPanel.add(promoPanel);
        bottomPanel.add(actionPanel);
        bottomPanel.add(pricePanel);

        seatContainer.add(bottomPanel, BorderLayout.SOUTH);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(seatContainer, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(CARD_COLOR);
        footerPanel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(224, 224, 224)),
                new EmptyBorder(10, 20, 10, 20)
        ));
        footerPanel.add(statusLabel, BorderLayout.WEST);
        footerPanel.add(createStyledLabel("© 2025 Cinema Booking System", TEXT_SECONDARY, 10),
                BorderLayout.EAST);
        return footerPanel;
    }

    /* ---------- Event Handlers ---------- */
    private void setupEventHandlers() {
        showCombo.addActionListener(e -> {
            Show sh = (Show) showCombo.getSelectedItem();
            if (sh != null) {
                loadSeats(sh);
                updateMovieInfo(sh);
            }
        });

        trailerBtn.addActionListener(e -> {
            Show sh = (Show) showCombo.getSelectedItem();
            if (sh != null && sh.trailer != null && !sh.trailer.isEmpty()) {
                TrailerWindow.showTrailer(sh.trailer);
            }
        });

        comboBtn.addActionListener(e -> chooseCombo());
        promoBtn.addActionListener(e -> applyPromo());
        bookButton.addActionListener(e -> doBookTickets()); // 👈 thêm handler đặt vé
    }

    /* ---------- NEW: Đặt vé ---------- */
 
    private void doBookTickets() {
        Show sh = (Show) showCombo.getSelectedItem();
        if (sh == null || selectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ghế!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Hiển thị danh sách ghế chọn
        StringBuilder seatNames = new StringBuilder();
        for (int[] rc : selectedSeats) {
            if (seatNames.length() > 0) seatNames.append(", ");
            seatNames.append((char)('A' + rc[0])).append(rc[1] + 1);
        }

        int res = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn đặt vé?\nPhim: " + sh.title +
                "\nSuất: " + sh.time +
                "\nGhế: " + seatNames.toString() +
                "\n" + totalPriceLabel.getText(),
                "Xác nhận đặt vé",
                JOptionPane.YES_NO_OPTION
        );

        if (res != JOptionPane.YES_OPTION) return;

        // 🔹 Build dữ liệu chuẩn bị gửi server
        StringBuilder sbSeats = new StringBuilder();
        for (int[] rc : selectedSeats) {
            if (sbSeats.length() > 0) sbSeats.append(",");
            sbSeats.append(rc[0]).append("-").append(rc[1]);
        }
        String seatStr = sbSeats.toString();

        StringBuilder sbCombos = new StringBuilder();
        for (ComboItem c : selectedCombos) {
            if (sbCombos.length() > 0) sbCombos.append(",");
            sbCombos.append(c.id).append(":").append(c.qty);
        }
        String comboStr = sbCombos.toString();

        String promoCode = promoField.getText().trim();

        // 🔹 Tính tổng tiền
        int total = selectedSeats.size() * seatPrice;
        for (ComboItem c : selectedCombos) total += c.price * c.qty;
        if (promoDiscount > 0) total = total * (100 - promoDiscount) / 100;

        // 🔹 Thay vì gửi BOOKM ngay, mở dialog thanh toán
        showPaymentDialog(sh, seatStr, comboStr, promoCode, total, seatNames.toString());
    }

    
    private String sendCmd(String cmd) {
        try (Socket sock = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream(), "UTF-8"));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF-8"))) {
            out.write(cmd + "\n");
            out.flush();
            String line = in.readLine();
            return (line != null) ? line : "ERR Mất kết nối";
        } catch (IOException ex) {
            return "ERR " + ex.getMessage();
        }
    }

    /* ---------- Combo + Promo ---------- */
    private void chooseCombo() {
        java.util.List<ComboItem> combos = requestCombos();
        if (combos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có combo nào.");
            return;
        }

        JPanel panel = new JPanel(new GridLayout(combos.size(), 2, 5, 5));
        java.util.List<JSpinner> spinners = new ArrayList<>();

        for (ComboItem ci : combos) {
            panel.add(new JLabel(ci.name + " - " + ci.price + "đ"));
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
            spinners.add(spinner);
            panel.add(spinner);
        }

        int res = JOptionPane.showConfirmDialog(this, panel, "Chọn combo", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            selectedCombos.clear();
            for (int i = 0; i < combos.size(); i++) {
                int qty = (Integer) spinners.get(i).getValue();
                if (qty > 0) {
                    ComboItem c = combos.get(i);
                    selectedCombos.add(new ComboItem(c.id, c.name, c.price, qty));
                }
            }
            updateTotalPrice();
        }
    }

    private void applyPromo() {
        String code = promoField.getText().trim();
        if (code.isEmpty()) return;
        java.util.List<PromoItem> promos = requestPromos();
        for (PromoItem p : promos) {
            if (p.code.equalsIgnoreCase(code)) {
                promoDiscount = p.discount;
                JOptionPane.showMessageDialog(this, "Áp dụng KM: -" + promoDiscount + "%");
                updateTotalPrice();
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Mã khuyến mãi không hợp lệ!");
    }

    private void updateTotalPrice() {
        int total = selectedSeats.size() * seatPrice;
        for (ComboItem c : selectedCombos) total += c.price * c.qty;
        if (promoDiscount > 0) total = total * (100 - promoDiscount) / 100;
        totalPriceLabel.setText("Tổng tiền: " + total + "đ");
    }

    /* ---------- Network ---------- */
    private java.util.List<Show> requestShows() {
        java.util.List<Show> list = new ArrayList<>();
        try (Socket sock = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream(), "UTF-8"));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF-8"))) {
            out.write("LIST\n"); out.flush();
            if (!"OK".equals(in.readLine())) return list;
            String line;
            while ((line = in.readLine()) != null && !"END".equals(line)) {
                String[] p = line.split("\\|", -1);
                if (p.length >= 7)
                    list.add(new Show(p[0], p[1], p[2], Integer.parseInt(p[3]), Integer.parseInt(p[4]), p[5], p[6]));
            }
        } catch (IOException ex) { showError("Không thể lấy suất chiếu: " + ex.getMessage()); }
        return list;
    }

    private java.util.List<ComboItem> requestCombos() {
        java.util.List<ComboItem> list = new ArrayList<>();
        try (Socket sock = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream(), "UTF-8"));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF-8"))) {
            out.write("LISTCOMBOS\n"); out.flush();
            if (!"OK".equals(in.readLine())) return list;
            String line;
            while ((line = in.readLine()) != null && !"END".equals(line)) {
                String[] p = line.split("\\|");
                if (p.length >= 3)
                    list.add(new ComboItem(Integer.parseInt(p[0]), p[1], Integer.parseInt(p[2]), 0));
            }
        } catch (IOException ex) { showError("Không thể lấy combo: " + ex.getMessage()); }
        return list;
    }

    private java.util.List<PromoItem> requestPromos() {
        java.util.List<PromoItem> list = new ArrayList<>();
        try (Socket sock = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream(), "UTF-8"));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF-8"))) {
            out.write("LISTPROMOS\n"); out.flush();
            if (!"OK".equals(in.readLine())) return list;
            String line;
            while ((line = in.readLine()) != null && !"END".equals(line)) {
                String[] p = line.split("\\|");
                if (p.length >= 2)
                    list.add(new PromoItem(p[0], Integer.parseInt(p[1])));
            }
        } catch (IOException ex) { showError("Không thể lấy KM: " + ex.getMessage()); }
        return list;
    }

    /* ---------- Load + Render ---------- */
    private void loadShows() {
        statusLabel.setText("Đang tải suất chiếu...");
        java.util.List<Show> list = requestShows();
        showCombo.removeAllItems();
        for (Show s : list) showCombo.addItem(s);
        if (!list.isEmpty()) {
            Show sh = list.get(0);
            loadSeats(sh);
            updateMovieInfo(sh);
        }
        statusLabel.setText("Sẵn sàng");
    }

    private void loadSeats(Show sh) {
        boolean[][] seats = requestSeats(sh.id, sh.rows, sh.cols);
        renderSeatGrid(sh, seats);
    }

    private boolean[][] requestSeats(String showId, int rows, int cols) {
        boolean[][] seats = new boolean[rows][cols];
        try (Socket sock = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream(), "UTF-8"));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF-8"))) {
            out.write("SEATS " + showId + "\n"); out.flush();
            if (!"OK".equals(in.readLine())) return seats;
            for (int r = 0; r < rows; r++) {
                String line = in.readLine();
                for (int c = 0; c < cols && c < line.length(); c++) seats[r][c] = line.charAt(c) == '1';
            }
            while (!"END".equals(in.readLine())) {}
        } catch (IOException ex) { showError("Không thể tải ghế: " + ex.getMessage()); }
        return seats;
    }

    private void renderSeatGrid(Show sh, boolean[][] seats) {
        seatPanel.removeAll();
        seatPanel.setLayout(new GridLayout(sh.rows, sh.cols, 3, 3));
        seatButtons.clear();
        selectedSeats.clear();
        updateSelectedSeatsInfo();

        for (int r = 0; r < sh.rows; r++) {
            for (int c = 0; c < sh.cols; c++) {
                JToggleButton b = new JToggleButton((char)('A' + r) + "" + (c + 1));
                b.setFont(new Font("Segoe UI", Font.BOLD, 10));
                b.setPreferredSize(new Dimension(45, 35));
                final int row = r, col = c;
                if (seats[r][c]) {
                    b.setBackground(ERROR_COLOR); b.setEnabled(false);
                } else {
                    b.setBackground(SUCCESS_COLOR);
                    b.addActionListener(e -> {
                        if (b.isSelected()) {
                            b.setBackground(WARNING_COLOR);
                            selectedSeats.add(new int[]{row, col});
                        } else {
                            b.setBackground(SUCCESS_COLOR);
                            selectedSeats.removeIf(seat -> seat[0]==row && seat[1]==col);
                        }
                        updateSelectedSeatsInfo();
                        updateTotalPrice();
                    });
                }
                seatButtons.add(b);
                seatPanel.add(b);
            }
        }
        seatPanel.revalidate(); seatPanel.repaint();
    }

    private void updateSelectedSeatsInfo() {
        if (selectedSeats.isEmpty()) {
            selectedSeatsLabel.setText("Chưa chọn ghế nào");
            selectedSeatsLabel.setForeground(TEXT_SECONDARY);
        } else {
            selectedSeatsLabel.setText(String.format("Đã chọn: %d ghế", selectedSeats.size()));
            selectedSeatsLabel.setForeground(WARNING_COLOR);
        }
    }

    /* ---------- Cập nhật info phim ---------- */
    private void updateMovieInfo(Show show) {
        movieInfoLabel.setText(String.format("<html><b>%s</b><br/>Thời gian: %s<br/>%d x %d ghế</html>",
                show.title, show.time, show.rows, show.cols));

        try {
            if (show.poster != null && !show.poster.isEmpty()) {
                ImageIcon icon = new ImageIcon(new java.net.URL(show.poster));
                Image scaled = icon.getImage().getScaledInstance(200, 280, Image.SCALE_SMOOTH);
                posterLabel.setIcon(new ImageIcon(scaled));
            } else {
                posterLabel.setIcon(null);
            }
        } catch (Exception e) {
            posterLabel.setIcon(null);
        }
    }

    /* ---------- Tickets ---------- */
    private void showMyTickets() {
        java.util.List<String> tickets = requestMyTickets();
        if (tickets.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bạn chưa có vé nào.",
                    "Vé của tôi", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(Color.WHITE);

            for (String t : tickets) {
                JPanel card = new JPanel(new BorderLayout(10, 10));
                card.setBackground(new Color(245, 245, 245));
                card.setBorder(new CompoundBorder(
                        new LineBorder(new Color(200, 200, 200), 1, true),
                        new EmptyBorder(10, 10, 10, 10)
                ));

                String[] lines = t.split("\\|");
                StringBuilder info = new StringBuilder("<html>");
                String posterUrl = null;

                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("Poster:")) {
                        posterUrl = line.substring(7).trim();
                    } else {
                        info.append(line).append("<br/>");
                    }
                }
                info.append("</html>");

                JLabel lblInfo = new JLabel(info.toString());
                lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

                card.add(lblInfo, BorderLayout.CENTER);

                // Nếu có poster thì load ảnh
                if (posterUrl != null && !posterUrl.isEmpty()) {
                    try {
                        ImageIcon icon = new ImageIcon(new java.net.URL(posterUrl));
                        Image scaled = icon.getImage().getScaledInstance(100, 140, Image.SCALE_SMOOTH);
                        JLabel imgLabel = new JLabel(new ImageIcon(scaled));
                        card.add(imgLabel, BorderLayout.WEST);
                    } catch (Exception ex) {
                        // load lỗi thì bỏ qua
                    }
                }

                panel.add(card);
                panel.add(Box.createVerticalStrut(10));
            }

            JScrollPane scroll = new JScrollPane(panel);
            scroll.setPreferredSize(new Dimension(600, 400));
            scroll.setBorder(null);

            JOptionPane.showMessageDialog(this, scroll,
                    "Vé của tôi", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    private java.util.List<String> requestMyTickets() {
        java.util.List<String> list = new ArrayList<>();
        try (Socket sock = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream(), "UTF-8"));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF-8"))) {

            out.write("MYBOOKINGS " + userEmail + "\n");
            out.flush();

            if (!"OK".equals(in.readLine())) return list;

            StringBuilder current = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null && !"END".equals(line)) {
                if (line.equals("---")) {
                    list.add(current.toString().trim());
                    current.setLength(0);
                } else {
                    current.append(line).append("|");
                }
            }
            if (current.length() > 0) list.add(current.toString());

        } catch (IOException ex) {
            list.add("Lỗi: " + ex.getMessage());
        }
        return list;
    }


    /* ---------- Helpers ---------- */
    private JLabel createStyledLabel(String text, Color color, int size) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setBackground(bgColor);
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JComboBox<Show> createStyledComboBox() {
        JComboBox<Show> combo = new JComboBox<>();
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setPreferredSize(new Dimension(300, 35));
        return combo;
    }

    private Border createCardBorder() {
        return new CompoundBorder(
                new LineBorder(new Color(224, 224, 224), 1, true),
                new EmptyBorder(20, 20, 20, 20));
    }

    private void showError(String msg) {
        statusLabel.setText(msg);
        statusLabel.setForeground(Color.RED);
        Timer timer = new Timer(5000, e -> {
            statusLabel.setText("Sẵn sàng");
            statusLabel.setForeground(TEXT_SECONDARY);
        });
        timer.setRepeats(false);
        timer.start();
    }

    /* ---------- Data Models ---------- */
    private static class Show {
        String id, title, time;
        int rows, cols;
        String poster, trailer;
        String genre = "N/A";
        Show(String id, String title, String time, int rows, int cols, String poster, String trailer) {
            this.id = id; this.title = title; this.time = time;
            this.rows = rows; this.cols = cols;
            this.poster = poster; this.trailer = trailer;
        }
        public String toString() { return title + " - " + time; }
    }

    private static class ComboItem {
        int id, price, qty;
        String name;
        ComboItem(int id, String name, int price, int qty) {
            this.id=id; this.name=name; this.price=price; this.qty=qty;
        }
    }

    private static class PromoItem {
        String code; int discount;
        PromoItem(String code, int discount) { this.code=code; this.discount=discount; }
    }
}
