package application;

import java.io.*;
import java.net.*;

public class MyClient {
	public static void main(String[] args) {
		try {
			Socket s = new Socket("192.168.1.34", 6666);
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());
			dout.writeUTF("Hello Server!!!");
			dout.flush();
			dout.close();
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}
}
