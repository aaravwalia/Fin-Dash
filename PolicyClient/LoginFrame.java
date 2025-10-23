// File: PolicyClient/LoginFrame.java
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;

public class LoginFrame extends JFrame {
    private final NetworkClient networkClient = new NetworkClient();
    
    private final JTextField phoneField = new JTextField(15);
    private final JTextField otpField = new JTextField(6);
    private final JTextArea statusArea = new JTextArea(5, 25);
    private final JButton requestOtpButton = new JButton("Request OTP");
    private final JButton verifyButton = new JButton("Verify & Login");
    
    private String currentPhoneNumber = "";

    public LoginFrame() {
        super("Secure Client Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // --- UI Setup ---
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Enter Credentials (1234567890)"));
        formPanel.add(new JLabel("Phone Number:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("OTP:"));
        formPanel.add(otpField);
        formPanel.add(requestOtpButton);
        formPanel.add(verifyButton);

        statusArea.setEditable(false);
        
        // --- Assemble ---
        add(formPanel, BorderLayout.NORTH);
        add(new JScrollPane(statusArea), BorderLayout.CENTER);
        
        otpField.setEnabled(false);
        verifyButton.setEnabled(false);
        
        // --- Actions ---
        requestOtpButton.addActionListener(this::requestOtpAction);
        verifyButton.addActionListener(this::verifyOtpAction);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void requestOtpAction(ActionEvent e) {
        currentPhoneNumber = phoneField.getText().trim();
        if (currentPhoneNumber.length() != 10) { 
             statusArea.setText("Error: Phone number must be 10 digits.");
             return;
        }

        try {
            String jsonBody = String.format("{\"phoneNumber\": \"%s\"}", currentPhoneNumber);
            String fullResponse = networkClient.sendAuthRequest("/auth/request-otp", jsonBody);
            String[] response = fullResponse.split("::", 2);

            if (response[0].equals("200")) {
                 statusArea.setText("Status: OTP requested successfully. Check server console for code.");
                 otpField.setEnabled(true);
                 verifyButton.setEnabled(true);
                 requestOtpButton.setEnabled(false);
            } else {
                 statusArea.setText("Error (HTTP " + response[0] + "). Server response:\n" + response[1]);
            }
        } catch (Exception ex) { 
            statusArea.setText("Connection Error. Is the server running on port 8080?"); 
        }
    }

    private void verifyOtpAction(ActionEvent e) {
        String otp = otpField.getText().trim();
        
        try {
            String jsonBody = String.format("{\"phoneNumber\": \"%s\", \"otp\": \"%s\"}", currentPhoneNumber, otp);
            String fullResponse = networkClient.sendAuthRequest("/auth/verify-otp", jsonBody);
            String[] response = fullResponse.split("::", 2);
            
            if (response[0].equals("200")) {
                // Extract token using regex
                Matcher matcher = Pattern.compile("\"accessToken\":\"([^\"]+)\"").matcher(response[1]);
                if (matcher.find()) {
                    String token = "Bearer " + matcher.group(1); 
                    
                    // LOGIN SUCCESS: Launch Dashboard and close login window
                    // This is the CRITICAL line that transitions to the dashboard
                    new DashboardFrame(networkClient, token).setVisible(true);
                    dispose(); 
                } else {
                    statusArea.setText("Login Failed. Server did not return a token.");
                }
            } else {
                 statusArea.setText("Login Failed. Invalid OTP or error code " + response[0] + ".");
            }
        } catch (Exception ex) { 
            statusArea.setText("Verification Error. Check connection."); 
        }
    }
}