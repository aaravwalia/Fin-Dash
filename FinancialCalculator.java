// File: FinancialCalculator.java
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FinancialCalculator {

    // Constant for annual compounding (used for simple FD/RD calculation)
    private static final int COMPOUNDS_PER_YEAR = 1; 

    /**
     * Calculates the projected maturity amount for an investment.
     * This uses the simple compounding formula (A = P * (1 + r/n)^(nt)).
     * It's simplified for demonstration; real SIP/RD calculations are more complex.
     * * @param principal The initial amount (or total yearly contribution for simple cases).
     * @param annualRate The annual interest rate (e.g., 6.5 for 6.5%).
     * @param years The investment duration in years.
     * @return The final projected maturity amount.
     */
    private static double calculateCompoundMaturity(double principal, double annualRate, int years) {
        double rateDecimal = annualRate / 100.0;
        double futureValue = principal * Math.pow((1 + rateDecimal / COMPOUNDS_PER_YEAR), (COMPOUNDS_PER_YEAR * years));
        return futureValue;
    }

    /**
     * Retrieves bank rates, calculates the maturity amount for each, and sorts them.
     * * @param policyType The type of policy (e.g., "FD", "RD").
     * @param principalAmount The investment amount (e.g., FD amount or total yearly RD contribution).
     * @param durationYears The duration in years.
     * @return A sorted list of BankRate objects with maturity amounts calculated.
     */
    public static List<BankRate> compareRates(String policyType, double principalAmount, int durationYears) {
        
        // 1. Get the list of rates from the database (DatabaseHandler is called here)
        List<BankRate> results = DatabaseHandler.getAllRatesByType(policyType);
        
        // 2. Calculate the maturity for each bank
        for (BankRate rate : results) {
            double maturity = calculateCompoundMaturity(principalAmount, rate.getInterestRate(), durationYears);
            rate.setCalculatedMaturity(maturity);
        }
        
        // 3. Sort the results from HIGHEST maturity amount to LOWEST
        Collections.sort(results, new Comparator<BankRate>() {
            @Override
            public int compare(BankRate r1, BankRate r2) {
                // We want descending order, so we compare r2 to r1
                return Double.compare(r2.getCalculatedMaturity(), r1.getCalculatedMaturity());
            }
        });
        
        return results;
    }
}