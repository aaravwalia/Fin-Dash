// File: BankRate.java
public class BankRate {
    private String bankName;
    private String policyType;
    private double interestRate;
    private double calculatedMaturity; // Added to store calculated projection

    public BankRate(String bankName, String policyType, double interestRate) {
        this.bankName = bankName;
        this.policyType = policyType;
        this.interestRate = interestRate;
    }

    // Getters
    public String getBankName() { return bankName; }
    public String getPolicyType() { return policyType; }
    public double getInterestRate() { return interestRate; }
    public double getCalculatedMaturity() { return calculatedMaturity; }

    // Setter for the projected amount (used by the calculator logic)
    public void setCalculatedMaturity(double amount) { this.calculatedMaturity = amount; }
    
    @Override
    public String toString() {
        return String.format("%-15s | %-10s | %6.2f%%", 
                             bankName, policyType, interestRate);
    }
}