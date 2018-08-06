package studio.jedjiang;

public class MainLauncherTest {
	public static void main(String[] args) {
		String site = "0A-B1.xml";

		// System.out.println(Const.getStartSite(site));
		// System.out.println(Const.getDestSite(site));
		
		int pos = site.indexOf(".xml");
		if(pos != -1) {
			site = site.substring(0, pos);
		}
		
		System.out.println(site);
	}
}
