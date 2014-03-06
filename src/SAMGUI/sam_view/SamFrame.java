/**
 * 	Author:	Alexander Pataky
 * 	Date:	11/21/2010
 * 	Class:	SamFrame
 * 	Use:	Extends JFrame to provide user with main form for application.
 * 			Uses Singleton design pattern so use getInstance() to instantiate.
 */
package SAMGUI.sam_view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import SAMGUI.sam_controller.AddButtonHandler;
import SAMGUI.sam_controller.AnalysisModuleHandler;
import SAMGUI.sam_controller.FileHandler;
import SAMGUI.sam_model.Component;
import SAMGUI.sam_model.SamModel;
import SAMGUI.sam_model.XMLTransformer;

public class SamFrame extends JFrame {
	// singleton instance of SamFrame
	private static SamFrame instance = null;
	private ArrayList<SamCanvas> canvases = new ArrayList<SamCanvas>();
	
	private static ArrayList<SamTabData> samTabs = new ArrayList<SamTabData>();
	
	private static class SamTabData { // a structure for holding a tab's data
	      
	      public SamModel samModel;
	      public SamCanvas samCanvas;
	   }

	public enum Toggle {
		COMPONENT(0), CONNECTOR(1), ARC(2), PORT(3), INVALID(4);

		private int value;

		Toggle(int x) {
			value = x;
		}

		public int getValue() {
			return value;
		}

		public static Toggle getValue(JToggleButton btn) {
			if (btn.getName().compareTo("btnComponent") == 0) {
				return COMPONENT;
			} else if (btn.getName().compareTo("btnConnector") == 0) {
				return CONNECTOR;
			} else if (btn.getName().compareTo("btnArc") == 0) {
				return ARC;
			} else if (btn.getName().compareTo("btnPort") == 0) {
				return PORT;
			}

			return INVALID;
		}

	}

	// buttons for creating actual model objects
	private JToggleButton btnPort;
	private JToggleButton btnArc;
	private JToggleButton btnConnector;
	private JToggleButton btnComponent;

	// buttons for editing
	private JButton btnCopy;
	private JButton btnPaste;
	private JButton btnUndo;
	private JButton btnRedo;

	// buttons for file operations
	private JButton btnCreateFile;
	private JButton btnSaveFile;
	private JButton btnOpenFile;
	private JButton btnCloseFile;
	
	//buttons for model analysis
	private JButton btnModelToPromela;

	private final JTabbedPane pane = new JTabbedPane();

	private SamStatusBar statusBar = new SamStatusBar();

	private void enableButtons(boolean enable){
		btnSaveFile.setEnabled(enable);
		btnCloseFile.setEnabled(enable);
		btnPort.setEnabled(enable);
		btnArc.setEnabled(enable);
		btnConnector.setEnabled(enable);
		btnComponent.setEnabled(enable);
	}
	
	// returns instance of SamFrame
	public static SamFrame getInstance() {
		if (instance == null) {
			instance = new SamFrame();
		}
		return instance;
	}

	public static void main(String[] args) {
		SamFrame x = SamFrame.getInstance();
		x.setVisible(true);
	}

	// private constructor for singleton
	// sets up toolbars and menus
	private SamFrame() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("SAM Framework Modeling Tool");
		this.setName("frameSAMMain");
		this.setMinimumSize(new Dimension(600, 400));
		this.setSize(800, 600);
		

		pane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		setupMenus();
		
		getContentPane().setLayout(new BorderLayout(0, 0));

		setupToolbars();
		
		getContentPane().add(pane);

		statusBar.setLabel(" ");
		getContentPane().add(statusBar, BorderLayout.SOUTH);
		
		setExtendedState(this.getExtendedState()|JFrame.MAXIMIZED_BOTH);

		this.enableButtons(false);

	}

	public void setupMenus(){
		JMenuBar menuBarMain = new JMenuBar();
		setJMenuBar(menuBarMain);

		//file menu
		JMenu mnFile = new JMenu("File");
		mnFile.setMnemonic('F');
		menuBarMain.add(mnFile);
		FileHandler fileHandler = new FileHandler();
		
		JMenuItem  createMenuItem = new JMenuItem("New");
		createMenuItem.setName("new");
		createMenuItem.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/createDoc16.png")));
		createMenuItem.setMnemonic(KeyEvent.VK_N);
		createMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
		        ActionEvent.ALT_MASK));
		createMenuItem.addActionListener(fileHandler);
		mnFile.add(createMenuItem);		
		
		JMenuItem  saveMenuItem = new JMenuItem("Save");
		saveMenuItem.setName("save");
		saveMenuItem.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/fileSave16.png")));
		saveMenuItem.setMnemonic(KeyEvent.VK_S);
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
		        ActionEvent.ALT_MASK));
		saveMenuItem.addActionListener(fileHandler);
		mnFile.add(saveMenuItem);
		
		JMenuItem  saveAsMenuItem = new JMenuItem("Save As");
		saveAsMenuItem.setName("saveas");
		saveAsMenuItem.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/fileSave16.png")));
		saveAsMenuItem.setMnemonic(KeyEvent.VK_S);
		saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
		        ActionEvent.ALT_MASK));
		saveAsMenuItem.addActionListener(fileHandler);
		mnFile.add(saveAsMenuItem);
		
		JMenuItem  openMenuItem = new JMenuItem("Open");
		openMenuItem.setName("open");
		openMenuItem.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/fileOpen16.png")));
		openMenuItem.setMnemonic(KeyEvent.VK_O);
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
		        ActionEvent.ALT_MASK));
		openMenuItem.addActionListener(fileHandler);
		mnFile.add(openMenuItem);
		
		JMenuItem  closeMenuItem = new JMenuItem("Close");
		closeMenuItem.setName("close");
		closeMenuItem.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/closeDoc16.png")));
		closeMenuItem.setMnemonic(KeyEvent.VK_C);
		closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
		        ActionEvent.ALT_MASK));
		closeMenuItem.addActionListener(fileHandler);
		mnFile.add(closeMenuItem);
		

		//edit menu
		JMenu mnEdit = new JMenu("Edit");
		mnEdit.setMnemonic('E');
		menuBarMain.add(mnEdit);
		
		JMenuItem  copyMenuItem = new JMenuItem("Copy");
		copyMenuItem.setName("copy");
		copyMenuItem.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/copy16.png")));
		copyMenuItem.setMnemonic(KeyEvent.VK_C);
		copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
		        ActionEvent.ALT_MASK));
		mnEdit.add(copyMenuItem);		
		
		JMenuItem  pasteMenuItem = new JMenuItem("Paste");
		pasteMenuItem.setName("paste");
		pasteMenuItem.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/paint16.png")));
		pasteMenuItem.setMnemonic(KeyEvent.VK_P);
		pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
		        ActionEvent.ALT_MASK));
		mnEdit.add(pasteMenuItem);
		
		JMenuItem  undoMenuItem = new JMenuItem("Undo");
		undoMenuItem.setName("undo");
		undoMenuItem.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/undo16.png")));
		undoMenuItem.setMnemonic(KeyEvent.VK_U);
		undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U,
		        ActionEvent.ALT_MASK));
		mnEdit.add(undoMenuItem);
		
		JMenuItem  redoMenuItem = new JMenuItem("Redo");
		redoMenuItem.setName("redo");
		redoMenuItem.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/redo16.png")));
		redoMenuItem.setMnemonic(KeyEvent.VK_R);
		redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
		        ActionEvent.ALT_MASK));
		mnEdit.add(redoMenuItem);

		//draw menu
		JMenu mnDraw = new JMenu("Draw");
		mnDraw.setMnemonic('D');
		menuBarMain.add(mnDraw);
		
		JMenuItem  componentMenuItem = new JMenuItem("Component");
		componentMenuItem.setName("component");
		componentMenuItem.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/component16.png")));
		componentMenuItem.setMnemonic(KeyEvent.VK_C);
		componentMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
		        ActionEvent.ALT_MASK));
		mnDraw.add(componentMenuItem);		
		
		JMenuItem  connectorMenuItem = new JMenuItem("Connector");
		connectorMenuItem.setName("connector");
		connectorMenuItem.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/connector16.png")));
		connectorMenuItem.setMnemonic(KeyEvent.VK_C);
		connectorMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
		        ActionEvent.ALT_MASK));
		mnDraw.add(connectorMenuItem);
		
		JMenuItem  arcMenuItem = new JMenuItem("Arc");
		arcMenuItem.setName("arc");
		arcMenuItem.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/arc16.png")));
		arcMenuItem.setMnemonic(KeyEvent.VK_A);
		arcMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
		        ActionEvent.ALT_MASK));
		mnDraw.add(arcMenuItem);
		
		JMenuItem  portMenuItem = new JMenuItem("Port");
		portMenuItem.setName("port");
		portMenuItem.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/port16.png")));
		portMenuItem.setMnemonic(KeyEvent.VK_P);
		portMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
		        ActionEvent.ALT_MASK));
		mnDraw.add(portMenuItem);

		//view menu to be done
		JMenu mnView = new JMenu("View");
		mnView.setMnemonic('V');
		menuBarMain.add(mnView);

		//help menu to be done
		JMenu mnHelp = new JMenu("Help");
		mnHelp.setMnemonic('H');
		mnHelp.setIcon(null);
		menuBarMain.add(mnHelp);
	}
	
	// sets up toolbars by calling setup methods for file, edit, view
	// and model toolbars
	public void setupToolbars() {
		JPanel panelToolBars = new JPanel();
		getContentPane().add(panelToolBars, BorderLayout.NORTH);
		panelToolBars.setLayout(new BoxLayout(panelToolBars, BoxLayout.X_AXIS));

		setupToolbarsFile(panelToolBars);
		setupToolbarsEdit(panelToolBars);
		setupToolbarsView(panelToolBars);
		setupToolbarsModel(panelToolBars);
		setupToolbarsAnalysis(panelToolBars);
	}

	public void setupToolbarsFile(JPanel panelToolBars) {
		JToolBar toolBarFile = new JToolBar();
		toolBarFile.setFloatable(false);
		panelToolBars.add(toolBarFile);
		JLabel lblFile = new JLabel("");
		toolBarFile.add(lblFile);
		
		FileHandler fileHandler = new FileHandler();
		
		btnCreateFile = new JButton("");
		btnCreateFile.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/createDoc16.png")));
		btnCreateFile.setBackground(SystemColor.menu);
		toolBarFile.add(btnCreateFile);
		btnCreateFile.setToolTipText("Create New File");
		btnCreateFile.setName("btnCreateFile");
		btnCreateFile.addActionListener(fileHandler);

		btnCloseFile = new JButton("");
		btnCloseFile.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/closeDoc16.png")));
		btnCloseFile.setBackground(SystemColor.menu);
		toolBarFile.add(btnCloseFile);
		btnCloseFile.setToolTipText("Close File");
		btnCloseFile.setName("btnCloseFile");
		btnCloseFile.addActionListener(fileHandler);

		btnOpenFile = new JButton("");
		btnOpenFile.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/fileOpen16.png")));
		btnOpenFile.setBackground(SystemColor.menu);
		toolBarFile.add(btnOpenFile);
		btnOpenFile.setToolTipText("Open File");
		btnOpenFile.setName("btnOpenFile");
		btnOpenFile.addActionListener(fileHandler);

		btnSaveFile = new JButton("");
		btnSaveFile.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/fileSave16.png")));
		btnSaveFile.setBackground(SystemColor.menu);
		toolBarFile.add(btnSaveFile);
		btnSaveFile.setToolTipText("Save File");
		btnSaveFile.setName("btnSaveFile");
		btnSaveFile.addActionListener(fileHandler);
	}

	public void setupToolbarsEdit(JPanel panelToolBars) {
		//add action listener
		JToolBar toolBarEdit = new JToolBar();
		panelToolBars.add(toolBarEdit);
		toolBarEdit.setFloatable(false);
		JLabel lblEdit = new JLabel("          ");
		toolBarEdit.add(lblEdit);

		btnCopy = new JButton("");
		btnCopy.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/copy16.png")));
		toolBarEdit.add(btnCopy);
		btnCopy.setToolTipText("Copy");
		btnCopy.setName("btnCopy");
		btnCopy.setBackground(SystemColor.menu);
		btnCopy.setEnabled(false);

		btnPaste = new JButton("");
		btnPaste.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/paint16.png")));
		toolBarEdit.add(btnPaste);
		btnPaste.setBackground(SystemColor.menu);
		btnPaste.setToolTipText("Paste");
		btnPaste.setName("btnPaste");
		btnPaste.setEnabled(false);

		btnUndo = new JButton("");
		btnUndo.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/undo16.png")));
		btnUndo.setBackground(SystemColor.menu);
		toolBarEdit.add(btnUndo);
		btnUndo.setToolTipText("Undo");
		btnUndo.setName("btnUndo");
		btnUndo.setEnabled(false);

		btnRedo = new JButton("");
		btnRedo.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/redo16.png")));
		btnRedo.setBackground(SystemColor.menu);
		toolBarEdit.add(btnRedo);
		btnRedo.setToolTipText("Redo");
		btnRedo.setName("btnRedo");
		btnRedo.setEnabled(false);
	}

	public void setupToolbarsView(JPanel panelToolBars) {
		JToolBar toolBarView = new JToolBar();
		panelToolBars.add(toolBarView);
		toolBarView.setFloatable(false);

		JLabel lblView = new JLabel("          ");
		toolBarView.add(lblView);

		JButton btnZoomIn = new JButton("");
		toolBarView.add(btnZoomIn);
		btnZoomIn.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/zoom16.png")));
		btnZoomIn.setToolTipText("Zoom In");
		btnZoomIn.setBackground(SystemColor.menu);
		btnZoomIn.setName("btnZoomIn");
		btnZoomIn.setEnabled(false);

		JButton btnZoomOut = new JButton("");
		toolBarView.add(btnZoomOut);
		btnZoomOut.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/zoomOut16.png")));
		btnZoomOut.setBackground(SystemColor.menu);
		btnZoomOut.setToolTipText("Zoom Out");
		btnZoomOut.setName("btnZoomOut");
		btnZoomOut.setEnabled(false);
	}

	public void setupToolbarsModel(JPanel panelToolBars) {
		JToolBar toolBarModel = new JToolBar();
		panelToolBars.add(toolBarModel);
		toolBarModel.setFloatable(false);
		JLabel lblModeling = new JLabel("          ");
		toolBarModel.add(lblModeling);

		AddButtonHandler btnHandler = new AddButtonHandler();

		btnComponent = new JToggleButton("");
		toolBarModel.add(btnComponent);
		btnComponent.addActionListener(btnHandler);
		btnComponent.setBackground(UIManager.getColor("Button.background"));
		btnComponent.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/component16.png")));
		btnComponent.setToolTipText("Add New Component");
		btnComponent.setName("btnComponent");
		// btnComponent.setEnabled(false);

		btnConnector = new JToggleButton("");
		toolBarModel.add(btnConnector);
		btnConnector.addActionListener(btnHandler);
		btnConnector.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/connector16.png")));
		btnConnector.setBackground(UIManager.getColor("Button.background"));
		btnConnector.setToolTipText("Add New Connector");
		btnConnector.setName("btnConnector");
		// btnConnector.setEnabled(false);

		btnArc = new JToggleButton("");
		toolBarModel.add(btnArc);
		btnArc.addActionListener(btnHandler);
		btnArc.setBackground(UIManager.getColor("Button.background"));
		btnArc.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/arc16.png")));
		btnArc.setToolTipText("Add New Arc");
		btnArc.setName("btnArc");
		// btnArc.setEnabled(false);

		btnPort = new JToggleButton("");
		toolBarModel.add(btnPort);
		btnPort.addActionListener(btnHandler);
		btnPort.setBackground(UIManager.getColor("Button.background"));
		btnPort.setIcon(new ImageIcon(SamFrame.class
				.getResource("/sam_view/resources/port16.png")));
		btnPort.setToolTipText("Add New Port");
		btnPort.setName("btnPort");
		// btnPort.setEnabled(false);
	}

	public void setupToolbarsAnalysis(JPanel panelToolBars){
		JToolBar toolBarAnalysis = new JToolBar();
		panelToolBars.add(toolBarAnalysis);
		toolBarAnalysis.setFloatable(false);
		JLabel lblAnalysis = new JLabel("          ");
		toolBarAnalysis.add(lblAnalysis);
		
		AnalysisModuleHandler btnHandler = new AnalysisModuleHandler();
		
		btnModelToPromela = new JButton("");
//		Dimension buttonSize = new Dimension(20,20);
//		btnModelToPromela.setMaximumSize(buttonSize);
		btnModelToPromela.setIcon(new ImageIcon(SamFrame.class.getResource("/sam_view/resources/anchor.png")));
		btnModelToPromela.setBackground(SystemColor.menu);
		toolBarAnalysis.add(btnModelToPromela);
		toolBarAnalysis.setToolTipText("Convert Elementary Level Petri Net to Promela");
		btnModelToPromela.setName("btnModelToPromela");
		btnModelToPromela.addActionListener(btnHandler);
		
		
	}
	//adds a tab to frame, to be used for new tabs
	public void addTab(String title) {
		SamCanvas canvas = new SamCanvas(title);
		canvases.add(canvas);
		pane.add(title, canvas);
		this.enableButtons(true);
	}
	
	/**
	 * adds a tab to frame, to be used for opening files
	 * @param title
	 * @param model
	 */
	public void addTab(String title, SamModel model) {
		SamCanvas canvas = new SamCanvas(title, model);
		canvases.add(canvas);
		pane.add(title, canvas);
		this.enableButtons(true);
		
	}
	
	public void addSubCompositionTab(Component comp){
		String title = comp.getName()+"::subComposition";
		if(comp.isSetSubComposition == true){
			addTab(title, comp.subCompositionModel);
		}else{
			comp.subCompositionModel = new SamModel();
			comp.getSubComposiionModel().setIsSubCompositionModelTure();
			comp.getSubComposiionModel().setParentComposition(comp);
			addTab(title, comp.subCompositionModel);
			comp.setSubCompositionModel(comp.subCompositionModel);
			
		}
	}
	
	//close current tab in frame
	public void closeTab(){
		
		int i = pane.getSelectedIndex();
		canvases.remove(i);
		
		if(i > -1){
			pane.remove(i);
		}
		
		if(canvases.isEmpty()){

			this.enableButtons(false);
		}
	}
	
	// returns JPanel inside currently selected tab
	public SamCanvas getCurrentCanvas() {
		int index = pane.getSelectedIndex();
		if (index >= 0) {
			SamCanvas selected = canvases.get(index);

			return selected;
		}

		return null;
	}
	
	//sets text of status bar
	public void setStatus(String text) {
		statusBar.setLabel(text);
	}
	public SamStatusBar getStatusBar() {
		return statusBar;
	}

	// use toggle enum so only one toggle button can be checked at a time
	public void setToggle(Toggle x) {
		if (x != Toggle.COMPONENT) {
			btnComponent.setSelected(false);
		}
		if (x != Toggle.CONNECTOR) {
			btnConnector.setSelected(false);
		}
		if (x != Toggle.ARC) {
			btnArc.setSelected(false);
		}
		if (x != Toggle.PORT) {
			btnPort.setSelected(false);
		}
	}
}
