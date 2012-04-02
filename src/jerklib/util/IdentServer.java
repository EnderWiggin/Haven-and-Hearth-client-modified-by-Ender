package jerklib.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

//http://books.google.com/books?id=MbHAnBh9AqQC&pg=PA310&lpg=PA310&dq=irc+fake+ident&source=web&ots=c5sHoXuzFS&sig=ZOuAeIFxKUYanirnj_hbnfpCXBQ&hl=en#PPA311,M1
public class IdentServer implements Runnable
{
	private ServerSocket socket;
	private String login;

	public IdentServer(String login)
	{
		this.login = login;
		try
		{
			socket = new ServerSocket(113);
			socket.setSoTimeout(60000);
			new Thread(this).start();
		}
		catch (Exception e){}
	}

	public void run()
	{
		if (socket == null) return;
		try
		{
			Socket soc = socket.accept();
			soc.setSoTimeout(60000);

			BufferedReader reader = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream()));

			String line = reader.readLine();
			if (line != null)
			{
				writer.write(line + " : USERID : UNIX : " + login + "\r\n");
				writer.flush();
				writer.close();
				reader.close();
			}
			socket.close();
		}
		catch (IOException e)
		{
			// log
		}

	}
}
