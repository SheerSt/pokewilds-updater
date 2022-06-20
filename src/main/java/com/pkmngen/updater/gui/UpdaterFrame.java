package com.pkmngen.updater.gui;

import com.pkmngen.updater.Updater;
import javax.swing.JFrame;

public final class UpdaterFrame {
    private static final int DEFAULT_FRAME_SCALE = 3;

    private final Updater updater;
    private final JFrame frame;

    public UpdaterFrame(Updater updater, String localVersion, String version, String changelog) {
        this.updater = updater;
        this.frame = new JFrame();

        frame.setTitle("Pokemon Wilds - Updater");
        frame.setSize(160 * DEFAULT_FRAME_SCALE, 144 * DEFAULT_FRAME_SCALE);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setContentPane(new MainPanel(updater, localVersion, version, changelog));
        frame.setVisible(true);
    }

    public void show() {
        frame.setVisible(true);
    }
}
