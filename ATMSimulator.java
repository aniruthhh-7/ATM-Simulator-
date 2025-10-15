import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class ATMSimulator extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private JTextField pinField;
    private JLabel balanceLabel, messageLabel;
    private JTextArea statementArea;

    private double balance = 0.0;
    private String pin = "1234"; // default PIN
    private final String FILE_NAME = "account.txt";

    public ATMSimulator() {
        setTitle("ATM Simulator");
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // ==== Load Balance from File ====
        loadAccount();

        // ==== Create Screens ====
        mainPanel.add(loginPanel(), "login");
        mainPanel.add(menuPanel(), "menu");
        mainPanel.add(depositPanel(), "deposit");
        mainPanel.add(withdrawPanel(), "withdraw");
        mainPanel.add(statementPanel(), "statement");

        add(mainPanel, BorderLayout.CENTER);

        setVisible(true);
        cardLayout.show(mainPanel, "login");
    }

    private JPanel loginPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel label = new JLabel("Enter PIN:", SwingConstants.CENTER);
        pinField = new JPasswordField();
        JButton loginBtn = new JButton("Login");

        loginBtn.addActionListener(e -> {
            if (pinField.getText().equals(pin)) {
                cardLayout.show(mainPanel, "menu");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid PIN!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(label);
        panel.add(pinField);
        panel.add(loginBtn);
        return panel;
    }

    private JPanel menuPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 80, 30, 80));

        balanceLabel = new JLabel("Balance: ₹" + balance, SwingConstants.CENTER);

        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton statementBtn = new JButton("View Statement");
        JButton logoutBtn = new JButton("Logout");

        depositBtn.addActionListener(e -> cardLayout.show(mainPanel, "deposit"));
        withdrawBtn.addActionListener(e -> cardLayout.show(mainPanel, "withdraw"));
        statementBtn.addActionListener(e -> {
            loadStatement();
            cardLayout.show(mainPanel, "statement");
        });
        logoutBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        panel.add(balanceLabel);
        panel.add(depositBtn);
        panel.add(withdrawBtn);
        panel.add(statementBtn);
        panel.add(logoutBtn);

        return panel;
    }

    private JPanel depositPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        JTextField amountField = new JTextField();
        JButton depositBtn = new JButton("Deposit");
        JButton backBtn = new JButton("Back");
        messageLabel = new JLabel("", SwingConstants.CENTER);

        depositBtn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (amount <= 0) throw new Exception();
                balance += amount;
                saveTransaction("Deposited ₹" + amount);
                updateBalance();
                messageLabel.setText("✅ Deposited Successfully!");
                amountField.setText("");
            } catch (Exception ex) {
                messageLabel.setText("⚠️ Enter a valid amount!");
            }
        });

        backBtn.addActionListener(e -> {
            messageLabel.setText("");
            cardLayout.show(mainPanel, "menu");
        });

        panel.add(new JLabel("Enter Amount to Deposit:", SwingConstants.CENTER));
        panel.add(amountField);
        panel.add(depositBtn);
        panel.add(backBtn);
        panel.add(messageLabel);

        return panel;
    }

    private JPanel withdrawPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        JTextField amountField = new JTextField();
        JButton withdrawBtn = new JButton("Withdraw");
        JButton backBtn = new JButton("Back");
        JLabel msg = new JLabel("", SwingConstants.CENTER);

        withdrawBtn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (amount <= 0 || amount > balance) {
                    msg.setText("⚠️ Invalid or Insufficient Balance!");
                } else {
                    balance -= amount;
                    saveTransaction("Withdrew ₹" + amount);
                    updateBalance();
                    msg.setText("💰 Withdrawn Successfully!");
                    amountField.setText("");
                }
            } catch (Exception ex) {
                msg.setText("⚠️ Enter a valid amount!");
            }
        });

        backBtn.addActionListener(e -> {
            msg.setText("");
            cardLayout.show(mainPanel, "menu");
        });

        panel.add(new JLabel("Enter Amount to Withdraw:", SwingConstants.CENTER));
        panel.add(amountField);
        panel.add(withdrawBtn);
        panel.add(backBtn);
        panel.add(msg);

        return panel;
    }

    private JPanel statementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        statementArea = new JTextArea();
        statementArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(statementArea);

        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "menu"));

        panel.add(new JLabel("Mini Statement", SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(backBtn, BorderLayout.SOUTH);

        return panel;
    }

    private void updateBalance() {
        balanceLabel.setText("Balance: ₹" + balance);
        saveAccount();
    }

    private void saveTransaction(String text) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("transactions.txt", true))) {
            pw.println(new Date() + " - " + text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadStatement() {
        try (BufferedReader br = new BufferedReader(new FileReader("transactions.txt"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            statementArea.setText(sb.toString());
        } catch (IOException e) {
            statementArea.setText("No transactions found.");
        }
    }

    private void saveAccount() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            pw.println(pin);
            pw.println(balance);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAccount() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            pin = br.readLine();
            balance = Double.parseDouble(br.readLine());
        } catch (IOException e) {
            pin = "1234";
            balance = 1000.0; // default balance
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ATMSimulator::new);
    }
}
