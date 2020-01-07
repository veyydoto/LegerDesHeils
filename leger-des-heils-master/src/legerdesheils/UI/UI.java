/**
 * Created by:
 * Adnan Akbas, 17005116
 * Bart Willems, 17098335
 * Joel Duurkoop, 17076021
 * Jari van Menxel, 17030072
 * Vedat Yilmaz, 17118700
 */
package legerdesheils.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.naming.InvalidNameException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;

import legerdesheils.BusinessRules.BusinessRule;
import legerdesheils.Helpers.SignalsCsvWriter;
import legerdesheils.Signal;
import legerdesheils.SignalManager;
import legerdesheils.User;

public class UI extends JFrame {

    private SignalManager          signalManager;
    private SignalTableModel       signalTableModel;
    private BusinessRuleTableModel businessRuleTableModel;
    private User                   user;

    private Font font;

    public UI(SignalManager signalManager, User user) {
        this.signalManager = signalManager;
        this.user = user;

        // settings of the frame
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 800));
        setTitle("Horizontaal Toezicht LdH");

        // create all components
        createComponents();
        createPanels();
        setFonts(new Font("monospace", Font.PLAIN, 18));

        // set icon
        String    filePath = "ldh.png";
        ImageIcon img      = new ImageIcon(filePath);
        setIconImage(img.getImage());

        // reset and setup 
        reset();
        disableAllButtons();
        btnSearchSignals.setEnabled(true);
    }

    private void createComponents() {
        // labels
        lblAccountName = new JLabel();
        lblEntityName = new JLabel();

        // BUSINESSRULE TABLE
        businessRuleTableModel = new BusinessRuleTableModel();
        tableBusinessRules = new JTable(businessRuleTableModel);
        tableBusinessRules.setAutoCreateRowSorter(true);

        // SIGNAL TABLE
        signalTableModel = new SignalTableModel();
        tableSignals = new JTable(signalTableModel);
        tableSignals.setAutoCreateRowSorter(true);

        // SIGNAL PANEL
        tableSignals.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                // get the row the mouse was clicked at
                int row = tableSignals.convertRowIndexToModel(tableSignals.rowAtPoint(evt.getPoint()));

                // get the signal that was selected
                ArrayList<Signal> signals        = signalTableModel.getSignals();
                Signal            selectedSignal = signals.get(row);
                try {
                    if (selectedSignal.getImpact().size() == 0) {
                        signalManager.addImpactToSignal(selectedSignal);
                    }

                    // create a table model for the impact
                    ImpactTableModel model = new ImpactTableModel(selectedSignal.getImpact());

                    // create table with font
                    JTable impactTable = new JTable(model);
                    impactTable.getTableHeader().setFont(font);
                    impactTable.setRowHeight((int) (1.5 * font.getSize()));

                    impactTable.setFont(font);

                    // generate title of impact screen
                    JPanel panel = new JPanel(new BorderLayout());
                    panel.add(new JScrollPane(impactTable), BorderLayout.CENTER);

                    panel.add(new JLabel(String.format("Signaal Aanwezig voor %.0f dag(en)", selectedSignal.getDaysActive())), BorderLayout.NORTH);

                    // show impact
                    String title = String.format("Signaal : [%s, %s, %s]", selectedSignal.getEntity(), selectedSignal.getAccount(), selectedSignal.getBusinessRule());
                    JOptionPane.showMessageDialog(null, panel, title, JOptionPane.PLAIN_MESSAGE);
                } catch (SQLException e) {
                    showErrorMessage("Fout bij het zoeken van impact voor signaal", e.getMessage());
                }
            }
        });
        
        /*
        SEARCH SIGNALS
        */
        btnSearchSignals = new JButton("Zoek signalen");
        btnSearchSignals.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                signalManager.reset();
                disableAllButtons();

                lblInfoText.setText("Bezig met het lezen van signalen...");
                try {
                    // search and get signals
                    signalManager.connectAll();

                    signalManager.searchSignals(user);

                    ArrayList<Signal>       signals       = signalManager.getSignals();
                    ArrayList<BusinessRule> businessRules = signalManager.getBusinessRules();

                    // create Business rule table model
                    businessRuleTableModel.setBusinessRules(businessRules);

                    // create Signal table model
                    signalTableModel.setSignals(signals);

                    // show text
                    lblInfoText.setText(String.format("Nieuw: %d, Onopgelost: %d, Net opgelost: %d.",
                            signalManager.getNewSignals().size(),
                            signalManager.getUnsolvedSignals().size(),
                            signalManager.getSolvedSignals().size()));

                } catch (SQLException ex) {
                    lblInfoText.setText(String.format("SQL Fout: %s", ex.getMessage()));
                } catch (ClassNotFoundException ex) {
                    lblInfoText.setText(ex.getMessage());
                } finally {
                    btnWriteSignals.setEnabled(true);
                    btnPrint.setEnabled(true);
                    btnReset.setEnabled(true);
                    // close connection
                    try {
                        signalManager.disconnectAll();
                        lblInfoText.setText(lblInfoText.getText() + " De verbinding is verbroken");
                    } catch (SQLException ex) {
                        lblInfoText.setText(lblInfoText.getText() + " Verbinding met server is al verbroken");
                    }
                }
            }
        });

        /*
        Resets program
        */
        btnReset = new JButton("Reset");
        btnReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                reset();
                disableAllButtons();
                btnSearchSignals.setEnabled(true);
                lblInfoText.setText("Gereset");
            }
        });

        btnWriteSignals = new JButton("Schrijf en Update signalen");
        btnWriteSignals.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                disableAllButtons();

                btnReset.setEnabled(true);
                btnPrint.setEnabled(true);
                try {
                    signalManager.connectAll();
                    // write signals
                    lblInfoText.setText("Schrijven van signalen...");
                    signalManager.writeSignals();
                    lblInfoText.setText("Schrijven is met succes voltooid");
                    signalManager.disconnectAll();
                } catch (SQLException ex) {
                    lblInfoText.setText(String.format("SQL Fout: %s", ex.getMessage()));
                } catch (ClassNotFoundException ex) {
                    lblInfoText.setText(ex.getMessage());
                } catch (Exception ex) {
                    lblInfoText.setText(ex.getMessage());
                }
            }
        });

        /*
        WRITE TO CSV
        */
        btnPrint = new JButton("Schrijf naar Csv");
        btnPrint.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // write to excel file
                JFileChooser saveDialog = new JFileChooser();
                String       filename   = "Signalen ".concat(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
                String       path       = "";

                saveDialog.setSelectedFile(new File(filename + ".csv"));

                int rVal = saveDialog.showSaveDialog(UI.this);

                // write to excel file
                if (rVal == JFileChooser.APPROVE_OPTION) {
                    filename = saveDialog.getSelectedFile().getName();
                    path = saveDialog.getCurrentDirectory().toString();
                    try {
                        lblInfoText.setText("Bezig met schrijven naar excel");
                        SignalsCsvWriter signalsCsvWriter = new SignalsCsvWriter(signalManager.getSignals(), filename, path);
                        signalsCsvWriter.writeCsvFile();
                        lblInfoText.setText("Signalen zijn geschreven naar csv op deze locatie: " + signalsCsvWriter.getFullPath());
                    } catch (InvalidNameException e) {
                        lblInfoText.setText("Incorrecte bestandsnaam. " + e.getMessage());
                    } catch (IOException e) {
                        lblInfoText.setText("Bestanden kunnen niet geschreven worden. Error message: " + e.getMessage());
                    }
                }
            }
        });

        /*
        SHOW INFO TEXT
        */
        lblInfoText = new JLabel();
        lblInfoText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showErrorMessage("Melding", lblInfoText.getText());
            }
        });
        scrollPane = new JScrollPane(tableSignals);

        // DETERMINE USER ACCOUNT
        String accountName;
        String entityName;

        accountName = user.getUsername();

        lblAccountName = new JLabel("Account: " + user.getUsername());
        StringBuilder workunit = new StringBuilder();
        for (String s : user.getWorkunit()) {
            workunit.append(s);
            workunit.append("\n");
        }
        entityName = workunit.toString();

        lblAccountName = new JLabel("Account: " + accountName);
        lblEntityName = new JLabel("Entiteit: " + entityName);

        /*
        WINDOW CLOSING
        */
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                try {
                    signalManager.disconnectAll();
                    lblInfoText.setText("Open connections closed...");
                } catch (SQLException ex) {
                    lblInfoText.setText("No open connections...");
                }
            }
        });

    }

    private void createPanels() {
        // BUTTON PANEL
        JPanel buttonPanel = new JPanel(new GridLayout());
        buttonPanel.add(btnSearchSignals);
        buttonPanel.add(btnReset);
        buttonPanel.add(btnWriteSignals);
        buttonPanel.add(btnPrint);


        // SIGNAL PANEL
        JPanel signalPanel = new JPanel(new BorderLayout());
        signalPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), "Signalen"));

        signalPanel.add(lblInfoText, BorderLayout.NORTH);
        signalPanel.add(scrollPane, BorderLayout.CENTER);
        signalPanel.add(buttonPanel, BorderLayout.SOUTH);
        signalPanel.setMinimumSize(new Dimension(150, 200));

        // DATA
        JPanel dataPanel = new JPanel(new GridLayout(2, 1));
        dataPanel.setMinimumSize(new Dimension(150, 100));
        dataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), "Data"));

        dataPanel.add(lblAccountName);
        dataPanel.add(lblEntityName);

        // BUSINESS RULES
        JPanel businessPanel = new JPanel(new GridLayout(1, 1));
        businessPanel.setMinimumSize(new Dimension(150, 200));
        businessPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), "Business Rules"));
        businessPanel.add(new JScrollPane(tableBusinessRules));

        // UPPER PANEL
        JPanel     upperPanel = new JPanel(new GridLayout());
        JSplitPane smallSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        upperPanel.add(smallSplit);
        smallSplit.setDividerLocation(350);
        smallSplit.setLeftComponent(dataPanel);
        smallSplit.setRightComponent(businessPanel);

        // MAIN PANEL
        JPanel     mainPanel = new JPanel(new GridLayout());
        JSplitPane bigSplit  = new JSplitPane();
        mainPanel.add(bigSplit);

        bigSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
        bigSplit.setDividerLocation(200);
        bigSplit.setTopComponent(upperPanel);
        bigSplit.setBottomComponent(signalPanel);

        add(mainPanel);
        pack();
    }

    private void setFonts(Font font) {
        this.font = font;
        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());

        // Buttons
        btnSearchSignals.setFont(boldFont);
        btnSearchSignals.setBackground(Color.WHITE);
        btnSearchSignals.setForeground(Color.RED);

        btnWriteSignals.setFont(boldFont);
        btnWriteSignals.setBackground(Color.WHITE);
        btnWriteSignals.setForeground(Color.RED);

        btnReset.setFont(boldFont);
        btnReset.setBackground(Color.WHITE);
        btnReset.setForeground(Color.RED);

        btnPrint.setFont(boldFont);
        btnPrint.setBackground(Color.WHITE);
        btnPrint.setForeground(Color.RED);

        // Table signals
        tableSignals.setFont(font);
        tableSignals.getTableHeader().setFont(boldFont);
        tableSignals.setRowHeight((int) (1.5 * font.getSize()));
        tableSignals.setBackground(Color.WHITE);

        // Table businessrules
        tableBusinessRules.setFont(font);
        tableBusinessRules.getTableHeader().setFont(boldFont);
        tableBusinessRules.setRowHeight((int) (1.5 * font.getSize()));
        tableBusinessRules.setBackground(Color.WHITE);

        // Labels
        lblAccountName.setFont(font);
        lblEntityName.setFont(font);
        lblInfoText.setFont(font);
    }

    /*
    SHOW ERROR MESSAGE WITH SCROLLABLE SCREEN
    */
    private void showErrorMessage(String title, String message) {
        JTextPane text = new JTextPane();
        text.setText(message);
        text.setFont(font);
        text.setPreferredSize(new Dimension(500, 200));
        text.setEditable(false);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(text, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this,
                new JScrollPane(text),
                title,
                JOptionPane.PLAIN_MESSAGE);
    }

    private void reset() {
        // reset signal manager
        signalManager.reset();
        signalTableModel.setSignals(signalManager.getSignals());
        businessRuleTableModel.setBusinessRules(signalManager.getBusinessRules());
    }

    private void disableAllButtons() {
        btnSearchSignals.setEnabled(false);
        btnPrint.setEnabled(false);
        btnWriteSignals.setEnabled(false);
        btnReset.setEnabled(false);
    }

    private JButton btnSearchSignals;
    private JButton btnWriteSignals;
    private JButton btnReset;
    private JButton btnPrint;

    private JTable tableSignals;
    private JTable tableBusinessRules;
    private JLabel lblInfoText;

    private JLabel lblAccountName;
    private JLabel lblEntityName;

    private JScrollPane scrollPane;
}