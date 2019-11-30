package com.guide.park_szczytnicki.Objects;

import java.io.*;
import java.util.*;
import com.guide.park_szczytnicki.R;

//@SuppressWarnings("FieldCanBeLocal")
public class Upload
{
    private static final Map<String, Integer> POINTER_TYPES = new HashMap<>();
    static
    {
        POINTER_TYPES.put("N", R.drawable.pointer_nature);
        POINTER_TYPES.put("H", R.drawable.pointer_history);
        POINTER_TYPES.put("C", R.drawable.pointer_culture);
        POINTER_TYPES.put("O", R.drawable.pointer_other);

    }

    private static final String REGEX_SEPARATORS = ", |: ";
    private static final String REGEX_COORDINATES_SEPARATORS = ", | ";
    private static final String ADDITIONAL_NAME_TEXT = "Obiekt jest również znany pod nazwą ";

    public static Object[] getData(Scanner sc)
    {
        LinkedList<Object> objects = new LinkedList<>();
        while(sc.hasNextLine())
        {
            LinkedList<Object> objectInfo = new LinkedList<>();
            String line = sc.nextLine();
            String[] s = line.split(REGEX_SEPARATORS);
            if (POINTER_TYPES.containsKey(s[0]))
            {
                objectInfo.add(POINTER_TYPES.get(s[0]));
                objectInfo.add(s[1]);
                line = sc.nextLine();

                StringBuilder sb = new StringBuilder(265);
                if (line.charAt(0) == '(')
                {
                    //objectInfo.add(removeBrackets(line));
                    sb.append(ADDITIONAL_NAME_TEXT).append(removeBrackets(line)).append(".\n");
                    line = sc.nextLine();
                    objectInfo.add(parseFloats(line.split(REGEX_COORDINATES_SEPARATORS)));
                }
                else if (line.charAt(0) == '5')
                    objectInfo.add(parseFloats(line.split(REGEX_COORDINATES_SEPARATORS)));

                while(sc.hasNextLine() && !(line = sc.nextLine()).equals(""))
                    sb.append("\t").append(line).append("\n");

                objectInfo.add(sb.toString());
                objects.add(objectInfo.toArray());
            }
        }
        return objects.toArray();
    }

    public static String removeBrackets(String s)
    {    return s.replaceAll("[(]|[)]", "");    }

    public static Float[] parseFloats(String [] doubleStrings)
    {
        Float[] floats = new Float[doubleStrings.length];
        for (int i = 0; i < doubleStrings.length; i++)
            floats[i] = defaultRound(Float.parseFloat(doubleStrings[i]));

        return floats;
    }

    private static final int    ROUND   = 100_000;
    private static final float  ROUND_F = 100_000F;
    private static final double ROUND_D = 100_000.;
    public static float defaultRound(float value)
    {    return (int)(value * ROUND) / ROUND_F;    }

    public static double defaultRound(double value)
    {    return (int)(value * ROUND) / ROUND_D;    }

    public static Scanner getFileScanner(String fullFilePath) throws FileNotFoundException
    {
        return new Scanner(new DataInputStream(new BufferedInputStream(new FileInputStream(
                new File(fullFilePath)))));
    }

    public static List<ParkObject> create(Scanner sc)
    {   return create(getData(sc));   }

    public static List<ParkObject> create(Object object)
    {
        List<ParkObject> parkObjects = new LinkedList<>();
        if (object.getClass() == Object[].class)
            for (Object infoParkObject : (Object[]) object)
                if (infoParkObject.getClass() == Object[].class)
                    parkObjects.add(createParkObjectFromInfo((Object[]) infoParkObject));

        ratesGenerator(parkObjects);
        return parkObjects;
    }

    public static void ratesGenerator(List<ParkObject> parkObjects)
    {
        double maxRate  = 6.;
        double maxVotes = 3_000;
        for (ParkObject parkObject : parkObjects)
        {
            float rating = (float)Math.min(5., (maxRate * Math.random()));
            parkObject.setRate(rating);
            parkObject.setRateNum((int) (maxVotes * Math.random()));
        }
//        int max = Integer.MIN_VALUE;
//        for (ParkObject parkObject : parkObjects)
//        {
//            int curr = parkObject.getInfo().length();
//            if (curr > max)
//                max = curr;
//        }
//        double ratingParameter = max  / 20.;
//        int    votes           = 3000 / 5;
//        for (ParkObject parkObject : parkObjects)
//        {
//            float rating = (float)Math.min(5., (parkObject.getInfo().length() * Math.random() / ratingParameter));
//            parkObject.setRate(rating);
//            parkObject.setRateNum((int) (votes * Math.random() * rating));
//        }
    }

    public static ParkObject createParkObjectFromInfo(Object[] infoParkObject)
    {
        ParkObject parkObject = null;
        if (infoParkObject.length > 2)// objectType, name, coordinates[], desc
        {
            double latitude         = -1;
            double longitude        = -1;
            String name             = null;
            String heading          = null;
            String info             = null;
            int pointerType         = -1;
            float rate              = -1;
            int rateNum             = -1;
            String additionalInfo   = null;
            int myRate              = -1;
            if (infoParkObject[0].getClass() == Integer.class)
                pointerType = (Integer)infoParkObject[0];
            if (infoParkObject[1].getClass() == String.class)
                name = (String)infoParkObject[1];
            if (infoParkObject[2].getClass() == Float[].class)
            {
                Float[] coordinates = (Float[])infoParkObject[2];
                if (coordinates.length == 2)
                {
                    latitude  = coordinates[0];
                    longitude = coordinates [1];
                }
            }
            if (infoParkObject.length > 3 && infoParkObject[3].getClass() == String.class)
                info = (String)infoParkObject[3];

            parkObject = new ParkObject(latitude, longitude, name, heading, info, pointerType, rate, rateNum,
                    additionalInfo, myRate);
        }
        return parkObject;
    }
}

