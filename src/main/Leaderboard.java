package main;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Leaderboard {
    
    private List<GameRecord> records;
    private static final String SAVE_FILE = "leaderboard.dat";
    
    public Leaderboard() {
        records = new ArrayList<>();
        loadRecords();
    }
    
    // Add a new game record
    public void addRecord(String p1Name, String p2Name, int p1Score, int p2Score, int level, String winner) {
        records.add(new GameRecord(p1Name, p2Name, p1Score, p2Score, level, winner));
        // Keep only last 20 records
        if (records.size() > 20) {
            records.remove(0);
        }
        saveRecords();
    }
    
    // SORTING ALGORITHM 1: Bubble Sort by Winner Name
    public void bubbleSortByWinner() {
        int n = records.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (records.get(j).winner.compareTo(records.get(j + 1).winner) > 0) {
                    Collections.swap(records, j, j + 1);
                }
            }
        }
        saveRecords();
    }
    
    // SORTING ALGORITHM 2: Selection Sort by Total Score
    public void selectionSortByTotalScore() {
        int n = records.size();
        for (int i = 0; i < n - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (records.get(j).getTotalScore() > records.get(maxIdx).getTotalScore()) {
                    maxIdx = j;
                }
            }
            Collections.swap(records, i, maxIdx);
        }
        saveRecords();
    }
    
    // SORTING ALGORITHM 3: Insertion Sort by Date
    public void insertionSortByDate() {
        int n = records.size();
        for (int i = 1; i < n; i++) {
            GameRecord key = records.get(i);
            int j = i - 1;
            while (j >= 0 && records.get(j).date.compareTo(key.date) < 0) {
                records.set(j + 1, records.get(j));
                j--;
            }
            records.set(j + 1, key);
        }
        saveRecords();
    }
    
    // SORTING ALGORITHM 4: Quick Sort by Level
    public void quickSortByLevel() {
        quickSortRecursive(0, records.size() - 1);
        saveRecords();
    }
    
    private void quickSortRecursive(int low, int high) {
        if (low < high) {
            int pi = partition(low, high);
            quickSortRecursive(low, pi - 1);
            quickSortRecursive(pi + 1, high);
        }
    }
    
    private int partition(int low, int high) {
        int pivot = records.get(high).level;
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (records.get(j).level <= pivot) {
                i++;
                Collections.swap(records, i, j);
            }
        }
        Collections.swap(records, i + 1, high);
        return i + 1;
    }
    
    // SORTING ALGORITHM 5: Merge Sort by Winner's Score
    public void mergeSortByWinnerScore() {
        records = mergeSortRecursive(records);
        saveRecords();
    }
    
    private List<GameRecord> mergeSortRecursive(List<GameRecord> list) {
        if (list.size() <= 1) return list;
        
        int mid = list.size() / 2;
        List<GameRecord> left = mergeSortRecursive(new ArrayList<>(list.subList(0, mid)));
        List<GameRecord> right = mergeSortRecursive(new ArrayList<>(list.subList(mid, list.size())));
        
        return merge(left, right);
    }
    
    private List<GameRecord> merge(List<GameRecord> left, List<GameRecord> right) {
        List<GameRecord> result = new ArrayList<>();
        int i = 0, j = 0;
        
        while (i < left.size() && j < right.size()) {
            int leftScore = left.get(i).winner.equals(left.get(i).p1Name) ? left.get(i).p1Score : left.get(i).p2Score;
            int rightScore = right.get(j).winner.equals(right.get(j).p1Name) ? right.get(j).p1Score : right.get(j).p2Score;
            
            if (leftScore >= rightScore) {
                result.add(left.get(i));
                i++;
            } else {
                result.add(right.get(j));
                j++;
            }
        }
        
        while (i < left.size()) {
            result.add(left.get(i));
            i++;
        }
        while (j < right.size()) {
            result.add(right.get(j));
            j++;
        }
        return result;
    }
    
    // Save records to file
    private void saveRecords() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(records);
        } catch (IOException e) {
            System.err.println("Error saving leaderboard: " + e.getMessage());
        }
    }
    
    // Load records from file
    @SuppressWarnings("unchecked")
    private void loadRecords() {
        File file = new File(SAVE_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
                records = (List<GameRecord>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading leaderboard: " + e.getMessage());
                records = new ArrayList<>();
            }
        }
    }
    
    // Show leaderboard GUI
    public void showLeaderboard(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Leaderboard", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(parent);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Title
        JLabel title = new JLabel("Game Leaderboard", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(new EmptyBorder(20, 0, 20, 0));
        mainPanel.add(title, BorderLayout.NORTH);
        
        // Sort buttons panel
        JPanel sortPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        sortPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        sortPanel.setBackground(Color.WHITE);
        
        JButton bubbleBtn = createSortButton("Bubble Sort (Winner)", new Color(0xF23F3A));
        bubbleBtn.addActionListener(e -> {
            bubbleSortByWinner();
            refreshTable(dialog);
        });
        
        JButton selectionBtn = createSortButton("Selection Sort (Score)", new Color(0x00B487));
        selectionBtn.addActionListener(e -> {
            selectionSortByTotalScore();
            refreshTable(dialog);
        });
        
        JButton insertionBtn = createSortButton("Insertion Sort (Date)", new Color(0xFFC549));
        insertionBtn.addActionListener(e -> {
            insertionSortByDate();
            refreshTable(dialog);
        });
        
        JButton quickBtn = createSortButton("Quick Sort (Level)", new Color(0x775AFF));
        quickBtn.addActionListener(e -> {
            quickSortByLevel();
            refreshTable(dialog);
        });
        
        JButton mergeBtn = createSortButton("Merge Sort (Winner %)", new Color(0xFF76B4));
        mergeBtn.addActionListener(e -> {
            mergeSortByWinnerScore();
            refreshTable(dialog);
        });
        
        sortPanel.add(bubbleBtn);
        sortPanel.add(selectionBtn);
        sortPanel.add(insertionBtn);
        sortPanel.add(quickBtn);
        sortPanel.add(mergeBtn);
        
        mainPanel.add(sortPanel, BorderLayout.CENTER);
        
        // Table
        String[] columns = {"#", "Winner", "Player 1", "Score 1", "Player 2", "Score 2", "Level", "Date"};
        Object[][] data = prepareTableData();
        
        JTable table = new JTable(data, columns);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(10, 20, 20, 20));
        
        mainPanel.add(scrollPane, BorderLayout.SOUTH);
        
        // Close button
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        closeBtn.setBackground(new Color(0x444444));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.addActionListener(e -> dialog.dispose());
        
        JPanel closePanel = new JPanel();
        closePanel.setBackground(Color.WHITE);
        closePanel.add(closeBtn);
        mainPanel.add(closePanel, BorderLayout.AFTER_LAST_LINE);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void refreshTable(JDialog dialog) {
        // Refresh the dialog content
        dialog.dispose();
        showLeaderboard((JFrame) SwingUtilities.getWindowAncestor(dialog));
    }
    
    private Object[][] prepareTableData() {
        Object[][] data = new Object[records.size()][8];
        for (int i = 0; i < records.size(); i++) {
            GameRecord r = records.get(i);
            data[i][0] = i + 1;
            data[i][1] = r.winner;
            data[i][2] = r.p1Name;
            data[i][3] = r.p1Score + "%";
            data[i][4] = r.p2Name;
            data[i][5] = r.p2Score + "%";
            data[i][6] = "Level " + r.level;
            data[i][7] = r.date;
        }
        return data;
    }
    
    private JButton createSortButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    public List<GameRecord> getRecords() {
        return records;
    }
    
    // Game Record class
    static class GameRecord implements Serializable {
        private static final long serialVersionUID = 1L;
        String p1Name, p2Name, winner;
        int p1Score, p2Score, level;
        String date;
        
        GameRecord(String p1Name, String p2Name, int p1Score, int p2Score, int level, String winner) {
            this.p1Name = p1Name;
            this.p2Name = p2Name;
            this.p1Score = p1Score;
            this.p2Score = p2Score;
            this.level = level;
            this.winner = winner;
            this.date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        }
        
        int getTotalScore() {
            return p1Score + p2Score;
        }
    }
}