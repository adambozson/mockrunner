package com.mockrunner.test;

import java.io.File;
import java.util.List;

import com.mockrunner.util.FileUtil;

import junit.framework.TestCase;

public class FileUtilTest extends TestCase
{
    public void testGetLinesFromFile()
    {
        File file = new File("src/com/mockrunner/test/testlines.txt");
        List lineList = FileUtil.getLinesFromFile(file);
        assertTrue(lineList.size() == 6);
        assertEquals("line1", lineList.get(0));
        assertEquals("line2", lineList.get(1));
        assertEquals("line3", lineList.get(2));
        assertEquals("", lineList.get(3));
        assertEquals("line4", lineList.get(4));
        assertEquals("line5", lineList.get(5));
    }
}
