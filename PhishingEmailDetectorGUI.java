import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class PhishingEmailDetectorGUI extends JFrame {

    private JTextArea emailTextArea;
    private JTextPane resultPane;
    private JButton scanButton, loadButton;

    private static final String[] SUSPICIOUS_KEYWORDS = {
        "verify your account", "urgent", "click here", "password expired",
        "update your information", "suspended", "login immediately"
    };

    private static final String[] SUSPICIOUS_LINK_PATTERNS = {
        "http://", "https://", "bit.ly", ".ru", ".tk", ".zip", ".exe"
    };

    public PhishingEmailDetectorGUI() {
        setTitle("Phishing Email Detector v2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 550);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        emailTextArea = new JTextArea(10, 50);
        emailTextArea.setLineWrap(true);
        emailTextArea.setWrapStyleWord(true);
        JScrollPane emailScroll = new JScrollPane(emailTextArea);

        scanButton = new JButton("Scan Email");
        scanButton.addActionListener(e -> scanEmail());

        loadButton = new JButton("Load Email from File");
        loadButton.addActionListener(e -> loadFromFile());

        resultPane = new JTextPane();
        resultPane.setEditable(false);
        JScrollPane resultScroll = new JScrollPane(resultPane);

        JLabel instructions = new JLabel("   Paste email content or load a .txt file to scan for phishing");

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(instructions, BorderLayout.NORTH);
        topPanel.add(emailScroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loadButton);
        buttonPanel.add(scanButton);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(resultScroll, BorderLayout.CENTER);

        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(bottomPanel, BorderLayout.CENTER);
    }

    private void loadFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Email Text File");
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                emailTextArea.setText(""); // clear current content
                String line;
                while ((line = reader.readLine()) != null) {
                    emailTextArea.append(line + "\n");
                }
                reader.close();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage());
            }
        }
    }

    private void scanEmail() {
        String content = emailTextArea.getText().toLowerCase();

        if (content.isEmpty()) {
            showMessage("Please paste or load an email before scanning.", Color.RED);
            return;
        }

        int phishingScore = 0;
        StringBuilder reasons = new StringBuilder();

        for (String keyword : SUSPICIOUS_KEYWORDS) {
            if (content.contains(keyword)) {
                phishingScore += 2;
                reasons.append("- Suspicious phrase found: \"").append(keyword).append("\"\n");
            }
        }

        for (String pattern : SUSPICIOUS_LINK_PATTERNS) {
            if (content.contains(pattern)) {
                phishingScore += 2;
                reasons.append("- Suspicious link pattern found: \"").append(pattern).append("\"\n");
            }
        }

        if (content.contains("from:") && (!content.contains(".com") || content.contains("paypa1"))) {
            phishingScore += 3;
            reasons.append("- Suspicious sender address detected.\n");
        }

        String riskLevel;
        Color riskColor;

        if (phishingScore >= 8) {
            riskLevel = "HIGH RISK ⚠️ - Do NOT trust this email!";
            riskColor = Color.RED.darker();
        } else if (phishingScore >= 4) {
            riskLevel = "MEDIUM RISK ⚠ - Be cautious.";
            riskColor = Color.ORANGE.darker();
        } else {
            riskLevel = "LOW RISK ✅ - This email appears safe.";
            riskColor = new Color(0, 128, 0);
        }

        String message = "Phishing Score: " + phishingScore + "\n" + riskLevel + "\n\nReasons:\n";
        message += reasons.length() > 0 ? reasons.toString() : "No suspicious indicators found.";

        showMessage(message, riskColor);
    }

    private void showMessage(String message, Color color) {
        resultPane.setText("");
        resultPane.setForeground(color);
        resultPane.setText(message);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PhishingEmailDetectorGUI().setVisible(true));
    }
}
