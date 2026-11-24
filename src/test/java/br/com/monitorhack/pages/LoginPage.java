package br.com.monitorhack.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page Object da tela de login.
 */
public class LoginPage extends BasePage {

    private final By username   = By.id("username");
    private final By password   = By.id("password");
    private final By btnLogin   = By.id("btn-login");
    private final By loginError = By.id("login-error");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    /** Abre a aplicação na URL informada e espera a tela de login aparecer. */
    public LoginPage open(String baseUrl) {
        driver.get(baseUrl);
        find(username);
        return this;
    }

    /** Preenche usuário e senha e clica em Entrar. */
    public DashboardPage loginAs(String user, String pass) {
        find(username).clear();
        find(username).sendKeys(user);
        find(password).clear();
        find(password).sendKeys(pass);
        pause();
        clickable(btnLogin).click();
        return new DashboardPage(driver);
    }

    /** Tenta logar esperando que FALHE (usado para credenciais inválidas). */
    public LoginPage loginExpectingFailure(String user, String pass) {
        find(username).clear();
        find(username).sendKeys(user);
        find(password).clear();
        find(password).sendKeys(pass);
        clickable(btnLogin).click();
        return this;
    }

    public boolean isErrorVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(loginError)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorText() {
        return find(loginError).getText();
    }
}
