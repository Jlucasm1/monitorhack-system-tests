package br.com.monitorhack.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

/**
 * Page Object do painel (dashboard) de monitoramento de ataques.
 * Concentra toda a interação com a tela principal.
 */
public class DashboardPage extends BasePage {

    private final By dashboardView = By.id("dashboard-view");
    private final By filterType    = By.id("filter-type");
    private final By filterSev     = By.id("filter-severity");
    private final By searchIp      = By.id("search-ip");
    private final By btnClear      = By.id("btn-clear");
    private final By btnSimulate   = By.id("btn-simulate");
    private final By notification  = By.id("notification");
    private final By rows          = By.cssSelector("#attacks-body tr.attack-row");

    // Estatísticas
    private final By statTotal   = By.id("stat-total");
    private final By statCrit    = By.id("stat-critical");
    private final By statActive  = By.id("stat-active");
    private final By statBlocked = By.id("stat-blocked");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return find(dashboardView).isDisplayed();
    }

    // ---------- Estatísticas ----------
    public int getTotal()   { return parse(statTotal); }
    public int getCritical(){ return parse(statCrit); }
    public int getActive()  { return parse(statActive); }
    public int getBlocked() { return parse(statBlocked); }

    private int parse(By locator) {
        return Integer.parseInt(find(locator).getText().trim());
    }

    // ---------- Filtros ----------
    public DashboardPage filterByType(String type) {
        new Select(find(filterType)).selectByVisibleText(type);
        pause();
        return this;
    }

    public DashboardPage filterBySeverity(String severity) {
        new Select(find(filterSev)).selectByVisibleText(severity);
        pause();
        return this;
    }

    public DashboardPage searchByIp(String ipFragment) {
        WebElement input = find(searchIp);
        input.clear();
        input.sendKeys(ipFragment);
        pause();
        return this;
    }

    public DashboardPage clearFilters() {
        clickable(btnClear).click();
        pause();
        return this;
    }

    // ---------- Tabela ----------
    public int getVisibleRowCount() {
        return driver.findElements(rows).size();
    }

    private By rowByIp(String ip) {
        return By.cssSelector("#attacks-body tr.attack-row[data-ip='" + ip + "']");
    }

    public String getStatusForIp(String ip) {
        return find(rowByIp(ip)).getAttribute("data-status");
    }

    public boolean isRowVisibleForIp(String ip) {
        return !driver.findElements(rowByIp(ip)).isEmpty();
    }

    // ---------- Ações ----------
    public DashboardPage blockIp(String ip) {
        By btn = By.cssSelector("#attacks-body tr.attack-row[data-ip='" + ip + "'] .btn-block");
        clickable(btn).click();
        waitAttribute(rowByIp(ip), "data-status", "bloqueado"); // espera explícita
        pause();
        return this;
    }

    public DashboardPage acknowledgeIp(String ip) {
        By btn = By.cssSelector("#attacks-body tr.attack-row[data-ip='" + ip + "'] .btn-ack");
        clickable(btn).click();
        waitAttribute(rowByIp(ip), "data-status", "reconhecido"); // espera explícita
        pause();
        return this;
    }

    public DashboardPage simulateAttack() {
        int before = getTotal();
        clickable(btnSimulate).click();
        // Espera explícita até o total realmente aumentar
        wait.until(ExpectedConditions.textToBe(statTotal, String.valueOf(before + 1)));
        pause();
        return this;
    }

    public String getNotificationText() {
        return find(notification).getText();
    }

    public List<WebElement> getRows() {
        return driver.findElements(rows);
    }
}
