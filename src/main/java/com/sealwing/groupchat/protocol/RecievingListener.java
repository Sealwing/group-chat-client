package com.sealwing.groupchat.protocol;

public interface RecievingListener {

	void connected();

	void connected(String[] groups);

	void connectionError();

	void disconnected();

	void userIn(String userNick);

	void userOut(String userNick);

	void inGroup(String groupName);

	void inGroup(String groupName, String[] users);

	void inGroupError();

	void newGroup(String groupName);

	void removedGroup(String groupName);

	void chatMessage(String userNick, String message);
}
