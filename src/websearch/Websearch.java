package websearch;

/**
 * @author Sebastian Mai
 */

import java.util.Scanner;


public class Websearch {
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter seed URL:");
		System.out.print("http://");
		String startUrl = "http://" + sc.next();
		System.out.println("Enter recursion Depth (>= 0, <0 for infinit crawling):");
		int depth = sc.nextInt();
		
		Crawler crawler = new Crawler("index.dat");
		crawler.crawlStep(startUrl, depth);
		
		QueryEngine q = new QueryEngine("index.dat");
		sc.useDelimiter("\n");
		while(true) {
			System.out.print("Enter Query: ");
			q.query(sc.next());
		}
	}
}
