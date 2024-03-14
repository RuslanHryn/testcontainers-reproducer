package org.example.testcontainersreproducer;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@Testcontainers
@ContextConfiguration(initializers = TestcontainersReproducerTests.Initializer.class)
class TestcontainersReproducerTests {

    @LocalServerPort
    private int port;

    @Container
    private BrowserWebDriverContainer<?> chrome = new BrowserWebDriverContainer<>()
            .withCapabilities(new ChromeOptions())
            .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL, Paths.get("/target").toFile());

    @Test
    void testVnc() {
        RemoteWebDriver driver = new RemoteWebDriver(chrome.getSeleniumAddress(), new ChromeOptions());
        driver.get("http://host.testcontainers.internal:" + port);

        driver.findElement(By.ByLinkText.linkText("Open Popup")).click();
        for (String winHandle : driver.getWindowHandles()) {
            driver.switchTo().window(winHandle);
        }
        driver.close();

        driver.switchTo().defaultContent();

        driver.quit();
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            applicationContext.addApplicationListener(
                    (ApplicationListener<WebServerInitializedEvent>) event -> {
                        org.testcontainers.Testcontainers.exposeHostPorts(event.getWebServer().getPort());
                    }
            );
        }
    }

}
