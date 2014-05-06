package de.fun2code.android.piratebox.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import de.fun2code.android.pawserver.util.Utils;

/**
 * Support class to execute shell commands
 * 
 * @author joschi
 *
 */
public class ShellUtil {
	public static String SH_BIN = "sh";
	public static String SU_BIN = "su";
	
	/**
	 * Execute shell command with norma shell
	 * 
	 * @param commands	command to execute
	 * @return			{@code true} if the operation succeeded
	 */
	public boolean execShell(String[] commands) {
		return execShell(commands , false);
	}

	/**
	 * Executes commands by using the root shell (su)
	 * 
	 * @param commands	command to execute
	 * @return			{@code true} if the operation succeeded
	 */
	public boolean execRootShell(String[] commands) {
		return execShell(commands , true);
	}

	/**
	 * Executes shell command with standard or su shell
	 * 
	 * @param commands	commands to execute
	 * @param root		{@code true} if root shell should be used otherwise {@code false}
	 * @return			{@code true} if the operation succeeded
	 */
	private boolean execShell(String[] commands, boolean root) {
		try {
			String shellCmd = root ? SU_BIN + " -c sh" : SH_BIN;

			Process sh = Runtime.getRuntime().exec(shellCmd);
			OutputStream os = sh.getOutputStream();

			for (String cmd : commands) {
				os.write((cmd + "\n").getBytes());
			}

			os.write(("exit\n").getBytes());
			os.flush();
			sh.waitFor();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Checks if a command is available
	 * 
	 * @param command	command to check
	 * @return			{@code true} if command is available, otherwise {@code false}
	 */
	public boolean isCommandAvailable(String command) {
		try {
			Process proc = Runtime.getRuntime().exec(command);
			proc.destroy();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Returns the PID of a process
	 * 
	 * @param processName	process to retrieve PID from
	 * @return				PID or -1 if  process was not found
	 */
	public int getProcessPid(String processName) {
		try {
			int pid = -1;
			Process ps = Runtime.getRuntime().exec("ps");
			InputStream is = ps.getInputStream();
			BufferedReader stdout = new BufferedReader(
					new InputStreamReader(is));

			int lines = 0;
			int pidCol = 0;
			int procCol = 4;

			String line;
			while ((line = stdout.readLine()) != null) {

				lines++;

				/* There seem to be two versions of ps commands */
				if (lines == 1) {
					if (line.startsWith("USER")) {
						pidCol = 1;
						procCol = 8;
					}

					continue;
				}

				line = line.replaceAll("^\\s+", "");
				String[] processInfo = line.split("\\s+");

				if (processInfo[procCol].equals(processName)) {
					pid = Integer.parseInt(processInfo[pidCol]);
					break;
				}
			}
			is.close();
			return pid;
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Kill process with a given name
	 * 
	 * @param processName	process to kill
	 * @return				PID of killed process, if process could not be killed -1
	 */
	public int killProcessByName(String processName) {
		int pid = getProcessPid(processName);
		if (pid != -1) {
			String[] killCmd = new String[] { "kill " + pid,

			};

			execRootShell(killCmd);
			return pid;
		} else {
			return -1;
		}

	}
	
	/**
	 * Remounts /system
	 * 
	 * @param options	option to use, nomally {@code rw} of {@code ro}
	 */
	public void remountSystem(String options) {
		String[] cmd = new String[] { "mount -o " + options + ",remount /system" };
		execRootShell(cmd);
	}
	
	
	/**
	 * Wait for a process with the given process name
	 * 
	 * @param processName	name of process to wait for
	 * @param timeout		timeout in millis
	 * @return				returns the process id or {@code -1} if no process was found
	 */
	public int waitForProcess(String processName, long timeout) {
		long until = System.currentTimeMillis() + timeout;
		
		int id;
		while((id = getProcessPid(processName)) == -1 && System.currentTimeMillis() < until) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return id;
	}
}
