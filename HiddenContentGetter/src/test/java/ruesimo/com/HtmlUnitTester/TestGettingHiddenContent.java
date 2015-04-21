package ruesimo.com.HtmlUnitTester;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class TestGettingHiddenContent {

	ArrayList<String> urlsWithHiddenContent;

	@Before
	public void setOutputFile() {
		PrintStream out;
		try {
			out = new PrintStream(new FileOutputStream("outputTest.txt"));
			System.setOut(out);
		} catch (FileNotFoundException e) {
			System.err
					.println("Couldn't find output file. Output will be sent to console.");
		}
		urlsWithHiddenContent = new ArrayList<String>();

		urlsWithHiddenContent
				.add("http://stats.stackexchange.com/questions/423/what-is-your-favorite-data-analysis-cartoon?page=1");
		urlsWithHiddenContent
				.add("http://stats.stackexchange.com/questions/423/what-is-your-favorite-data-analysis-cartoon?page=2");
		urlsWithHiddenContent
				.add("http://stats.stackexchange.com/questions/1507/example-of-using-binomial-distribution?page=1");

	}

	@Test
	public void getElements() throws Exception {
		for (int urlIndex = 0; urlIndex < urlsWithHiddenContent.size(); urlIndex++) {
			WebClient webClient = new WebClient();
			HtmlPage page = webClient.getPage(urlsWithHiddenContent
					.get(urlIndex));
			HtmlPage newPage = null;

			List<HtmlAnchor> anchors = page.getAnchors();

			for (int i = 0; i < anchors.size(); i++) {
				if (anchors.get(i).asXml()
						.contains("js-show-link comments-link")) {
					String anchor = anchors.get(i).asXml();
					System.out.println(anchor);
					newPage = anchors.get(i).click();
					//webClient.waitForBackgroundJavaScript(60000);
					String text = newPage.asText();
					System.out.println(text);
				}
			}
			webClient.closeAllWindows();
			String html = newPage.asXml();
			System.out.println(html);
		}
	}
}
