package edu.univ.erp.ui.instructor;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GradeStatsDialog extends JDialog {

    public GradeStatsDialog(Window owner, List<Double> scores) {
        super(owner, "Class Statistics", ModalityType.APPLICATION_MODAL);
        setSize(600, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        if (scores == null || scores.isEmpty()) {
            add(new JLabel("No data available.", SwingConstants.CENTER), BorderLayout.CENTER);
            return;
        }

        double mean = calculateMean(scores);
        double median = calculateMedian(scores);
        String mode = calculateMode(scores);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        statsPanel.add(createStatCard("Average", String.format("%.2f%%", mean), new Color(0, 120, 215)));
        statsPanel.add(createStatCard("Median", String.format("%.2f%%", median), new Color(0, 150, 100)));
        statsPanel.add(createStatCard("Mode", mode, new Color(200, 80, 0)));

        add(statsPanel, BorderLayout.NORTH);

        HistogramPanel graphPanel = new HistogramPanel(scores);
        graphPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        add(graphPanel, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel createStatCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(Color.WHITE);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLbl.setForeground(Color.GRAY);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLbl.setForeground(accent);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        return card;
    }

    private double calculateMean(List<Double> data) {
        return data.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private double calculateMedian(List<Double> data) {
        List<Double> sorted = new ArrayList<>(data);
        Collections.sort(sorted);
        int size = sorted.size();
        if (size == 0) return 0;
        if (size % 2 == 1) return sorted.get(size / 2);
        return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
    }

    private String calculateMode(List<Double> data) {
        Map<Integer, Integer> frequency = new HashMap<>();
        for (Double d : data) {
            int rounded = (int) Math.round(d);
            frequency.put(rounded, frequency.getOrDefault(rounded, 0) + 1);
        }

        int maxFreq = 0;
        List<Integer> modes = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : frequency.entrySet()) {
            if (entry.getValue() > maxFreq) {
                maxFreq = entry.getValue();
                modes.clear();
                modes.add(entry.getKey());
            } else if (entry.getValue() == maxFreq) {
                modes.add(entry.getKey());
            }
        }

        if (modes.isEmpty() || maxFreq == 1) return "N/A";
        return modes.stream().sorted().map(String::valueOf).collect(Collectors.joining(", "));
    }

    private static class HistogramPanel extends JPanel {
        private final int[] bins = new int[10];
        private int maxFreq = 0;

        public HistogramPanel(List<Double> scores) {
            setBackground(Color.WHITE);
            for (Double s : scores) {
                int val = (int) Math.round(s);
                if (val < 0) val = 0;
                if (val > 100) val = 100;

                int bucket = (val == 100) ? 9 : val / 10;
                bins[bucket]++;
            }
            for (int f : bins) maxFreq = Math.max(maxFreq, f);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int padding = 30;
            int graphH = h - 2 * padding;
            int barWidth = (w - 2 * padding) / 10;

            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(padding, h - padding, w - padding, h - padding);

            for (int i = 0; i < 10; i++) {
                int count = bins[i];
                if (count == 0) continue;

                int barHeight = (int) ((double) count / maxFreq * (graphH - 20));
                int x = padding + i * barWidth + 5;
                int y = h - padding - barHeight;

                g2.setColor(new Color(100, 149, 237));
                g2.fillRoundRect(x, y, barWidth - 10, barHeight, 5, 5);

                g2.setColor(Color.DARK_GRAY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String label = String.valueOf(count);
                int labelW = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, x + (barWidth - 10 - labelW) / 2, y - 5);

                g2.setColor(Color.GRAY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String range = (i * 10) + "";
                g2.drawString(range, x, h - padding + 15);
            }
            g2.drawString("100", w - padding - 15, h - padding + 15);
        }
    }
}