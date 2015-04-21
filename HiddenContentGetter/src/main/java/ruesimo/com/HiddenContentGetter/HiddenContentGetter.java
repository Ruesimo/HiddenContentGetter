/**
 * @author Simon
 * Project Crawl_CrossValidated creates a database with all questions, commentquestions, answers, and commentanswers (messages) from CrossValidated.
 * If there was hidden content, that had to be retrieved using javascript first, it was initialized by entering a 1 in table column "with_hidden_content".
 * 
 * This project searches for messages that have hidden content, retrieves it using HTMLUnit, and saves the additional data in the database.
 */
package ruesimo.com.HiddenContentGetter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public abstract class HiddenContentGetter {

	private static HashMap<Integer, String> hiddenContents;

	private static final String SQL_INSERT = "INSERT INTO hidden_contents"
			+ " VALUES(?, ?) ON DUPLICATE KEY UPDATE html=?";

	public static void main(String[] args) {

		String database = args[0];
		String password = args[1];

		PrintStream out;
		try {
			out = new PrintStream(new FileOutputStream("output.txt"));
			System.setOut(out);
		} catch (FileNotFoundException e) {
			System.err
					.println("Couldn't find output file. Output will be sent to console.");
		}

		hiddenContents = new HashMap<Integer, String>();

		setHiddenContents(database, password);
		for (Map.Entry<Integer, String> entry : hiddenContents.entrySet()) {
			int id = entry.getKey();
			String html = entry.getValue();
			save(database, password, id, html);
		}
	}

	private static void setHiddenContents(String database, String password) {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		int number = 0;

		try {
			connection = DriverManager.getConnection("jdbc:mysql://localhost/"
					+ database, "root", password);
			statement = connection.createStatement();
			resultSet = statement
					.executeQuery("SELECT id, COUNT(*) count FROM messages WHERE with_hidden_content = 1");

			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				int count = resultSet.getInt("count");
				try {
					hiddenContents.put(id, getHtml(id, ++number, count));
				} catch (Exception e) {
					System.out.println("getHtml(" + id + ") " + e);
				}
			}
			connection.close();
		} catch (Exception e) {
			System.out.println("setHiddenContents() " + e);
		}
	}

	private static String getHtml(int id, int number, int count)
			throws FailingHttpStatusCodeException, MalformedURLException,
			IOException {
		String html = "";
		WebClient webClient = new WebClient();
		HtmlPage page = webClient
				.getPage("http://stats.stackexchange.com/questions/" + id + "/");
		HtmlPage newPage = null;

		List<HtmlAnchor> anchors = page.getAnchors();

		for (int i = 0; i < anchors.size(); i++) {
			if (anchors.get(i).asXml()
					.contains("js-show-link comments-link \"")) {
				newPage = anchors.get(i).click();
				webClient.waitForBackgroundJavaScript(1000);
			}
		}
		webClient.closeAllWindows();
		html = newPage.asXml();
		System.out.print("getHtml(" + id + ") successful (");
		System.out.println(number + " of " + count + " completed)");
		return html;
	}

	private static void save(String database, String password, int id,
			String html) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DriverManager.getConnection("jdbc:mysql://localhost/"
					+ database, "root", password);
			statement = connection.prepareStatement(SQL_INSERT);
			statement.setInt(1, id);
			statement.setString(2, html);

			statement.setString(3, html);

			statement.executeUpdate();
			System.out.println("save(" + id + ") successful");
			connection.close();
		} catch (SQLException e) {
			System.out.println("save(" + id + ") " + e);
		}
	}
}