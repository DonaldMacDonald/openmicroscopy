/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

import tree.DataField;
import tree.DataFieldConstants;
import tree.DataFieldObserver;
import tree.IAttributeSaver;
import tree.IDataFieldObservable;
import tree.IDataFieldSelectable;
import util.BareBonesBrowserLaunch;
import util.ImageFactory;

// FormField dictates how dataField is displayed as a row in the complete form
// This FormField superclass merely arranges Name and Description (as a toolTip)
// Subclasses have eg TextFields etc

public class FormField extends JPanel implements DataFieldObserver{
	
	IDataFieldObservable dataFieldObs;
	IAttributeSaver dataField;
	
	// property change listener, property name
	public static final String HAS_FOCUS = "hasFocus";
	
	boolean textChanged = false;
	TextChangedListener textChangedListener = new TextChangedListener();
	FocusListener focusChangedListener = new FocusChangedListener();
	FocusListener componentFocusListener = new FormFieldComponentFocusListener();
	
	// swing components
	Box horizontalFrameBox;
	Box horizontalBox;
	JButton collapseButton;	
	JLabel nameLabel;
	JLabel descriptionLabel;
	Icon infoIcon;
	Icon wwwIcon;
	JButton descriptionButton;
	JButton urlButton;	// used (if url) to open browser 
	JButton collapseAllChildrenButton;
	
	Container childContainer;
	
	boolean childrenCollapsed = false; 	// used for root field only - to toggle all collapsed
	
	boolean highlighted = false;
	Color backgroundColour = null;
	
	Icon collapsedIcon;
	Icon notCollapsedIcon;
	
	// used in Diff (comparing two trees), to get a ref to all components, to colour red if different!
	ArrayList<JComponent> visibleAttributes = new ArrayList<JComponent>();
	
	public static final Dimension MINSIZE = new Dimension(30, 25);
	
	boolean showDescription = false;	// not saved, just used to toggle
	
	public FormField(IDataFieldObservable dataFieldObs) {
		
		this.dataFieldObs = dataFieldObs;
		this.dataFieldObs.addDataFieldObserver(this);
		
		// save a reference to the datafield as an IAttributeSaver (get and set-Attribute methods)
		if (dataFieldObs instanceof IAttributeSaver) {
			this.dataField = (IAttributeSaver)dataFieldObs;
		} else {
			throw new RuntimeException("FormField(dataField) needs dataField to implement IAttributeSaver");
		}
		
		//System.out.println("FormField Constructor: name is " + dataField.getName());
		
		// build the formField panel
		Border eb = BorderFactory.createEmptyBorder(3, 3, 3, 3);
		this.setBorder(eb);
		this.setLayout(new BorderLayout());
		//this.setFocusable(true);
		this.addMouseListener(new FormPanelMouseListener());
		
		horizontalBox = Box.createHorizontalBox();
		
		boolean subStepsCollapsed = dataField.isAttributeTrue(DataFieldConstants.SUBSTEPS_COLLAPSED);
		
		collapseButton = new JButton();
		collapseButton.setFocusable(false);
		collapseButton.setVisible(false);	// only made visible if hasChildren
		collapseButton.setBackground(null);
		unifiedButtonLookAndFeel(collapseButton);
		collapsedIcon = ImageFactory.getInstance().getIcon(ImageFactory.COLLAPSED_ICON);
		notCollapsedIcon = ImageFactory.getInstance().getIcon(ImageFactory.NOT_COLLAPSED_ICON);
		if (subStepsCollapsed) collapseButton.setIcon(collapsedIcon);
		else collapseButton.setIcon(notCollapsedIcon);
		collapseButton.setToolTipText("Collapse or Expand sub-steps");
		collapseButton.setBorder(new EmptyBorder(2,2,2,2));
		collapseButton.addActionListener(new CollapseListener());
		collapseButton.addMouseListener(new FormPanelMouseListener());
		
		nameLabel = new JLabel();
		nameLabel.addMouseListener(new FormPanelMouseListener());
		
		wwwIcon = ImageFactory.getInstance().getIcon(ImageFactory.WWW_ICON);
		urlButton = new JButton(wwwIcon);
		urlButton.setFocusable(false); // so it is not selected by tabbing
		unifiedButtonLookAndFeel(urlButton);
		urlButton.addActionListener(new URLclickListener());
		urlButton.setBackground(null);
		Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
		urlButton.setCursor(handCursor);
		urlButton.setBorder(eb);
		urlButton.setVisible(false);	// only made visible if url exists.
		
		
		
		descriptionLabel = new JLabel();
		visibleAttributes.add(descriptionLabel);
		infoIcon = ImageFactory.getInstance().getIcon(ImageFactory.INFO_ICON);
		descriptionButton = new JButton(infoIcon);
		unifiedButtonLookAndFeel(descriptionButton);
		descriptionButton.setFocusable(false); // so it is not selected by tabbing
		descriptionButton.addActionListener(new ToggleDescriptionListener());
		descriptionButton.setBackground(null);
		descriptionButton.setBorder(eb);
		descriptionButton.setVisible(false);	// only made visible if description exists.
		setDescriptionText(dataField.getAttribute(DataFieldConstants.DESCRIPTION)); 	// will update description label
		
		collapseAllChildrenButton = new JButton("Collapse/Expand All", notCollapsedIcon);
		collapseAllChildrenButton.setToolTipText("Collapse or Expand every field in this document");
		collapseAllChildrenButton.setBackground(null);
		collapseAllChildrenButton.addActionListener(new CollapseChildrenListener());
		collapseAllChildrenButton.setVisible(false);
		// visibility set by refreshRootField(boolean);
		
		/*
		 * complex layout required to limit the size of nameLabel (expands with html content)
		 * horizontalFrameBox holds collapseButton and then the BorderLayout JPanel
		 * nameLabel is in the WEST of this, then the horizontalBox for all other items is in the CENTER
		 */
		horizontalFrameBox = Box.createHorizontalBox();
		horizontalFrameBox.add(collapseButton);
		
		JPanel contentsNorthPanel = new JPanel(new BorderLayout());
		contentsNorthPanel.setBackground(null);
		contentsNorthPanel.add(nameLabel, BorderLayout.WEST);
		
		horizontalBox.add(descriptionButton);
		horizontalBox.add(urlButton);
		horizontalBox.add(collapseAllChildrenButton);
		horizontalBox.add(Box.createHorizontalStrut(10));
		contentsNorthPanel.add(horizontalBox, BorderLayout.CENTER);
		horizontalFrameBox.add(contentsNorthPanel);

		// refresh the current state
		nameLabel.setText(addHtmlTagsForNameLabel(dataField.getAttribute(DataFieldConstants.ELEMENT_NAME)));
		setDescriptionText(dataField.getAttribute(DataFieldConstants.DESCRIPTION));
		setURL(dataField.getAttribute(DataFieldConstants.URL));
		refreshBackgroundColour();
		refreshHighlighted();
		
		this.add(horizontalFrameBox, BorderLayout.NORTH);
		this.add(descriptionLabel, BorderLayout.WEST);
		
	}
	
	// called by dataField to notify observers that something has changed.
	public void dataFieldUpdated() {
		setNameText(addHtmlTagsForNameLabel(dataField.getAttribute(DataFieldConstants.ELEMENT_NAME)));
		setDescriptionText(dataField.getAttribute(DataFieldConstants.DESCRIPTION));
		setURL(dataField.getAttribute(DataFieldConstants.URL));
		
		//setHighlighted(dataField.isAttributeTrue(DataField.FIELD_SELECTED));
		refreshBackgroundColour();
	}
	
	// these methods called when user updates the fieldEditor panel
	public void setNameText(String name) {
		nameLabel.setText(name);
	}
	public void setDescriptionText(String description) {
		if ((description != null) && (description.trim().length() > 0)) {
			String htmlDescription = "<html><div style='width:200px; padding-left:30px;'>" + description + "</div></html>";
			nameLabel.setToolTipText(htmlDescription);
			descriptionButton.setVisible(true);
			descriptionLabel.setVisible(showDescription);
			descriptionLabel.setFont(XMLView.FONT_TINY);
			descriptionLabel.setText(htmlDescription);
		}
		else
		{
			nameLabel.setIcon(null);
			nameLabel.setToolTipText(null);
			descriptionButton.setVisible(false);
			descriptionLabel.setText(null);
			descriptionLabel.setVisible(false);
		}
	}
	public void setURL(String url) {
		if (url == null) {
			urlButton.setVisible(false);
			return;
		}
		if (url.length() > 0) {
			urlButton.setVisible(true);
			urlButton.setToolTipText(url);
		} else {
			urlButton.setVisible(false);
		}
	}
	
	
	// called when user clicks on panel
	public void setHighlighted(boolean highlight) {
		highlighted = highlight;
		refreshHighlighted();
	}
	private void refreshHighlighted() {
		if (highlighted) { 
			this.setBackground(XMLView.BLUE_HIGHLIGHT);
		}
		else {
			this.setBackground(backgroundColour);
		}
	}
	
	public class FormPanelMouseListener implements MouseListener {
		public void mouseEntered(MouseEvent event) {}
		public void mouseExited(MouseEvent event) {}
		public void mousePressed(MouseEvent event) {}
		public void mouseReleased(MouseEvent event) {}
		
		public void mouseClicked(MouseEvent event) {
			
			int clickType = event.getModifiers();
			if (clickType == XMLView.SHIFT_CLICK) {
				panelClicked(false);
			} else
				panelClicked(true);
		}
	}	
	
	// add this to every focusable component (button, field etc within the panel)
	public class FormFieldComponentFocusListener implements FocusListener {
		// when the component gets focus, the field is selected 
		// - this re-applies focus to the component - recursion is prevented by 
		// checking that this field is not already highlighted. 
		public void focusGained(FocusEvent e) {
			if (!highlighted)
				panelClicked(true);
		}
		public void focusLost(FocusEvent e) {}
	}
	// add this to every non-focusable component (Panels etc)
	public class FocusGainedPropertyChangedListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			if(evt.getPropertyName().equals(FormField.HAS_FOCUS)) {
				panelClicked(true);
			}
		}
	}
	
	public class URLclickListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			BareBonesBrowserLaunch.openURL(dataField.getAttribute(DataFieldConstants.URL));
		}
	}
	
	public class ToggleDescriptionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (showDescription) showDescription = false;
			else showDescription = true;
			
			descriptionLabel.setVisible(showDescription);
		}
	}
	
	public void panelClicked(boolean clearOthers) {
		if (dataFieldObs instanceof IDataFieldSelectable){
			System.out.println("FormField panelClicked() name:" + 
					dataField.getAttribute(DataFieldConstants.ELEMENT_NAME) + " clearOthers = " + clearOthers);
			((IDataFieldSelectable)dataFieldObs).dataFieldSelected(clearOthers);
		}
	}
	
	//public void checkForChildren() {
	//	refreshHasChildren(dataField.hasChildren());
	//}

	public void refreshHasChildren(boolean hasChildren) {
		if (!hasChildren) {
			// if this node has just lost it's children, need to un-collapse it
			// if it had children previously, the collapseButton will be visible
			if (collapseButton.isVisible()) {
				dataField.setAttribute(DataFieldConstants.SUBSTEPS_COLLAPSED, DataFieldConstants.FALSE, false);
				// reset to uncollapsed state
				collapseButton.setIcon(notCollapsedIcon);
			}
		}
		collapseButton.setVisible(hasChildren);
	}
	
	public class CollapseListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			
			boolean collapsed = dataField.isAttributeTrue(DataFieldConstants.SUBSTEPS_COLLAPSED); 
			
			// toggle collapsed state
			setSubStepsCollapsed(!collapsed);
		}
	}
	
	public void setSubStepsCollapsed(boolean collapsed) {
		if (hasChildren()) {
			dataField.setAttribute(DataFieldConstants.SUBSTEPS_COLLAPSED, Boolean.toString(collapsed), false);
			refreshTitleCollapsed();
		}
	}
	
	// called when formField panel is loaded into UI, so that it is displayed correctly
	// also called when user collapses or expands sub-steps
	public void refreshTitleCollapsed() {
		
		boolean collapsed = dataField.isAttributeTrue(DataFieldConstants.SUBSTEPS_COLLAPSED);
		collapseButton.setIcon(collapsed ? collapsedIcon : notCollapsedIcon);
		
		showChildren(!collapsed);
		
		// this is only needed when building UI (superfluous when simply collapsing)
		refreshHasChildren(hasChildren());
	}

	public boolean hasChildren() {
		return childContainer.getComponentCount()>0;
	}
	
	public void refreshRootField(boolean rootField) {
		// only show this button for the root FormField (ie if parent == null)
		collapseAllChildrenButton.setVisible(rootField);
	}
	
	public void setChildContainer(Container container) {
		childContainer = container;
	}
	public Container getChildContainer() {
		return childContainer;
	}
	public void showChildren(boolean visible) {
		childContainer.setVisible(visible);
	}
	
	public class CollapseChildrenListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			
			// toggle collapsed state of node and it's children
			childrenCollapsed = !childrenCollapsed;
			
			if (getParent() instanceof FormFieldContainer) {
				((FormFieldContainer)getParent()).collapseAllFormFieldChildrn(childrenCollapsed);
			}
			
			// expand this node
			setSubStepsCollapsed(false);
			
			collapseAllChildrenButton.setIcon(childrenCollapsed ? collapsedIcon : notCollapsedIcon);
		}	
	}
	
	public static void unifiedButtonLookAndFeel(AbstractButton b)
    {
        b.setMargin(new Insets(0, 2, 0, 3));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
    }
	
	private void refreshBackgroundColour() {
		backgroundColour = getColorFromString(dataField.getAttribute(DataFieldConstants.BACKGROUND_COLOUR));
		horizontalFrameBox.setBackground(backgroundColour);
	}
	
	// used to convert a stored string Colour attribute to a Color
	public static Color getColorFromString(String colourAttribute) {
		if (colourAttribute == null) 
			return null;
		
		try{
			String[] rgb = colourAttribute.split(":");
			int red = Integer.parseInt(rgb[0]);
			int green = Integer.parseInt(rgb[1]);
			int blue = Integer.parseInt(rgb[2]);
			return new Color(red,green,blue);
		} catch (Exception ex) {
			return null;
		}
	}
	
	
	/*
	 * returns the pixel-distance this panel-bottom from the top of FormDisplay.
	 * used to control scrolling to display this field within the ScrollPanel that contains FormDisplay
	 */ 
	public int getHeightOfPanelBottom() {
		if (isThisRootField()) {
			return this.getHeight();
		// if panel has a parent, this panel will be within a box, below the parent
		} else {
			int y = this.getHeight() + this.getY();		// get position within the box...
			// then add parent's position - will call recursively 'till root.
			//y = y + ((FormField)df.getNode().getParentNode().getDataField().getFormField()).getHeightOfPanelBottom();
			y = y + ((FormFieldContainer)this.getParent()).getYPositionWithinRootContainer();
			return y;
		}
	}
	
	private boolean isThisRootField() {
		return ((FormFieldContainer)this.getParent()).isRootContainer();
		
	}

	public ArrayList<JComponent> getVisibleAttributes() {
		return visibleAttributes;
	}
	
	private String addHtmlTagsForNameLabel(String text) {
		text = "<html>" + text + "</html>";
		return text;
	}
	
	
	public class TextChangedListener implements KeyListener {
		
		public void keyTyped(KeyEvent event) {
			textChanged = true;		// some character was typed, so set this flag
		}
		public void keyPressed(KeyEvent event) {}
		public void keyReleased(KeyEvent event) {}
	
	}
	
	public class FocusChangedListener implements FocusListener {
		
		public void focusLost(FocusEvent event) {
			if (textChanged) {
				JTextComponent source = (JTextComponent)event.getSource();
				
				setDataFieldAttribute(source.getName(), source.getText(), true);
				
				textChanged = false;
			}
		}
		public void focusGained(FocusEvent event) {}
	}
	

	// called to update dataField with attribute
	protected void setDataFieldAttribute(String attributeName, String value, boolean notifyUndoRedo) {
		dataField.setAttribute(attributeName, value, notifyUndoRedo);
	}
	

}
