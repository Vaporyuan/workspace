package com.meig.logkit;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The thread of command exec.
 * each command with one thread 
 */

public class CommandExecThread extends Thread {

    // the command
    private String cmd;

    // the process of command
    private Process mLogcatProc = null;

    // the path of file
    private String mFilePath = null;

    public CommandExecThread(String cmd) {
        this.cmd = cmd;
    }

    public CommandExecThread(String cmd, String filepath) {
        this.cmd = cmd;
        this.mFilePath = filepath;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void run() {
        BufferedWriter bw = null;
        DataInputStream dis = null;
        try {
            LogkitUtils.LogE(cmd);
            // exec the command
            String[] cmds = new String[] { "sh", "-c", cmd };
            mLogcatProc = Runtime.getRuntime().exec(cmds);
            mLogcatProc.getOutputStream().write("\nexit".getBytes());
            mLogcatProc.getOutputStream().flush();
            String reader = null;
            if (null != mFilePath) {
                // get input msg and write to sd
                dis = new DataInputStream(mLogcatProc.getInputStream());
                bw = new BufferedWriter(new FileWriter(new File(mFilePath),
                        true));
                while ((reader = dis.readLine()) != null) {
                    bw.write(reader+"\n");
                }
                bw.flush();
            }
            String msg = "";
            reader = null;
            // get error msg
            dis = new DataInputStream(mLogcatProc.getErrorStream());
            while ((reader = dis.readLine()) != null) {
                msg += reader;
            }
            if (!"".equals(msg)) {
                LogkitUtils.LogE("Runtime.getRuntime().exec error:" + msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogkitUtils.LogE("Exception=" + e);
        } finally {
            // close input and output stream
            try {
                if (null != dis) {
                    dis.close();
                }
                if (null != bw) {
                    bw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            stopLogcatProcess();
        }
    }

    /**
     * destory the process
     */
    public void stopLogcatProcess() {
        if (null != mLogcatProc) {
            mLogcatProc.destroy();
        }
    }

}
