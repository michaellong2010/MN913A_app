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
        StringBuffer output = new StringBuffer();
        String result=null;
        Process p;
        
        try {
            p = Runtime.getRuntime().exec(shell_cmd);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            if ( p.exitValue() == 0 ) {
            while ((line = reader.readLine())!= null) {
                output.append(line + "n");
            }
            result ="COPY FILES PASS";
            }else{
            	result = Msg_Shell_Command_Error;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return result;
	}
}