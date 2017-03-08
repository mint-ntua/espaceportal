package browser;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import play.test.WithBrowser;

public class BrowserFunctionalTest extends WithBrowser {

	@Test
	public void runInBrowser() {
		browser.goTo("/");
		System.out.println(browser.$("title").getText());
		assertNotNull(browser.$("title").getText());
	}

}