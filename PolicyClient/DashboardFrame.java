// File: PolicyClient/DashboardFrame.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DashboardFrame extends JFrame {
    private final NetworkClient networkClient;
    private final String accessToken;
    private final JTabbedPane tabbedPane;

    public DashboardFrame(NetworkClient client, String token) {
        super("Policy Management Dashboard");
        this.networkClient = client;
        this.accessToken = token;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        tabbedPane = new JTabbedPane();

        // 1. My Holdings (Shows total maturity, fetches details on open)
        tabbedPane.addTab("My Holdings & Maturity", createHoldingsPanel());
        
        // 2. Policy Lookup (Uses the separate, functional PolicyDetailsPanel.java)
        // THIS IS THE CORRECTED LINE: Uses the external class
        tabbedPane.addTab("Policy Details Lookup", new PolicyDetailsPanel(networkClient, accessToken));
        
        // 3. Rate Comparison (Placeholder for future implementation)
        tabbedPane.addTab("Rate Comparison", new RateComparisonPanel());

        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Fetch and display initial holdings data
        fetchHoldings();
    }
    
    // --- Utility Method to Get the Holdings Area ---
    private JTextArea getHoldingsTextArea() {
        // Navigates down the component tree to find the JTextArea in the first tab
        JScrollPane scroll = (JScrollPane)((JPanel)tabbedPane.getComponentAt(0)).getComponent(0);
        return (JTextArea) scroll.getViewport().getView();
    }
    
    // --- Panel Creation Methods ---
    
    private JPanel createHoldingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea holdingsArea = new JTextArea("Fetching secure holdings data...");
        holdingsArea.setEditable(false);
        panel.add(new JScrollPane(holdingsArea), BorderLayout.CENTER);
        return panel;
    }
    
    private void fetchHoldings() {
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return networkClient.sendDataRequest("/api/details", accessToken);
            }

            @Override
            protected void done() {
                JTextArea holdingsArea = getHoldingsTextArea();
                
                try {
                    String fullResponse = get();
                    String[] response = fullResponse.split("::", 2);
                    
                    if (response[0].equals("200")) {
                        String rawJson = response[1];
                        
                        String display = "Welcome back! Data last refreshed: " + java.time.LocalDateTime.now() + "\n";
                        display += "----------------------------------------------------------------\n";
                        display += "TOTAL ESTIMATED FUTURE VALUE: $450,000 (Simulated Total)\n\n";
                        display += "RAW POLICY DETAILS:\n" + rawJson;
                        
                        holdingsArea.setText(display);
                    } else {
                        holdingsArea.setText("Error fetching holdings (HTTP " + response[0] + "): " + response[1]);
                    }
                } catch (Exception e) {
                    holdingsArea.setText("Fatal Network Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    // Placeholder class for the Rate Comparison tab (Needs implementation)
    private class RateComparisonPanel extends JPanel {
        public RateComparisonPanel() { 
            setLayout(new BorderLayout());
            add(new JLabel("Compare Interest Rates (FD, RD, Mutual Funds) - Needs logic/data API", SwingConstants.CENTER));
        }
    }
    
    // NOTE: The placeholder for PolicyDetailsPanel was REMOVED to force the use of the external class.
}