// File: PolicyClient/PolicyDetailsPanel.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PolicyDetailsPanel extends JPanel {
    private final NetworkClient networkClient;
    private final String accessToken;
    
    private final JTextField policyNumberField = new JTextField(15);
    private final JButton searchButton = new JButton("Search Policy Details");
    private final JPanel resultContainer = new JPanel(); // Panel to hold the visually styled boxes
    private final JScrollPane scrollPane;
    private final JLabel statusLabel = new JLabel("Enter policy number and press Search.", SwingConstants.CENTER);

    public PolicyDetailsPanel(NetworkClient client, String token) {
        this.networkClient = client;
        this.accessToken = token;
        
        setLayout(new BorderLayout(10, 10));
        
        // --- Input Panel ---
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Policy Number:"));
        inputPanel.add(policyNumberField);
        inputPanel.add(searchButton);
        
        // --- Results Container Setup (The visual output) ---
        resultContainer.setLayout(new BoxLayout(resultContainer, BoxLayout.Y_AXIS)); // Stack panels vertically
        resultContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        scrollPane = new JScrollPane(resultContainer);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Policy Details (Visual View)"));
        
        // --- Assemble Panel ---
        add(inputPanel, BorderLayout.NORTH);
        add(statusLabel, BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);
        
        searchButton.addActionListener(this::searchPolicyAction);
        
        // Initialize the view
        showInitialState();
    }
    
    private void showInitialState() {
        resultContainer.removeAll();
        resultContainer.add(new JLabel("Awaiting search input...", SwingConstants.CENTER));
        resultContainer.revalidate();
        resultContainer.repaint();
    }
    
    private void searchPolicyAction(ActionEvent e) {
        String policyNumberStr = policyNumberField.getText().trim();
        if (policyNumberStr.isEmpty()) {
            statusLabel.setText("Please enter a policy number.");
            return;
        }

        statusLabel.setText("Searching for policy " + policyNumberStr + "...");
        
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
                        
                        // Parse and display the data visually
                        displayPolicyDetails(rawJson, policyNumberStr);
                        
                    } else {
                         statusLabel.setText("Error retrieving data (HTTP " + response[0] + "): " + response[1]);
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Network Error: Could not connect to the server.");
                }
            }
        };
        worker.execute();
    }
    
    // --- NEW METHOD: Parses JSON and builds the visual boxes ---
    private void displayPolicyDetails(String rawJson, String searchNumber) {
        resultContainer.removeAll();
        
        String cleanedJson = rawJson.replaceAll("\\s", "");
        String searchPatternNumeric = "\"policyNumber\":" + searchNumber;
        
        if (cleanedJson.contains(searchPatternNumeric)) 
        {
             // If found, format and display the data visually.
             statusLabel.setText("Policy data found and displayed successfully.");

             // Simulated Extraction based on the known structure:
             String type = extractValue(rawJson, "policyType");
             String maturityDate = extractValue(rawJson, "maturityDate");
             String maturityAmount = extractValue(rawJson, "maturityAmount");
             
             // Add a title card
             resultContainer.add(createTitleCard("Policy ID: " + searchNumber + " (" + type + ")"));
             resultContainer.add(Box.createVerticalStrut(10));
             
             // Add visual detail boxes
             resultContainer.add(createDetailBox("Policy Type", type, new Color(200, 220, 255))); // Light Blue
             resultContainer.add(createDetailBox("Maturity Date", maturityDate, new Color(255, 240, 180))); // Light Yellow
             resultContainer.add(createDetailBox("Matured Amount", "$" + formatAmount(maturityAmount), new Color(180, 255, 180))); // Light Green
             
        } else {
            statusLabel.setText("Error: Policy number " + searchNumber + " not found.");
            // Added raw data output here for final debugging visibility if needed
            resultContainer.add(new JTextArea("No policy details available for this ID.\nRaw Server Response: " + rawJson));
        }

        resultContainer.revalidate();
        resultContainer.repaint();
    }
    
    // --- UI/Formatting Helper Methods ---
    
    private JPanel createTitleCard(String title) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(Color.DARK_GRAY);
        panel.setMaximumSize(new Dimension(800, 40));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titleLabel);
        return panel;
    }
    
    private JPanel createDetailBox(String title, String value, Color bgColor) {
        JPanel box = new JPanel(new BorderLayout(10, 5));
        box.setMaximumSize(new Dimension(800, 60)); 
        box.setBackground(bgColor);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker().darker(), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        
        JLabel valueLabel = new JLabel("<html><b>" + value + "</b></html>", SwingConstants.RIGHT);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        box.add(titleLabel, BorderLayout.WEST);
        box.add(valueLabel, BorderLayout.EAST);
        
        return box;
    }
    
    // Simple utility to extract value from JSON string (based on known structure)
    private String extractValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return "N/A";
        
        start += search.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start); // Handles last element

        String value = json.substring(start, end).trim();
        // Remove quotes if present (e.g., "FD" -> FD)
        return value.replace("\"", "");
    }
    
    // Simple utility to format currency amount
    private String formatAmount(String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            return String.format("%,.2f", amount);
        } catch (NumberFormatException e) {
            return amountStr;
        }
    }
    
    // Helper method to format JSON (retained from old code structure, not used in final display)
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