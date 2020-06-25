package GameController;

/*
 * MacOS users should start their application passing "-XstartOnFirstThread" as a VM option.
 */
public class Main {
	
	public static GameManager gm;
	
	public static void main(String[] args) {
		System.out.println("hello world");
		
		GameManager gm = new GameManager();
	}
}
