package com.sealwing.groupchat.protocol;

public interface OutputProtocol {
	final static int CONNECT = 10;
	final static int DISCONNECT = 11;
	
	final static int JOIN_GROUP = 20;
	final static int LEAVE_GROUP = 21;
	
	final static int MESSAGE = 30;

	final static int CREATE_GROUP = 40;
}
