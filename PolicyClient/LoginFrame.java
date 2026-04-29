// File: PolicyClient/LoginFrame.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginFrame extends JFrame {
    private final NetworkClient networkClient = new NetworkClient();
    
    private final JTextField phoneField = new JTextField(15);
    private final JTextField otpField = new JTextField(6);
    private final JTextArea statusArea = new JTextArea(5, 25);
    private final JButton requestOtpButton = new JButton("Request OTP");
    private final JButton verifyButton = new JButton("Verify & Login");
    
    // NEW: Role Selection Components
    private final JRadioButton userRadio = new JRadioButton("User", true);
    private final JRadioButton adminRadio = new JRadioButton("Manager");
    
    private String currentPhoneNumber = "";
    private String selectedRole = "USER"; // Default

    public LoginFrame() {
        super("Secure Client Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // --- Role Selection Panel ---
        JPanel rolePanel = new JPanel(new FlowLayout());
        rolePanel.setBorder(BorderFactory.createTitledBorder("Select Role"));
        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(userRadio);
        roleGroup.add(adminRadio);
        rolePanel.add(userRadio);
        rolePanel.add(adminRadio);
        
        userRadio.addActionListener(e -> selectedRole = "USER");
        adminRadio.addActionListener(e -> selectedRole = "ADMIN");
        
        // --- Authentication Panel ---
        JPanel authPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        authPanel.setBorder(BorderFactory.createTitledBorder("Enter Credentials (1234567890)"));
        authPanel.add(new JLabel("Phone Number:"));
        authPanel.add(phoneField);
        authPanel.add(new JLabel("OTP:"));
        authPanel.add(otpField);
        authPanel.add(requestOtpButton);
        authPanel.add(verifyButton);

        statusArea.setEditable(false);
        
        // --- Assemble ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(rolePanel, BorderLayout.NORTH);
        topPanel.add(authPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
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
        try {
            // Include role in the request body (Server can use this for future validation)
            String jsonBody = String.format("{\"phoneNumber\": \"%s\", \"role\":\"%s\"}", currentPhoneNumber, selectedRole);
            String fullResponse = networkClient.sendAuthRequest("/auth/request-otp", jsonBody);
            String[] response = fullResponse.split("::", 2);

            if (response[0].equals("200")) {
                 statusArea.setText("Status: OTP requested successfully. Check server console for code.");
                 otpField.setEnabled(true);
                 verifyButton.setEnabled(true);
                 requestOtpButton.setEnabled(false);
            } else {
                 // The server should ideally send back a specific error if the account is frozen here
                 statusArea.setText("Error (HTTP " + response[0] + "): " + response[1]);
            }
        } catch (Exception ex) { statusArea.setText("Connection Error. Is the server running on port 8080?"); }
    }

    private void verifyOtpAction(ActionEvent e) {
        String otp = otpField.getText().trim();
        
        try {
            // Include role in verification
            String jsonBody = String.format("{\"phoneNumber\": \"%s\", \"otp\": \"%s\", \"role\":\"%s\"}", currentPhoneNumber, otp, selectedRole);
            String fullResponse = networkClient.sendAuthRequest("/auth/verify-otp", jsonBody);
            String[] response = fullResponse.split("::", 2);
            
            if (response[0].equals("200")) {
                // Extract token
                Matcher matcher = Pattern.compile("\"accessToken\":\"([^\"]+)\"").matcher(response[1]);
                if (matcher.find()) {
                    String token = "Bearer " + matcher.group(1); 
                    
                    // LOGIN SUCCESS: Launch Dashboard, passing the validated role
                    new DashboardFrame(networkClient, token, selectedRole).setVisible(true);
                    dispose(); 
                }
            } else if (response[1].contains("FROZEN")) {
                statusArea.setText("LOGIN FAILED: This account is currently FROZEN.");
            } else {
                 statusArea.setText("Login Failed. Invalid OTP or error code " + response[0] + ".");
            }
        } catch (Exception ex) { statusArea.setText("Verification Error. Check connection."); }
    }
}