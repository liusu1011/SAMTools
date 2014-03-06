/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * handles the file buttons on toolbar in samframe
 * 
 * saves/opens/close/creates files
 */
package SAMGUI.sam_controller;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JToggleButton;

import SAMGUI.sam_model.SamModel;
import SAMGUI.sam_model.XMLTransformer;
import SAMGUI.sam_view.SamCanvas;
import SAMGUI.sam_view.SamFrame;
import SAMGUI.sam_view.SamFrame.Toggle;

public class FileButtonHandler extends AbstractAction {
	private int count = 0;
	
	public FileButtonHandler() {
		putValue(NAME, "FileButtonHandler");
		putValue(SHORT_DESCRIPTION, "Handles button click from file toolbar");
	}

	public void actionPerformed(ActionEvent e) {
		JButton btn = (JButton) e.getSource();
		
		if(btn.getName().compareTo("btnCreateFile") == 0){
			count++;
			SamFrame.getInstance().addTab("tab" + count);
		}
		else if(btn.getName().compareTo("btnSaveFile") == 0){
			if(SamFrame.getInstance().getCurrentCanvas().getModel().getIsSubCompositionModel()){
				SamFrame.getInstance().getCurrentCanvas().getModel().getParentComposition()
				.setSubCompositionModel(SamFrame.getInstance().getCurrentCanvas().getModel());
			}else{
			FileDialog dlg = new FileDialog(SamFrame.getInstance(), "Save File", FileDialog.SAVE);
			dlg.setFile(SamFrame.getInstance().getCurrentCanvas().getName() + ".xml");
			dlg.setDirectory("C:\\");
		    dlg.setLocation(50, 50);

			dlg.setVisible(true);
			
			XMLTransformer xml = new XMLTransformer();
			xml.saveFile(SamFrame.getInstance().getCurrentCanvas().getModel(), dlg.getDirectory() + dlg.getFile());
			}
		}
		else if(btn.getName().compareTo("btnOpenFile") == 0){
			FileDialog dlg = new FileDialog(SamFrame.getInstance(), "Open File", FileDialog.LOAD);

		    dlg.setDirectory("C:\\");
		    dlg.setLocation(50, 50);


			dlg.setFile(".xml");
			dlg.setVisible(true);
			
			String path = dlg.getFile();
			String directory = dlg.getDirectory();
			XMLTransformer xml = new XMLTransformer();
			SamModel model = new SamModel();
			
			
			try {
				if(path != null && xml.openFile(model, directory + path)){
					SamFrame.getInstance().addTab(path, model);
				}
			} catch (Throwable e1) {
				e1.printStackTrace();
			}
			
			
			
		}
		
		else if(btn.getName().compareTo("btnCloseFile") == 0){
			SamFrame.getInstance().closeTab();;
			
			
		}
		
	}

}