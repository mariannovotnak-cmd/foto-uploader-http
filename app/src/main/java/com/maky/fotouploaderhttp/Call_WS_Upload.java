package com.maky.fotouploaderhttp;

import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Call_WS_Upload {


	public Call_WS_Upload() {
		super();
	}


	public HashMap<String,Object> Call(Object par){

        Log.i("Call_WS_Upload", "- Call");

        HashMap<String,Object> resp = new HashMap<String, Object>();

        String URLA =  "http://" + ((HashMap) par).get("URL").toString();
        String URL = URLA + "/uploader/Upload.php?wsdl";
        String NAMESPACE = URLA + "/uploader/urn:Target";
        String METHOD = ((HashMap) par).get("serviceName").toString();

        String SOAP_ACTION = URLA + "/uploader/urn:Target/" + METHOD;

        HashMap<String,String> vstup = (HashMap) par;


        try {
			// SoapEnvelop.VER11 is SOAP Version 1.1 constant

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			SoapObject request = new SoapObject(NAMESPACE, METHOD);


            Set<String> keyset = vstup.keySet();
            Iterator<String> it = keyset.iterator();
            while(it.hasNext()){
                String key = it.next();
                request.addProperty(key, ((HashMap) par).get(key));
            }

			// bodyOut is the body object to be sent out with this envelope
			envelope.bodyOut = request;
			HttpTransportSE transport = new HttpTransportSE(URL);
			transport.debug = true;
            Log.i("Call_WS_Upload", "- Volam");
			try {
				// transport.call(NAMESPACE + SOAP_ACTION_PREFIX + METHOD,
				// envelope);
				// transport.call(NAMESPACE + SOAP_ACTION_PREFIX + METHOD,
				// envelope)	;
				transport.call(SOAP_ACTION, envelope);
			} catch (IOException e){
                e.printStackTrace();
                Log.i("Call_WS_Upload", "- Chyba 1");
            } catch (XmlPullParserException e) {
				e.printStackTrace();
				//ignorujem ak nefunguje konektivita
                Log.i("Call_WS_Upload", "- Chyba 2");
			}
            // bodyIn is the body object received with this envelope
			if (envelope.bodyIn != null) {
                // getProperty() Returns a specific property at a certain
                // index.

				if (envelope.bodyIn instanceof SoapFault)
				{
					final SoapFault sf = (SoapFault) envelope.bodyIn;
					Log.i("Call_WS_Upload.Call", "- Fault:" + sf.faultstring);
				}

                SoapObject objectsoap = (SoapObject) envelope.bodyIn;
                Object property = objectsoap.getProperty(0);
                // SoapPrimitive resultSOAP = (SoapPrimitive)

                // objectsoap.getProperty(0);
                String vysledok = property.toString();

                Log.i("Call_WS_Upload", "- Zavolane");
                Log.i("Call_WS_Upload", "- Vysledok:"+vysledok);

                resp.put("vysledok", vysledok);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

}

