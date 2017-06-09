package com.sealwing.groupchat.main;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.sealwing.groupchat.outwriter.OutWriter;
import com.sealwing.groupchat.protocol.OutputProtocol;
import com.sealwing.groupchat.protocol.RecievingListener;
import com.sealwing.groupchat.reciever.ServerListener;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class ChatStarter extends Application implements OutputProtocol, RecievingListener {

	// VARIABLES FOR VIEW IS UNDER IT SCENE'S LINE

	private OutWriter outWriter;
	private ServerListener serverListener;

	private Stage primaryStage;
	private Scene loginScene;
	private Scene mainScene;

	private Socket socket;

	private String currentGroup;
	private ArrayList<String> userNicknames;
	private ArrayList<String> groupNames;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		userNicknames = new ArrayList<>();
		groupNames = new ArrayList<>();
		currentGroup = "none";

		primaryStage = stage;
		primaryStage.setTitle("Sealwing Chat");
		primaryStage.setResizable(false);
		primaryStage.initStyle(StageStyle.DECORATED);
		loginGrid = new GridPane();
		loginScene = new Scene(getLoginGrid());
		primaryStage.setScene(loginScene);
		primaryStage.show();

	}

	private void resettingStage() {
		Platform.runLater(() -> {
			setUpMainScene();
			primaryStage.setMinWidth(500);
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent arg0) {
					outEvent();
				}
			});
			primaryStage.setScene(mainScene);
			primaryStage.show();
		});
	}

	private void outEvent() {
		if (socket != null) {
			if (outWriter == null) {
				outWriter = new OutWriter(socket);
			}
			outWriter.writeOut(DISCONNECT, "out");
		}
	}

	private boolean logIn(String ip) {
		try {
			socket = new Socket(ip, 6666);
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	// RECIEVING LISTENER

	@Override
	public void connected() {
		resettingStage();
	}

	@Override
	public void connected(String[] groups) {
		resettingStage();
		for (String groupName : groups) {
			groupNames.add(groupName);
		}
	}

	@Override
	public void connectionError() {
		statusLabel.setText("Error from server");
	}

	@Override
	public void disconnected() {
		System.exit(0);
	}

	@Override
	public void userIn(String userNick) {
		userNicknames.add(userNick);
	}

	@Override
	public void userOut(String userNick) {
		userNicknames.remove(userNick);
	}

	@Override
	public void inGroup(String groupName) {
		userNicknames.clear();
		currentGroup = groupName;
	}

	@Override
	public void inGroup(String groupName, String[] users) {
		currentGroup = groupName;
		userNicknames.clear();
		for (String nickname : users) {
			if (!nickname.equals(groupName)) {
				userNicknames.add(nickname);
			}
		}
		chatArea.clear();
	}

	@Override
	public void inGroupError() {
		// TODO Auto-generated method stub

	}

	@Override
	public void newGroup(String groupName) {
		groupNames.add(groupName);
	}

	@Override
	public void removedGroup(String groupName) {
		groupNames.remove(groupName);
	}

	@Override
	public void chatMessage(String userNick, String message) {
		SimpleDateFormat format = new SimpleDateFormat("hh:mm");
		chatArea.appendText(userNick + " (" + format.format(new Date()) + "): " + message + '\n');
	}

	// LOGIN

	private GridPane loginGrid;
	private TextField nickField, ipField;
	private Label statusLabel;
	private Button signInButton;

	private final GridPane getLoginGrid() {
		loginGrid.setAlignment(Pos.CENTER);
		loginGrid.setHgap(10);
		loginGrid.setVgap(10);
		loginGrid.setPadding(new Insets(10, 10, 10, 10));
		addLogText();
		addLogControls();
		return loginGrid;
	}

	private final void addLogText() {
		nickField = new TextField();
		nickField.setOnKeyTyped(e -> clearStatus());
		ipField = new TextField();
		// DEFAULT IP
		ipField.setText("127.0.0.1");
		ipField.setOnKeyTyped(e -> clearStatus());

		statusLabel = new Label("");
		statusLabel.setId("status");
		loginGrid.add(new Text("Welcome"), 0, 0, 2, 1);
		loginGrid.add(new Label("Nickname:"), 0, 1);
		loginGrid.add(nickField, 1, 1);
		loginGrid.add(new Label("Server IP:"), 0, 2);
		loginGrid.add(ipField, 1, 2);
		loginGrid.add(statusLabel, 0, 3, 2, 1);
	}

	private final void addLogControls() {
		signInButton = new Button("Sign in");
		signInButton.setOnMouseClicked(e -> connection());
		HBox hBoxButton = new HBox(10);
		hBoxButton.setAlignment(Pos.BOTTOM_RIGHT);
		hBoxButton.getChildren().add(signInButton);
		loginGrid.add(hBoxButton, 1, 4);
	}

	private void connection() {
		if (logIn(ipField.getText())) {
			serverListener = new ServerListener(socket, this);
			serverListener.setDaemon(true);
			serverListener.start();
			outWriter = new OutWriter(socket);
			outWriter.writeOut(CONNECT, nickField.getText());
		} else {
			statusLabel.setText("Connection error occured");
		}
	}

	private void clearStatus() {
		statusLabel.setText("");
	}

	public void setErrorMessage(String message) {
		statusLabel.setText(message);
	}

	// MAIN SCENE

	private VBox mainBox;

	// inner stuff
	private MenuBar menus;

	// menus
	private Menu aboutMenu;
	private MenuItem infoItem;
	private Menu groupMenu;
	private MenuItem showGroupsItem;
	private MenuItem showUsersItem;

	// group info
	private TextField groupNameField;
	private PasswordField groupPasswordField;

	// group controllers
	private HBox groupControllers;
	private Button join;
	private Button create;
	private Button leave;

	private TextArea chatArea;
	private TextField messageArea;

	// CREATING

	private void setUpMainScene() {
		initMenus();
		initGroupController();
		initChat();
		addToBox();
		mainScene = new Scene(mainBox);
	}

	private void initMenus() {
		aboutMenu = new Menu("About");
		infoItem = new MenuItem("Info");
		infoItem.setOnAction(e -> showInfo());
		aboutMenu.getItems().add(infoItem);
		groupMenu = new Menu("Group");
		showGroupsItem = new MenuItem("Show groups");
		showGroupsItem.setOnAction(e -> showGroups());
		showUsersItem = new MenuItem("Show users");
		showUsersItem.setOnAction(e -> showUsers());
		groupMenu.getItems().addAll(showGroupsItem, showUsersItem);
		menus = new MenuBar();
		menus.getMenus().addAll(aboutMenu, groupMenu);
	}

	private void initGroupController() {
		join = new Button("Join");
		join.setPrefSize(60, 30);
		join.setOnAction(e -> join());
		create = new Button("Create");
		create.setPrefSize(60, 30);
		create.setOnAction(e -> create());
		leave = new Button("Leave");
		leave.setPrefSize(60, 30);
		leave.setOnAction(e -> leave());
		groupNameField = new TextField();
		groupPasswordField = new PasswordField();
		groupControllers = new HBox();
		groupControllers.setAlignment(Pos.CENTER);
		groupControllers.getChildren().addAll(groupNameField, groupPasswordField, join, create, leave);
	}

	private void join() {
		if (!currentGroup.equals("none")) {
			outWriter.writeOut(LEAVE_GROUP, currentGroup);
		}
		outWriter.writeOut(JOIN_GROUP, groupNameField.getText() + ":" + groupPasswordField.getText());
	}

	private void create() {
		outWriter.writeOut(CREATE_GROUP, groupNameField.getText() + ":" + groupPasswordField.getText());
	}

	private void leave() {
		if (!currentGroup.equals("none")) {
			outWriter.writeOut(LEAVE_GROUP, currentGroup);
			userNicknames.clear();
		}
	}

	private void initChat() {
		chatArea = new TextArea();
		chatArea.setEditable(false);
		chatArea.applyCss();
		chatArea.setMinWidth(300);
		messageArea = new TextField();
		messageArea.setMinSize(chatArea.getWidth(), 20);
		messageArea.setOnKeyPressed((KeyEvent e) -> {
			if (e.getCode().equals(KeyCode.ENTER)) {
				sendMessage();
			}
		});
	}

	private void sendMessage() {
		if (!currentGroup.equals("none")) {
			outWriter.writeOut(MESSAGE, messageArea.getText());
			messageArea.setText("");
		}
	}

	private void addToBox() {
		mainBox = new VBox();
		mainBox.getChildren().addAll(menus, groupControllers, chatArea, messageArea);
	}

	// MENUS EVENTS

	private void showInfo() {
		Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
		infoAlert.setContentText("This product is created by Araslanov Yegor.\nFell free to use");
		infoAlert.showAndWait().filter(response -> response == ButtonType.OK);
	}

	private void showUsers() {
		Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
		if (!currentGroup.equals("none") && userNicknames.size() > 0) {
			StringBuilder builder = new StringBuilder();
			for (String nickname : userNicknames) {
				if (!nickname.equals("none")) {
					builder.append(nickname + "\n");
				}
			}
			infoAlert.setContentText("Your current group is " + currentGroup + '\n' + builder.toString());
		} else {
			infoAlert.setContentText("None users in group");
		}
		infoAlert.showAndWait().filter(response -> response == ButtonType.OK);
	}

	private void showGroups() {
		Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
		if (groupNames.size() > 0) {
			StringBuilder builder = new StringBuilder();
			for (String groupName : groupNames) {
				builder.append(groupName + "\n");
			}
			infoAlert.setContentText(builder.toString());
		} else {
			infoAlert.setContentText("None groups on server");
		}
		infoAlert.showAndWait().filter(response -> response == ButtonType.OK);

	}

}
