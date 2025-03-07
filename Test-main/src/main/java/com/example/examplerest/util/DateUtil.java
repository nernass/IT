package com.example.examplerest.util;

import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class DateUtil {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    public Date textToDate(String text) throws ParseException {
        if(text == null) {
            return null;
        }
        return sdf.parse(text);
    }

    public String dateToText(Date date) {
        return sdf.format(date);
    }

}
