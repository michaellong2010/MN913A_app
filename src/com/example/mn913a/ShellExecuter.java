/*20160627 add by JanChen*/
package com.example.mn913a;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.util.Log;
 
public class ShellExecuter {
 
   //  public ShellExecuter() {
 
     //   }
public final String Tag = "ShellExecuter";
public String Executer(String command) {
 
            StringBuffer output = new StringBuffer();
           
            Process p;
            try {
            //	p = Runtime.getRuntime().exec("su");
                p = Runtime.getRuntime().exec(command);
                p.waitFor();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
 
                String line = "";
                while ((line = reader.readLine())!= null) {
                    output.append(line + "n");
                }
 
            } catch (Exception e) {
                e.printStackTrace();
            }
            String response = output.toString();
            return response;
 
        }

private final String Msg_Shell_Command_Error = "shell command error";
	public String exec_shell_command_mn913a( String shell_cmd ) {
		// TODO Auto-generated method stub
		String result, line;
		java.lang.Process p;
		java.lang.Runtime rt;
		byte[] buff;
		int readed;

		result = Msg_Shell_Command_Error;
		buff = new byte[100];
		/*try {
				 Thread.sleep(3000);
				 Log.d(Tag, "?????  ###################  exec_shell_command: " );
	     } catch (Exception ex) {
	     }*/
		try {
			
			rt = Runtime.getRuntime();
			p = rt.exec(new String[] { "su" });///system/xbin/
			DataOutputStream os = new DataOutputStream(p.getOutputStream());

			InputStreamReader is_reader = new InputStreamReader ( p.getInputStream() );
			BufferedReader buf_is_reader = new BufferedReader ( is_reader );
			os.writeBytes( shell_cmd );
			os.writeBytes( "exit\n" );
			os.flush();
			p.waitFor();
			if ( p.exitValue() == 0 ) {
				result = "";
				while ( ( line = buf_is_reader.readLine() ) != null ) {
					result += line;
				}
				Log.d(Tag, "???  ###################  exitValue : " );
			}
			else
				result = Msg_Shell_Command_Error;
			os.flush();
			os.close();
			is_reader.close();
			buf_is_reader.close();
			p.waitFor();
			p.destroy();
		} catch (IOException e) {
			Log.d(Tag, "???  ###################  exec_shell_command error: " );
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception ex) {
			Log.d(Tag, "???  ###################  exec_shell_command Exception: " );
		}
		
		return result;
	}
}


