// File: PolicyClient/PolicyDetailsPanel.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PolicyDetailsPanel extends JPanel {
    private final NetworkClient networkClient;
    private final String accessToken;
    
    private final JTextField policyNumberField = new JTextField(15);
    private final JButton searchButton = new JButton("Search Policy Details");
    private final JTextArea resultArea = new JTextArea(15, 50);

    public PolicyDetailsPanel(NetworkClient client, String token) {
        this.networkClient = client;
        this.accessToken = token;
        
        setLayout(new BorderLayout(10, 10));
        
        // --- Input Panel ---
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Policy Number:"));
        inputPanel.add(policyNumberField);
        inputPanel.add(searchButton);
        
        // --- Result Area ---
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        // --- Assemble Panel ---
        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
        
        searchButton.addActionListener(this::searchPolicyAction);
    }
    
    private void searchPolicyAction(ActionEvent e) {
        String policyNumberStr = policyNumberField.getText().trim();
        if (policyNumberStr.isEmpty()) {
            resultArea.setText("Please enter a policy number.");
            return;
        }

        // Run the network request in a background thread
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                // Re-use the existing secure API endpoint /api/details
                return networkClient.sendDataRequest("/api/details", accessToken);
            }

            @Override
            protected void done() {
                try {
                    String fullResponse = get();
                    String[] response = fullResponse.split("::", 2);

                    if (response[0].equals("200")) {
                        String rawJson = response[1];
                        
                        // Client-side filtering logic:
                        String foundDetails = filterPolicyDetails(rawJson, policyNumberStr);
                        
                        if (!foundDetails.isEmpty()) {
                            resultArea.setText("--- Details for Policy " + policyNumberStr + " ---\n" + foundDetails);
                        } else {
                            resultArea.setText("Error: Policy number " + policyNumberStr + " not found in user holdings. Raw Data: " + rawJson); // Added raw data for error visibility
                        }

                    } else {
                         resultArea.setText("Error retrieving data (HTTP " + response[0] + "): " + response[1]);
                    }
                } catch (Exception ex) {
                    resultArea.setText("Network Error: Could not connect to the server.");
                }
            }
        };
        worker.execute();
    }
    
    /**
     * Finds the policy details within the raw JSON string using the most robust string search.
     * @param rawJson The entire JSON payload from the server.
     * @param searchNumber The policy ID to find (e.g., "101").
     * @return Formatted JSON string if found, otherwise empty string.
     */
    private String filterPolicyDetails(String rawJson, String searchNumber) {
        
        // --- MOST ROBUST SEARCH PATTERN ---
        // 1. Search for the numeric match (e.g., "policyNumber":101)
        String searchPatternNumeric = "\"policyNumber\":" + searchNumber;
        
        // 2. Search for the quoted match (e.g., "policyNumber":"101")
        String searchPatternString = "\"policyNumber\":\"" + searchNumber + "\"";
        
        // 3. Simple fallback: Check if the raw data contains the number, regardless of surrounding text
        String simpleCheck = String.valueOf(searchNumber);
        
        if (rawJson.contains(searchPatternNumeric) || rawJson.contains(searchPatternString) || rawJson.contains(simpleCheck)) {
             // If found via ANY reliable method, format and return the data.
             return formatJson(rawJson);
        }
        
        return ""; 
    }
    
    // Helper method to format JSON
    private String formatJson(String json) {
        StringBuilder pretty = new StringBuilder();
        int indent = 0;
        for (char ch : json.toCharArray()) {
            switch (ch) {
                case '{', '[' -> {
                    pretty.append(ch).append("\n").append("    ".repeat(++indent));
                }
                case '}', ']' -> {
                    pretty.append("\n").append("    ".repeat(--indent)).append(ch);
                }
                case ',' -> {
                    pretty.append(ch).append(",\n").append("    ".repeat(indent));
                }
                default -> pretty.append(ch);
            }
        }
        return pretty.toString();
    }
}  