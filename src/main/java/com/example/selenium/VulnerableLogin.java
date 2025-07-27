package com.example.selenium;

import java.sql.*;
import java.security.MessageDigest;
import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;
import java.io.File;
import java.io.FileInputStream;

/**
 * Intentionally vulnerable code for Amplify SAST testing
 * Contains multiple security vulnerabilities that should be detected
 */
public class VulnerableLogin {
    
    private static final Logger logger = Logger.getLogger(VulnerableLogin.class.getName());
    
    // 🔥 HARDCODED CREDENTIALS (CWE-798)
    private static final String DB_PASSWORD = "admin123";
    private static final String API_SECRET = "sk-1234567890abcdef";
    private static final String JWT_SECRET = "mySecretKey123";
    
    // 🔥 SQL INJECTION VULNERABILITY (CWE-89)
    public boolean authenticateUser(String username, String password) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // Hardcoded database credentials
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/userdb", 
                "root", 
                DB_PASSWORD
            );
            
            stmt = conn.createStatement();
            
            // VULNERABLE: Direct string concatenation allows SQL injection
            String query = "SELECT * FROM users WHERE username = '" + username + 
                          "' AND password = '" + password + "'";
            
            ResultSet rs = stmt.executeQuery(query);
            return rs.next();
            
        } catch (SQLException e) {
            // 🔥 INFORMATION DISCLOSURE (CWE-200)
            logger.severe("SQL Error: " + e.getMessage() + " with credentials: " + 
                         username + "/" + password);
            return false;
        }
    }
    
    // 🔥 WEAK CRYPTOGRAPHY (CWE-327)
    public String hashPassword(String password) {
        try {
            // Using weak MD5 algorithm
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(password.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return password; // Fallback returns plaintext!
        }
    }
    
    // 🔥 PATH TRAVERSAL VULNERABILITY (CWE-22)
    public String readUserFile(HttpServletRequest request) {
        String filename = request.getParameter("file");
        
        try {
            // No input validation - allows directory traversal
            File file = new File("/app/userfiles/" + filename);
            FileInputStream fis = new FileInputStream(file);
            
            byte[] data = new byte[fis.available()];
            fis.read(data);
            fis.close();
            
            return new String(data);
        } catch (Exception e) {
            // Information disclosure
            return "Error reading file: " + filename + " - " + e.getMessage();
        }
    }
    
    // 🔥 COMMAND INJECTION (CWE-78)
    public String executeSystemCommand(String userInput) {
        try {
            // Direct execution without sanitization
            Process process = Runtime.getRuntime().exec("grep -r " + userInput + " /logs/");
            
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream())
            );
            
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            return output.toString();
        } catch (Exception e) {
            return "Command failed: " + userInput;
        }
    }
    
    // 🔥 INSECURE RANDOM (CWE-330)
    public String generateToken() {
        // Using predictable random
        java.util.Random random = new java.util.Random(System.currentTimeMillis());
        return "token_" + random.nextInt(10000);
    }
    
    // 🔥 LDAP INJECTION (CWE-90)
    public boolean validateLdapUser(String username) {
        try {
            javax.naming.directory.InitialDirContext ctx = new javax.naming.directory.InitialDirContext();
            
            // Vulnerable LDAP query construction
            String filter = "(&(objectClass=user)(sAMAccountName=" + username + "))";
            
            javax.naming.directory.SearchControls controls = new javax.naming.directory.SearchControls();
            ctx.search("DC=company,DC=com", filter, controls);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
