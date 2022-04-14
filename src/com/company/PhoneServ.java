package com.company;

import java.io.*;
import java.net.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.io.FileWriter;

public class PhoneServ {
    public static void main(String[] args) throws Exception {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); //sets the date in a pattern readable for us
        int my_port = 2014; //port to listen to
        ServerSocket listener = new ServerSocket(my_port); //creates server socket to connect to
        System.out.println("Server is running.."); //prompts to show its running
        // Begin infinite loop for server to wait for client connection requests
        while (true)
        {
            Socket clientSocket = null;
            try { //handle connection method from example
                // Handle incoming client request
                clientSocket = listener.accept();
                LocalDateTime now = LocalDateTime.now(); //gets time set from before
                System.out.println(dtf.format(now) + "-> Client has connected to Multi Threaded PhoneServer from: " + clientSocket);
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());// creates input streams to store string data from user
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                Thread thread = new ClientThread(clientSocket, dis, dos);
                thread.start(); //starts new client thread with data stream
            }
            catch (Exception e) { //catches error
                clientSocket.close();
            }
        }
    }
}


class ClientThread extends Thread //thread created for client that has data input stream, constructor, and a exported method run
{
    private final DataInputStream dis;
    private final DataOutputStream dos;
    final Socket s;
    // Constructor
    public ClientThread(Socket s, DataInputStream dis, DataOutputStream dos) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
    }
    @Override
    public void run()
    {
        String data_file_name = "phone_data.txt";
        String tmp_data_file_name = "phone_data.tmp";
        String received = "";
        String toreturn = "";
        String name_deleted = "";
        String keys[];
        String get_tokens[];
        while (true) {
            try {

                dos.writeUTF("\n**** PHONE SERVER ****\n-> STORE <name> <number> \t(where <number> = xxx-xxxx)\n" +
                        "-> GET <name>\n-> REMOVE <name>\n-> QUIT");

                received = dis.readUTF();
                keys = received.split(" ");

                if(received.equals("QUIT"))
                {
                    System.out.println("Client " + this.s + " is disconnecting from server...");
                    this.s.close();
                    System.out.println("Connection closed to client: " + this.s);
                    break;
                }

                switch(keys[0]) {

                    case "STORE":
                        FileWriter writer = new FileWriter(data_file_name, true);
                        writer.write(keys[1] + " " + keys[2] + "\n");
                        writer.close();
                        toreturn = "'" + keys[1] + " " + keys[2] + "' has been stored in local text file.";
                        dos.writeUTF(toreturn);
                        break;

                    case "GET":
                        File phone_data = new File(data_file_name);
                        Scanner reader = new Scanner(phone_data);

                        while (reader.hasNextLine()) {
                            String data = reader.nextLine();
                            get_tokens = data.split(" ");

                            // If line in text file matches client's request, send it back to client.
                            if (get_tokens[0].contains(keys[1])) {
                                toreturn = get_tokens[0] + " phone # is -> " + get_tokens[1];
                                break;
                            }

                            toreturn = "No name was found with that request.";
                        }

                        reader.close();
                        dos.writeUTF(toreturn);
                        break;

                    case "REMOVE":
                        FileWriter writer_tmp = new FileWriter(tmp_data_file_name);
                        File phone_data_tmp = new File(data_file_name);
                        Scanner reader_tmp = new Scanner(phone_data_tmp);

                        while (reader_tmp.hasNextLine()) {
                            String line_read = reader_tmp.nextLine();
                            get_tokens = line_read.split(" ");

                            if (get_tokens[0].contains(keys[1])) {
                                // dont write to the new temp file
                                name_deleted = get_tokens[0] + " was deleted from the database.";
                                continue;

                            } else {

                                writer_tmp.write(get_tokens[0] + " " + get_tokens[1] + "\n");
                                System.out.println("Writing to temp file " + get_tokens[0] + " " + get_tokens[1]);
                            }
                        }

                        writer_tmp.close();
                        reader_tmp.close();

                        // Delete original file & replace with new file
                        File original_file = new File(data_file_name);
                        original_file.delete();

                        File oldName = new File(tmp_data_file_name);
                        File newName = new File(data_file_name);
                        oldName.renameTo(newName);

                        dos.writeUTF(name_deleted);
                        break;

                    default:
                        dos.writeUTF("Erroneous message received. That is not a valid option. Please try again.");
                        break;

                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
