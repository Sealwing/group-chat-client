package com.sealwing.groupchat.reciever;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import com.sealwing.groupchat.protocol.InputProtocol;
import com.sealwing.groupchat.protocol.RecievingListener;

public class ServerListener extends Thread implements InputProtocol {

	private InputStream in;

	private RecievingListener listener;

	private boolean running = true;

	public ServerListener(Socket socket, RecievingListener listener) {
		this.listener = listener;
		try {
			in = socket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (running) {
			try {
				if (in.available() > 0) {
					executeCommand(in.read());
				}
			} catch (IOException e) {

			}
		}
	}

	private void executeCommand(int command) throws IOException {
		if (command == CONNECTION_ACCEPTED) {
			connectionAccepted();
		} else {
			if (command == DISCONNECTION_ACCEPTED) {
				disconnectionAccepted();
			} else {
				if (command == CONNECTION_DENIED) {
					connectionDenied();
				} else {
					if (command == USER_JOINED_GROUP) {
						userJoinedGroup();
					} else {
						if (command == USER_MISSED_GROUP) {
							userMissedGroup();
						} else {
							if (command == MESSAGE_SENT) {
								messageSent();
							} else {
								if (command == GROUP_CREATED) {
									groupCreated();
								} else {
									if (command == GROUP_REMOVED) {
										groupRemoved();
									} else {
										if (command == JOINING_ACCEPTED) {
											joiningAccepted();
										} else {
											if (command == JOINING_DENIED) {
												joiningDenied();
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void connectionAccepted() throws IOException {
		String[] groups = (new String(read(), SET)).split(":");
		if (groups[0].equals("none")) {
			listener.connected();
		} else {
			listener.connected(groups);
		}
	}

	private void disconnectionAccepted() throws IOException {
		read();
		listener.disconnected();
	}

	private void connectionDenied() throws IOException {
		read();
		listener.connectionError();
	}

	private void userJoinedGroup() throws IOException {
		listener.userIn(new String(read(), SET));
	}

	private void userMissedGroup() throws IOException {
		listener.userOut(new String(read(), SET));
	}

	private void messageSent() throws IOException {
		String line = new String(read(), SET);
		int separateInd = line.indexOf(':');
		if (separateInd != -1) {
			listener.chatMessage(line.substring(0, separateInd), line.substring(separateInd + 1));
		}
	}

	private void groupCreated() throws IOException {
		String groupName = new String(read(), SET);
		listener.newGroup(groupName);
	}

	private void groupRemoved() throws IOException {
		String groupName = new String(read(), SET);
		listener.removedGroup(groupName);
	}

	private void joiningAccepted() throws IOException {
		String line = new String(read(), SET);
		if (line.split(":")[1].equals("none")) {
			listener.inGroup(line.split(":")[0]);
		} else {
			int separateInd = line.indexOf(':');
			String groupName = line.substring(0, separateInd);
			String[] users = (line.substring(separateInd + 1)).split(":");
			listener.inGroup(groupName, users);
		}
	}

	private void joiningDenied() throws IOException {
		read();
		listener.inGroupError();
	}

	private byte[] read() throws IOException {
		byte[] buffer = new byte[in.read()];
		in.read(buffer);
		return buffer;
	}
}