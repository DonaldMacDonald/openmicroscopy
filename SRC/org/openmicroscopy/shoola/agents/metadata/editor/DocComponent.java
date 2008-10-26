/*
 * org.openmicroscopy.shoola.agents.metadata.editor.DocComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;


//Third-party libraries
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.editor.EditFileEvent;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;

/** 
 * Component displaying the file annotation.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class DocComponent 
	extends JPanel
{

	/** The annotation hosted by this component. */
	private Object		data;
	
	/** Reference to the model. */
	private EditorModel	model;
	
	/** Button to delete the attachment. */
	private JButton		deleteButton;
	
	/** Component displaying the file name. */
	private JLabel		label;
	
	/**
	 * Formats the passed annotation.
	 * 
	 * @param f The value to format.
	 * @return See above.
	 */
	private String formatTootTip(FileAnnotationData f)
	{
		StringBuffer buf = new StringBuffer();
		buf.append("<html><body>");
		ExperimenterData exp = model.getOwner(f);
		if (exp != null) {
			buf.append("<b>");
			buf.append("Owner: ");
			buf.append("</b>");
			buf.append(EditorUtil.formatExperimenter(exp));
			buf.append("<br>");
		}
		buf.append("<b>");
		buf.append("Date Added: ");
		buf.append("</b>");
		buf.append(UIUtilities.formatWDMYDate(f.getLastModified()));
		buf.append("<br>");
		buf.append("<b>");
		buf.append("Size: ");
		buf.append("</b>");
		buf.append(UIUtilities.formatFileSize(f.getFileSize()));
		buf.append("<br>");
		buf.append("</body></html>");
		return buf.toString();
	}
	
	/** 
	 * Posts an event on the eventBus, with the attachment file's ID, name etc.
	 */
	private void postFileClicked()
	{
		if (data == null) return;
		if (data instanceof FileAnnotationData) {
			FileAnnotationData f = (FileAnnotationData) data;
			Registry reg = MetadataViewerAgent.getRegistry();		
			reg.getEventBus().post(new EditFileEvent(f.getFileName(), 
					f.getFileID(), f.getFileSize()));
		}
	}
	
	/** Initializes the {@link #deleteButton}. */
	private void initButton()
	{
		IconManager icons = IconManager.getInstance();
		deleteButton = new JButton(icons.getIcon(IconManager.MINUS));
		UIUtilities.unifiedButtonLookAndFeel(deleteButton);
		deleteButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		deleteButton.setToolTipText("Remove the attachment.");
		deleteButton.addActionListener(new ActionListener() {
		
			/**
			 * Fires a property change to delete the attachment.
			 * @see ActionListener#actionPerformed(ActionEvent)
			 */
			public void actionPerformed(ActionEvent e)
			{
				firePropertyChange(AnnotationUI.DELETE_ANNOTATION_PROPERTY,
						null, data);
			}
		
		});
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		if (model.isCurrentUserOwner(data)) initButton();
		label = new JLabel();
		label.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
		if (data == null) {
			label.setText(AnnotationUI.DEFAULT_TEXT);
		} else {
			if (data instanceof FileAnnotationData) {
				FileAnnotationData f = (FileAnnotationData) data;
				label.setToolTipText(formatTootTip(f));
				label.setText(EditorUtil.getPartialName(
						f.getContentAsString()));
			} else if (data instanceof File) {
				initButton();
				File f = (File) data;
				label.setText(f.getName());
				label.setForeground(Color.BLUE);
			}
		}
			
		label.addMouseListener(new MouseAdapter() {
		
			/** 
			 * Posts an event to edit the file.
			 * @see MouseAdapter#mouseReleased(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e)
			{
				if (e.getClickCount() == 2) postFileClicked();
			}
		
		});
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		add(label);
		if (deleteButton != null) {
			JToolBar bar = new JToolBar();
			bar.setBackground(UIUtilities.BACKGROUND_COLOR);
			bar.setFloatable(false);
			bar.setRollover(true);
			bar.setBorder(null);
			bar.setOpaque(true);
			bar.add(deleteButton);
			add(bar);
		}
	}
	
	/**
	 * Creates a new instance,
	 * 
	 * @param data	The document annotation. 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	DocComponent(Object data, EditorModel model)
	{
		if (model == null)
			throw new IllegalArgumentException("No Model.");
		this.model = model;
		this.data = data;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Returns the object hosted by this component.
	 * 
	 * @return See above.
	 */
	Object getData() { return data; }
	
}
