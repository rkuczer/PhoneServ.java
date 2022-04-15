package com.company;
import java.io.*;
import java.net.*;
import java.util.Scanner;


public class PhoneClient {

    public static void main(String[] args) throws Exception {
        System.out.println("Client running...");

        int server_port = 2014;
        String server_ip   = "localhost";

        Scanner scanner = new Scanner(System.in);

        //Establish a connection with the Phone Server
        Socket connection_to_PServer = new Socket(server_ip, server_port);

        // obtaining input and out streams
        DataInputStream dis = new DataInputStream(connection_to_PServer.getInputStream());
        DataOutputStream dos = new DataOutputStream(connection_to_PServer.getOutputStream());

        while (true) //input loop
        {
            System.out.println(dis.readUTF());
            System.out.println("INPUT> ");  //prompts user for input
            String tosend = scanner.nextLine();
            dos.writeUTF(tosend); //writes the next line to output streamn

            if(tosend.equals("QUIT")) //when quit is typed it closes the connection
            {
                System.out.println("Closing this connection : " + connection_to_PServer);
                connection_to_PServer.close();
                System.out.println("Connection closed");
                break;//closes connection
            }
            String received_from_server = dis.readUTF();
            System.out.println(received_from_server); //prints the received info from the server
        }

    }

}