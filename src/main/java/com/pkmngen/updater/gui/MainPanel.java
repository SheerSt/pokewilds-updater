package com.pkmngen.updater.gui;

import com.pkmngen.updater.Updater;
import com.pkmngen.updater.platform.PlatformUtil;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public final class MainPanel extends JPanel {
    private final Updater updater;
    private final String localVersion;
    private final String version;
    private final String changelog;
    private final JTextArea textAreaChangelog;
    private final JLabel labelStatus;
    private final JProgressBar progressBar;
    private final JButton buttonUpdate;

    public MainPanel(Updater updater, String localVersion, String version, String changelog) {
        this.updater = updater;
        this.localVersion = localVersion;
        this.version = version;
        this.changelog = changelog;

        setupPanel();

        this.textAreaChangelog = createTextAreaChangelog();
        this.labelStatus = createLabelStatus();
        this.progressBar = createProgressBar();
        this.buttonUpdate = createButtonUpdate();

        add(new JScrollPane(textAreaChangelog));
        add(labelStatus);
        add(progressBar);
        add(buttonUpdate);
    }

    public void setChangelogText(String text) {
        textAreaChangelog.setText(text);
    }

    public void setStatusText(String text) {
        labelStatus.setText(text);
    }

    public void setProgress(int progress) {
        progressBar.setValue(progress);
    }

    private void setupPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    private JTextArea createTextAreaChangelog() {
        var textArea = new JTextArea();

        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setText("Changelog (v" + version + "):\n" + changelog);

        return textArea;
    }

    private JLabel createLabelStatus() {
        var label = new JLabel();

        if (version.equals(localVersion)) {
            label.setText("The game is up to date");
        }

        return label;
    }

    private JProgressBar createProgressBar() {
        var progressBar = new JProgressBar();

        return progressBar;
    }

    private JButton createButtonUpdate() {
        var file = PlatformUtil.getPlatform().getDownloadFileName();

        var isUpToDate = new AtomicBoolean(version.equals(localVersion));
        var button = new JButton(isUpToDate.get() ? "Play game" : "Update");

        button.addActionListener(event -> {
            button.setEnabled(false);
            setProgress(0);

            if (isUpToDate.get()) {
                try {
                    updater.runGame();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return;
            }

            updater.update(file, status -> SwingUtilities.invokeLater(() -> {
                setStatusText(status.message());
                setProgress(status.progress());
                System.out.println(status.progress() + ": " + status.message());
            })).thenRunAsync(() -> SwingUtilities.invokeLater(() -> {
                isUpToDate.set(true);
                button.setText("Play game");
                button.setEnabled(true);
                labelStatus.setText("The game is now up to date");
            }));
        });

        return button;
    }
}
