package com.dpower.cintercom.util;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.dpower.cintercom.R;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class DpTypeTable {
    private static final String SC = "SC";
    private static final String TC = "TC";
    private static final String EN = "EN";

    public static String getType(Context context, String pos){
//		return getXmlInfo(context, pos, getLanguage(context), "ALARM_TYPE");
        return getXmlInfo(context, pos, getLanguage(context), R.xml.typeinfo);
    }

    public static String getArea(Context context,String pos) {
//		return getXmlInfo(context, pos, getLanguage(context), "POSITION");
        return getXmlInfo(context, pos, getLanguage(context), R.xml.areainfo);
    }

    public static String getSmartHomeMode(Context context, String pos){
        return getXmlInfo(context, pos, getLanguage(context), R.xml.smartmodeinfo);
    }

    public static String getSafeMode(Context context,String pos){
        return getXmlInfo(context, pos, getLanguage(context),R.xml.safemodeinfo);
    }

    public static String getMonitorError(Context context,String errno){
        String err = getXmlInfo(context, errno, getLanguage(context), R.xml.callerrorinfo);
//        if(err == null)
//            return context.getResources().getString(R.string.unkown_error) + errno;
        return err;
    }

    public static String getReason(Context context, String reason) {
        String rea = getXmlInfo(context, reason, getLanguage(context), R.xml.reason);
//        if (rea == null)
//            return context.getResources().getString(R.string.unkown_error) + rea;
        return rea;
    }

    private static String getLanguage(Context context){
        String language = EN;
        String country = context.getResources().getConfiguration().locale.getCountry();
        if(country.equals("CN")){
            language = SC;
        }else if(country.equals("TW")){
            language = TC;
        }else if(country.equals("HK")){
            language = TC;
        }
        return language;
    }

    private static String getXmlInfo(Context context, String value, String language, String area) {
        String name = null;

        boolean isLanguage = false;
        boolean isArea = false;

        XmlResourceParser xrp = context.getResources().getXml(R.xml.safeinfo);
        try {
            while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT){
                if(xrp.getEventType() == XmlResourceParser.START_TAG){
                    if(xrp.getName().equals(language)){
                        isLanguage = true;
                    }else if(xrp.getName().equals(area)){
                        isArea = true;
                    }else if("value".equals(xrp.getName())){
                        if(isLanguage && isArea && xrp.nextText().equals(value)){
                            return name;
                        }
                    }
                    else if("name".equals(xrp.getName())){
                        name = xrp.nextText();
                    }
                }
                else if(xrp.getEventType() == XmlResourceParser.END_TAG){
                    if(xrp.getName().equals(language)){
                        isLanguage = false;
                    }else if(xrp.getName().equals(area)){
                        isArea = false;
                    }
                }
                xrp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getXmlInfo(Context context, String value, String language,int xml) {
        String name = null;

        boolean isLanguage = false;

        XmlResourceParser xrp = context.getResources().getXml(xml);
        try {
            while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT){
                if(xrp.getEventType() == XmlResourceParser.START_TAG){
                    if(xrp.getName().equals(language)){
                        isLanguage = true;
                    }else if("value".equals(xrp.getName())){
                        if(isLanguage && xrp.nextText().equals(value)){
                            return name;
                        }
                    }
                    else if("name".equals(xrp.getName())){
                        name = xrp.nextText();
                    }
                }
                else if(xrp.getEventType() == XmlResourceParser.END_TAG){
                    if(xrp.getName().equals(language)){
                        isLanguage = false;
                    }
                }
                xrp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;
    }
}
