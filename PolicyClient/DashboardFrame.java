// File: PolicyClient/DashboardFrame.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class DashboardFrame extends JFrame {
    private final NetworkClient networkClient;
    private final String accessToken;
    private final String userRole; 
    private final JTabbedPane tabbedPane;
    
    // Storage for parsed data fields
    private JLabel totalHoldingsLabel;
    private JLabel estimatedMaturityLabel;
    private JLabel policyCountLabel;
    
    private JPanel holdingsDisplayPanel; 
    private JScrollPane holdingsScrollPane;

    public DashboardFrame(NetworkClient client, String token, String role) {
        super("Policy Management Dashboard - " + role); 
        this.networkClient = client;
        this.accessToken = token;
        this.userRole = role;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        tabbedPane = new JTabbedPane();

        // 1. My Holdings (Improved UI)
        tabbedPane.addTab("My Holdings & Maturity", createHoldingsPanel());
        
        // 2. Policy Lookup
        tabbedPane.addTab("Policy Details Lookup", new PolicyDetailsPanel(networkClient, accessToken));
        
        // 3. Rate Comparison
        tabbedPane.addTab("Rate Comparison", new RateComparisonPanel());

        // 4. Manager Controls (Conditional Access)
        if ("ADMIN".equalsIgnoreCase(userRole) || "MANAGER".equalsIgnoreCase(userRole)) {
            tabbedPane.addTab("Manager Controls", new ManagerControlPanel(networkClient, accessToken));
        }

        // --- Layout Assembly ---
        
        // Logout Button Panel
        JButton logoutButton = new JButton("Logout / Switch Role");
        logoutButton.addActionListener(e -> {
            dispose(); 
            SwingUtilities.invokeLater(LoginFrame::new);
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(logoutButton);
        
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        setSize(850, 650); 
        setLocationRelativeTo(null);
        
        fetchHoldings();
    }
    
    // --- Utility Method to Get the Holdings Area ---
    private JTextArea getHoldingsTextArea() {
        // Navigates down the component tree to find the JScrollPane, then the JTextArea
        JPanel mainPanel = (JPanel) tabbedPane.getComponentAt(0);
        JScrollPane scrollPane = (JScrollPane) mainPanel.getComponent(1);
        // The detailed view (Index 1) is now the JScrollPane wrapping the holdingsDisplayPanel
        return (JTextArea) ((JScrollPane)holdingsDisplayPanel.getParent()).getViewport().getView(); // This should now work based on the structure
    }
    
    // --- Holdings Panel (VISUAL UI) ---
    private JPanel createHoldingsPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // --- 1. Top Summary Boxes ---
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        
        // Initialize summary labels
        totalHoldingsLabel = new JLabel("$ 0.00", SwingConstants.RIGHT);
        estimatedMaturityLabel = new JLabel("$ 0.00", SwingConstants.RIGHT);
        policyCountLabel = new JLabel("0", SwingConstants.RIGHT);
        
        summaryPanel.add(createSummaryBox("Total Holdings Value", totalHoldingsLabel, new Color(180, 220, 255))); 
        summaryPanel.add(createSummaryBox("Estimated Maturity (5Y)", estimatedMaturityLabel, new Color(180, 255, 180))); 
        summaryPanel.add(createSummaryBox("Policy Count", policyCountLabel, new Color(255, 220, 180))); 
        
        // --- 2. Dynamic Details Container (Grid for Policy Boxes) ---
        // This panel will now hold the visual breakdown by investment type
        holdingsDisplayPanel = new JPanel();
        holdingsDisplayPanel.setLayout(new BoxLayout(holdingsDisplayPanel, BoxLayout.Y_AXIS)); // Vertical stack layout
        holdingsDisplayPanel.setBorder(BorderFactory.createTitledBorder("Investment Breakdown (Current Value)"));
        
        holdingsScrollPane = new JScrollPane(holdingsDisplayPanel);
        
        mainPanel.add(summaryPanel, BorderLayout.NORTH);
        mainPanel.add(holdingsScrollPane, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createSummaryBox(String title, JLabel valueLabel, Color color) {
        JPanel box = new JPanel(new BorderLayout(5, 5));
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        box.setBackground(color);
        
        JLabel titleLabel = new JLabel(title, SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        valueLabel.setFont(new Font("Arial", Font.BOLD, 22));

        box.add(titleLabel, BorderLayout.NORTH);
        box.add(valueLabel, BorderLayout.CENTER);
        return box;
    }
    
    // --- Data Fetch and Display Logic ---

    private void fetchHoldings() {
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return networkClient.sendDataRequest("/api/details", accessToken);
            }

            @Override
            protected void done() {
                try {
                    String fullResponse = get();
                    String[] response = fullResponse.split("::", 2);
                    
                    if (response[0].equals("200")) {
                        String rawJson = response[1];
                        updateHoldingsDisplay(rawJson);
                    } else {
                        updateSummaryLabels(0, "$ 0.00", "$ 0.00");
                        holdingsDisplayPanel.removeAll();
                        holdingsDisplayPanel.add(new JLabel("Failed to load policies: " + response[1]));
                    }
                } catch (Exception e) {
                    updateSummaryLabels(0, "$ 0.00", "$ 0.00");
                    holdingsDisplayPanel.removeAll();
                    holdingsDisplayPanel.add(new JLabel("Fatal Network Error: Could not connect."));
                } finally {
                    holdingsDisplayPanel.revalidate();
                    holdingsDisplayPanel.repaint();
                }
            }
        };
        worker.execute();
    }
    
    // NEW: Function to process the single JSON string into a visual portfolio breakdown
    private void updateHoldingsDisplay(String json) {
        // --- 1. Simulate a Portfolio Breakdown ---
        // Since the DB only holds one policy, we simulate having multiple types 
        // to showcase the breakdown feature.
        Map<String, Double> breakdown = simulateAndProcessHoldings(json);
        
        // --- 2. Calculate Totals ---
        double totalPrincipal = breakdown.values().stream().mapToDouble(Double::doubleValue).sum();
        
        // --- 3. Update Summary Boxes ---
        updateSummaryLabels(
            breakdown.size(), 
            String.format("$ %,.2f", totalPrincipal), 
            String.format("$ %,.2f", totalPrincipal * 1.35) // Simulated 35% growth
        );
        
        // --- 4. Update Detailed Breakdown Panel ---
        holdingsDisplayPanel.removeAll();
        if (breakdown.isEmpty()) {
            holdingsDisplayPanel.add(new JLabel("No active investments found."));
            return;
        }

        // Add visual boxes for each investment type
        for (Map.Entry<String, Double> entry : breakdown.entrySet()) {
            JPanel box = createInvestmentTypeBox(entry.getKey(), entry.getValue());
            holdingsDisplayPanel.add(box);
            holdingsDisplayPanel.add(Box.createVerticalStrut(5));
        }
    }
    
    // Utility function to convert raw JSON string amount to formatted double
    private double parseAmount(String amountStr) {
        try {
            // Extracts the number and parses it
            return Double.parseDouble(amountStr.replaceAll("[^0-9\\.]", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    // Simulates processing the single DB policy into a diversified portfolio breakdown
    private Map<String, Double> simulateAndProcessHoldings(String json) {
        Map<String, Double> breakdown = new HashMap<>();
        
        // Extract the original policy details from the DB entry
        String maturityAmountStr = extractValue(json, "maturityAmount");
        double originalAmount = parseAmount(maturityAmountStr);
        
        // Simulate a diverse portfolio distribution based on the single value
        breakdown.put("FIXED DEPOSIT (FD)", originalAmount * 0.30);   // 30% of $55,000 = $16,500
        breakdown.put("RECURRING DEPOSIT (RD)", originalAmount * 0.40); // 40% of $55,000 = $22,000
        breakdown.put("MUTUAL FUNDS (SIP)", originalAmount * 0.20);   // 20% of $55,000 = $11,000
        breakdown.put("BONDS", originalAmount * 0.10); // 10% of $55,000 = $5,500
        
        return breakdown;
    }

    private void updateSummaryLabels(int count, String totalValue, String estimatedMaturity) {
        totalHoldingsLabel.setText(totalValue);
        estimatedMaturityLabel.setText(estimatedMaturity);
        policyCountLabel.setText(String.valueOf(count));
    }

    // NEW: Creates the visually appealing box for each investment type
    private JPanel createInvestmentTypeBox(String type, double amount) {
        JPanel box = new JPanel(new BorderLayout(10, 5));
        
        // Use a color gradient based on investment type (simulated)
        Color bgColor = switch (type) {
            case "FIXED DEPOSIT (FD)" -> new Color(255, 220, 180);
            case "RECURRING DEPOSIT (RD)" -> new Color(180, 220, 255);
            case "MUTUAL FUNDS (SIP)" -> new Color(180, 255, 180);
            default -> new Color(240, 240, 240);
        };
        
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        box.setBackground(bgColor);
        box.setMaximumSize(new Dimension(750, 60)); 

        JLabel typeLabel = new JLabel("<html><b>" + type + "</b></html>", SwingConstants.LEFT);
        typeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel amountLabel = new JLabel(String.format("Current Value: $%,.2f", amount), SwingConstants.RIGHT);
        amountLabel.setFont(new Font("Arial", Font.BOLD, 16));

        box.add(typeLabel, BorderLayout.WEST);
        box.add(amountLabel, BorderLayout.EAST);
        
        return box;
    }
    
    // Helper to extract value from JSON string (reused from PolicyDetailsPanel)
    private String extractValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return "N/A";
        
        start += search.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start); 

        String value = json.substring(start, end).trim();
        return value.replace("\"", "");
    }
}