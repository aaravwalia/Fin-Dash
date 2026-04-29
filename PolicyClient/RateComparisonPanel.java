// File: PolicyClient/RateComparisonPanel.java
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;

public class RateComparisonPanel extends JPanel {
    
    // --- Data Model for a Comparison Result ---
    private record ComparisonResult(
        String bankName, 
        String type, 
        double rate, 
        double maturity
    ) {}

    // --- UI Components ---
    private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{"FD", "RD", "SIP", "MUTUAL FUNDS"});
    private final JTextField amountField = new JTextField("100000", 10);
    private final JTextField yearsField = new JTextField("5", 5);
    private final JButton compareButton = new JButton("Compare");
    private final JPanel resultsContainer = new JPanel(); // Container for visual boxes
    private final JScrollPane scrollPane; 

    public RateComparisonPanel() {
        setLayout(new BorderLayout(15, 15));
        
        // --- Input Controls ---
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Investment Parameters"));
        inputPanel.add(new JLabel("Type:"));
        inputPanel.add(typeCombo);
        inputPanel.add(new JLabel("Amount (Principal/Yearly):"));
        inputPanel.add(amountField);
        inputPanel.add(new JLabel("Duration (Years):"));
        inputPanel.add(yearsField);
        inputPanel.add(compareButton);

        // --- Results Container Setup ---
        resultsContainer.setLayout(new BoxLayout(resultsContainer, BoxLayout.Y_AXIS)); // Vertical stack layout
        scrollPane = new JScrollPane(resultsContainer);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Comparison Results (Best Match Highlighted)"));
        
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        compareButton.addActionListener(this::compareRatesAction);
        
        // Initial text
        resultsContainer.add(new JLabel("Enter parameters above and click 'Compare'...", SwingConstants.CENTER));
    }
    
    // --- Action and Logic ---
    
    private void compareRatesAction(ActionEvent e) {
        try {
            String type = (String) typeCombo.getSelectedItem();
            double amount = Double.parseDouble(amountField.getText().trim());
            int years = Integer.parseInt(yearsField.getText().trim());
            
            if (amount <= 0 || years <= 0) throw new IllegalArgumentException("Amount and years must be positive.");
            
            // Clear calculation status
            resultsContainer.removeAll(); 
            resultsContainer.add(new JLabel("Calculating comparison for " + type + "...", SwingConstants.CENTER));

            // Fetch/simulate rates and calculate maturity
            List<ComparisonResult> results = calculateComparison(type, amount, years);
            
            // Display sorted results
            displayResults(results);

        } catch (NumberFormatException ex) {
            resultsContainer.removeAll();
            resultsContainer.add(new JLabel("Error: Please enter valid numbers for Amount and Years.", SwingConstants.CENTER));
        } catch (IllegalArgumentException ex) {
            resultsContainer.removeAll();
            resultsContainer.add(new JLabel("Error: " + ex.getMessage(), SwingConstants.CENTER));
        } catch (Exception ex) {
            resultsContainer.removeAll();
            resultsContainer.add(new JLabel("An unexpected error occurred: " + ex.getMessage(), SwingConstants.CENTER));
        } finally {
            resultsContainer.revalidate();
            resultsContainer.repaint();
        }
    }
    
    // --- Simulation and Calculation ---

    private List<ComparisonResult> calculateComparison(String type, double amount, int years) {
        // --- SIMULATED BANK RATES AND RETURNS ---
        List<ComparisonResult> simulatedRates = new ArrayList<>();
        
        // Simulating different bank rates for comparison (Now includes extra data for diversity)
        simulatedRates.add(new ComparisonResult("Bank MegaCorp", type, 6.0, 0));
        simulatedRates.add(new ComparisonResult("Local Credit", type, 7.5, 0));
        simulatedRates.add(new ComparisonResult("FinTech Invest", type, 12.0, 0)); 
        simulatedRates.add(new ComparisonResult("Govt. Bonds", type, 5.0, 0)); 
        simulatedRates.add(new ComparisonResult("Union Trust Bank", type, 7.8, 0)); // Added new data
        simulatedRates.add(new ComparisonResult("Growth Fund X", type, 15.5, 0)); // Added new data
        simulatedRates.add(new ComparisonResult("Standard Market", type, 2.5, 0)); // Added new data
        
        List<ComparisonResult> resultsWithMaturity = new ArrayList<>();

        for (ComparisonResult result : simulatedRates) {
            double rate = result.rate();
            double maturity;
            
            // Simplified maturity calculation based on investment type
            if (type.equals("SIP") || type.equals("MUTUAL FUNDS")) {
                 // High growth projection
                 maturity = calculateFVAnnuity(amount * 12, rate, years); 
            } else {
                 // Simple compounding for FD/RD/Bonds
                 maturity = calculateFVSimple(amount, rate, years);
            }
            
            // Create a new result with the calculated maturity
            resultsWithMaturity.add(new ComparisonResult(result.bankName(), result.type(), rate, maturity));
        }

        // Sort by maturity amount (highest first)
        Collections.sort(resultsWithMaturity, Comparator.comparing(ComparisonResult::maturity).reversed());
        return resultsWithMaturity;
    }

    // Future Value (FV) Simple Compounding (for FD/RD)
    private double calculateFVSimple(double principal, double rate, int years) {
        double rateDecimal = rate / 100.0;
        return principal * Math.pow((1 + rateDecimal), years);
    }

    // Future Value (FV) Annuity/SIP Compounding (Highly simplified)
    private double calculateFVAnnuity(double periodicPayment, double rate, int years) {
        double totalContribution = periodicPayment * years;
        double rateDecimal = rate / 100.0;
        
        // Simple return projection based on total principal + approximated interest
        return totalContribution * (1 + rateDecimal * years * 0.65); 
    }
    
    // --- Display ---
    
    private void displayResults(List<ComparisonResult> results) {
        resultsContainer.removeAll(); // Clear previous results
        
        if (results.isEmpty()) {
            resultsContainer.add(new JLabel("No matching rates found."));
        }

        for (int i = 0; i < results.size(); i++) {
            ComparisonResult r = results.get(i);
            boolean isBest = (i == 0);
            
            // Call the box creation method
            JPanel box = createResultBox(r, isBest);
            resultsContainer.add(box);
            resultsContainer.add(Box.createVerticalStrut(10)); // Add spacing
        }
    }
    
    // NEW METHOD: Creates a visually appealing box for each result
    private JPanel createResultBox(ComparisonResult r, boolean isBest) {
        JPanel box = new JPanel(new GridLayout(1, 3, 10, 0));
        Color color = isBest ? new Color(200, 255, 200) : new Color(230, 240, 255); // Highlight Best Match
        
        box.setBackground(color);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isBest ? Color.BLUE.darker() : Color.GRAY, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // --- Left: Bank Name ---
        JLabel bankLabel = new JLabel("<html><b>" + r.bankName() + "</b><br><small>(" + r.type() + ")</small></html>");
        bankLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // --- Center: Rate ---
        // FIX: Cleaned up HTML styling for green color display
        JLabel rateLabel = new JLabel(String.format(
            "<html>Rate: <b style='color: %s'>%.2f%%</b></html>", 
            isBest ? "green" : "black", r.rate()), SwingConstants.CENTER);
        rateLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        // --- Right: Maturity ---
        JLabel maturityLabel = new JLabel(String.format("<html>Est. Maturity:<br><b>$%,.2f</b></html>", r.maturity()), SwingConstants.RIGHT);
        maturityLabel.setForeground(isBest ? Color.BLUE.darker() : Color.BLACK);
        maturityLabel.setFont(new Font("Arial", Font.BOLD, 16));

        box.add(bankLabel);
        box.add(rateLabel);
        box.add(maturityLabel);
        
        return box;
    }
}