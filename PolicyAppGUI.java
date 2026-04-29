// File: PolicyAppGUI.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class PolicyAppGUI {
    private JFrame frame;
    
    // Fields for My Policies Panel
    private JTextField policyNumberField;
    private JTextArea displayArea;
    
    // Fields for Comparison Panel
    private JComboBox<String> policyTypeCombo;
    private JTextField amountField;
    private JTextField yearsField;
    private JTextArea comparisonResultArea; 

    public PolicyAppGUI() {
        frame = new JFrame("Policy Details and Comparison"); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(550, 400); 
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // 1. Create the "My Policies" tab panel
        JPanel policyPanel = createPolicyLookupPanel();
        tabbedPane.addTab("My Policies", policyPanel);
        
        // 2. Create the "Rate Comparison" tab panel
        JPanel comparisonPanel = createComparisonPanel();
        tabbedPane.addTab("Rate Comparison", comparisonPanel);

        // Add the tabbed pane to the main frame
        frame.add(tabbedPane);
        
        frame.setLocationRelativeTo(null); 
        frame.setVisible(true);
    }
    
    // --- POLICY LOOKUP PANEL ---
    private JPanel createPolicyLookupPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JLabel label = new JLabel("Enter Policy Number (101, 202, or 303):");
        policyNumberField = new JTextField(15);
        JButton searchButton = new JButton("Search Details");
        
        displayArea = new JTextArea(10, 40);
        displayArea.setEditable(false);
        displayArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(displayArea);

        panel.add(label);
        panel.add(policyNumberField);
        panel.add(searchButton);
        panel.add(scrollPane);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchPolicy();
            }
        });
        
        return panel;
    }

    private void searchPolicy() {
        try {
            int policyNumber = Integer.parseInt(policyNumberField.getText().trim());
            
            Policy policy = DatabaseHandler.getPolicyDetails(policyNumber);
            
            if (policy != null) {
                displayArea.setText(policy.toString());
            } else {
                displayArea.setText(
                    "--------------------------------------------\n" +
                    "| ERROR: No policy found with number " + policyNumber + "\n" +
                    "--------------------------------------------"
                );
            }
        } catch (NumberFormatException ex) {
            displayArea.setText("Please enter a valid, whole number for the policy ID.");
        }
    }
    
    // --- RATE COMPARISON PANEL ---
    private JPanel createComparisonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        panel.add(new JLabel("Policy Type:"));
        String[] types = {"FD", "RD"}; 
        policyTypeCombo = new JComboBox<>(types);
        panel.add(policyTypeCombo);

        panel.add(new JLabel("Amount (Principal/Yearly):"));
        amountField = new JTextField("100000", 10);
        panel.add(amountField);

        panel.add(new JLabel("Duration (Years):"));
        yearsField = new JTextField("5", 5);
        panel.add(yearsField);

        JButton compareButton = new JButton("Compare Rates");
        panel.add(compareButton);

        comparisonResultArea = new JTextArea(10, 45);
        comparisonResultArea.setEditable(false);
        comparisonResultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(comparisonResultArea);
        panel.add(scrollPane);

        compareButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runComparison();
            }
        });

        return panel;
    }

    private void runComparison() {
        try {
            String type = (String) policyTypeCombo.getSelectedItem();
            double amount = Double.parseDouble(amountField.getText().trim());
            int years = Integer.parseInt(yearsField.getText().trim());

            if (amount <= 0 || years <= 0) {
                comparisonResultArea.setText("Amount and Duration must be positive numbers.");
                return;
            }

            // CORE LOGIC CALL
            List<BankRate> results = FinancialCalculator.compareRates(type, amount, years);
            
            // Format and display the results
            StringBuilder sb = new StringBuilder();
            sb.append("--- BANK RATE COMPARISON RESULTS ---\n");
            sb.append(String.format("%-15s | %-10s | %6s | %-15s\n", "Bank Name", "Policy", "Rate %", "Maturity Amount"));
            sb.append("-----------------------------------------------------------\n");

            if (results.isEmpty()) {
                 sb.append("No rates found for policy type: " + type);
            } else {
                for (int i = 0; i < results.size(); i++) {
                    BankRate rate = results.get(i);
                    String maturityStr = String.format("%,.2f", rate.getCalculatedMaturity());
                    
                    String marker = (i == 0) ? " ★" : ""; 
                    
                    sb.append(String.format("%-15s | %-10s | %6.2f | %-15s%s\n", 
                                            rate.getBankName(), 
                                            rate.getPolicyType(), 
                                            rate.getInterestRate(), 
                                            maturityStr,
                                            marker));
                }
            }
            
            comparisonResultArea.setText(sb.toString());

        } catch (NumberFormatException ex) {
            comparisonResultArea.setText("Please enter valid numerical values for Amount and Duration.");
        } catch (Exception ex) {
            comparisonResultArea.setText("An unexpected error occurred: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        // 1. Initialize Database
        DatabaseHandler.createTableAndInsertSampleData();
        
        // 2. Start the GUI
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new PolicyAppGUI();
            }
        });
    }
}