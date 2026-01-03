package View;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class IconCache {
    private static final Map<String, Map<Integer, ImageIcon>> CACHE = new HashMap<>();

    public static ImageIcon icon(String path, int sizePx) {
        CACHE.putIfAbsent(path, new HashMap<>());
        Map<Integer, ImageIcon> bySize = CACHE.get(path);

        if (bySize.containsKey(sizePx)) return bySize.get(sizePx);

        ImageIcon raw = new ImageIcon(IconCache.class.getResource(path));
        Image scaled = raw.getImage().getScaledInstance(sizePx, sizePx, Image.SCALE_SMOOTH);
        ImageIcon out = new ImageIcon(scaled);

        bySize.put(sizePx, out);
        return out;
    }
}
