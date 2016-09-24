/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/**
 *
 * @author ast
 */
public class MainWindow extends javax.swing.JFrame {

    
    private Timer timer = new Timer(1000, new ActionListener() {
        
        int lastProgress=-1;
        
        @Override
        public void actionPerformed(ActionEvent evt) 
        {
            int progress = GlobalResources.mappingProject.getProcessingProgressPercent();
            if(progress>0 && progress!=lastProgress)
            {
                if(GlobalResources.mappingProject!=null) GlobalResources.statusWindow.setProgress(progress);
                GlobalResources.statusWindow.update(GlobalResources.statusWindow.getGraphics());
                lastProgress=progress;
            }
        }    
});
    
    private DrawableData drawingPaneShows = null;
    protected MonteCarloRanksDialog mcDialog=new MonteCarloRanksDialog(this,true);
   
    /**
     * Creates new form MainWindow
     */
    public MainWindow() 
    {
        
        initComponents();
        
        ButtonGroup group = new ButtonGroup();
        group.add(this.radioButtonProcessedLayer);
        group.add(this.radioButtonRawLayer);
        
        //register as static variable
        GlobalResources.mainWindow=this;
        
        //hide non-released functions
        if(GlobalResources.releaseVersion)
        {
            this.menuItemExportLowerRes.setVisible(false);
            this.menuItemAois.setVisible(false);
            this.menuItemExportPolyAreas.setVisible(false);
            this.menuItemFreeMem.setVisible(false);
            this.menuItemRunCurrentTest.setVisible(false);
            this.menuDevelopment.setVisible(false);
        }
        //find folder that executable resides in
        File settingsFile;
        File logFile;
        final CodeSource codeSource = MainWindow.class.getProtectionDomain().getCodeSource();
        GlobalResources.statusWindow=new StatusWindow(this,true);
        try 
        {
            File exFile = new File(codeSource.getLocation().toURI().getPath());
            String exDir = exFile.getParentFile().getPath();
            // if logging directory doesnt exist, create it
            File dir = new File(exDir,"Logs");
            if (!dir.exists()) 
            {        
                dir.mkdir();
            }
            logFile = new File(dir,"log"+GlobalResources.getDateTime()+".txt");
            GlobalResources.statusWindow.setLogFile(logFile);
            
            
            //try to load settings (currently: only last used folder)
            settingsFile = new File(exDir,"settings.csv");
            if(settingsFile.exists())
            {
                CsvTableGeneral settings = new CsvTableGeneral();
                settings.readFromFile(settingsFile);
                ArrayList<String> settingNames = settings.getColumn("setting");
                ArrayList<String> settingValues = settings.getColumn("value");
                for(int i=0; i<settingNames.size();i++)
                {
                    if(settingNames.get(i).equals("lastfolder"))
                    {
                        File folder = new File(settingValues.get(i));
                        if(folder.exists() && folder.isDirectory())
                        {
                            GlobalResources.lastUsedFolder=folder.getAbsolutePath();
                        }
                    }
                    else if(settingNames.get(i).equals("lpfdistance"))
                    {
                        try
                        {
                            Float nr = Float.parseFloat(settingValues.get(i));
                            if(nr<0) throw new Exception();
                            GlobalResources.lowPassFilterDistance=nr;
                        }
                        catch(Exception e)
                        {
                            JOptionPane.showMessageDialog(null, "Failed to load low pass filter distance from settings file.");
                        }
                    }
                    else if(settingNames.get(i).equals("threads"))
                    {
                        try
                        {
                            int nr = Integer.parseInt(settingValues.get(i));
                            if(nr<1 || nr>32) throw new Exception();
                            GlobalResources.nrOfThreads=nr;
                        }
                        catch(Exception e)
                        {
                            JOptionPane.showMessageDialog(null, "Failed to load low pass filter distance from settings file.");
                        }
                    }
                }
            }
            
            
        } 
        catch (URISyntaxException ex) 
        {
           JOptionPane.showMessageDialog(null, "Exception while attempting to read program settings and setting up log file.");
        }
        
        
        
        GlobalResources.statusWindow.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        GlobalResources.mappingProject.initializeProcessors();


        
        //make sure user settings (at the moment only last used folder) are saved when window closes
        this.addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent e) 
            {
                File exFile;
                try 
                {
                    exFile = new File(codeSource.getLocation().toURI().getPath());
                    String exDir = exFile.getParentFile().getPath();
                    CsvTableGeneral settings=new CsvTableGeneral();
                    settings.addColumn("setting");
                    settings.addColumn("value");
                    ArrayList<String> line = new ArrayList<>();
                    line.add("lastfolder");
                    line.add(GlobalResources.lastUsedFolder);
                    settings.addRow(line);
                    line = new ArrayList<>();
                    line.add("lpfdistance");
                    line.add(GlobalResources.lowPassFilterDistance+"");
                    settings.addRow(line);
                    line = new ArrayList<>();
                    line.add("threads");
                    line.add(GlobalResources.nrOfThreads+"");
                    settings.addRow(line);
                    settings.writeToFile(new File(exDir,"settings.csv").getAbsolutePath());   
                }
                catch(Exception ex)
                {
                    JOptionPane.showMessageDialog(null, "Could not save settings.");
                }
                finally
                {
                    GlobalResources.statusWindow.closeLogWriter();
                }
            }
        } );


        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    //@SuppressWarnings("unchecked");
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        listEcocomps = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        listStressors = new javax.swing.JList();
        labelStressors = new javax.swing.JLabel();
        labelEcologicalComponents = new javax.swing.JLabel();
        drawingPane = new javax.swing.JPanel();
        buttonStressorsPlus = new javax.swing.JButton();
        buttonStressorsMinus = new javax.swing.JButton();
        buttonEcoPlus = new javax.swing.JButton();
        buttonEcoMinus = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        listOtherData = new javax.swing.JList();
        radioButtonRawLayer = new javax.swing.JRadioButton();
        radioButtonProcessedLayer = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        buttonResultsMinus = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        menuBarMain = new javax.swing.JMenuBar();
        menuProject = new javax.swing.JMenu();
        menuNew = new javax.swing.JMenuItem();
        menuSave = new javax.swing.JMenuItem();
        menuLoad = new javax.swing.JMenuItem();
        menuItemExportLowerRes = new javax.swing.JMenuItem();
        menuAssignSelectiveFactors = new javax.swing.JMenu();
        menuSensitivityscores = new javax.swing.JMenuItem();
        menuPreprocessing = new javax.swing.JMenuItem();
        menuItemExportLayer = new javax.swing.JMenuItem();
        menuItemExportPolyAreas = new javax.swing.JMenuItem();
        menuItemLoadRegions = new javax.swing.JMenuItem();
        menuItemAois = new javax.swing.JMenuItem();
        MenuView = new javax.swing.JMenu();
        jMenu5 = new javax.swing.JMenu();
        radioButtonMenuItemLinearStretch = new javax.swing.JRadioButtonMenuItem();
        radioButtonMenuItemQuantileStretch = new javax.swing.JRadioButtonMenuItem();
        menuColorScale = new javax.swing.JMenuItem();
        menuDiversityIndexAvg = new javax.swing.JMenu();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        menuItemSensitivityIndex = new javax.swing.JMenuItem();
        menuStressorIndex = new javax.swing.JMenuItem();
        menuWeightedStressorIndex = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        menuImpactIndex = new javax.swing.JMenuItem();
        menuImpactIndex1 = new javax.swing.JMenuItem();
        menuItemImpactIndexDominantSum = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        menuItemDiminishingImpactsSum = new javax.swing.JMenuItem();
        menuItemDiminishingImpactMean = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        menuItemAreaPlots = new javax.swing.JMenuItem();
        menuUncertainty = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        menuItemMonteCarloRanks = new javax.swing.JMenuItem();
        menuItemMorris = new javax.swing.JMenuItem();
        menuItemFreeMem = new javax.swing.JMenuItem();
        menuDevelopment = new javax.swing.JMenu();
        menuItemRunCurrentTest = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("EcoImpactMapper");
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                ResizedHandler(evt);
            }
        });

        listEcocomps.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listEcocompsMouseClicked(evt);
            }
        });
        listEcocomps.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listEcocompsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(listEcocomps);

        listStressors.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listStressorsMouseClicked(evt);
            }
        });
        listStressors.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listStressorsValueChanged(evt);
            }
        });
        listStressors.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                StressorListChanged(evt);
            }
        });
        jScrollPane2.setViewportView(listStressors);

        labelStressors.setText("Stressors");

        labelEcologicalComponents.setText("Ecosystem components");

        drawingPane.setBackground(new java.awt.Color(204, 204, 204));
        drawingPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        drawingPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                drawingPaneComponentResized(evt);
            }
        });

        org.jdesktop.layout.GroupLayout drawingPaneLayout = new org.jdesktop.layout.GroupLayout(drawingPane);
        drawingPane.setLayout(drawingPaneLayout);
        drawingPaneLayout.setHorizontalGroup(
            drawingPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );
        drawingPaneLayout.setVerticalGroup(
            drawingPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        buttonStressorsPlus.setText("+");
        buttonStressorsPlus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonStressorsPlusActionPerformed(evt);
            }
        });

        buttonStressorsMinus.setText("-");
        buttonStressorsMinus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonStressorsMinusActionPerformed(evt);
            }
        });

        buttonEcoPlus.setText("+");
        buttonEcoPlus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEcoPlusActionPerformed(evt);
            }
        });

        buttonEcoMinus.setText("-");
        buttonEcoMinus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEcoMinusActionPerformed(evt);
            }
        });

        jLabel2.setText("Other spatial data");

        listOtherData.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listOtherDataMouseClicked(evt);
            }
        });
        listOtherData.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listOtherDataValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(listOtherData);

        radioButtonRawLayer.setSelected(true);
        radioButtonRawLayer.setText("Selected data (raw)");
        radioButtonRawLayer.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                radioButtonRawLayerStateChanged(evt);
            }
        });
        radioButtonRawLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonRawLayerActionPerformed(evt);
            }
        });

        radioButtonProcessedLayer.setText("Selected data (processed)");
        radioButtonProcessedLayer.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                radioButtonProcessedLayerStateChanged(evt);
            }
        });
        radioButtonProcessedLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonProcessedLayerActionPerformed(evt);
            }
        });

        jLabel1.setText("Display content");

        buttonResultsMinus.setText("-");
        buttonResultsMinus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonResultsMinusActionPerformed(evt);
            }
        });

        menuProject.setText("Project");

        menuNew.setText("New");
        menuNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuNewActionPerformed(evt);
            }
        });
        menuProject.add(menuNew);

        menuSave.setText("Save...");
        menuSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSaveActionPerformed(evt);
            }
        });
        menuProject.add(menuSave);

        menuLoad.setText("Load...");
        menuLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuLoadActionPerformed(evt);
            }
        });
        menuProject.add(menuLoad);

        menuItemExportLowerRes.setText("** Export data with lower resolution...");
        menuItemExportLowerRes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemExportLowerResActionPerformed(evt);
            }
        });
        menuProject.add(menuItemExportLowerRes);

        menuBarMain.add(menuProject);

        menuAssignSelectiveFactors.setText("Processing & data");

        menuSensitivityscores.setText("Load sensitivity weights...");
        menuSensitivityscores.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSensitivityscoresActionPerformed(evt);
            }
        });
        menuAssignSelectiveFactors.add(menuSensitivityscores);

        menuPreprocessing.setText("Pre-processing...");
        menuPreprocessing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuPreprocessingActionPerformed(evt);
            }
        });
        menuAssignSelectiveFactors.add(menuPreprocessing);

        menuItemExportLayer.setText("Export displayed layer...");
        menuItemExportLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemExportLayerActionPerformed(evt);
            }
        });
        menuAssignSelectiveFactors.add(menuItemExportLayer);

        menuItemExportPolyAreas.setText("** Extract & export polygon areas...");
        menuItemExportPolyAreas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemExportPolyAreasActionPerformed(evt);
            }
        });
        menuAssignSelectiveFactors.add(menuItemExportPolyAreas);

        menuItemLoadRegions.setText("Load regions...");
        menuItemLoadRegions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemLoadRegionsActionPerformed(evt);
            }
        });
        menuAssignSelectiveFactors.add(menuItemLoadRegions);

        menuItemAois.setText("** Load areas of interest...");
        menuItemAois.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemAoisActionPerformed(evt);
            }
        });
        menuAssignSelectiveFactors.add(menuItemAois);

        menuBarMain.add(menuAssignSelectiveFactors);

        MenuView.setText("View");

        jMenu5.setText("Color stretch");

        radioButtonMenuItemLinearStretch.setSelected(true);
        radioButtonMenuItemLinearStretch.setText("Linear strecth");
        radioButtonMenuItemLinearStretch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonMenuItemLinearStretchActionPerformed(evt);
            }
        });
        jMenu5.add(radioButtonMenuItemLinearStretch);

        radioButtonMenuItemQuantileStretch.setText("Quantile (CDF) stretch");
        radioButtonMenuItemQuantileStretch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonMenuItemQuantileStretchActionPerformed(evt);
            }
        });
        jMenu5.add(radioButtonMenuItemQuantileStretch);

        MenuView.add(jMenu5);

        menuColorScale.setText("Color scale....");
        menuColorScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuColorScaleActionPerformed(evt);
            }
        });
        MenuView.add(menuColorScale);

        menuBarMain.add(MenuView);

        menuDiversityIndexAvg.setText("Analysis");

        jMenu1.setText("Indices");

        jMenuItem1.setText("Ecological diversity index...");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        menuItemSensitivityIndex.setText("Ecological sensitivity index...");
        menuItemSensitivityIndex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSensitivityIndexActionPerformed(evt);
            }
        });
        jMenu1.add(menuItemSensitivityIndex);

        menuStressorIndex.setText("Stressor index (unweighted)...");
        menuStressorIndex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuStressorIndexActionPerformed(evt);
            }
        });
        jMenu1.add(menuStressorIndex);

        menuWeightedStressorIndex.setText("Stressor index (weighted)");
        menuWeightedStressorIndex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuWeightedStressorIndexActionPerformed(evt);
            }
        });
        jMenu1.add(menuWeightedStressorIndex);

        jMenu3.setText("Impact index...");

        menuImpactIndex.setText("Additive model, sum");
        menuImpactIndex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuImpactIndexActionPerformed(evt);
            }
        });
        jMenu3.add(menuImpactIndex);

        menuImpactIndex1.setText("Additive model, mean");
        menuImpactIndex1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuImpactIndex1ActionPerformed(evt);
            }
        });
        jMenu3.add(menuImpactIndex1);

        menuItemImpactIndexDominantSum.setText("Dominant stressor model, sum");
        menuItemImpactIndexDominantSum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemImpactIndexDominantSumActionPerformed(evt);
            }
        });
        jMenu3.add(menuItemImpactIndexDominantSum);

        jMenuItem2.setText("Dominant stressor model, mean");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem2);

        menuItemDiminishingImpactsSum.setText("Antagonistic impact model, sum");
        menuItemDiminishingImpactsSum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemDiminishingImpactsSumActionPerformed(evt);
            }
        });
        jMenu3.add(menuItemDiminishingImpactsSum);

        menuItemDiminishingImpactMean.setText("Antagonistic impact model, mean");
        menuItemDiminishingImpactMean.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemDiminishingImpactMeanActionPerformed(evt);
            }
        });
        jMenu3.add(menuItemDiminishingImpactMean);

        jMenu1.add(jMenu3);

        menuDiversityIndexAvg.add(jMenu1);

        jMenu2.setText("Stressors");

        menuItemAreaPlots.setText("Overlay with diversity index...");
        menuItemAreaPlots.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemAreaPlotsActionPerformed(evt);
            }
        });
        jMenu2.add(menuItemAreaPlots);

        menuDiversityIndexAvg.add(jMenu2);

        menuUncertainty.setText("Uncertainty");

        jMenuItem5.setText("Assign selective factors...");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        menuUncertainty.add(jMenuItem5);

        menuItemMonteCarloRanks.setText("Monte Carlo with random sampling");
        menuItemMonteCarloRanks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemMonteCarloRanksActionPerformed(evt);
            }
        });
        menuUncertainty.add(menuItemMonteCarloRanks);

        menuItemMorris.setText("Elementary effects with Morris design");
        menuItemMorris.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemMorrisActionPerformed(evt);
            }
        });
        menuUncertainty.add(menuItemMorris);

        menuDiversityIndexAvg.add(menuUncertainty);

        menuItemFreeMem.setText("** Free memory");
        menuItemFreeMem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemFreeMemActionPerformed(evt);
            }
        });
        menuDiversityIndexAvg.add(menuItemFreeMem);

        menuBarMain.add(menuDiversityIndexAvg);

        menuDevelopment.setText("** Development");

        menuItemRunCurrentTest.setText("Run current test");
        menuItemRunCurrentTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemRunCurrentTestActionPerformed(evt);
            }
        });
        menuDevelopment.add(menuItemRunCurrentTest);

        menuBarMain.add(menuDevelopment);

        setJMenuBar(menuBarMain);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(21, 21, 21)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(labelStressors)
                            .add(labelEcologicalComponents)
                            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 269, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel2)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 269, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(buttonStressorsPlus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 44, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(buttonStressorsMinus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 44, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(buttonEcoPlus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 44, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(buttonEcoMinus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 44, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 269, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(buttonResultsMinus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 44, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(drawingPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)
                        .add(249, 249, 249))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(layout.createSequentialGroup()
                                .add(12, 12, 12)
                                .add(radioButtonRawLayer)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(radioButtonProcessedLayer)))
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(5, 5, 5)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(drawingPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(radioButtonRawLayer)
                            .add(radioButtonProcessedLayer))
                        .add(8, 8, 8))
                    .add(layout.createSequentialGroup()
                        .add(labelStressors)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(labelEcologicalComponents)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                            .add(layout.createSequentialGroup()
                                .add(buttonStressorsPlus)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(buttonStressorsMinus)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(buttonEcoPlus)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(buttonEcoMinus)
                                .add(120, 120, 120)))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(buttonResultsMinus)
                            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Shows new project window
     * @param evt 
     */
    private void menuNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuNewActionPerformed
       
        //clear data
        GlobalResources.mappingProject.reset();
        //clear graphics
        drawingPane.getGraphics().setColor(Color.BLACK);
        drawingPane.getGraphics().fillRect(0, 0, drawingPane.getWidth(), drawingPane.getHeight());
        //drawingPane.update(null);
        //remove data layer names from lists
        String[] ecocompNames=GlobalResources.mappingProject.getEcocompNames();
        DefaultListModel model = new DefaultListModel();
        for(int i=0; i<ecocompNames.length; i++) {model.addElement(ecocompNames[i]);}
        this.listEcocomps.setModel(model);
        String[] stressorNames=GlobalResources.mappingProject.getStressorNames();
        model = new DefaultListModel();
        for(int i=0; i<stressorNames.length; i++) {model.addElement(stressorNames[i]);}
        this.listStressors.setModel(model);
        String[] resultNames=GlobalResources.mappingProject.getResultNames();
        model = new DefaultListModel();
        for(int i=0; i<resultNames.length; i++) {model.addElement(resultNames[i]);}
        this.listOtherData.setModel(model);
        
        GlobalResources.statusWindow.println("Started new project.");
      
       
    }//GEN-LAST:event_menuNewActionPerformed

    private void updateGraphics()
    {
        if(GlobalResources.mappingProject.grid!=null)
       {
           if(this.drawingPaneShows!=null)
           {
               ImageCreator creator = new ImageCreator(drawingPane.getWidth(), drawingPane.getHeight());
               BufferedImage image;
               if(this.radioButtonRawLayer.isSelected())
               {
                   image = drawingPaneShows.getImage(creator, false);
                   
               }
               else if(this.radioButtonProcessedLayer.isSelected())
               {
                   image = drawingPaneShows.getImage(creator, true);
               }
               else //something went wrong
               {
                   float[][] hasData=GlobalResources.mappingProject.grid.isFilled();
                   image = creator.createStudyAreaImage(hasData, GlobalResources.mappingProject.grid.getDimensions());
               } 
                drawingPane.getGraphics().drawImage(image, 0, 0, this);
                
               
           }
           else
           {
                float[][] hasData=GlobalResources.mappingProject.grid.isFilled();
                ImageCreator creator = new ImageCreator(this.drawingPane.getWidth(),this.drawingPane.getHeight());
                BufferedImage image = creator.createStudyAreaImage(hasData, GlobalResources.mappingProject.grid.getDimensions());
                drawingPane.getGraphics().drawImage(image, 0, 0, this);
           }    
           
       }
        
    }
    
    private void ResizedHandler(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_ResizedHandler
     // updateGraphics(); NEVER CALLED
    }//GEN-LAST:event_ResizedHandler

    private void buttonStressorsPlusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonStressorsPlusActionPerformed
        
        //show load data dialog
        LoadDataForm dialog = new LoadDataForm(this,true);
        dialog.setDataType(GlobalResources.DATATYPE_STRESSOR);
        dialog.setVisible(true);
        
        //update stressor list
        String[] stressorNames=GlobalResources.mappingProject.getStressorNames();
        DefaultListModel model = new DefaultListModel();
        for(int i=0; i<stressorNames.length; i++) {model.addElement(stressorNames[i]);}
        this.listStressors.setModel(model);
        //this.update(null);
        
        //update graphics to show the latest stressor
        SpatialDataLayer newStressor = GlobalResources.mappingProject.getLastDataAdded();
        if(newStressor!=null && newStressor.getType()==GlobalResources.DATATYPE_STRESSOR)
        {
            drawingPaneShows=newStressor;
            updateGraphics();
            GlobalResources.statusWindow.println("Loaded stressor layer: "+newStressor.getSource().toString());
        }
    }//GEN-LAST:event_buttonStressorsPlusActionPerformed

    private void StressorListChanged(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_StressorListChanged
       
        //doesn't work - leave empty
       
           
    }//GEN-LAST:event_StressorListChanged

    private void listStressorsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listStressorsValueChanged
        
           String stressorName = (String) this.listStressors.getSelectedValue();
           SpatialDataLayer stressor=GlobalResources.mappingProject.getStressorByName(stressorName);
           if(stressor!=null)
           {
              listEcocomps.clearSelection();
              listOtherData.clearSelection();
              drawingPaneShows = stressor;
              this.updateGraphics();  
       }
        
    }//GEN-LAST:event_listStressorsValueChanged

    private void buttonStressorsMinusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonStressorsMinusActionPerformed
        String stressorName = (String) this.listStressors.getSelectedValue();
        if(stressorName==null || stressorName.equals(new String("null"))) return;
        GlobalResources.mappingProject.removeStressorByName(stressorName);
        GlobalResources.statusWindow.println("Removed stressor: "+stressorName);
        
        //update stressor list
         String[] stressorNames=GlobalResources.mappingProject.getStressorNames();
        DefaultListModel model = new DefaultListModel();
        for(int i=0; i<stressorNames.length; i++) {model.addElement(stressorNames[i]);}
        this.listStressors.setModel(model);
        
        //update graphics (stop showing removed stressor)
        drawingPaneShows = null;
        updateGraphics();

    }//GEN-LAST:event_buttonStressorsMinusActionPerformed

    private void buttonEcoPlusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEcoPlusActionPerformed
        //show load data dialog
        LoadDataForm dialog = new LoadDataForm(this,true);
        dialog.setDataType(GlobalResources.DATATYPE_ECOCOMP);
        dialog.setVisible(true);
        
        //update ecocomp list
        String[] ecocompNames=GlobalResources.mappingProject.getEcocompNames();
        DefaultListModel model = new DefaultListModel();
        for(int i=0; i<ecocompNames.length; i++) {model.addElement(ecocompNames[i]);}
        this.listEcocomps.setModel(model);
        //this.update(null);
        
        //update graphics to show the latest stressor
        SpatialDataLayer newEcocomp = GlobalResources.mappingProject.getLastDataAdded();
        if(newEcocomp!=null && newEcocomp.getType()==GlobalResources.DATATYPE_ECOCOMP)
        {
            drawingPaneShows = newEcocomp;  //so that it doesn't get lost on resize
            updateGraphics();
            GlobalResources.statusWindow.println("Loaded ecocystem component layer: "+newEcocomp.getSource().toString());
        }
    }//GEN-LAST:event_buttonEcoPlusActionPerformed

    private void listEcocompsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listEcocompsValueChanged
         String ecocompName = (String) this.listEcocomps.getSelectedValue();
           SpatialDataLayer ecocomp=GlobalResources.mappingProject.getEcocompByName(ecocompName);
           if(ecocomp!=null)
           {
               listStressors.clearSelection();
               listOtherData.clearSelection();
               drawingPaneShows = ecocomp;
               updateGraphics();
           }
         
    }//GEN-LAST:event_listEcocompsValueChanged

    private void listStressorsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listStressorsMouseClicked
      //  listStressorsValueChanged(null);
    }//GEN-LAST:event_listStressorsMouseClicked

    private void listEcocompsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listEcocompsMouseClicked
        //listEcocompsValueChanged(null);
    }//GEN-LAST:event_listEcocompsMouseClicked

    private void buttonEcoMinusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEcoMinusActionPerformed
        String ecocompName = (String) this.listEcocomps.getSelectedValue();
        if(ecocompName==null || ecocompName.equals(new String("null"))) return;
        GlobalResources.mappingProject.removeEcocompByName(ecocompName);
        GlobalResources.statusWindow.println("Removed ecosystem component: "+ecocompName);
        
        //update stressor list
         String[] ecocompNames=GlobalResources.mappingProject.getEcocompNames();
        DefaultListModel model = new DefaultListModel();
        for(int i=0; i<ecocompNames.length; i++) {model.addElement(ecocompNames[i]);}
        this.listEcocomps.setModel(model);
        
        //update graphics (stop showing removed)
        drawingPaneShows = null;
        updateGraphics();
        
        

    }//GEN-LAST:event_buttonEcoMinusActionPerformed

    private void menuLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuLoadActionPerformed

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCurrentDirectory(new File(GlobalResources.lastUsedFolder));
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            GlobalResources.statusWindow.setNewText("Loading project: "+selectedFile.getAbsolutePath());
            GlobalResources.statusWindow.println();
            GlobalResources.statusWindow.setProgress(0);
            GlobalResources.lastUsedFolder=selectedFile.getParent();
            this.update(this.getGraphics());
            
            try
            {
                
                CsvTableGeneral table = new CsvTableGeneral();
                table.readFromFile(selectedFile);
                
                GlobalResources.mappingProject.projectFolder=Paths.get(selectedFile.getAbsolutePath()).getParent().toString();
                timer.start();
                GlobalResources.mappingProject.createFromTable(table);
                GlobalResources.statusWindow.setVisible(true);
                timer.stop();
                
                //update GUI
                float[][] hasData=GlobalResources.mappingProject.grid.isFilled();
                drawingPaneShows=null;
                updateGraphics();
              
                
                String[] stressorNames=GlobalResources.mappingProject.getStressorNames();
                DefaultListModel model = new DefaultListModel();
                for(int i=0; i<stressorNames.length; i++) {model.addElement(stressorNames[i]);}
                this.listStressors.setModel(model);
                
                String[] ecocompNames=GlobalResources.mappingProject.getEcocompNames();
                model = new DefaultListModel();
                for(int i=0; i<ecocompNames.length; i++) {model.addElement(ecocompNames[i]);}
                this.listEcocomps.setModel(model);
                
                updateResultsList();
            }
            catch(Exception e)
            {
                GlobalResources.mappingProject.reset(); //in case loading failed
                JOptionPane.showMessageDialog(null, "Could not load file. " + e.getMessage().toString() + e.getStackTrace().toString());
            }
            finally
            {
                GlobalResources.mappingProject.setProcessingProgressPercent(0);
                this.setResizable(true);
                this.setFocusable(true);
            }

            
        }
    }//GEN-LAST:event_menuLoadActionPerformed

    private void menuSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveActionPerformed
        if(GlobalResources.mappingProject.grid==null)
        {
            JOptionPane.showMessageDialog(this, "Nothing to save.");
            return;
        }    
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCurrentDirectory(new File(GlobalResources.lastUsedFolder));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            GlobalResources.lastUsedFolder=selectedFile.getParent();
            GlobalResources.mappingProject.save(selectedFile.getAbsolutePath());
            GlobalResources.statusWindow.println("Saved project to: "+selectedFile.getAbsolutePath());
        }
    }//GEN-LAST:event_menuSaveActionPerformed

    private void menuSensitivityscoresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSensitivityscoresActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCurrentDirectory(new File(GlobalResources.lastUsedFolder));
        fileChooser.setName("Choose a sensitivity score CSV table");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            GlobalResources.lastUsedFolder=selectedFile.getParent();
            GlobalResources.mappingProject.sensitivityScores=new SensitivityScoreSet();
            GlobalResources.mappingProject.sensitivityScores.createFromFile(selectedFile.getAbsolutePath());
            if(GlobalResources.mappingProject.sensitivityScores.size()>0) 
                GlobalResources.statusWindow.println("Loaded sensitivity weights from "+selectedFile);
        }
    }//GEN-LAST:event_menuSensitivityscoresActionPerformed

    private void menuPreprocessingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuPreprocessingActionPerformed

        ProcessingWindow dialog = new ProcessingWindow(this, true);
        dialog.setVisible(true);
        
        
    }//GEN-LAST:event_menuPreprocessingActionPerformed

    private void radioButtonRawLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonRawLayerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_radioButtonRawLayerActionPerformed

    private void radioButtonRawLayerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_radioButtonRawLayerStateChanged

        radioButtonProcessedLayerStateChanged(evt);
    }//GEN-LAST:event_radioButtonRawLayerStateChanged

    private void radioButtonProcessedLayerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_radioButtonProcessedLayerStateChanged
        updateGraphics();
    }//GEN-LAST:event_radioButtonProcessedLayerStateChanged

    private void menuColorScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuColorScaleActionPerformed
        ColorScaleDialog dialog = new ColorScaleDialog(this, true);
        dialog.setVisible(true);
    }//GEN-LAST:event_menuColorScaleActionPerformed

    private void updateResultsList()
    {
        String[] resultNames=GlobalResources.mappingProject.getResultNames();
        DefaultListModel model = new DefaultListModel();
        for(int i=0; i<resultNames.length; i++) {model.addElement(resultNames[i]);}
        this.listOtherData.setModel(model);
        //this.update(null);
    }
    
    
    
    
    
    private void radioButtonProcessedLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonProcessedLayerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_radioButtonProcessedLayerActionPerformed

    private void listOtherDataMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listOtherDataMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_listOtherDataMouseClicked

    private void listOtherDataValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listOtherDataValueChanged
         String resultName = (String) this.listOtherData.getSelectedValue();
           DrawableData result=GlobalResources.mappingProject.getResultByName(resultName);
           if(result!=null)
           {
      
               listStressors.clearSelection();
               listEcocomps.clearSelection();
               drawingPaneShows = result;
               updateGraphics();
 
             
           }
    }//GEN-LAST:event_listOtherDataValueChanged

    private void buttonResultsMinusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResultsMinusActionPerformed
        String resultName = (String) this.listOtherData.getSelectedValue();
        if(resultName==null || resultName.equals(new String("null"))) return;
        GlobalResources.mappingProject.removeResultByName(resultName);
        GlobalResources.statusWindow.println("Removed spatial data layer: "+resultName);
        updateResultsList();
        drawingPaneShows = null;
        updateGraphics();
    }//GEN-LAST:event_buttonResultsMinusActionPerformed

    private void drawingPaneComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_drawingPaneComponentResized
        updateGraphics();
    }//GEN-LAST:event_drawingPaneComponentResized

    private void menuImpactIndex1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuImpactIndex1ActionPerformed
        if(GlobalResources.mappingProject.grid==null)
        {
            JOptionPane.showMessageDialog(this, "No data loaded.");
            return;
        } 
        //check if sensitivity scores exist
        if(GlobalResources.mappingProject.sensitivityScores==null || GlobalResources.mappingProject.sensitivityScores.size()<1)
        {
            JOptionPane.showMessageDialog(this,"To calculate an impact index, you must first load sensitivity weights.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCurrentDirectory(new File(GlobalResources.lastUsedFolder));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            final File selectedFile = fileChooser.getSelectedFile();
            GlobalResources.lastUsedFolder=selectedFile.getParent();
            
            //calculate index in worker thread
            GlobalResources.mappingProject.processing=true;
            
            SwingWorker<ImpactIndexAdditive, Void> worker = new SwingWorker<ImpactIndexAdditive, Void>() 
            {
                 @Override
                 protected ImpactIndexAdditive doInBackground() throws Exception 
                 {
                    GlobalResources.statusWindow.println("Started worker thread...");
                    ImpactIndexAdditive index = new ImpactIndexAdditive(selectedFile.getAbsolutePath(),GlobalResources.mappingProject.sensitivityScores, true);
                    GlobalResources.mappingProject.processing=false;

                    return index;
                }
                 
                 @Override 
                 protected void done()
                 {
                     GlobalResources.statusWindow.println("Worker thread is done.");
                     GlobalResources.mappingProject.setProcessingProgressPercent(100);
                     try    
                     {
                        ImpactIndexAdditive index = get();
                        GlobalResources.statusWindow.println("Writing results to file:"+selectedFile.getAbsolutePath());
                        CsvTableGeneral table = GlobalResources.mappingProject.grid.createTableFromLayer(index, false);
                        table.writeToFile(selectedFile.getAbsolutePath());

                        //write contributions
                        CsvTableGeneral cTable = index.getScores().getContributionsAsTable();
                        String basepath;
                        int pos=selectedFile.getAbsolutePath().lastIndexOf(".");
                        if(pos<1) {basepath=selectedFile.getAbsolutePath();}
                        else {basepath = selectedFile.getAbsolutePath().substring(0,pos);}
                        GlobalResources.statusWindow.println("Writing stressor and ecosystem component contributions to file:"+basepath+"_contributions.csv");
                        cTable.writeToFile(basepath+"_contributions.csv");
                     }
                     catch(Exception e)
                     {
                         GlobalResources.statusWindow.println("Error retrieving results from additive impact index (mean) thread.");
                         GlobalResources.statusWindow.println(e);
                     }
                     finally
                     {
                         GlobalResources.statusWindow.ready2bClosed();
                     }

                 }
            };

            try
            {
                GlobalResources.statusWindow.setNewText("Calculating additive impact index as mean...");
                timer.start();
                worker.execute();
                GlobalResources.statusWindow.setVisible(true);
                timer.stop();
                GlobalResources.mappingProject.setProcessingProgressPercent(0);
                ImpactIndexAdditive index = worker.get();
                //show in drawing pane;
                drawingPaneShows = index;
                updateGraphics();
                GlobalResources.mappingProject.results.add(index);
                updateResultsList();
            }
            catch(Exception e)
            {
                GlobalResources.statusWindow.println("Error retrieving unweighted stressor index from worker thread.");
                GlobalResources.statusWindow.println(e);
            }
            

        }
    }//GEN-LAST:event_menuImpactIndex1ActionPerformed

    //impact index as sum
    private void menuImpactIndexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuImpactIndexActionPerformed
        if(GlobalResources.mappingProject.grid==null)
        {
            JOptionPane.showMessageDialog(this, "No data loaded.");
            return;
        } 
        //check if sensitivity scores exist
        if(GlobalResources.mappingProject.sensitivityScores==null || GlobalResources.mappingProject.sensitivityScores.size()<1)
        {
            JOptionPane.showMessageDialog(this,"To calculate an impact index, you must first load sensitivity weights.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCurrentDirectory(new File(GlobalResources.lastUsedFolder));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            final File selectedFile = fileChooser.getSelectedFile();
            GlobalResources.lastUsedFolder=selectedFile.getParent();
            
            //calculate index in worker thread
            GlobalResources.mappingProject.processing=true;
            
            SwingWorker<ImpactIndexAdditive, Void> worker = new SwingWorker<ImpactIndexAdditive, Void>() 
            {
                 @Override
                 protected ImpactIndexAdditive doInBackground() throws Exception 
                 {
                    GlobalResources.statusWindow.println("Started worker thread...");
                    ImpactIndexAdditive index = new ImpactIndexAdditive(selectedFile.getAbsolutePath(),GlobalResources.mappingProject.sensitivityScores, false);
                    GlobalResources.mappingProject.processing=false;
                    return index;
                }
                 
                 @Override 
                 protected void done()
                 {
                     GlobalResources.statusWindow.println("Worker thread is done.");
                     GlobalResources.mappingProject.setProcessingProgressPercent(100);
                     try    
                     {
                        ImpactIndexAdditive index = get();
                        GlobalResources.statusWindow.println("Writing results to file: "+selectedFile.getAbsolutePath());
                        CsvTableGeneral table = GlobalResources.mappingProject.grid.createTableFromLayer(index, false);
                        table.writeToFile(selectedFile.getAbsolutePath());

                        //write contributions
                        CsvTableGeneral cTable = index.getScores().getContributionsAsTable();
                        String basepath;
                        int pos=selectedFile.getAbsolutePath().lastIndexOf(".");
                        if(pos<1) {basepath=selectedFile.getAbsolutePath();}
                        else {basepath = selectedFile.getAbsolutePath().substring(0,pos);}
                        GlobalResources.statusWindow.println("Writing stressor and ecosystem component contributions to file: "+basepath+"_contributions.csv");
                        cTable.writeToFile(basepath+"_contributions.csv");
                     }
                     catch(Exception e)
                     {
                         GlobalResources.statusWindow.println("Error retrieving results from additive impact index (mean) thread.");
                         GlobalResources.statusWindow.println(e);
                     }
                     finally
                     {
                         GlobalResources.statusWindow.ready2bClosed();
                     }
                 }
            };
            try
            {
                GlobalResources.statusWindow.setNewText("Calculating additive impact index as sum...");
                timer.start();
                worker.execute();
                GlobalResources.statusWindow.setVisible(true);
                timer.stop();
                
                ImpactIndexAdditive index = worker.get();            
                drawingPaneShows = index;
                updateGraphics();

                GlobalResources.mappingProject.results.add(index);

                updateResultsList();
                
           }
            catch(Exception e)
            {
                JOptionPane.showMessageDialog(this, "Error retrieving unweighted stressor index from worker thread.");
            }   
        }

    }//GEN-LAST:event_menuImpactIndexActionPerformed

    //weighted stressor index
    private void menuWeightedStressorIndexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuWeightedStressorIndexActionPerformed
        if(GlobalResources.mappingProject.grid==null)
        {
            JOptionPane.showMessageDialog(this, "No data loaded.");
            return;
        } 
        //check if sensitivity scores exist
        if(GlobalResources.mappingProject.sensitivityScores==null || GlobalResources.mappingProject.sensitivityScores.size()<1)
        {
            JOptionPane.showMessageDialog(this,"To calculate a weighted stressor index, you must first load sensitivity scores.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCurrentDirectory(new File(GlobalResources.lastUsedFolder));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            final File selectedFile = fileChooser.getSelectedFile();
            GlobalResources.lastUsedFolder=selectedFile.getParent();
            
             //calculate index in worker thread
            GlobalResources.mappingProject.processing=true;
            SwingWorker<WeightedStressorIndex, Void> worker = new SwingWorker<WeightedStressorIndex, Void>() 
            {
                 @Override
                 protected WeightedStressorIndex doInBackground() throws Exception 
                 {
                    GlobalResources.statusWindow.println("Started worker thread...");
                    WeightedStressorIndex index = new WeightedStressorIndex(selectedFile.getAbsolutePath());
                    GlobalResources.mappingProject.processing=false;
                    return index;
                }
                 
                 @Override 
                 protected void done()
                 {
                     try    
                     {
                        GlobalResources.statusWindow.println("Worker thread is done.");
                        GlobalResources.mappingProject.setProcessingProgressPercent(100);
                        GlobalResources.statusWindow.setProgress(100);
                        WeightedStressorIndex index = get();
                        GlobalResources.statusWindow.println("Writing results to file: "+selectedFile.getAbsolutePath());
                        CsvTableGeneral table = GlobalResources.mappingProject.grid.createTableFromLayer(index, false);
                        table.writeToFile(selectedFile.getAbsolutePath());
                     }
                     catch(Exception e)
                     {
                         GlobalResources.statusWindow.println("Error retriving results from weighted stressor index calculation thread.");
                         GlobalResources.statusWindow.println(e);
                     }
                     finally
                     {
                        GlobalResources.mappingProject.setProcessingProgressPercent(0);
                        GlobalResources.statusWindow.ready2bClosed();
                     }
                     
                 }
            };
            try
            {
                GlobalResources.mappingProject.setProcessingProgressPercent(0);
                GlobalResources.statusWindow.setProgress(0);
                GlobalResources.statusWindow.setNewText("Calculating weighted stressor index...");
                worker.execute();
                timer.start();
                GlobalResources.statusWindow.setVisible(true);
                timer.stop();

                WeightedStressorIndex index = worker.get();
                 //show in drawing pane;
                drawingPaneShows = index;
                updateGraphics();

                GlobalResources.mappingProject.results.add(index);
                updateResultsList();

            }
            catch(Exception e)
            {
                JOptionPane.showMessageDialog(this, "Error retrieving unweighted stressor index from worker thread.");
            }
        }
    }//GEN-LAST:event_menuWeightedStressorIndexActionPerformed

    private void menuStressorIndexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuStressorIndexActionPerformed
        if(GlobalResources.mappingProject.grid==null)
        {
            JOptionPane.showMessageDialog(this, "No data loaded.");
            return;
        } 
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCurrentDirectory(new File(GlobalResources.lastUsedFolder));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            final File selectedFile = fileChooser.getSelectedFile();
            GlobalResources.lastUsedFolder=selectedFile.getParent();
            
             //calculate index in worker thread
            GlobalResources.mappingProject.processing=true;
            SwingWorker<StressorIndex, Void> worker = new SwingWorker<StressorIndex, Void>() 
            {
                 @Override
                 protected StressorIndex doInBackground() throws Exception 
                 {
                    GlobalResources.statusWindow.println("Started worker thread...");
                    StressorIndex index = new StressorIndex(selectedFile.getAbsolutePath());
                    GlobalResources.mappingProject.processing=false;
                    return index;
                }
                 
                 @Override 
                 protected void done()
                 {
                     try    
                     {
                        GlobalResources.mappingProject.setProcessingProgressPercent(100);
                        GlobalResources.statusWindow.setProgress(100);
                        GlobalResources.statusWindow.println("Worker thread is done.");
                        GlobalResources.statusWindow.println("Writing results to file: "+selectedFile.getAbsolutePath());
                        StressorIndex index = get();
                        CsvTableGeneral table = GlobalResources.mappingProject.grid.createTableFromLayer(index, false);
                        table.writeToFile(selectedFile.getAbsolutePath());
                        
                     }
                     catch(Exception e)
                     {
                         JOptionPane.showMessageDialog(null, "Error retriving results from unweighted stressor index calculation thread.");
                     }
                     finally
                     {
                         GlobalResources.mappingProject.setProcessingProgressPercent(0);
                         GlobalResources.statusWindow.ready2bClosed();
                     }
                    
                     
                 }
            };
            try
            {
                GlobalResources.mappingProject.setProcessingProgressPercent(0);
                GlobalResources.statusWindow.setNewText("Calculating unweighted stressor index...");
                worker.execute();
                timer.start();
                GlobalResources.statusWindow.setVisible(true);
                timer.stop();
     
                StressorIndex index = worker.get();
                //show in drawing pane;
                drawingPaneShows = index;
                updateGraphics();

                GlobalResources.mappingProject.results.add(index);
                updateResultsList();  
            }
            catch(Exception e)
            {
                GlobalResources.statusWindow.println("Error retrieving unweighted stressor index from worker thread.");
                GlobalResources.statusWindow.println(e);
            }
            
        }
    }//GEN-LAST:event_menuStressorIndexActionPerformed

    //diversity index - refactoring failed thus the general name
    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        if(GlobalResources.mappingProject.grid==null)
        {
            JOptionPane.showMessageDialog(this, "No data loaded.");
            return;
        } 
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCurrentDirectory(new File(GlobalResources.lastUsedFolder));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            final File selectedFile = fileChooser.getSelectedFile();
            GlobalResources.lastUsedFolder=selectedFile.getParent();
            
            //calculate index in worker thread
            GlobalResources.mappingProject.processing=true;
            GlobalResources.mappingProject.setProcessingProgressPercent(0);
            GlobalResources.statusWindow.setNewText("Calculating diversity index...");
            
            SwingWorker<DiversityIndex, Void> worker = new SwingWorker<DiversityIndex, Void>() 
            {
                
                 @Override
                 protected DiversityIndex doInBackground() throws Exception 
                 {
                    GlobalResources.statusWindow.println("Started worker thread...");
                    MainWindow.this.timer.start();
                    DiversityIndex index = new DiversityIndex(selectedFile.getAbsolutePath());
                    GlobalResources.mappingProject.processing=false;
                    return index;
                }
                 
                 @Override 
                 protected void done()
                 {
                     GlobalResources.statusWindow.println("Worker thread is done.");
                     
                     try    {
                        DiversityIndex index = get();
                        drawingPaneShows = index;
                        GlobalResources.mappingProject.setProcessingProgressPercent(100);
                        GlobalResources.statusWindow.println("Writing result to file: "+selectedFile.getAbsolutePath());
                         CsvTableGeneral table = GlobalResources.mappingProject.grid.createTableFromLayer(index, false);
                         table.writeToFile(selectedFile.getAbsolutePath());
                         
                         GlobalResources.mappingProject.results.add(index);
                         MainWindow.this.drawingPaneShows=index;
                         updateResultsList();
                         
                     }
                     catch(Exception e)
                     {
                         GlobalResources.statusWindow.println("Error retrieving results from background thread.");
                         GlobalResources.statusWindow.println(e);
                     }
                     finally
                     {  
                        GlobalResources.mappingProject.setProcessingProgressPercent(100);
                        GlobalResources.statusWindow.ready2bClosed();
                        updateGraphics();
                     }
          
                     
                 }
            };

          
          try
          {
                this.setEnabled(false);
                worker.execute();
                GlobalResources.statusWindow.setVisible(true);
                timer.stop();
                this.setEnabled(true);
                this.updateGraphics();
                
          }
          catch(Exception e)
          {
                JOptionPane.showMessageDialog(this, "Error retrieving results from worker thread for diversity index.");
          }

                  
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void menuItemAreaPlotsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemAreaPlotsActionPerformed
        if(GlobalResources.mappingProject.grid==null)
        {
            JOptionPane.showMessageDialog(this, "No data loaded.");
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCurrentDirectory(new File(GlobalResources.lastUsedFolder));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            final File selectedFile = fileChooser.getSelectedFile();
            GlobalResources.lastUsedFolder=selectedFile.getParent();

            //calculation in worker thread
            GlobalResources.mappingProject.processing=true;
            SwingWorker<StressorAreaRelationships, Void> worker = new SwingWorker<StressorAreaRelationships, Void>() 
            {
                 @Override
                 protected StressorAreaRelationships doInBackground() throws Exception 
                 {
                    GlobalResources.statusWindow.println("Started worker thread...");
                    StressorAreaRelationships output = new StressorAreaRelationships();
                    GlobalResources.mappingProject.processing=false;
                    return output;
                }
                 
                 @Override 
                 protected void done()
                 {
                     GlobalResources.statusWindow.println("Worker thread is done.");
                     try    
                     {
                        GlobalResources.mappingProject.setProcessingProgressPercent(100);
                        StressorAreaRelationships output = get();
                        GlobalResources.statusWindow.println("Writing results to file: "+selectedFile.getAbsolutePath());
                        output.writeToFile(selectedFile.getAbsolutePath());
                     }
                     catch(Exception e)
                     {
                         GlobalResources.statusWindow.println("Error retrieving results from worker thread.");
                         GlobalResources.statusWindow.println(e);
                     }
                     finally
                     {
                         GlobalResources.statusWindow.ready2bClosed();
                     }
                 }
            };
            try
            {
                GlobalResources.statusWindow.setNewText("Calculating overlap between stressor intensities and diversity index.");
                timer.start();
                worker.execute();
                GlobalResources.statusWindow.setVisible(true);
                timer.stop();
                GlobalResources.mappingProject.setProcessingProgressPercent(0);
            }
            catch(Exception e)
            {
                GlobalResources.statusWindow.println("Error retrieving results from worker thread for stressor-area relationships.");
                GlobalResources.statusWindow.println(e);
            }
        }
    }//GEN-LAST:event_menuItemAreaPlotsActionPerformed

    private void menuItemExportLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemExportLayerActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCurrentDirectory(new File(GlobalResources.lastUsedFolder));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            if(drawingPaneShows==null)
            {
                JOptionPane.showMessageDialog(this, "No data displayed. Please select a data layer from the lists on the left of the window first.");
            }
            String selectedLayerName=this.drawingPaneShows.getName();
            //try to get shown data
            SpatialDataLayer selectedLayer=GlobalResources.mappingProject.getDataLayerByName(selectedLayerName);
            if(selectedLayer==null)
            {
                JOptionPane.showMessageDialog(this, "Could not export data layer: "+selectedLayerName);
            }
            else
            {
                CsvTableGeneral table;
                if(this.radioButtonProcessedLayer.isSelected())
                {
                    table=GlobalResources.mappingProject.grid.createTableFromLayer(selectedLayer, true);
                }
                else
                {
                    table=GlobalResources.mappingProject.grid.createTableFromLayer(selectedLayer, false);
                }
                table.writeToFile(selectedFile.getAbsolutePath());
            }
            
        }
    }//GEN-LAST:event_menuItemExportLayerActionPerformed

    private void menuItemExportPolyAreasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemExportPolyAreasActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCurrentDirectory(new File(GlobalResources.lastUsedFolder));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            if(drawingPaneShows==null)
            {
                JOptionPane.showMessageDialog(this, "No data displayed. Please select a data layer from the lists on the left of the window first.");
            }
            String selectedLayerName=this.drawingPaneShows.getName();
            //try to get shown data
            SpatialDataLayer selectedLayer=GlobalResources.mappingProject.getDataLayerByName(selectedLayerName);
            if(selectedLayer==null)
            {
                JOptionPane.showMessageDialog(this, "Could not export data layer: "+selectedLayerName);
            }
            else
            {
                int[][] areaCodes=selectedLayer.getGrid().calculatePresenceAreas();
                float[][] areaCodesFloat=new float[areaCodes.length][areaCodes[0].length];
                for(int x=0; x<areaCodes.length;x++)
                    for(int y=0;y<areaCodes[0].length;y++)
                    {
                        areaCodesFloat[x][y]=areaCodes[x][y];
                    }
                
                DataGrid areaGrid = new DataGrid(areaCodesFloat,selectedLayer.getGrid().getPresenceAreaSizes().length,0,selectedLayer.getGrid().getNoDataValue());
                SpatialDataLayer areaLayer=new SpatialDataLayer("areas",areaGrid,0,new DataSourceInfo());
                
                CsvTableGeneral table=GlobalResources.mappingProject.grid.createTableFromLayer(areaLayer, false);
                table.writeToFile(selectedFile.getAbsolutePath());
            } 
        }
    }//GEN-LAST:event_menuItemExportPolyAreasActionPerformed

    //load regions
    private void menuItemLoadRegionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemLoadRegionsActionPerformed
         //show load data dialog
        LoadDataForm dialog = new LoadDataForm(this,true);
        dialog.setDataType(GlobalResources.DATATYPE_REGIONS);
        dialog.setVisible(true);
        
        //update results/other data list
        String[] resultNames=GlobalResources.mappingProject.getResultNames();
        DefaultListModel model = new DefaultListModel();
        for(int i=0; i<resultNames.length; i++) {model.addElement(resultNames[i]);}
        this.listOtherData.setModel(model);
        //this.update(null);
        
        //update GUI
        SpatialDataLayer regions = GlobalResources.mappingProject.getLastDataAdded();
        drawingPaneShows=regions;
        updateGraphics();
    }//GEN-LAST:event_menuItemLoadRegionsActionPerformed

    private void menuItemMonteCarloRanksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemMonteCarloRanksActionPerformed
        if(GlobalResources.mappingProject.grid==null)
        {
            JOptionPane.showMessageDialog(this, "No data loaded.");
            return;
        }
        //check if sensitivity weights exist
        if(GlobalResources.mappingProject.sensitivityScores==null || GlobalResources.mappingProject.sensitivityScores.size()<1)
        {
            JOptionPane.showMessageDialog(this,"Please load sensitivity weights.");
            return;
        }
        //check if regions exist
        if(GlobalResources.mappingProject.regions==null)
        {
            JOptionPane.showMessageDialog(this,"Please load regions.");
            return;
        }
        final MCSimulationManager mcm = new MCSimulationManager();
        mcDialog.setSimulationManager(mcm);
        mcDialog.setVisible(true);
        if(mcDialog.isSimulationReady())
        {
           final long startTime=System.currentTimeMillis();
           if(GlobalResources.mappingProject.aois!=null) {GlobalResources.mappingProject.aois.getGrid().getUniqueDataValues();} //do here to avoid thread conflict
      
               //create simulation workers
               final MCSimulationWorker[] mcWorkers = new MCSimulationWorker[mcm.threads];
               GlobalResources.statusWindow.setNewText("Starting Monte Carlo simulations with "+mcm.simulationRuns+" runs and "+mcm.threads+" threads");
               GlobalResources.statusWindow.println("Setting up worker threads...");
               for(int i=0; i<mcWorkers.length; i++)
               {
                   mcWorkers[i]=new MCSimulationWorker();
                   mcWorkers[i].sampleSize=(int) Math.ceil(mcm.simulationRuns*1.0/mcm.threads);
                   mcWorkers[i].workerNr=i+1;
                   mcWorkers[i].setSimulationManager(mcm.clone());
                   mcWorkers[i].working=true; 
               }
               
  
              try
              {
                  GlobalResources.statusWindow.println("Starting worker threads...");
                   for(int i=0; i<mcWorkers.length; i++)
                   {
                       mcWorkers[i].execute();
                   }
                   
                   //this thread will only wait fhe simulation threads to end, and once that's done, make the status window closable, which in turn allows the GUI thread to continue.
                   SwingWorker<Void, Void> waitingThread = new SwingWorker<Void, Void>() 
                   {
                        @Override
                        protected Void doInBackground() throws Exception 
                        {
                            GlobalResources.statusWindow.println("Helper thread waits for simulations to finish...");
                            MCSimulationManager[] mcManagers = new MCSimulationManager[mcWorkers.length];
                            for(int i =0; i<mcWorkers.length; i++)
                            {
                                mcManagers[i]=mcWorkers[i].get();
                            }
                            GlobalResources.statusWindow.println("Merging thread results...");
                            for(int i=mcManagers.length-2; i>=0; i--)
                            {
                                mcManagers[i].mergeResults(mcManagers[i+1]);
                            }
                            GlobalResources.statusWindow.println("Writing thread results...");
                            Thread.sleep(500);
                            mcManagers[0].writeResults();
                            long duration=System.currentTimeMillis()-startTime;
                            GlobalResources.statusWindow.println("Completed Monte Carlo simulation with  "+mcm.simulationRuns+" runs in "+duration*0.001*(1.0/60)*(1.0/60)+" hours.");
                            GlobalResources.statusWindow.ready2bClosed();
                            return null;
                        }
 
                };
                   
                   waitingThread.execute();
                   timer.start();
                   GlobalResources.statusWindow.setProgressVisible(false);
                   GlobalResources.statusWindow.setVisible(true);
                   GlobalResources.statusWindow.setProgressVisible(true);
                   timer.stop();
                   updateResultsList();
                   updateGraphics();
              }
              catch(Exception e)
              {
                  GlobalResources.statusWindow.println("Error in Monte Carlo simulations.");
                  GlobalResources.statusWindow.println(e);
              }
        }
       
    }//GEN-LAST:event_menuItemMonteCarloRanksActionPerformed

    private void menuItemImpactIndexDominantSumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemImpactIndexDominantSumActionPerformed
        if(GlobalResources.mappingProject.grid==null)
        {
            JOptionPane.showMessageDialog(this, "No data loaded.");
            return;
        } 
        //check if sensitivity scores exist
        if(GlobalResources.mappingProject.sensitivityScores==null || GlobalResources.mappingProject.sensitivityScores.size()<1)
        {
            JOptionPane.showMessageDialog(this,"To calculate an impact index, you must first load sensitivity weights.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCurrentDirectory(new File(GlobalResources.lastUsedFolder));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            final File selectedFile = fileChooser.getSelectedFile();
            GlobalResources.lastUsedFolder=selectedFile.getParent();
            
            //calculate index in worker thread
            GlobalResources.mappingProject.processing=true;
 
            SwingWorker<ImpactIndexDominant, Void> worker = new SwingWorker<ImpactIndexDominant, Void>() 
            {
                 @Override
                 protected ImpactIndexDominant doInBackground() throws Exception 
                 {
                    GlobalResources.statusWindow.println("Started worker thread...");
                    ImpactIndexDominant index = new ImpactIndexDominant(selectedFile.getAbsolutePath(),GlobalResources.mappingProject.sensitivityScores, false);
                    GlobalResources.mappingProject.processing=false;
                    return index;
                }
                 
                 @Override 
                 protected void done()
                 {
                     GlobalResources.statusWindow.println("Worker thread is done.");
                     GlobalResources.mappingProject.setProcessingProgressPercent(100);
                     try    
                     {
                        ImpactIndexDominant index = get();
                        GlobalResources.statusWindow.println("Writing results to file: "+selectedFile.getAbsolutePath());
                        CsvTableGeneral table = GlobalResources.mappingProject.grid.createTableFromLayer(index, false);
                        table.writeToFile(selectedFile.getAbsolutePath());

                        //write contributions
                        CsvTableGeneral cTable = index.getScores().getContributionsAsTable();
                        String basepath;
                        int pos=selectedFile.getAbsolutePath().lastIndexOf(".");
                        if(pos<1) {basepath=selectedFile.getAbsolutePath();}
                         else {basepath = selectedFile.getAbsolutePath().substring(0,pos);}
                        GlobalResources.statusWindow.println("Writing stressor and ecosystem contributions to: "+basepath+"_contributions.csv");
                        cTable.writeToFile(basepath+"_contributions.csv");
                     }
                     catch(Exception e)
                     {
                         GlobalResources.statusWindow.println("Error retriving results from dominant impact index (sum) calculation thread.");
                         GlobalResources.statusWindow.println(e);
                     }
                     finally
                     {
                         GlobalResources.statusWindow.ready2bClosed();
                     }
                 }
            };
            try
            {
                GlobalResources.statusWindow.setNewText("Calculating dominant impact index as sum...");
                timer.start();
                worker.execute();
                GlobalResources.statusWindow.setVisible(true);
                timer.stop();
                GlobalResources.mappingProject.setProcessingProgressPercent(0);
                
                ImpactIndexDominant index = worker.get();            
                drawingPaneShows = index;
                        updateGraphics();
                GlobalResources.mappingProject.results.add(index);
                updateResultsList();
            }
            catch(Exception e)
            {
                GlobalResources.statusWindow.println("Error retrieving impact index from worker thread.");
                GlobalResources.statusWindow.println(e);
            }
   
        }
    }//GEN-LAST:event_menuItemImpactIndexDominantSumActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        if(GlobalResources.mappingProject.grid==null)
        {
            JOptionPane.showMessageDialog(this, "No data loaded.");
            return;
        }         
        //check if sensitivity scores exist
        if(GlobalResources.mappingProject.sensitivityScores==null || GlobalResources.mappingProject.sensitivityScores.size()<1)
        {
            JOptionPane.showMessageDialog(this,"To calculate an impact index, you must first load sensitivity weights.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCurrentDirectory(new File(GlobalResources.lastUsedFolder));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            final File selectedFile = fileChooser.getSelectedFile();
            GlobalResources.lastUsedFolder=selectedFile.getParent();
            
            //calculate index in worker thread
            GlobalResources.mappingProject.processing=true;
           
            SwingWorker<ImpactIndexDominant, Void> worker = new SwingWorker<ImpactIndexDominant, Void>() 
            {
                 @Override
                 protected ImpactIndexDominant doInBackground() throws Exception 
                 {
                    GlobalResources.statusWindow.println("Started worker thread...");
                    ImpactIndexDominant index = new ImpactIndexDominant(selectedFile.getAbsolutePath(),GlobalResources.mappingProject.sensitivityScores, true);
                    GlobalResources.mappingProject.processing=false;
                    return index;
                }
                 
                 @Override 
                 protected void done()
                 {
                     GlobalResources.statusWindow.println("Worker thread is done.");
                     GlobalResources.mappingProject.setProcessingProgressPercent(100);
                     try    
                     {
                        ImpactIndexDominant index = get();
                        CsvTableGeneral table = GlobalResources.mappingProject.grid.createTableFromLayer(index, false);
                        GlobalResources.statusWindow.println("Writing results to file: "+selectedFile.getAbsolutePath());
                        table.writeToFile(selectedFile.getAbsolutePath());

                        //write contributions
                        CsvTableGeneral cTable = index.getScores().getContributionsAsTable();
                        String basepath;
                        int pos=selectedFile.getAbsolutePath().lastIndexOf(".");
                        if(pos<1) {basepath=selectedFile.getAbsolutePath();}
                         else {basepath = selectedFile.getAbsolutePath().substring(0,pos);}
                        GlobalResources.statusWindow.println("Writing stressor and ecosystem component contributions to: "+basepath+"_contributions.csv");
                        cTable.writeToFile(basepath+"_contributions.csv");
                     
                     }
                     catch(Exception e)
                     {
                         GlobalResources.statusWindow.println("Error retrieving results from stressor - diversity index overlap calculation thread.");
                         GlobalResources.statusWindow.println(e);
                     }
                     finally {GlobalResources.statusWindow.ready2bClosed();}
                 }
            };
            try
            {
                GlobalResources.statusWindow.setNewText("Calculating dominant impact index as mean...");
                timer.start();
                worker.execute();
                GlobalResources.statusWindow.setVisible(true);
                timer.stop();
                GlobalResources.mappingProject.setProcessingProgressPercent(0);
                
                ImpactIndexDominant index = worker.get();            
                drawingPaneShows = index;
                updateGraphics();
                GlobalResources.mappingProject.results.add(index);
                updateResultsList();

           }
            catch(Exception e)
            {
                GlobalResources.statusWindow.println("Error retrieving index from worker thread.");
                GlobalResources.statusWindow.println(e);
            }    
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void menuItemDiminishingImpactsSumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemDiminishingImpactsSumActionPerformed
        if(GlobalResources.mappingProject.grid==null)
        {
            JOptionPane.showMessageDialog(this, "No data loaded.");
            return;
        }
        if(GlobalResources.mappingProject.grid==null)
        {
            JOptionPane.showMessageDialog(this, "No data loaded.");
            return;
        }         
        //check if sensitivity scores exist
        if(GlobalResources.mappingProject.sensitivityScores==null || GlobalResources.mappingProject.sensitivityScores.size()<1)
        {
            JOptionPane.showMessageDialog(this,"To calculate an impact index, you must first load sensitivity weights.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCurrentDirectory(new File(GlobalResources.lastUsedFolder));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            final File selectedFile = fileChooser.getSelectedFile();
            GlobalResources.lastUsedFolder=selectedFile.getParent();
            
            //calculate index in worker thread
            GlobalResources.mappingProject.processing=true;
            
            SwingWorker<ImpactIndexDiminishing, Void> worker = new SwingWorker<ImpactIndexDiminishing, Void>() 
            {
                 @Override
                 protected ImpactIndexDiminishing doInBackground() throws Exception 
                 {
                    GlobalResources.statusWindow.println("Started worker thread...");
                    ImpactIndexDiminishing index = new ImpactIndexDiminishing(selectedFile.getAbsolutePath(),GlobalResources.mappingProject.sensitivityScores, false);
                    GlobalResources.mappingProject.processing=false;
                    return index;
                }
                 
                 @Override 
                 protected void done()
                 {
                     GlobalResources.statusWindow.println("Worker thread is done.");
                     GlobalResources.mappingProject.setProcessingProgressPercent(100);
                     try    
                     {
                        ImpactIndexDiminishing index = get();
                        
                        CsvTableGeneral table = GlobalResources.mappingProject.grid.createTableFromLayer(index, false);
                        table.writeToFile(selectedFile.getAbsolutePath());
                        GlobalResources.statusWindow.println("Writing results to file: "+selectedFile.getAbsolutePath());
                        //write contributions
                        CsvTableGeneral cTable = index.getScores().getContributionsAsTable();
                        String basepath;
                        int pos=selectedFile.getAbsolutePath().lastIndexOf(".");
                        if(pos<1) {basepath=selectedFile.getAbsolutePath();}
                         else {basepath = selectedFile.getAbsolutePath().substring(0,pos);}
                        GlobalResources.statusWindow.println("Writing stressor and ecosystem component contributions to: "+basepath+"_contributions.csv");
                        cTable.writeToFile(basepath+"_contributions.csv");
                     }
                     catch(Exception e)
                     {
                         GlobalResources.statusWindow.println("Error retriving results from antagonistic impact index calculation thread.");
                         GlobalResources.statusWindow.println(e);
                     }
                     finally
                     {
                         GlobalResources.statusWindow.ready2bClosed();
                     }

                 }
            };
            try
            {
                GlobalResources.statusWindow.setNewText("Calculating antagonistic impact index as sum...");
                timer.start();
                worker.execute();
                GlobalResources.statusWindow.setVisible(true);
                timer.stop();
                GlobalResources.mappingProject.setProcessingProgressPercent(0);
 
                ImpactIndexDiminishing index = worker.get();            
                drawingPaneShows = index;
                updateGraphics();
                GlobalResources.mappingProject.results.add(index);
                updateResultsList();
           }
            catch(Exception e)
            {
                GlobalResources.statusWindow.println("Error retrieving sptial data from worker thread.");
                GlobalResources.statusWindow.println(e);
            } 
        }
    }//GEN-LAST:event_menuItemDiminishingImpactsSumActionPerformed

    private void menuItemDiminishingImpactMeanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemDiminishingImpactMeanActionPerformed
        if(GlobalResources.mappingProject.grid==null)
        {
            JOptionPane.showMessageDialog(this, "No data loaded.");
            return;
        }        
        //check if sensitivity weights exist
        if(GlobalResources.mappingProject.sensitivityScores==null || GlobalResources.mappingProject.sensitivityScores.size()<1)
        {
            JOptionPane.showMessageDialog(this,"To calculate an impact index, you must first load sensitivity weights.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCurrentDirectory(new File(GlobalResources.lastUsedFolder));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            final File selectedFile = fileChooser.getSelectedFile();
            GlobalResources.lastUsedFolder=selectedFile.getParent();
            
            //calculate index in worker thread
            GlobalResources.mappingProject.processing=true;
            SwingWorker<ImpactIndexDiminishing, Void> worker = new SwingWorker<ImpactIndexDiminishing, Void>() 
            {
                 @Override
                 protected ImpactIndexDiminishing doInBackground() throws Exception 
                 {
                    GlobalResources.statusWindow.println("Started worker thread...");
                    ImpactIndexDiminishing index = new ImpactIndexDiminishing(selectedFile.getAbsolutePath(),GlobalResources.mappingProject.sensitivityScores, true);
                    GlobalResources.mappingProject.processing=false;
                    return index;
                }
                 
                 @Override 
                 protected void done()
                 {
                     GlobalResources.statusWindow.println("Background thread is done.");
                     GlobalResources.mappingProject.setProcessingProgressPercent(100);
                     try    
                     {
                        ImpactIndexDiminishing index = get();
                        GlobalResources.statusWindow.println("Writing results to: "+selectedFile.getAbsolutePath());
                        CsvTableGeneral table = GlobalResources.mappingProject.grid.createTableFromLayer(index, false);
                        table.writeToFile(selectedFile.getAbsolutePath());

                        //write contributions
                        CsvTableGeneral cTable = index.getScores().getContributionsAsTable();
                        String basepath;
                        int pos=selectedFile.getAbsolutePath().lastIndexOf(".");
                        if(pos<1) {basepath=selectedFile.getAbsolutePath();}
                         else {basepath = selectedFile.getAbsolutePath().substring(0,pos);}
                        GlobalResources.statusWindow.println("Writing stressor and ecosystem component contributions to: "+basepath+"_contributions.csv");
                        cTable.writeToFile(basepath+"_contributions.csv");
                     }
                     catch(Exception e)
                     {
                         GlobalResources.statusWindow.println("Error retrieving results from antagonistic impact index calculation thread.");
                         GlobalResources.statusWindow.println(e);
                     }
                     finally
                     {
                         GlobalResources.statusWindow.ready2bClosed();
                     }

                 }
            };
            try
            {
                GlobalResources.statusWindow.setNewText("Calculating antagonistic impact index as mean...");
                timer.start();
                worker.execute();
                GlobalResources.statusWindow.setVisible(true);
                timer.stop();
                GlobalResources.mappingProject.setProcessingProgressPercent(100);
               
                ImpactIndexDiminishing index = worker.get();            
                drawingPaneShows = index;
                updateGraphics();
                GlobalResources.mappingProject.results.add(index);
                updateResultsList();       
           }
            catch(Exception e)
            {
                GlobalResources.statusWindow.println("Error retrieving sptial data from worker thread.");
            }
        }
    }//GEN-LAST:event_menuItemDiminishingImpactMeanActionPerformed

    //export data with lower resolution
    private void menuItemExportLowerResActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemExportLowerResActionPerformed
        if(GlobalResources.mappingProject.grid==null)
        {
            JOptionPane.showMessageDialog(this, "No data loaded.");
            return;
        } 
        int reductionFactor=2;  //TODO show dialog, give option

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCurrentDirectory(new File(GlobalResources.lastUsedFolder));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            GlobalResources.mappingProject.saveLowerResolutionVersion(selectedFile.getAbsolutePath(),reductionFactor);
        }
    }//GEN-LAST:event_menuItemExportLowerResActionPerformed

    private void menuItemMorrisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemMorrisActionPerformed
       
         if(GlobalResources.mappingProject.grid==null)
        {
            JOptionPane.showMessageDialog(this, "No data loaded.");
            return;
        }
        //check if sensitivity scores exist
        if(GlobalResources.mappingProject.sensitivityScores==null || GlobalResources.mappingProject.sensitivityScores.size()<1)
        {
            JOptionPane.showMessageDialog(this,"Please load sensitivity weights.");
            return;
        }
        //check if regions exist
        if(GlobalResources.mappingProject.regions==null)
        {
            JOptionPane.showMessageDialog(this,"Please load regions.");
            return;
        }
        
        final MorrisDialog dialog = new MorrisDialog(this, true);
        final MorrisSampler ms = new MorrisSampler();
        dialog.setVisible(true);
        
        if(!dialog.isCanceled())
        {
           final long startTime=System.currentTimeMillis();
           int r = dialog.getSampleSize();
           String outputFolder = dialog.getOutputFolder();
           
            //create worker threads
            final MorrisWorker[] workers = new MorrisWorker[GlobalResources.nrOfThreads];
            GlobalResources.statusWindow.setNewText("Starting elementary effect calculations with "+dialog.getSampleSize()+" trajectories and "+GlobalResources.nrOfThreads+" threads");
               GlobalResources.statusWindow.println("Setting up worker threads...");
               for(int i=0; i<workers.length; i++)
               {
                   workers[i]=new MorrisWorker();
                   workers[i].sampleSize=(int) Math.ceil(dialog.getSampleSize()*1.0/GlobalResources.nrOfThreads);
                   workers[i].workerNr=i+1;
                   //workers[i].setMorrisSampler(ms.clone());
                   workers[i].working=true; 
               }
              //
              try
              {
                  GlobalResources.statusWindow.println("Starting worker threads...");
                   for(int i=0; i<workers.length; i++)
                   {
                       workers[i].execute();
                   }
                   
                   //this thread will only wait fhe simulation threads to end, and once that's done, make the status window closable, which in turn allows the GUI thread to continue.
                   SwingWorker<Void, Void> waitingThread = new SwingWorker<Void, Void>() 
                   {
                        @Override
                        protected Void doInBackground() throws Exception 
                        {
                            GlobalResources.statusWindow.println("Helper thread waits for calculations to finish...");
                            MorrisSampler[] samplers = new MorrisSampler[workers.length];
                            for(int i =0; i<workers.length; i++)
                            {
                                samplers[i]=workers[i].get();
                            }
                            GlobalResources.statusWindow.println("Merging thread results...");
                            for(int i=samplers.length-2; i>=0; i--)
                            {
                                samplers[i].mergeResults(samplers[i+1]);
                            }
                            GlobalResources.statusWindow.println("Writing thread results...");
                            Thread.sleep(500);
                            samplers[0].calculateElementaryEffectStatistics(dialog.getSampleSize());
                            samplers[0].saveResults(dialog.getOutputFolder());
                            long duration=System.currentTimeMillis()-startTime;
                            GlobalResources.statusWindow.println("Completed elementary effects calculation with  "+dialog.getSampleSize()+" runs in "+duration*0.001*(1.0/60)*(1.0/60)+" hours.");
                            GlobalResources.statusWindow.ready2bClosed();
                            return null;
                        }
 
                };
                   
                   waitingThread.execute();
                   timer.start();
                   GlobalResources.statusWindow.setProgressVisible(false);
                   GlobalResources.statusWindow.setVisible(true);
                   GlobalResources.statusWindow.setProgressVisible(true);
                   timer.stop();
              }
              catch(Exception e)
              {
                  GlobalResources.statusWindow.println("Error in elementary effects calculations.");
                  GlobalResources.statusWindow.println(e);
              }
        }
       /*
       boolean multiThreading=true;
       if(!dialog.wasCanceled())
       {
       
           long startTime=System.currentTimeMillis();
           int r = dialog.getSampleSize();
           String outputFolder = dialog.getOutputFolder();
           //do the multi-threading here: create 2 Morris samplers in 2 threads; they don't have static variables and thus won't interefere;
           //call their processTrajectories functions; then merge them (new method MorrisSampler.mergeResults(MorrisSampler sampler2) needed);
           //then call the merged one's calculate elementary effect stats methods
           
           if(!multiThreading)
           {
                MorrisSampler ms = new MorrisSampler();
                ms.setup();
                ms.processTrajectories(r);  //this one calculates the elementary effects
                ms.calculateElementaryEffectStatistics(r);
                ms.saveResults(outputFolder);
                long duration=System.currentTimeMillis()-startTime;
                System.out.println("Calculated "+r+" elementary effects in "+duration*0.001*(1.0/60)*(1.0/60)+" hours.");
           }
           else
           {
               MorrisWorker mw1=new MorrisWorker();
               MorrisWorker mw2=new MorrisWorker();
               mw1.sampleSize=(int) Math.ceil(0.5*r);
               mw2.sampleSize=(int) Math.ceil(0.5*r);
               mw1.workerNr=1;
               mw2.workerNr=2;
               mw1.working=true;
               mw2.working=true;
               
              try
              {
                    mw1.execute();
                    Thread.sleep(1000);
                    mw2.execute();
                    Thread.sleep(1000);
                    //block while processing
                    while(mw1.working || mw2.working)
                    {
                        if(mw1.getState().equals(StateValue.DONE)) 
                        {
                            System.out.println("THREAD 1 STATE: "+mw1.getState());
                        }
                        if(mw2.getState().equals(StateValue.DONE)) 
                        {
                            System.out.println("THREAD 2 STATE: "+mw2.getState());
                        }
                        
                        Thread.sleep(1000);
                    }
                    System.out.println("Threads completed.");
                    //both threads are done
                    //Thread.sleep(500); //
                    MorrisSampler ms1 = mw1.get();
                    MorrisSampler ms2 = mw2.get();
                    ms1.mergeResults(ms2);
                    ms1.calculateElementaryEffectStatistics(r);
                    ms1.saveResults(outputFolder);
                    long duration=System.currentTimeMillis()-startTime;
                    System.out.println("Calculated "+r+" elementary effects in "+duration*0.001*(1.0/60)*(1.0/60)+" hours.");
              }
              catch(Throwable e)
              {
                  JOptionPane.showMessageDialog(this, "Multithreading error: "+e.getMessage());
                  System.out.println("Multithreading error: "+e.getMessage());
                  System.out.println(e.getStackTrace()[0]);
                  System.out.println(e.getStackTrace()[1]);
                  System.out.println(e.getStackTrace()[2]);
              }

           }

           
       }*/
    }//GEN-LAST:event_menuItemMorrisActionPerformed

    private void menuItemRunCurrentTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemRunCurrentTestActionPerformed
       //got through all stressors; make a clone; calculate unique values; and compare results using 2 methods
        /*int passed=0; 
        int failed=0;
        for(int i=0; i<GlobalResources.mappingProject.stressors.size(); i++)
        {
            DataGrid grid1 = GlobalResources.mappingProject.stressors.get(i).getGrid().clone();
            DataGrid grid2 = GlobalResources.mappingProject.stressors.get(i).getGrid().clone();
            grid1.createDataInfo();
            grid2.createDataInfo2();
            ArrayList<Double> uniqueValues1=grid1.getUniqueDataValues();
            ArrayList<Double> uniqueValues2=grid2.getUniqueDataValues();
            if(uniqueValues1.equals(uniqueValues2))
            {
                passed++;
            }
            else
            {
                failed++;
            }
           
        }
         JOptionPane.showMessageDialog(null,"Passed: "+passed+";   failed: "+failed);*/
    }//GEN-LAST:event_menuItemRunCurrentTestActionPerformed

    private void radioButtonMenuItemQuantileStretchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonMenuItemQuantileStretchActionPerformed
        if(radioButtonMenuItemQuantileStretch.isSelected()) 
        {
            this.radioButtonMenuItemLinearStretch.setSelected(false);
            ImageCreator.quantileStretch=true;
            updateGraphics();
        }
        else if(!this.radioButtonMenuItemLinearStretch.isSelected())
        {
            radioButtonMenuItemQuantileStretch.setSelected(true);
            ImageCreator.quantileStretch=true;
            updateGraphics();
        }
        
    }//GEN-LAST:event_radioButtonMenuItemQuantileStretchActionPerformed

    private void radioButtonMenuItemLinearStretchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonMenuItemLinearStretchActionPerformed
        if(radioButtonMenuItemLinearStretch.isSelected()) 
        {
            this.radioButtonMenuItemQuantileStretch.setSelected(false);
            ImageCreator.quantileStretch=false;
            updateGraphics();
        }
        else if(!this.radioButtonMenuItemQuantileStretch.isSelected())
        {
            radioButtonMenuItemLinearStretch.setSelected(true);
            ImageCreator.quantileStretch=false;
            updateGraphics();
        }
    }//GEN-LAST:event_radioButtonMenuItemLinearStretchActionPerformed

    private void menuItemFreeMemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemFreeMemActionPerformed
        GlobalResources.lastOpenedTable=null;
        GlobalResources.lastOpenedTableFile="";
    }//GEN-LAST:event_menuItemFreeMemActionPerformed

    private void menuItemAoisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemAoisActionPerformed
           //show load data dialog
        LoadDataForm dialog = new LoadDataForm(this,true);
        dialog.setDataType(GlobalResources.DATATYPE_AOIS);
        dialog.setVisible(true);
        
        //update results/other data list
        String[] resultNames=GlobalResources.mappingProject.getResultNames();
        DefaultListModel model = new DefaultListModel();
        for(int i=0; i<resultNames.length; i++) {model.addElement(resultNames[i]);}
        this.listOtherData.setModel(model);
        //this.update(null);
        
        //update graphics to show the latest stressor
        SpatialDataLayer newStressor = GlobalResources.mappingProject.getLastDataAdded();
        drawingPaneShows=newStressor;
        updateGraphics();
    }//GEN-LAST:event_menuItemAoisActionPerformed

    private void menuItemSensitivityIndexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSensitivityIndexActionPerformed
        if(GlobalResources.mappingProject.grid==null)
        {
            JOptionPane.showMessageDialog(this, "No data loaded.");
            return;
        } 
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCurrentDirectory(new File(GlobalResources.lastUsedFolder));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            final File selectedFile = fileChooser.getSelectedFile();
            GlobalResources.lastUsedFolder=selectedFile.getParent();
            
            //calculate index in worker thread
            GlobalResources.mappingProject.processing=true;
            GlobalResources.mappingProject.setProcessingProgressPercent(0);
            
            SwingWorker<SensitivityIndex, Void> worker = new SwingWorker<SensitivityIndex, Void>() 
            {
                 @Override
                 protected SensitivityIndex doInBackground() throws Exception 
                 {
                    GlobalResources.statusWindow.println("Started background thread...");
                    SensitivityIndex index = new SensitivityIndex(selectedFile.getAbsolutePath());
                    GlobalResources.mappingProject.processing=false;
                    return index;
                }
                 
                 @Override 
                 protected void done()
                 {
                     GlobalResources.statusWindow.println("Background thread is done.");
                     GlobalResources.mappingProject.setProcessingProgressPercent(100);
                     try    
                     {
                        SensitivityIndex index = get();
                        CsvTableGeneral table = GlobalResources.mappingProject.grid.createTableFromLayer(index, false);
                        GlobalResources.statusWindow.println("Writing results to file: "+selectedFile.getAbsolutePath());
                        table.writeToFile(selectedFile.getAbsolutePath());
                        GlobalResources.mappingProject.results.add(index);
                     }
                     catch(Exception e)
                     {
                         GlobalResources.statusWindow.println( "Error retriving results from sensitivity index calculation thread.");
                         GlobalResources.statusWindow.println(e);
                     }
                     finally
                     {
                         GlobalResources.statusWindow.ready2bClosed();
                     }
                     
                 }
            };
            try
            {
                GlobalResources.statusWindow.setNewText("Calculating sensitivity index...");
                worker.execute();
                timer.start();
                GlobalResources.statusWindow.setVisible(true);
                timer.stop();
                SensitivityIndex index = worker.get();
                //show in drawing pane;
                drawingPaneShows = index;
                this.setEnabled(true);  
                updateResultsList();
                updateGraphics();
            }
            catch(Exception e)
            {
                GlobalResources.statusWindow.println("Error retrieving results from worker thread for sensitivty index.");
                GlobalResources.statusWindow.println(e);
            }
        }
    }//GEN-LAST:event_menuItemSensitivityIndexActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        
        FactorLayerDialog dialog = new FactorLayerDialog(this, true);
        dialog.setVisible(true);
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu MenuView;
    private javax.swing.JButton buttonEcoMinus;
    private javax.swing.JButton buttonEcoPlus;
    private javax.swing.JButton buttonResultsMinus;
    private javax.swing.JButton buttonStressorsMinus;
    private javax.swing.JButton buttonStressorsPlus;
    private javax.swing.JPanel drawingPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel labelEcologicalComponents;
    private javax.swing.JLabel labelStressors;
    private javax.swing.JList listEcocomps;
    private javax.swing.JList listOtherData;
    private javax.swing.JList listStressors;
    private javax.swing.JMenu menuAssignSelectiveFactors;
    private javax.swing.JMenuBar menuBarMain;
    private javax.swing.JMenuItem menuColorScale;
    private javax.swing.JMenu menuDevelopment;
    private javax.swing.JMenu menuDiversityIndexAvg;
    private javax.swing.JMenuItem menuImpactIndex;
    private javax.swing.JMenuItem menuImpactIndex1;
    private javax.swing.JMenuItem menuItemAois;
    private javax.swing.JMenuItem menuItemAreaPlots;
    private javax.swing.JMenuItem menuItemDiminishingImpactMean;
    private javax.swing.JMenuItem menuItemDiminishingImpactsSum;
    private javax.swing.JMenuItem menuItemExportLayer;
    private javax.swing.JMenuItem menuItemExportLowerRes;
    private javax.swing.JMenuItem menuItemExportPolyAreas;
    private javax.swing.JMenuItem menuItemFreeMem;
    private javax.swing.JMenuItem menuItemImpactIndexDominantSum;
    private javax.swing.JMenuItem menuItemLoadRegions;
    private javax.swing.JMenuItem menuItemMonteCarloRanks;
    private javax.swing.JMenuItem menuItemMorris;
    private javax.swing.JMenuItem menuItemRunCurrentTest;
    private javax.swing.JMenuItem menuItemSensitivityIndex;
    private javax.swing.JMenuItem menuLoad;
    private javax.swing.JMenuItem menuNew;
    private javax.swing.JMenuItem menuPreprocessing;
    private javax.swing.JMenu menuProject;
    private javax.swing.JMenuItem menuSave;
    private javax.swing.JMenuItem menuSensitivityscores;
    private javax.swing.JMenuItem menuStressorIndex;
    private javax.swing.JMenu menuUncertainty;
    private javax.swing.JMenuItem menuWeightedStressorIndex;
    private javax.swing.JRadioButtonMenuItem radioButtonMenuItemLinearStretch;
    private javax.swing.JRadioButtonMenuItem radioButtonMenuItemQuantileStretch;
    private javax.swing.JRadioButton radioButtonProcessedLayer;
    private javax.swing.JRadioButton radioButtonRawLayer;
    // End of variables declaration//GEN-END:variables
}
