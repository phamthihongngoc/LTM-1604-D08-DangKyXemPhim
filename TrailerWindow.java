import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;

import javax.swing.*;

public class TrailerWindow {

    public static void showTrailer(String url) {
        JFrame frame = new JFrame("Trailer");
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final JFXPanel jfxPanel = new JFXPanel();
        frame.add(jfxPanel);

        Platform.runLater(() -> {
            WebView webView = new WebView();

            // Giả lập trình duyệt Chrome để tránh bị YouTube block
            webView.getEngine().setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36"
            );

            // Sửa lỗi HTML entity (&amp; -> &)
            String safeUrl = url.replace("&amp;", "&");

            webView.getEngine().load(safeUrl);

            Scene scene = new Scene(webView);
            jfxPanel.setScene(scene);

            // Dừng video khi đóng cửa sổ
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    Platform.runLater(() -> webView.getEngine().load(null));
                }
            });
        });

        frame.setVisible(true);
    }
}
