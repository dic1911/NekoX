package tw.nekomimi.nekogram.utils;


import android.util.Log;

import org.telegram.messenger.MessageObject;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import tw.nekomimi.nekogram.NekoConfig;

public class TelegramUtil {

    public static String getFileNameWithoutEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    // 消息是否为文件
    public static boolean messageObjectIsFile(int type, MessageObject messageObject) {
        boolean cansave = (type == 4 || type == 5 || type == 6 || type == 10);
        boolean downloading = messageObject.loadedFileSize > 0;

        //图片的问题
        if (type == 4 && messageObject.getDocument() == null) {
            return false;
        }
        return cansave || downloading;
    }

    // 当文件有过加载过程，loadedFileSize > 0 ，所以不能用loadedFileSize判断是否正在下载
    public static boolean messageObjectIsDownloading(int type) {
        boolean cansave = (type == 4 || type == 5 || type == 6 || type == 10);
        return !cansave;
    }

    public static boolean isConnecting() {
        int state = ConnectionsManager.getInstance(UserConfig.selectedAccount).getConnectionState();
        return state == ConnectionsManager.ConnectionStateConnecting;
    }

    private static Thread toggleProxyOnOffThread = null;
    private static final Runnable toggleProxyOnOffRunnable = new Runnable() {
        @Override
        public void run() {
            boolean suc = false;
            while (!suc) {
                try {
                    Thread.sleep(1000);
                    suc = true;
                } catch (InterruptedException e) {
                    Log.w("030-conn", "interrupted");
                    if (!isConnecting())
                        return;
                }
            }
            Log.d("030-conn", "applying workaround, enable proxy");
            SharedConfig.setProxyEnable(true);
            try {
                Thread.sleep(300);
            } catch (InterruptedException ignored) {}
            Log.d("030-conn", "applying workaround, disable proxy");
            SharedConfig.setProxyEnable(false);
        }
    };
    public static void toggleProxyOnOff(boolean cancel) {
        if (toggleProxyOnOffThread != null) {
            if (cancel) toggleProxyOnOffThread.interrupt();
            else {
                try {
                    toggleProxyOnOffThread.join(350);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            toggleProxyOnOffThread = null;
            return;
        }
        else if (cancel || !NekoConfig.fasterReconnectHack.Bool()) return;
        else if (!isConnecting()) return;
        toggleProxyOnOffThread = new Thread(toggleProxyOnOffRunnable);
        toggleProxyOnOffThread.start();
    }
}