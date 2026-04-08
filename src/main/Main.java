package main;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("InkFlag");

        MainMenu mainMenu = new MainMenu();

        window.add(mainMenu);
        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        //gamePanel.startGameThread();
    }
}