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

package search;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import ui.XMLView;
import util.BareBonesBrowserLaunch;
import util.ImageFactory;

// instance of this class made for each search (passed search term in constructor)
// gets a list of SearchResultHtml objects and builds html from them. Displays in JEditorPane
public class SearchPanel extends JPanel {

	boolean raw = false;
	XMLView xmlView;
	
	String resultsText;
	
	ArrayList<SearchResultHtml> results = new ArrayList<SearchResultHtml>();
	
	public SearchPanel(File file, XMLView xmlView) {
	
		this.xmlView = xmlView;
		
		search(file);
		
		buildResultsPanel(file.getName());
	}
	
	public SearchPanel(String searchString, XMLView xmlView) {
		
		this.xmlView = xmlView;
		
		search(searchString);
		
		buildResultsPanel(searchString);
		
	}
	
	public void buildResultsPanel(String searchTerm) {
		resultsText = "<html><div style='padding: 5px 5px 5px 5px; width=300;'>";
		
		// show the top 10 results
		for (int i=0; (i<results.size() && i<10); i++) {
			// opens each original document to get context for search string
			resultsText = resultsText + results.get(i).getHtmlText();
		}
		
		if (results.isEmpty()) {
			resultsText = resultsText + "Your search for <b>" + searchTerm + "</b> returned no results";
		}
		
		resultsText = resultsText + "</div></html>";
		
		JEditorPane resultsPane;
		resultsPane = new JEditorPane("text/html", resultsText);
		// size!! Does f***-all!
		Dimension size = new Dimension(400, 400);
		resultsPane.setMaximumSize(size);
		resultsPane.setPreferredSize(size);
		resultsPane.setMinimumSize(size);
		this.setMaximumSize(size);
		this.setMinimumSize(size);
		this.setPreferredSize(size);

		resultsPane.setEditable(false);
		resultsPane.addHyperlinkListener(new ResultHyperLinkListener());
		resultsPane.setCaretPosition(0); // so that the scrollPane views the top

		JScrollPane resultsScrollPane = new JScrollPane(resultsPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		Icon noIcon = ImageFactory.getInstance().getIcon(ImageFactory.N0);
		JButton closeButton = new JButton("Close this window", noIcon);
		closeButton.addActionListener(new ClosePanelListener());
		
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(300, 500));
		this.add(resultsScrollPane, BorderLayout.CENTER);
		this.add(closeButton, BorderLayout.NORTH);
		
		this.setMinimumSize(new Dimension(300, 1000));
	}
	
	
	public void search(String searchString) {
		// try to search index
		try {
			SearchFiles.search(searchString, results);
		 
		} catch (FileNotFoundException ex) {
		
			int result = JOptionPane.showConfirmDialog(this, "Search index not found.\n" +
					"You need to create an index of all the files you want to search.\n"+
					"Please choose the root directory containing all your files","Index not found" ,JOptionPane.YES_NO_OPTION);
    		if (result == JOptionPane.YES_OPTION) {
    			xmlView.indexFiles();
    			
    			// assuming indexing went OK. Try searching again. 
    			try {
					SearchFiles.search(searchString, results);
				} catch (Exception e) {
					// user didn't index, or indexing failed. 
					JOptionPane.showMessageDialog(this, "No Search Index found", "No Search Index", JOptionPane.ERROR_MESSAGE);
				}
    		}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void search(File findMoreLikeThis) {
		// try to search index
		try {
			SearchFiles.search(findMoreLikeThis, results);
		 
		} catch (FileNotFoundException ex) {
		
			int result = JOptionPane.showConfirmDialog(this, "Search index not found.\n" +
					"You need to create an index of all the files you want to search.\n"+
					"Please choose the root directory containing all your files","Index not found" ,JOptionPane.YES_NO_OPTION);
    		if (result == JOptionPane.YES_OPTION) {
    			xmlView.indexFiles();
    			
    			// assuming indexing went OK. Try searching again. 
    			try {
					SearchFiles.search(findMoreLikeThis, results);
				} catch (Exception e) {
					// user didn't index, or indexing failed. 
					JOptionPane.showMessageDialog(this, "No Search Index found", "No Search Index", JOptionPane.ERROR_MESSAGE);
				}
    		}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void openSearchResultFile(File file) {
		xmlView.openThisFile(file);
	}
	
	public class ResultHyperLinkListener implements HyperlinkListener {
		public void hyperlinkUpdate (HyperlinkEvent event) {
			if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				if (event != null) {
					String filePath = event.getURL().toString();
					
					System.out.println("SearchPanel.hyperlinkUpdate: filePath: " + filePath);
					
					if (filePath.endsWith(".html") || filePath.endsWith(".htm")) {
						filePath = filePath.replace("http:/", "file://");
						BareBonesBrowserLaunch.openURL(filePath);
						return;
					}
					
					filePath = filePath.replace("http:/", "");
					
					File file = new File(filePath);
					//System.out.println(file.getAbsolutePath());
					
					openSearchResultFile(file);
				}
			}
		}
	}
	
	public class ClosePanelListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			xmlView.updateSearchPanel(null);
		}
	}
	
}
