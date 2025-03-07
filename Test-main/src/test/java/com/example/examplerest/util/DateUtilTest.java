package com.example.examplerest.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilTest {

    private DateUtil dateUtil = new DateUtil();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void textToDate() throws ParseException {
        String dateTxt = "02-02-2022";
        Date actual = dateUtil.textToDate(dateTxt);
        Date expected = sdf.parse(dateTxt);
        assertEquals(expected, actual);
//        assertNotNull(date);
//        int day = date.getDate();
//        assertEquals(2, day);
//        assertEquals(1, date.getMonth());
//        assertEquals(2022, date.getYear());
    }

    @Test
    void textToDateNull() throws ParseException {
        String dateTxt = null;
        assertNull(dateUtil.textToDate(dateTxt));
    }

    @Test
    void textToDateParseException() throws ParseException {
        String dateTxt = "02.02.2022";
        assertThrows(ParseException.class, () -> {
            dateUtil.textToDate(dateTxt);
        });
    }

    @Test
    void dateToTextNotNull() {
        Date date = new Date();
        assertNotNull(dateUtil.dateToText(date));
    }

    @Test
    void dateToText() throws ParseException {
        Date parsedDate = sdf.parse("03-03-2022");
        String actual = dateUtil.dateToText(parsedDate);
        assertEquals("03-03-2022", actual);
    }
}