package com.guide.park_szczytnicki.Objects;

import com.google.firebase.database.DataSnapshot;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

public class Load
{
    ///*
    public static <T> String toJSON(T object)
    {
        StringBuilder sb = new StringBuilder();
        try {
            for (Field field : object.getClass().getDeclaredFields())
                if (!Modifier.toString(field.getModifiers()).contains("static final") )
                {
                    sb  .append("\t\"").append(field.getName()).append("\" : ")
                            .append('\"').append(field.get(object)).append("\",\n");
                }
        } catch (IllegalAccessException e) {e.printStackTrace();}
        return sb.substring(0, sb.length()-2);
    }
//*/
    public static <T> T fromJSON(T object, Scanner sc, Map<String, Field> attributes)
    {
        if(sc.nextLine().contains(object.getClass().getSimpleName()))
        {
            String[] s = sc.nextLine().split("\" : \"");
            Field field;
            while (!s[0].contains("}"))
            {
                field = attributes.get(s[0].replace("\"", "").trim());
                if (field != null)
                    try {
                        field.set(object, getClass(field, s[1]));
                    } catch (IllegalAccessException e) {e.printStackTrace();}
                s = sc.nextLine().split("\" : \"");
            }
        }
        else
            object = null;
        return object;
    }

    public static Object getClass(Field field, String stringValue)
    {
        stringValue = stringValue.trim().replace(",", "").replace("\"", "");
        switch (field.getType().getSimpleName())
        {
            case "String"   : return stringValue;
            case "int"      : return Integer.parseInt   (stringValue);
            case "double"   : return Double .parseDouble(stringValue);
            case "float"    : return Float  .parseFloat (stringValue);
            default         : return null;
        }
    }


    public static LinkedList<ParkObject> fromJSON(LinkedList<ParkObject> objects, Class<ParkObject> c, Scanner sc)
    {
        Map<String, Field> mapOfAttributes = mapOfAttributes(c);
        do
        {
            objects.add(fromJSON(new ParkObject(), sc, mapOfAttributes));
        }while (objects.getLast() != null);
        objects.removeLast();
        return objects;
    }

    public static Map<String, Field> mapOfAttributes(Class c)
    {
        Map<String, Field> attributes = new HashMap<>();
        for (Field field : c.getDeclaredFields())
            if (!Modifier.toString(field.getModifiers()).contains("static final") )
                attributes.put(field.getName(), field);
        return attributes;
    }

    public static Map<Class, LinkedList> mainLoad(InputStream inputStream)
    {
        Scanner sc = new Scanner(new DataInputStream(new BufferedInputStream(inputStream)));
        Map<Class, LinkedList> map = new HashMap<>();
        if(sc.nextLine().equals("{"))
        {
            String s = sc.nextLine();
            while (!s.equals("}"))
            {
                map.put(getClass(s), fromJSON(getClass(s), sc));
                s = sc.nextLine();
            }
            //llTop.forEach(e -> e.forEach(System.out::println));
        }
        return map;
    }

    public static Class getClass(String stringValue)
    {
        switch (stringValue)
        {
            case "  \"ParkObject\" : {"   : return ParkObject.class;
            case "  \"ParkObjec\" : {"   : return Object.class;
            default                     : return null;
        }
    }

    public static <T> LinkedList<T> fromJSON(Class<T> c, Scanner sc)
    {
        Map<String, Field> mapOfAttributes = mapOfAttributes(c);
        LinkedList<T> objects = new LinkedList<>();
        T object = null;
        do
        {
            try {
                object = fromJSON(c.getConstructor().newInstance(), sc, mapOfAttributes);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {e.printStackTrace();}

            if (object != null)
                objects.add(object);

        }while (object != null);
        return objects;
    }

    //TODO ro remove

    public static <T> HashMap<String, Object> toJSON1(T object)
    {
        HashMap<String, Object> attributeValue = new HashMap<>();
        try {
            for (Field field : object.getClass().getDeclaredFields())
                if (!Modifier.toString(field.getModifiers()).contains("static final"))
                {
                    System.out.println(field.getType());
                    attributeValue.put(field.getName(), field.get(object));
                }
        } catch (IllegalAccessException e) {e.printStackTrace();}
        return attributeValue;
    }

    public static <T> HashMap<String, Object> toJSON2(T object)
    {
        HashMap<String, Object> attributeValue =toJSON1(object);
        for (Map.Entry e : attributeValue.entrySet())
        {
            System.out.println(e.getKey());
            System.out.println(e.getValue());
            System.out.println();
        }
        return attributeValue;
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
                    field.set(object, getClass(field, singleEntry.getValue().toString()));
                } catch (IllegalAccessException e) {e.printStackTrace();}
            }
        }
        return object;
    }
}
