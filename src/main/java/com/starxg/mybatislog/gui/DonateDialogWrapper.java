package com.starxg.mybatislog.gui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.util.ui.JBUI;
import com.starxg.mybatislog.Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

/**
 * DonateDialogWrapper
 * 
 * @author huangxingguang
 */
public class DonateDialogWrapper extends DialogWrapper {

    public DonateDialogWrapper(@Nullable Project project) {
        super(project, false);

        init();

        setTitle("Donate - Thank You Very Much!");

        setResizable(false);

    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel();
        final GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = new int[] { 0, 0, 0 };
        layout.rowHeights = new int[] { 0, 0, 0 };
        layout.columnWeights = new double[] { 1, 1, 1 };
        layout.rowWeights = new double[] { 1, 1, 1 };
        panel.setLayout(layout);

        try {

            final JLabel alipay = new JLabel(new ImageIcon(
                    ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/alipay.png"), "alipay.png"))
                            .getScaledInstance(200, 200, Image.SCALE_SMOOTH)));
            final JLabel wechatpay = new JLabel(new ImageIcon(ImageIO
                    .read(Objects.requireNonNull(getClass().getResource("/images/wechatpay.png"), "wechatpay.png"))
                    .getScaledInstance(200, 200, Image.SCALE_SMOOTH)));

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.insets = JBUI.insets(10);
            c.gridy = 0;
            c.gridx = 0;
            panel.add(new JLabel("Alipay", JLabel.CENTER), c);

            c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.insets = JBUI.insets(0, 0, 0, 10);
            c.gridy = 0;
            c.gridx = 1;
            panel.add(new JLabel("WeChat Pay", JLabel.CENTER), c);

            c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.insets = JBUI.insets(0, 0, 0, 10);
            c.gridy = 1;
            c.gridx = 0;
            panel.add(alipay, c);

            c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridy = 1;
            c.gridx = 1;
            panel.add(wechatpay, c);

            c = new GridBagConstraints();
            c.insets = JBUI.insets(10);
            c.gridy = 2;
            c.gridx = 0;
            final HyperlinkLabel kofi = new HyperlinkLabel();
            kofi.setIcon(Icons.DONATE);
            kofi.setHyperlinkText("Donate on Ko-fi");
            kofi.setUseIconAsLink(true);
            kofi.setHyperlinkTarget("https://ko-fi.com/huangxingguang");
            panel.add(kofi, c);

            c = new GridBagConstraints();
            c.insets = JBUI.insets(10);
            c.gridy = 2;
            c.gridx = 1;
            final HyperlinkLabel paypal = new HyperlinkLabel();
            paypal.setIcon(Icons.DONATE);
            paypal.setHyperlinkText("Donate on PayPal");
            paypal.setUseIconAsLink(true);
            paypal.setHyperlinkTarget("https://paypal.me/huangxingguang");

            panel.add(paypal, c);

        } catch (IOException e) {
            Logger.getInstance(getClass()).error(e.getMessage(), e);
        }

        return panel;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[] { getOKAction() };
    }
}
