package com.pkmngen.updater;

import com.pkmngen.updater.platform.PlatformUtil;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

public class UpdaterApplication extends JPanel {

    public JFrame frame = new JFrame();
    public JProgressBar progress;
    public JLabel message;
    public JTextArea changelog;

    public Updater updater = new Updater(this);
    public VersionControl versionControl = new VersionControl();

    public final String VERSION_FILE = "version.txt";

    public static void main(String[] args) {
        new UpdaterApplication();
    }

    public UpdaterApplication() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {
            e.printStackTrace();
        }

        frame.setTitle("Pokemon Wilds - Updater");
        var scale = 3;
        frame.setSize(160 * scale, 144 * scale);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(null);

        changelog = addTextArea(10, 10, frame.getWidth() - 40, frame.getHeight() - 150);
        changelog.setText(versionControl.getChangelog());

        var scroll = addScrollPane(changelog, 10, 10, frame.getWidth() - 40, frame.getHeight() - 150);
        add(scroll);

        progress = addProgressBar(10, frame.getHeight() - 100, frame.getWidth() - 40, 25);
        add(progress);

        message = addLabel("Getting latest...", 10, frame.getHeight() - 130, frame.getWidth() - 40, 25);
        add(message);

        frame.add(this);
        frame.setVisible(true);

        var platform = PlatformUtil.getPlatform();
        var file = switch (platform) {
            case WINDOWS -> "pokemon-wilds-windows64";
            case MAC -> "pokemon-wilds-mac64";
            case LINUX -> "pokemon-wilds-linux64";
            default -> throw new RuntimeException("Unknown platform");
        } + ".zip";

        updater.update(file);
    }

    public JProgressBar addProgressBar(int x, int y, int width, int height) {
        var bar = new JProgressBar();
        bar.setLocation(x, y);
        bar.setSize(width, height);
        bar.setValue(0);

        return bar;
    }

    public JLabel addLabel(String text, int x, int y, int width, int height) {
        var label = new JLabel(text);
        label.setLocation(x, y);
        label.setSize(width, height);

        return label;
    }

    public JTextArea addTextArea(int x, int y, int width, int height) {
        var area = new JTextArea();
        area.setLocation(x, y);
        area.setSize(width, height);
        area.setLineWrap(true);
        area.setEditable(false);

        return area;
    }

    public JScrollPane addScrollPane(JTextArea area, int x, int y, int width, int height) {
        var scroll = new JScrollPane(area);
        scroll.setLocation(x, y);
        scroll.setSize(width, height);

        return scroll;
    }

    public void closeApplication() {
        System.exit(0);
    }
}
