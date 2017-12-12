/**Copyright (c) 2017, AT&T Intellectual Property.  All other rights reserved.**/
package com.att.jsoneditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.plugins.auto.PluginResponseEditorView;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;
import com.eviware.soapui.support.editor.xml.XmlEditor;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.support.JsonUtil;



import net.sf.json.JSON;
import net.sf.json.JSONException;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

@PluginResponseEditorView(viewId = "JSONTreeView")
public class JSONResponseEditor extends AbstractXmlEditorView implements PropertyChangeListener,TreeSelectionListener{

	private ModelItem modelItem;
	private HttpRequestInterface httpRequest;	
	private JPanel content;	
	private String initialJson;	
	private JTextArea jsonTextArea;
	JSONEditPanel treeView;
	Map<JSONEditPanel.AllowedOps, JButton> treeChangeButtons = new HashMap<JSONEditPanel.AllowedOps, JButton>();
	
	public JSONResponseEditor(Editor<?> editor, ModelItem modelItem) {
		super("JSON Tree View", (XmlEditor) editor,
				"JSONTreeView");
		this.modelItem = modelItem;
		
		if (modelItem instanceof HttpRequestInterface) {
			
			httpRequest = (HttpRequestInterface) modelItem;		
			httpRequest.addPropertyChangeListener(this);
		}
	}

	@Override
	public JComponent getComponent() {
		
		HttpResponse httpResponse;
        if (httpRequest != null ) { 
			httpResponse = httpRequest.getResponse();
	    }	
		else{ 
			httpResponse=null;
		}
		
		if (httpResponse == null || httpResponse.getContentAsString() == null) { 
			initialJson="";			
	    } else {
	        	
	        if (JsonUtil.seemsToBeJsonContentType(httpResponse.getContentType())) {
	            try {
	                JSON json = new JsonUtil().parseTrimmedText(httpResponse.getContentAsString());
	                if (json.isEmpty()) {
	                	initialJson = "";
						
	                } else { 
	                	initialJson = json.toString(3);
						
	                }
	            } catch (JSONException e) {
	            	initialJson = httpResponse.getContentAsString();
					
	            }
	            
	        } else { 
	        	initialJson="";
				
	        }
	    }
		
				
		initialJson=initialJson.trim().replaceAll("\n", "");
		
		
		if (content == null) { 
			content= new JPanel();
		
			// Edit panel is the center panel
			JPanel centerPanel = new JPanel();
			BoxLayout centerPanelLayout = new BoxLayout(centerPanel,
					BoxLayout.Y_AXIS);
			centerPanel.setLayout(centerPanelLayout);
			JLabel label = new JLabel("JSON Tree View:", SwingConstants.LEFT);
			label.setAlignmentX(JFrame.LEFT_ALIGNMENT);
			centerPanel.add(label);
			centerPanel.setSize(600, 300);
			treeView = new JSONEditPanel();			
			treeView.setJson(initialJson, JSONEditPanel.UpdateType.REPLACE);			
			treeView.setAlignmentX(JFrame.LEFT_ALIGNMENT);
			treeView.addTreeSelectionListener(this);
			centerPanel.add(treeView);
			content.add(centerPanel, BorderLayout.CENTER);
	
			JPanel bottomPanelWrapper = new JPanel();
			BoxLayout wrapperLayout = new BoxLayout(bottomPanelWrapper,
					BoxLayout.X_AXIS);
			bottomPanelWrapper.setLayout(wrapperLayout);
	
			JPanel bottomPanel = new JPanel();
			BoxLayout bottomPanelLayout = new BoxLayout(bottomPanel,
					BoxLayout.Y_AXIS);
			bottomPanel.setLayout(bottomPanelLayout);
			Component rigid = Box.createRigidArea(new Dimension(0, 10));
			bottomPanel.add(rigid);
	
			JPanel taPanel = new JPanel();
			taPanel.setLayout(new BoxLayout(taPanel, BoxLayout.Y_AXIS));
			taPanel.setAlignmentX(JFrame.RIGHT_ALIGNMENT);
			label = new JLabel("JSON Path Expression:", SwingConstants.LEFT);
			label.setAlignmentX(JFrame.LEFT_ALIGNMENT);
			taPanel.add(label);
			JPanel scrollWrapper = new JPanel();
			scrollWrapper.setLayout(new BoxLayout(scrollWrapper, BoxLayout.X_AXIS));
			jsonTextArea = new JTextArea();
			jsonTextArea.setText("");
			jsonTextArea.setPreferredSize(new Dimension(600, 70));
			JScrollPane textScroller = new JScrollPane(jsonTextArea);
			textScroller.setAlignmentX(JFrame.LEFT_ALIGNMENT);
			taPanel.add(textScroller);
			bottomPanel.add(taPanel);
			bottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));
	
			// Add the Buttons
			JPanel buttonPanel = new JPanel();
			
			
			BoxLayout horizontalLayout = new BoxLayout(buttonPanel,
					BoxLayout.X_AXIS);
			buttonPanel.setLayout(horizontalLayout);
			buttonPanel.setAlignmentX(JFrame.RIGHT_ALIGNMENT);
			
			
	
			JButton button = new JButton(new CopyJsonAction(CopyJsonAction.Direction.GET,
					treeView, jsonTextArea));
			treeChangeButtons.put(JSONEditPanel.AllowedOps.GET_JSON, button);
			buttonPanel.add(button);
			bottomPanel.add(buttonPanel);
			bottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));
	
			
	
			bottomPanelWrapper.add(Box.createRigidArea(new Dimension(10, 10)));
			bottomPanelWrapper.add(bottomPanel);
			bottomPanelWrapper.add(Box.createRigidArea(new Dimension(10, 10)));
	
			content.add(bottomPanelWrapper, BorderLayout.SOUTH);
			
			
		}
		
		
		return content;	
	}

	@Override
	public void setEditable(boolean enabled) {
	}

	@Override
	public boolean saveDocument(boolean validate) {
		return true;
	}

	private static class CopyJsonAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public enum Direction {
			 GET(
					"Get",
					"Get JSON from the tree view or selected tree node into text area.");

			final String shortName;
			final String description;

			private Direction(String shortName, String description) {
				this.shortName = shortName;
				this.description = description;
			}
		};

		private final Direction direction;
		private final JSONEditPanel jEditPanel;
		private final JTextArea jTextArea;

		public CopyJsonAction(Direction direction, JSONEditPanel jEditPanel,
				JTextArea jTextArea) {
			super(direction.shortName);
			putValue(SHORT_DESCRIPTION, direction.description);
			this.direction = direction;
			this.jEditPanel = jEditPanel;
			this.jTextArea = jTextArea;
		}

		@Override
		public void actionPerformed(ActionEvent e) {		
			
				jTextArea.setText(jEditPanel.getJson());				
			
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
		updateButtonStates();
	}

	private void updateButtonStates() {
		// Update the button controls to enable and disable tree manipulation
		// operations
		List<JSONEditPanel.AllowedOps> allowedOps = treeView
				.getAllowedOperations();
		for (Entry<JSONEditPanel.AllowedOps, JButton> entry : treeChangeButtons
				.entrySet()) {
			if (allowedOps.contains(entry.getKey()))
				entry.getValue().setEnabled(true);
			else
				entry.getValue().setEnabled(false);
		}
	}
	
	public void propertyChange(PropertyChangeEvent evt) {    	
    	if (evt.getPropertyName().equals(AbstractHttpRequestInterface.RESPONSE_PROPERTY) ) {    		
            setEditorContent(((HttpResponse) evt.getNewValue()));
                        
        }
    }
	
	 protected void setEditorContent(HttpResponse httpResponse) {
		 	  
			if (httpResponse == null || httpResponse.getContentAsString() == null) {
				initialJson="";
				
		    } else {		        
		
		        if (JsonUtil.seemsToBeJsonContentType(httpResponse.getContentType())) {
		            try {
		                JSON json = new JsonUtil().parseTrimmedText(httpResponse.getContentAsString());
		                if (json.isEmpty()) {
		                	initialJson = "";
							
		                } else {
		                	initialJson = json.toString(3);							
							
		                }
		            } catch (JSONException e) {
		            	initialJson = httpResponse.getContentAsString();
						
		            }
		            
		        } else { 
		        	initialJson="";
					
		        }
		    }
			
			initialJson=initialJson.trim().replaceAll("\n", "");
			treeView.setJson(initialJson, JSONEditPanel.UpdateType.REPLACE);
	 }
}
