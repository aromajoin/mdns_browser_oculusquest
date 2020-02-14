/************************************************************************************
Aromajoin Corporation
Trung: as of 2020/2/14 (Valentine)
************************************************************************************/

using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Net;
using System.Net.NetworkInformation;
using System.Net.Sockets;
using System.IO;
using System;
using Ping = UnityEngine.Ping;

public class mDNSBrowserOculusQuest : MonoBehaviour
{
    // Use this for initialization
    void Start () {

        // get the context of the unity application
        AndroidJavaClass unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        AndroidJavaObject activity = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity");
        AndroidJavaObject context = activity.Call<AndroidJavaObject>("getApplicationContext");

        Debug.Log("Android is to be called");

        // get the Android library object
        AndroidJavaClass cls = new AndroidJavaClass("com.example.mdns_browser.mDNSBrowser");
        AndroidJavaObject obj = new AndroidJavaObject("com.example.mdns_browser.mDNSBrowser", context);

        // start browsing mDNS services
        obj.Call("startScanService");

        // wait for 10 seconds before reading the result from the library
        StartCoroutine(WaitCoroutine(obj));

        Debug.Log("Android is already called");

    }

    IEnumerator WaitCoroutine(AndroidJavaObject obj){
        yield return new WaitForSeconds(10);
        string[] listOfSerials = obj.Call<string[]>("getListOfSerials");

        Debug.Log("ASN: " + listOfSerials[0]);

    }

/*
 * some reference for Pinging method in Unity if some needs.
 * Though this is not a perfect solution, but might work with calling Coroutines (asynchronously running functions)
    Ping ping;

    string ipBase = getIPAddress();
    string[] ipParts = ipBase.Split('.');
    ipBase = ipParts[0] + "." + ipParts[1] + "." + ipParts[2] + ".";

    for (int i = 1; i < 255; i++)
    {
        string ip = ipBase + i.ToString();

        ping = new Ping(ip);

        while (!ping.isDone)
        {                
        }
        if (ping.time != -1)
        {
            try
            {
                IPHostEntry hostEntry = Dns.GetHostEntry(ip);
                Debug.Log(ip + ": " + hostEntry.HostName);

                var httpWebRequest = (HttpWebRequest)WebRequest.Create("http://" + ip + ":1003/as2/stop_all");
                httpWebRequest.ContentType = "application/json";
                httpWebRequest.Method = "POST";

                var httpResponse = (HttpWebResponse)httpWebRequest.GetResponse();

                using (var streamReader = new StreamReader(httpResponse.GetResponseStream()))
                {
                    var result = streamReader.ReadToEnd();
                    if (result.Contains("ASN"))
                    {
                        Debug.Log("Found Aroma Shooter: " + result + "at the IP = " + ip);
                    }
                }
            }
            catch (Exception x)
            {
                Console.Write(x.StackTrace);
            }

        }
    }

    --- Get IP Address function ---
    public static string getIPAddress()
    {
        IPHostEntry host;
        string localIP = "";
        host = Dns.GetHostEntry(Dns.GetHostName());
        foreach (IPAddress ip in host.AddressList)
        {
            if (ip.AddressFamily == AddressFamily.InterNetwork)
            {
                localIP = ip.ToString();
            }
        }
        return localIP;
    }
*/
}
