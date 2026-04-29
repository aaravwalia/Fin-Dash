// File: PolicyClient/ManagerControlPanel.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ManagerControlPanel extends JPanel {
    private final NetworkClient networkClient;
    private final String accessToken;
    
    // UI Components
    private final JTextField targetPhoneField = new JTextField(15);
    private final JButton freezeButton = new JButton("FREEZE Account");
    private final JButton activateButton = new JButton("ACTIVATE Account");
    private final JTextArea statusArea = new JTextArea(8, 50);

    // BASE_URL is now accessible via NetworkClient.BASE_URL

    public ManagerControlPanel(NetworkClient client, String token) {
        this.networkClient = client;
        this.accessToken = token;
        
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // --- Input and Controls ---
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Manage User Status"));
        
        controlPanel.add(new JLabel("Target Phone (e.g., 1234567890):"));
        controlPanel.add(targetPhoneField);
        
        // Style buttons for clarity and security
        freezeButton.setBackground(new Color(220, 100, 100)); 
        freezeButton.setForeground(Color.WHITE);
        activateButton.setBackground(new Color(100, 180, 100)); 
        activateButton.setForeground(Color.WHITE);
        
        controlPanel.add(freezeButton);
        controlPanel.add(activateButton);
        
        // --- Status Display ---
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // --- Assemble ---
        add(controlPanel, BorderLayout.NORTH);
        add(new JScrollPane(statusArea), BorderLayout.CENTER);
        
        // Action Listeners: Call setStatusAction with the desired status string
        freezeButton.addActionListener(e -> setStatusAction("FROZEN"));
        activateButton.addActionListener(e -> setStatusAction("ACTIVE"));
        
        statusArea.setText("MANAGER CONTROLS ACTIVE. Use '1234567890' to test actions.");
    }

    /**
     * Handles the network request to update the target user's account status.
     * Uses direct HttpClient access for robust POST with headers and body.
     */
    private void setStatusAction(String status) {
        String targetPhone = targetPhoneField.getText().trim();
        if (targetPhone.isEmpty()) {
            statusArea.setText("Error: Enter a target phone number to " + status + ".");
            return;
        }

        // Runs network request in a background thread
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                // 1. CONSTRUCT JSON BODY (MUST MATCH SERVER RECORD)
                String jsonBody = String.format(
                    "{\"targetPhone\":\"%s\", \"newStatus\":\"%s\"}", 
                    targetPhone, 
                    status 
                );
                
                String endpoint = "/api/admin/status";
                
                // 2. SEND DIRECT HTTP REQUEST (using the public networkClient.client)
                HttpRequest request = HttpRequest.newBuilder()
                        // Use the public constant BASE_URL
                        .uri(URI.create(NetworkClient.BASE_URL + endpoint))
                        .header("Authorization", accessToken)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                // Uses the public client instance
                HttpResponse<String> response = networkClient.client.send(request, HttpResponse.BodyHandlers.ofString());
                
                // Return status code and body separated by '::'
                return response.statusCode() + "::" + response.body();
            }

            @Override
            protected void done() {
                try {
                    String fullResponse = get();
                    String[] response = fullResponse.split("::", 2);
                    String status = response[0];
                    String body = response[1];

                    if (status.equals("200")) {
                        statusArea.setText("SUCCESS (HTTP 200):\nAccount status for " + targetPhone + " updated to " + status.toUpperCase() + ".\n" + body);
                    } else if (status.equals("403") || status.equals("401")) {
                         statusArea.setText("FAILURE (HTTP " + status + " Forbidden/Unauthorized):\nPermission or target account error.\n" + body);
                    } else {
                        // Catches 400 Bad Request
                        statusArea.setText("FAILURE (HTTP " + status + "): Server rejected request.\n" + body);
                    }
                } catch (Exception ex) {
                    statusArea.setText("Fatal Network Error: Could not reach the server.");
                }
            }
        };
        worker.execute();
    }
}