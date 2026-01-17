package util;

import Controller.GameController;
import View.ConfirmDialog;

import java.awt.Window;

public final class ExitConfirmHelper {

    private ExitConfirmHelper() {}

    public static boolean confirmExit(Window owner) {
        var lang = GameController.getInstance().getCurrentLanguage();
        boolean isHe = (lang == util.LanguageManager.Language.HE);

        String title = isHe ? "יציאה" : "Exit";
        String msg = isHe
                ? "האם אתה בטוח שברצונך לצאת?\nההתקדמות תאבד."
                : "Are you sure you want to exit?\nProgress will be lost.";

        // play sound when dialog opens
        SoundManager.exitDialog();

        // same style as game panel
        return ConfirmDialog.show(owner, title, msg, new java.awt.Color(255, 200, 80), isHe);
    }
}
