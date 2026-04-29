// File: Policy.java
public class Policy {
    private int policyNumber;
    private String policyType;
    private String maturityDate;
    private double maturityAmount;

    public Policy(int policyNumber, String policyType, String maturityDate, double maturityAmount) {
        this.policyNumber = policyNumber;
        this.policyType = policyType;
        this.maturityDate = maturityDate;
        this.maturityAmount = maturityAmount;
    }

    // Getters
    public int getPolicyNumber() { return policyNumber; }
    public String getPolicyType() { return policyType; }
    public String getMaturityDate() { return maturityDate; }
    public double getMaturityAmount() { return maturityAmount; }

    @Override
    public String toString() {
        return "--------------------------------------------\n" +
               "| Policy Details:\n" +
               "|-------------------------------------------\n" +
               "| Policy Number: " + policyNumber + "\n" +
               "| Policy Type: " + policyType + "\n" +
               "| Maturity Date: " + maturityDate + "\n" +
               "| Matured Amount: " + String.format("$%,.2f", maturityAmount) + "\n" +
               "--------------------------------------------";
    }
}