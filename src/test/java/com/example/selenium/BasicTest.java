package com.example.selenium;

import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * Basic TestNG test to ensure the build works
 */
public class BasicTest {
    
    @Test
    public void testBasicAssertion() {
        // Simple test to verify TestNG is working
        Assert.assertTrue(true, "This should always pass");
        System.out.println("Basic test executed successfully!");
    }
    
    @Test
    public void testVulnerableCodeExists() {
        // Test that our vulnerable classes exist
        VulnerableLogin login = new VulnerableLogin();
        VulnerableFileHandler handler = new VulnerableFileHandler();
        
        Assert.assertNotNull(login, "VulnerableLogin class should exist");
        Assert.assertNotNull(handler, "VulnerableFileHandler class should exist");
        
        System.out.println("Vulnerable code classes are available for testing");
    }
}
