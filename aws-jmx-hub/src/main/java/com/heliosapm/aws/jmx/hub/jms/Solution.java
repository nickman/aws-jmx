package com.heliosapm.aws.jmx.hub.jms;

import java.util.*;

public class Solution {
	 public static class LinkedListNode{
	        int val;
	        LinkedListNode next;
	    
	        LinkedListNode(int node_value) {
	            val = node_value;
	            next = null;
	        }
	    };
	    
	    public static LinkedListNode _insert_node_into_singlylinkedlist(LinkedListNode head, LinkedListNode tail, int val){
	        if(head == null) {
	            head = new LinkedListNode(val);
	            tail = head;
	        }
	        else {
	            tail.next = new LinkedListNode(val);
	            tail = tail.next;
	        }
	        return tail;
	    }
	    
	    
	    public static void main(String[] args) {
	    	LinkedListNode x = null;
	    	LinkedListNode root = null;
	    	boolean inited = false;
	    	for(int i = 2; i < 10; i++ ) {
	    		if(!inited) {
	    			x = new LinkedListNode(i);
	    			root = x;
	    			inited = true;
	    		} else {
	    			x.next = new LinkedListNode(i);	   
	    			x = x.next;
	    		}	    		
	    		//x = x.next;
	    	}
	    	System.out.println("Initial");
	    	print(root);
	    	LinkedListNode y = deleteEven(root);
	    	System.out.println("Modified");
	    	print(y);
	    }
	    
	    static void print(LinkedListNode node) {
	    	while(node!=null) {
	    		System.out.println(node.val);
	    		node = node.next;
	    	}
	    	
	    }
	    
	    static LinkedListNode deleteEven(LinkedListNode list) {
	    	LinkedListNode currentIn = list;
	    	LinkedListNode currentOut = null;
	    	LinkedListNode root = null;
	    	boolean inited = false;
	    	int x = 0;
	    	while(currentIn!=null) {
	    		x = currentIn.val;
	    		if(x%2!=0) {
	    			if(!inited) {
	    				currentOut = new LinkedListNode(x);
	    				root = currentOut;
	    				inited = true;
	    			} else {
		    			currentOut.next = new LinkedListNode(x);	   
		    			currentOut = currentOut.next;
	    			}
	    		}
	    		currentIn = currentIn.next;
	    	}
	    	return root;
	    	
	    	
	    	
//	        LinkedListNode currentIn = list;
//	        LinkedListNode currentOut = null;
//	        LinkedListNode result = null;
//	        int x = 0;
//	        do {
//	            x = currentIn.val;
//	            if(x%2!=0) {
//	                if(currentOut==null) {
//	                    currentOut = new LinkedListNode(x);
//	                } else {
//	                    currentOut.next = new LinkedListNode(x);
//	                }
//	            }
//	            currentIn = currentIn.next;
//	        } while (currentIn!=null);
	        //return currentOut;
	    }	    
 
}
