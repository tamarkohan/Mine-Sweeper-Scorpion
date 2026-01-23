package util;

import Controller.GameController;
import View.ConfirmDialog;

import java.awt.Window;

public final class ExitConfirmHelper {

    private ExitConfirmHelper() {}

    public static boolean confirmExit(Window owner) {
        var lang = GameController.getInstance().getCurrentLanguage();
        boolean isRTL = LanguageManager.isRTL(lang);

        String title = LanguageManager.get("exit_title", lang);
        String msg = LanguageManager.get("exit_confirm_msg", lang);

        // play sound when dialog opens
        SoundManager.exitDialog();

        // same style as game panel
        return ConfirmDialog.show(owner, title, msg, new java.awt.Color(255, 200, 80), isRTL);
    }
}