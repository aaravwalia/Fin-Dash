// File: PolicyClient/ClientApp.java
import javax.swing.SwingUtilities;

public class ClientApp {
    public static void main(String[] args) {
        // Starts the Login Frame
        SwingUtilities.invokeLater(LoginFrame::new); 
    }
}