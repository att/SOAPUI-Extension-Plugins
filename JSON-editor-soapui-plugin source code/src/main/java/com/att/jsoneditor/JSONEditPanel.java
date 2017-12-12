/**Copyright (c) 2017, AT&T Intellectual Property.  All other rights reserved.**/
package com.att.jsoneditor;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;


public class JSONEditPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	
	JTree jTree;
	
	public enum UpdateType { REPLACE, INSERT, APPEND, AS_CHILD };
	public enum AllowedOps { REPLACE, INSERT, APPEND, AS_CHILD, DELETE, RENAME, GET_JSON };
	
	
	public JSONEditPanel() {		
		setLayout(new BorderLayout());
		JSONJTreeNode root = new JSONJTreeNode(null, -1, new JsonNull());
		jTree = new JTree(root);
		jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);		
		add(new JScrollPane(jTree), BorderLayout.CENTER);
	}
	
	
	public void addTreeSelectionListener(TreeSelectionListener tsl) {
		jTree.addTreeSelectionListener(tsl);
	}
	
	
	@SuppressWarnings("unchecked")
	public void setJson(String json, UpdateType updateType) {
				
		TreePath selection = jTree.getSelectionPath();		
				
		if(selection == null) { 
			if(updateType == UpdateType.REPLACE) { 
				JsonElement root = new JsonParser().parse(json);
				JSONJTreeNode rootNode = new JSONJTreeNode(null, -1, root);
				jTree.setModel(new DefaultTreeModel(rootNode));		
				if(json.equals("")){
					jTree.setRootVisible(false);					
				}else{
					jTree.setRootVisible(true);	
				}
				
			} else { 
				JOptionPane.showMessageDialog(this,
						 "Only replace JSON and get JSON are supported when no node is selected.",
						 "Notice", JOptionPane.INFORMATION_MESSAGE);
			}
		} else { 
			JSONJTreeNode selectedNode = (JSONJTreeNode) selection.getLastPathComponent();
			JSONJTreeNode parent = (JSONJTreeNode)selectedNode.getParent();	
			
			if(parent == null) { 
				JsonElement root = new JsonParser().parse(json);
				JSONJTreeNode rootNode = new JSONJTreeNode(null, -1, root);
				jTree.setModel(new DefaultTreeModel(rootNode));						
				return;
			}
			JsonElement root = new JsonParser().parse(json);
			JSONJTreeNode replacementNode = new JSONJTreeNode(selectedNode.fieldName, selectedNode.index, root);
			int index = selectedNode.getParent().getIndex(selectedNode);
			selectedNode.removeFromParent();
			parent.insert(replacementNode, index);	
			((DefaultTreeModel)jTree.getModel()).reload(parent);
				
			
		}
	}
	
		
	public String getJson() {
		TreePath selection = jTree.getSelectionPath();
		String jsonPath=selection.toString();
		JSONJTreeNode node = null;
		if(selection == null) {
			((DefaultTreeModel)jTree.getModel()).reload();
			node = (JSONJTreeNode) jTree.getModel().getRoot();
		} else {
			
			jsonPath=jsonPath.replace("(OBJECT)", "");
			jsonPath=jsonPath.replace(",", ".");
			jsonPath=jsonPath.replace(" ", "");
			jsonPath=jsonPath.replace("(ARRAY).", "");
			
			jsonPath=jsonPath.substring(1, jsonPath.indexOf(':'));
			jsonPath="$"+jsonPath;
			
			
			
			((DefaultTreeModel)jTree.getModel()).reload(node);
			node = (JSONJTreeNode) selection.getLastPathComponent();
		}
		if(node != null)
			
			return jsonPath;
		else
			return null;
	}
	
	
	public List<AllowedOps> getAllowedOperations() {
		List<AllowedOps> result = new ArrayList<AllowedOps>();
		result.add(AllowedOps.REPLACE);
		result.add(AllowedOps.GET_JSON);
		
		TreePath selection = jTree.getSelectionPath();
		if(selection == null)
			return result;
		
		JSONJTreeNode selectedNode = (JSONJTreeNode) selection.getLastPathComponent();
		JSONJTreeNode parentNode = null;
		
		if(selectedNode != null) {
			result.add(AllowedOps.DELETE);
			parentNode = (JSONJTreeNode) selectedNode.getParent();
		}
		if(parentNode != null) {
			result.add(AllowedOps.APPEND);
			result.add(AllowedOps.INSERT);
		}
		if(selectedNode.dataType.equals(JSONJTreeNode.DataType.ARRAY) ||
				selectedNode.dataType.equals(JSONJTreeNode.DataType.OBJECT)	)
			result.add(AllowedOps.AS_CHILD);
		if((parentNode != null) && (parentNode.dataType.equals(JSONJTreeNode.DataType.OBJECT)))
			result.add(AllowedOps.RENAME);
		return result;
	}
}
