package minesweeper.ai.utils;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface User32 extends StdCallLibrary {

    User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class,
            W32APIOptions.DEFAULT_OPTIONS);

    WinDef.HWND FindWindow(String lpClassName, String lpWindowName);
    boolean BringWindowToTop(WinDef.HWND hWnd);
    int GetWindowRect(WinDef.HWND handle, int[] rect);
    boolean SetForegroundWindow(WinDef.HWND hWnd);
    void SwitchToThisWindow(WinDef.HWND hWnd, boolean b);
}

