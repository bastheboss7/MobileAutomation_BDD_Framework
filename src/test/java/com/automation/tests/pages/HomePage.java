package com.automation.tests.pages;

import com.automation.framework.pages.BasePage;
import com.automation.framework.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.time.Duration;
import java.util.List;

/**
 * Page Object for the Home Page (sky.com).
 * Contains only locators and page actions.
 * 
 * @author Baskar
 * @version 2.0.0
 */
public class HomePage extends BasePage {
    
    // Locators using @FindBy (recommended for stable elements)
    @FindBy(id = "burger-nav-toggle")
    private WebElement hamburgerMenu;
    
    @FindBy(xpath = "//*[@data-test-id='input-box']")
    private WebElement searchInput;
    
    @FindBy(id = "search-results-wrapper")
    private WebElement searchResultsWrapper;
    
    /**
     * Handle cookie consent dialog if present.
     * Searches through all iframes for accept button.
     * 
     * @return true if cookies were handled
     */
    public boolean handleCookieConsent() {
        try {
            WaitUtils.sleep(2000); // Wait for cookie dialog to appear
            
            // Temporarily reduce implicit wait for faster iframe scanning
            getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
            
            List<WebElement> iframes = getDriver().findElements(By.tagName("iframe"));
            logger.info("Found {} iframes on page", iframes.size());
            
            for (int i = 0; i < iframes.size(); i++) {
                try {
                    getDriver().switchTo().frame(iframes.get(i));
                    
                    List<WebElement> acceptButtons = getDriver().findElements(
                        By.xpath("//button[contains(@title,'Agree') or contains(@title,'Accept') or contains(text(),'Accept') or contains(text(),'Agree')]")
                    );
                    
                    if (!acceptButtons.isEmpty()) {
                        acceptButtons.get(0).click();
                        logger.info("Cookie consent accepted in iframe {}", i);
                        switchToDefaultContent();
                        
                        // Restore implicit wait
                        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
                        return true;
                    }
                    switchToDefaultContent();
                } catch (Exception e) {
                    switchToDefaultContent();
                    logger.debug("No cookie button in iframe {}", i);
                }
            }
            
            // Try in main content
            List<WebElement> acceptButtons = getDriver().findElements(
                By.xpath("//button[contains(@title,'Agree') or contains(@title,'Accept') or contains(text(),'Accept') or contains(text(),'Agree')]")
            );
            
            if (!acceptButtons.isEmpty()) {
                acceptButtons.get(0).click();
                logger.info("Cookie consent accepted in main content");
                
                // Restore implicit wait
                getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
                return true;
            }
            
            // Restore implicit wait
            getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
            logger.info("No cookie consent dialog found");
            return false;
            
        } catch (Exception e) {
            getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
            logger.warn("Cookie handling failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Open the hamburger menu.
     */
    public void openMenu() {
        try {
            click(hamburgerMenu);
            WaitUtils.sleep(1000); // Wait for animation
            logger.info("Opened hamburger menu");
        } catch (Exception e) {
            // Try alternative selectors
            List<WebElement> menuButtons = getDriver().findElements(
                By.xpath("//*[@aria-label='Menu' or @aria-label='Open menu' or contains(@class,'burger') or contains(@class,'menu-toggle')]")
            );
            if (!menuButtons.isEmpty()) {
                menuButtons.get(0).click();
                WaitUtils.sleep(1000);
                logger.info("Opened menu using alternative selector");
            } else {
                throw new RuntimeException("Could not find menu button");
            }
        }
    }
    
    /**
     * Perform a search.
     * @param searchTerm The term to search for
     */
    public void search(String searchTerm) {
        openMenu();
        
        try {
            enter(searchInput, searchTerm);
            searchInput.submit();
            logger.info("Searched for: {}", searchTerm);
        } catch (Exception e) {
            // Try alternative search input
            List<WebElement> searchInputs = getDriver().findElements(
                By.xpath("//input[@type='search' or @type='text' or contains(@placeholder,'Search')]")
            );
            if (!searchInputs.isEmpty()) {
                searchInputs.get(0).sendKeys(searchTerm);
                searchInputs.get(0).submit();
                logger.info("Searched using alternative selector");
            } else {
                throw new RuntimeException("Could not find search input");
            }
        }
    }
    
    /**
     * Check if search results are displayed.
     * @return true if results are visible
     */
    public boolean isSearchResultsDisplayed() {
        try {
            WaitUtils.sleep(2000); // Wait for results to load
            return searchResultsWrapper.isDisplayed();
        } catch (Exception e) {
            // Try alternative selectors
            List<WebElement> results = getDriver().findElements(
                By.xpath("//*[contains(@id,'search') or contains(@class,'search-results') or contains(@class,'results')]")
            );
            return !results.isEmpty() && results.get(0).isDisplayed();
        }
    }
    
    /**
     * Navigate to Deals page.
     */
    public void navigateToDeals() {
        clickByXpath(getLocator("Home.Deals.Xpath"));
        logger.info("Navigated to Deals page");
    }
    
    /**
     * Get current URL.
     */
    public String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }
}
