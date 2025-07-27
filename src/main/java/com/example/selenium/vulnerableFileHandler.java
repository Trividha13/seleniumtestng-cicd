package com.example.selenium;

import javax.xml.parsers.*;
import org.w3c.dom.Document;
import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * More vulnerable code patterns for comprehensive SAST testing
 */
public class VulnerableFileHandler {
    
    // 🔥 XXE (XML External Entity) VULNERABILITY (CWE-611)
    public Document parseXMLFile(String xmlContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            
            // VULNERABLE: External entities enabled by default
            // Should disable external entities for security
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            ByteArrayInputStream input = new ByteArrayInputStream(xmlContent.getBytes());
            return builder.parse(input);
            
        } catch (Exception e) {
            System.err.println("XML parsing error: " + e.getMessage());
            return null;
        }
    }
    
    // 🔥 ZIP SLIP VULNERABILITY (CWE-22)
    public void extractZipFile(String zipFilePath, String destDir) {
        try {
            FileInputStream fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry entry;
            
            while ((entry = zis.getNextEntry()) != null) {
                // VULNERABLE: No path validation
                File destFile = new File(destDir, entry.getName());
                
                FileOutputStream fos = new FileOutputStream(destFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fos.close();
            }
            zis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 🔥 SSRF (Server-Side Request Forgery) (CWE-918)
    public String fetchExternalContent(String userUrl) {
        try {
            // VULNERABLE: No URL validation
            URL url = new URL(userUrl);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(url.openStream())
            );
            
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            
            return content.toString();
        } catch (Exception e) {
            return "Error fetching: " + userUrl;
        }
    }
    
    // 🔥 OPEN REDIRECT (CWE-601)
    public void redirectUser(HttpServletRequest request, HttpServletResponse response) {
        try {
            String redirectUrl = request.getParameter("redirect");
            
            // VULNERABLE: No URL validation
            response.sendRedirect(redirectUrl);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 🔥 DESERIALIZATION VULNERABILITY (CWE-502)
    public Object deserializeUserData(byte[] serializedData) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(serializedData);
            ObjectInputStream ois = new ObjectInputStream(bis);
            
            // VULNERABLE: Deserializing untrusted data
            Object obj = ois.readObject();
            ois.close();
            
            return obj;
        } catch (Exception e) {
            return null;
        }
    }
    
    // 🔥 REGEX DENIAL OF SERVICE (ReDoS) (CWE-1333)
    public boolean validateEmail(String email) {
        // VULNERABLE: Catastrophic backtracking regex
        String regex = "^([a-zA-Z0-9])(([\\-.]|[_]+)?([a-zA-Z0-9]+))*(@){1}[a-z0-9]+[.]{1}(([a-z]{2,3})|([a-z]{2,3}[.]{1}[a-z]{2,3}))$";
        return email.matches(regex);
    }
    
    // 🔥 TAINTED DATA FLOW
    public void processUserInput(HttpServletRequest request) {
        String userInput = request.getParameter("data");
        
        // Data flows through multiple methods without sanitization
        String processed = processData(userInput);
        logData(processed);
        executeQuery(processed);
    }
    
    private String processData(String input) {
        return input.toUpperCase();
    }
    
    private void logData(String data) {
        System.out.println("Processing: " + data);
    }
    
    private void executeQuery(String data) {
        try {
            // This would be flagged due to tainted data flow
            Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
            Statement stmt = conn.createStatement();
            stmt.execute("SELECT * FROM users WHERE name = '" + data + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
