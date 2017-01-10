package com.example.samar.getmylocation;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * Created by samar on 05/01/17.
 */

public class ClientServer extends AsyncTask<Void, Void, Void> {

    String dstAddress;
    int dstPort;
    String response = "";
    TextView textResponse;
    JSONObject params;

    DataInputStream is;
    DataOutputStream os;

    boolean result = true;
    String noReset = "Could not reset.";
    String reset = "The server has been reset.";

    ClientServer(String addr, int port, JSONObject params, TextView textResponse) {
        dstAddress = addr;
        dstPort = port;
        this.textResponse = textResponse;
        this.params = params;
    }


    @Override
    protected Void doInBackground(Void... arg0) {

        try {
            Socket socket = new Socket(dstAddress,dstPort);
            String strParam=params.toString();

            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];
            int bytesRead;

            PrintWriter pw = new PrintWriter(os);
            pw.println(strParam);
            pw.flush();

           /* BufferedReader in = new BufferedReader(new InputStreamReader(is));
            JSONObject json = new JSONObject(in.readLine());
            if(!json.has("result")) {
                System.out.println(noReset);
                result = false;
            }
            response=is.readUTF();*/

            while ((bytesRead = is.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                response += byteArrayOutputStream.toString("UTF-8");
            }


            Log.d("##RESPONSE", response);

            is.close();
            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        Log.d("##RESPONSE", response);
        Log.d("##REQUEST-PARAMS", params.toString());

        textResponse.setText(response);
        super.onPostExecute(result);
    }

}
