// File: DatabaseHandler.java
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:policies.db";

    private static final String CREATE_POLICIES_SQL = "CREATE TABLE IF NOT EXISTS policies (\n"
                                                      + " policy_number INTEGER PRIMARY KEY,\n"
                                                      + " policy_type TEXT NOT NULL,\n"
                                                      + " maturity_date TEXT NOT NULL,\n"
                                                      + " maturity_amount REAL NOT NULL\n"
                                                      + ");";
    
    private static final String CREATE_RATES_SQL = "CREATE TABLE IF NOT EXISTS bank_rates (\n"
                                                   + " bank_name TEXT NOT NULL,\n"
                                                   + " policy_type TEXT NOT NULL,\n"
                                                   + " interest_rate REAL NOT NULL,\n"
                                                   + " PRIMARY KEY (bank_name, policy_type)\n"
                                                   + ");";

    private static Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC"); 
        } catch (ClassNotFoundException e) {
            System.err.println("Error: SQLite JDBC Driver not found. Ensure 'sqlite.jar' is in your classpath.");
            throw new SQLException("JDBC Driver not available.", e); 
        }
        return DriverManager.getConnection(DB_URL);
    }

    public static void createTableAndInsertSampleData() {
        
        // 1. CREATE TABLES (Policies and Bank Rates)
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(CREATE_POLICIES_SQL);
            stmt.execute(CREATE_RATES_SQL); // Create the new table
            System.out.println("All tables created/verified successfully.");

        } catch (SQLException e) {
            System.err.println("Database Initialization Error (CREATE TABLES): " + e.getMessage());
            return; 
        }

        // 2. INSERT SAMPLE POLICY DATA
        String insertPolicySql = "INSERT OR IGNORE INTO policies(policy_number, policy_type, maturity_date, maturity_amount) VALUES(?,?,?,?)";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(insertPolicySql)) {

            Policy[] samples = {
                new Policy(101, "FD", "2027-03-15", 55000.00),
                new Policy(202, "RD", "2028-09-22", 125000.50),
                new Policy(303, "SIP", "2030-01-01", 350000.75)
            };
            
            for (Policy p : samples) {
                pstmt.setInt(1, p.getPolicyNumber());
                pstmt.setString(2, p.getPolicyType());
                pstmt.setString(3, p.getMaturityDate());
                pstmt.setDouble(4, p.getMaturityAmount());
                pstmt.executeUpdate();
            }
            System.out.println("Sample policy data inserted/verified.");

        } catch (SQLException e) {
            System.err.println("Database Initialization Error (INSERT POLICIES): " + e.getMessage());
        }
        
        // 3. INSERT SAMPLE BANK RATE DATA
        String insertRateSql = "INSERT OR IGNORE INTO bank_rates(bank_name, policy_type, interest_rate) VALUES(?,?,?)";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(insertRateSql)) {

            Object[][] rates = {
                {"Bank Alpha", "FD", 6.50},
                {"Bank Beta", "FD", 7.10},
                {"Bank Gamma", "RD", 6.80},
                {"Bank Delta", "RD", 7.25}
            };
            
            for (Object[] rate : rates) {
                pstmt.setString(1, (String) rate[0]);
                pstmt.setString(2, (String) rate[1]);
                pstmt.setDouble(3, (Double) rate[2]);
                pstmt.executeUpdate();
            }
            System.out.println("Sample bank rate data inserted/verified.");

        } catch (SQLException e) {
            System.err.println("Database Initialization Error (INSERT RATES): " + e.getMessage());
        }
    }

    public static Policy getPolicyDetails(int policyNumber) {
        String sql = "SELECT * FROM policies WHERE policy_number = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, policyNumber);
            ResultSet rs  = pstmt.executeQuery();

            if (rs.next()) {
                return new Policy(
                    rs.getInt("policy_number"),
                    rs.getString("policy_type"),
                    rs.getString("maturity_date"),
                    rs.getDouble("maturity_amount")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving policy: " + e.getMessage());
        }
        return null; 
    }
    
    // NEW METHOD: Retrieves all rates for a given policy type (FD, RD, etc.)
    public static List<BankRate> getAllRatesByType(String type) {
        List<BankRate> rates = new ArrayList<>();
        String sql = "SELECT * FROM bank_rates WHERE policy_type = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, type);
            ResultSet rs  = pstmt.executeQuery();

            while (rs.next()) {
                rates.add(new BankRate(
                    rs.getString("bank_name"),
                    rs.getString("policy_type"),
                    rs.getDouble("interest_rate")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving bank rates: " + e.getMessage());
        }
        return rates; 
    }
}