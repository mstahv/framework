/*
 * Copyright 2000-2016 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.tests.navigator;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.tests.tb3.MultiBrowserTest;

public class NavigatorViewBlocksBackButtonActionTest extends MultiBrowserTest {

    @Test
    public void testIfConfirmBack() {
        openTestURL();

        // Ensure the test page is rendered
        $(ButtonElement.class).first();

        // keep URL of main view
        final String initialUrl = getUrlForReal(driver);

        // do it 2 times to verify that login is not broken after first time
        for (int i = 0; i < 2; i++) {
            WebElement button = $(ButtonElement.class).first();
            // go to prompted view
            button.click();

            // click back button
            driver.navigate().back();

            // confirm "go back by clicking confirm button
            WebElement buttonConfirmView = $(ButtonElement.class).first();
            buttonConfirmView.click();

            // verify we are in main view and url is correct
            waitForElementPresent(By
                    .id(NavigatorViewBlocksBackButtonAction.LABEL_MAINVIEW_ID));
            String currentUrl = getUrlForReal(driver);
            assertEquals("Current URL should be equal to initial main view URL",
                    initialUrl, currentUrl);
        }
    }

    @Test
    public void testIfCancelBack() {
        openTestURL();

        $(ButtonElement.class).first();

        // go to prompted view
        WebElement button = $(ButtonElement.class).first();
        button.click();

        // keep URL of prompted view
        final String initialPromptedUrl = getUrlForReal(driver);

        // click back button
        driver.navigate().back();

        // verify url is correct (is not changed)
        waitForElementPresent(By
                .id(NavigatorViewBlocksBackButtonAction.LABEL_PROMPTEDVIEW_ID));
        String currentUrl = getUrlForReal(driver);
        assertEquals("Current URL should be equal to initial prompted view URL",
                initialPromptedUrl, currentUrl);

        WebElement cancelButton = driver
                .findElement(By.className("v-window-closebox"));

        // click cancel button
        cancelButton.click();

        // verify we leave in prompted view and url is correct
        waitForElementPresent(By
                .id(NavigatorViewBlocksBackButtonAction.LABEL_PROMPTEDVIEW_ID));
        currentUrl = getUrlForReal(driver);
        assertEquals("Current URL should be equal to initial prompted view URL",
                initialPromptedUrl, currentUrl);
    }

    public static String getUrlForReal(WebDriver driver) {
        // IE11 driver has a bug so read with JS instead
        // https://github.com/SeleniumHQ/selenium-google-code-issue-archive/issues/7966
        Object url = null;
        if (driver instanceof JavascriptExecutor) {
            try {
                driver.manage().timeouts().setScriptTimeout(500,
                        TimeUnit.MILLISECONDS);
                url = ((JavascriptExecutor) driver).executeAsyncScript(
                        "var callback = arguments[arguments.length - 1]; window.setTimeout(function(){var str = top.location.href; callback(str);}, 200);");
            } catch (Exception e) {
                e.printStackTrace();
                // TODO: handle exception
            }
            if (url == null) {

                // try again in a while...
                try {
                    Thread.sleep(5000);
                    url = ((JavascriptExecutor) driver)
                            .executeScript("top.location.toString();");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (url != null && url instanceof String) {
                    return (String) url;
                }
            }
        }
        return driver.getCurrentUrl();
    }
}
