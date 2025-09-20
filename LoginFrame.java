import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.*;
import java.net.Socket;
import javax.swing.Timer;

public class LoginFrame extends JFrame {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 5000;

    private static final Color PRIMARY_COLOR    = new Color(30, 41, 59);
    private static final Color BACKGROUND_COLOR = new Color(241, 245, 249);
    private static final Color CARD_COLOR       = Color.WHITE;
    private static final Color TEXT_COLOR       = new Color(30, 41, 59);
    private static final Color SUCCESS_COLOR    = new Color(34, 197, 94);
    private static final Color ERROR_COLOR      = new Color(239, 68, 68);

    private JTextField emailField;
    private JPasswordField passField;
    private JLabel statusLabel;
    private JButton loginBtn;
    private JButton registerBtn;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    public LoginFrame() {
        super("Movie App - Đăng nhập");
        initializeFrame();
        createComponents();
        setupLayout();
        addEventListeners();
    }

    private void initializeFrame() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 550);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BACKGROUND_COLOR);
    }

    private void createComponents() {
        emailField = createStyledTextField("Email của bạn");
        passField = createStyledPasswordField("Mật khẩu");

//        loginBtn = createStyledButton("Đăng nhập", PRIMARY_COLOR);
//        registerBtn = createStyledButton("Đăng ký", new Color(100, 116, 139));
        loginBtn = createStyledButton("Đăng nhập", new Color(226, 232, 240), TEXT_COLOR);

        registerBtn = createStyledButton("Đăng ký", new Color(226, 232, 240), TEXT_COLOR);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(TEXT_COLOR);
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(300, 45));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(new Color(120, 120, 120));
        field.setText(placeholder);

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        field.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_COLOR);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(new Color(120, 120, 120));
                }
            }
        });
        return field;
    }

    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(new Dimension(300, 45));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setEchoChar((char) 0);
        field.setText(placeholder);
        field.setForeground(new Color(120, 120, 120));

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        field.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('•');
                    field.setForeground(TEXT_COLOR);
                }
            }
            public void focusLost(FocusEvent e) {
                if (String.valueOf(field.getPassword()).isEmpty()) {
                    field.setEchoChar((char) 0);
                    field.setText(placeholder);
                    field.setForeground(new Color(120, 120, 120));
                }
            }
        });
        return field;
    }

    private JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(300, 45));
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(textColor); 
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }


    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(40, 50, 40, 50));

        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(CARD_COLOR);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(40, 40, 40, 40)
        ));

        JLabel titleLabel = new JLabel("Chào mừng trở lại!");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Đăng nhập vào tài khoản của bạn");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 116, 139));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        emailField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passField.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        cardPanel.add(titleLabel);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(subtitleLabel);
        cardPanel.add(Box.createVerticalStrut(30));
        cardPanel.add(emailField);
        cardPanel.add(Box.createVerticalStrut(15));
        cardPanel.add(passField);
        cardPanel.add(Box.createVerticalStrut(25));
        cardPanel.add(loginBtn);
        cardPanel.add(Box.createVerticalStrut(15));
        cardPanel.add(registerBtn);
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(statusLabel);

        mainPanel.add(cardPanel);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void addEventListeners() {
        loginBtn.addActionListener(e -> doLogin());
        registerBtn.addActionListener(e -> doRegister());
        getRootPane().setDefaultButton(loginBtn);
    }

    private void doLogin() {
        String email = getEmailText();
        String password = getPasswordText();

        if (email.isEmpty() || password.isEmpty()) {
            showStatus("Vui lòng nhập email và mật khẩu", ERROR_COLOR);
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("Đang đăng nhập...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            protected String doInBackground() {
                return sendCmd("LOGIN " + email + " " + password);
            }
            protected void done() {
                try {
                    String result = get();
                    loginBtn.setEnabled(true);
                    loginBtn.setText("Đăng nhập");
                    if (result.startsWith("OK")) {
                        showStatus("Đăng nhập thành công!", SUCCESS_COLOR);
                        Timer timer = new Timer(1000, evt -> {
                            dispose();
                            SwingUtilities.invokeLater(() -> new MovieClient(email).setVisible(true));
                        });
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                    	if (result.startsWith("ERR")) {
                    	    String msg = (result.length() > 4) ? result.substring(4) : "Lỗi không xác định";
                    	    showStatus(msg, ERROR_COLOR);
                    	} else {
                    	    showStatus(result, ERROR_COLOR);
                    	}

                    }
                } catch (Exception ex) {
                    showStatus("Có lỗi xảy ra: " + ex.getMessage(), ERROR_COLOR);
                }
            }
        };
        worker.execute();
    }

    private void doRegister() {
        String email = getEmailText();
        String password = getPasswordText();

        if (email.isEmpty() || password.isEmpty()) {
            showStatus("Vui lòng nhập email và mật khẩu", ERROR_COLOR);
            return;
        }
        if (password.length() < 6) {
            showStatus("Mật khẩu phải có ít nhất 6 ký tự", ERROR_COLOR);
            return;
        }

        registerBtn.setEnabled(false);
        registerBtn.setText("Đang đăng ký...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            protected String doInBackground() {
                return sendCmd("REGISTER " + email + " " + password);
            }
            protected void done() {
                try {
                    String result = get();
                    registerBtn.setEnabled(true);
                    registerBtn.setText("Đăng ký");
                    if (result.startsWith("OK")) {
                        showStatus("Đăng ký thành công! Hãy đăng nhập.", SUCCESS_COLOR);
                    } else {
                        showStatus(result.startsWith("ERR") ? result.substring(4) : result, ERROR_COLOR);
                    }
                } catch (Exception ex) {
                    showStatus("Có lỗi xảy ra: " + ex.getMessage(), ERROR_COLOR);
                }
            }
        };
        worker.execute();
    }

    private String getEmailText() {
        String email = emailField.getText().trim();
        return email.equals("Email của bạn") ? "" : email;
    }

    private String getPasswordText() {
        String password = new String(passField.getPassword());
        return password.equals("Mật khẩu") ? "" : password;
    }

    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
        Timer timer = new Timer(4000, e -> statusLabel.setText(" "));
        timer.setRepeats(false);
        timer.start();
    }

    private String sendCmd(String cmd) {
        try (Socket sock = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream(), "UTF-8"));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF-8"))) {

            out.write(cmd + "\n");
            out.flush();
            String line = in.readLine();
            return (line == null) ? "ERR Mất kết nối" : line;
        } catch (IOException ex) {
            return "ERR " + ex.getMessage();
        }
    }
}
