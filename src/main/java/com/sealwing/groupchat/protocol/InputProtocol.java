package com.sealwing.groupchat.protocol;

public interface InputProtocol {
	final static int CONNECTION_ACCEPTED = 10;
	final static int DISCONNECTION_ACCEPTED = 11;
	final static int CONNECTION_DENIED = 12;

	final static int USER_JOINED_GROUP = 20;
	final static int USER_MISSED_GROUP = 21;

	final static int MESSAGE_SENT = 30;
	
	final static int GROUP_CREATED = 40;
	final static int GROUP_REMOVED = 41;
	
	final static int JOINING_ACCEPTED = 100;
	final static int JOINING_DENIED = 101;
	
	final static String SET = "UTF-8";
}
