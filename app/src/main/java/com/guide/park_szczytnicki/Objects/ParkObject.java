package com.guide.park_szczytnicki.Objects;

import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.guide.park_szczytnicki.R;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Map;

public class ParkObject
{
    public final static int POINTER_NATURE  = R.drawable.pointer_nature;
    public final static int POINTER_HISTORY = R.drawable.pointer_history;
    public final static int POINTER_CULTURE = R.drawable.pointer_culture;
    public final static int POINTER_OTHER   = R.drawable.pointer_other;

    private final static int[] R_DRAWABLE_POINTERS = new int[]
            {POINTER_NATURE, POINTER_HISTORY, POINTER_CULTURE};

    private String name;
    //private int id = 0;
    private double latitude;
    private double longitude;
    private String heading;
    private float rate;
    private int rateNum;
    private int pointerType;
    private String info;
    private String additionalInfo;
    private int myRate;


    private Marker marker;


    public ParkObject() {}

    public ParkObject(double latitude, double longitude, String name, String heading, String info,
                      int pointerType, float rate, int rateNum, String additionalInfo, int myRate)
    {
        this.name           = name;
        //this.id             = id;
        this.latitude       = latitude;
        this.longitude      = longitude;
        this.heading        = heading;
        this.rate           = rate;
        this.rateNum        = rateNum;
        this.pointerType    = pointerType;
        this.info           = info;
        this.additionalInfo = additionalInfo;
        this.myRate         = myRate;
        /*
        if(pointerType > -1 && pointerType < R_DRAWABLE_POINTERS.length)
            pointerType = R_DRAWABLE_POINTERS[pointerType];
        else
            pointerType = POINTER_OTHER;
            */
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public void setRateNum(int rateNum) {
        this.rateNum = rateNum;
    }

    public void setPointerType(int pointerType) {
        this.pointerType = pointerType;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public double getLatitude()
    {    return latitude;    }

    public double getLongitude()
    {    return longitude;    }

    public String getName()
    {    return name;    }

    public String getHeading()
    {    return heading + String.format(Locale.UK,", Ocena: %3.1f", rate);    }

    public int getPointerType()
    {    return pointerType;    }

    public void setMarker(Marker marker)
    {    this.marker = marker;    }

    public void hideMarker()
    {    marker.setVisible(false);    }

    public float getRate()
    {    return rate;    }

    public int getRateNum()
    {    return rateNum;    }

    public String getInfo()
    {    return info;    }

    public String getAdditionalInfo()
    {    return additionalInfo;    }

    public int getMyRate()
    {    return myRate;    }

    public void setMyRate(int myRate)
    {    this.myRate = myRate;    }

    public void filterMarker(boolean[] filterCategories, String filterName, int filterRate, int filterMinRateNum, int filterMaxRateNum)
    {
        if (marker != null)
            if (        filterCategories[0] && pointerType == R_DRAWABLE_POINTERS[0]
                    ||  filterCategories[1] && pointerType == R_DRAWABLE_POINTERS[1]
                    ||  filterCategories[2] && pointerType == R_DRAWABLE_POINTERS[2]
                    ||  !name.toLowerCase().contains(filterName)
                    ||  rate    < filterRate
                    ||  rateNum < filterMinRateNum
                    ||  rateNum > filterMaxRateNum                                      )
                marker.setVisible(false);
            else
                marker.setVisible(true);
    }

    public String toJSON()
    {
        return   "\"" + this.getClass().getSimpleName()+ "\" : \"" + name + "\" : {\n"
                + toJSON(this)
                + "\n}";
    }


    public static <T> String toJSON(T object)
    {
        StringBuilder sb = new StringBuilder();
        try {
            for (Field field : object.getClass().getDeclaredFields())
                if (!Modifier.toString(field.getModifiers()).contains("static final") )
                {
                    sb  .append("\t\"").append(field.getName()).append("\" : ")
                            .append(field.get(object)).append(",\n");
                }
        } catch (IllegalAccessException e) {e.printStackTrace();}
        return sb.substring(0, sb.length()-2);
    }


    public static <T> T fromJSON(T object, DataSnapshot singleObject, Map<String, Field> attributes)
    {
        Field field;
        for (DataSnapshot singleEntry: singleObject.getChildren())
        {
            field = attributes.get(singleEntry.getKey());
            if(field != null && singleEntry.getValue() != null)
            {
                try {
                    field.set(object, Load.getClass(field, singleEntry.getValue().toString()));
                } catch (IllegalAccessException e) {e.printStackTrace();}
            }
        }
        return object;
    }

    @Override
    public String toString()
    {
        return toJSON();
    }

/*
    public static void main(String[] args)
    {
        StringBuilder sb = new StringBuilder();
        //sb.append("\"").append(name).append(id).append("\" : {\n");
        ParkObject po = new ParkObject(51.116, 17.087, "Nature",  "Nature thing",  ParkObject.POINTER_NATURE, 0, 101);
        //System.out.println(po.toJSON());
        //ParkObject po2 = Load.fromJSON(new ParkObject(), new Scanner(po.toJSON()), MAP_OF_ATTRIBUTES);
        //System.out.println(po2);

        //for (ParkMap.Entry<String, Field> e : MAP_OF_ATTRIBUTES.entrySet())
        {
            //System.out.println(e.getValue().getType().getSimpleName());
        }
        //Load.mainLoad();
    }
*/
}