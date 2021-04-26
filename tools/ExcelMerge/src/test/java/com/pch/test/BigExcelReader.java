package com.pch.test;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ooxml.util.SAXHelper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * sax
 */
public class BigExcelReader {
    private XSSFReader xssfReader;
    //获取一行时最小数组长度
    private final int minColumnCount;
    private int currentRow = 0;
    private String sheetName;
    private String[] record;
    private int thisColumnIndex = -1;
    // 日期标志
    private boolean dateFlag;
    // 数字标志
    private boolean numberFlag;
    private boolean isTElement;
    private RowReader rowReader;

    public void setRowReader(RowReader rowReader) {
        this.rowReader = rowReader;
    }

    /**
     * 构造方法
     */
    public BigExcelReader(String filename, int minCols) throws Exception {
        if (StringUtils.isEmpty(filename))
            throw new Exception("文件名不能空");
        this.minColumnCount = minCols;
        record = new String[this.minColumnCount];
        OPCPackage pkg = OPCPackage.open(filename);
        xssfReader = new XSSFReader(pkg);

    }

    /**
     * 获取sheet
     */
    public void process() throws Exception {
        SharedStringsTable sst = xssfReader.getSharedStringsTable();
        XMLReader parser = fetchSheetParser(sst);
        XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
        while (iter.hasNext()) {
            InputStream sheet = iter.next();
            sheetName = iter.getSheetName();
            InputSource sheetSource = new InputSource(sheet);
            parser.parse(sheetSource);
            sheet.close();
        }
    }

    /**
     * 加载sax 解析器
     */
    private XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException, ParserConfigurationException {
        XMLReader parser = SAXHelper.newXMLReader();
        ContentHandler handler = new PagingHandler(sst);
        parser.setContentHandler(handler);
        return parser;
    }

    /**
     * 通过继承DefaultHandler类，重写process()，startElement()，characters()，endElement()这四个方法。
     * process()方式主要是遍历所有的sheet，并依次调用startElement()、characters()方法、endElement()这三个方法。
     * startElement()用于设定单元格的数字类型（如日期、数字、字符串等等）。
     * characters()用于获取该单元格对应的索引值或是内容值（如果单元格类型是字符串、INLINESTR、数字、日期则获取的是索引值；
     * 其他如布尔值、错误、公式则获取的是内容值）。
     * endElement()根据startElement()的单元格数字类型和characters()的索引值或内容值，最终得出单元格的内容值，并打印出来。
     */
    private class PagingHandler extends DefaultHandler {
        private SharedStringsTable sst;
        private String lastContents;
        private boolean nextIsString;
        private String index = null;

        private PagingHandler(SharedStringsTable sst) {
            this.sst = sst;
        }

        /**
         * 开始元素
         */
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            if (name.equals("c")) {
                index = attributes.getValue("r");
                int firstDigit = -1;
                for (int c = 0; c < index.length(); ++c) {
                    if (Character.isDigit(index.charAt(c))) {
                        firstDigit = c;
                        break;
                    }
                }
                thisColumnIndex = nameToColumn(index.substring(0, firstDigit));
                // 判断是否是新的一行
                if (Pattern.compile("^A[0-9]+$").matcher(index).find()) {
                    currentRow++;
                }
                String cellType = attributes.getValue("t");
                if (cellType != null && cellType.equals("s")) {
                    nextIsString = true;
                } else {
                    nextIsString = false;
                }
                // 日期格式
                String cellDateType = attributes.getValue("s");
                if ("1".equals(cellDateType)) {
                    dateFlag = true;
                } else {
                    dateFlag = false;
                }
                String cellNumberType = attributes.getValue("s");
                if ("2".equals(cellNumberType)) {
                    numberFlag = true;
                } else {
                    numberFlag = false;
                }
            }
            // 当元素为t时
            if ("t".equals(name)) {
                isTElement = true;
            } else {
                isTElement = false;
            }
            lastContents = "";
        }

        /**
         * 获取value
         */
        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            if (nextIsString) {
                int idx = Integer.parseInt(lastContents);
                lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
                nextIsString = false;
            }
            // t元素也包含字符串
            if (isTElement) {
                String value = lastContents.trim();
                record[thisColumnIndex] = value;
                isTElement = false;
                // v => 单元格的值，如果单元格是字符串则v标签的值为该字符串在SST中的索引
                // 将单元格内容加入rowlist中，在这之前先去掉字符串前后的空白符
            } else if ("v".equals(name)) {
                String value = lastContents.trim();
                value = value.equals("") ? " " : value;
                // 日期格式处理
                if (dateFlag) {
                    try {
                        Date date = HSSFDateUtil.getJavaDate(Double.parseDouble(value));
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        value = dateFormat.format(date);
                    } catch (NumberFormatException e) {
                    }
                }
                // 数字类型处理
                if (numberFlag) {
                    try {
                        BigDecimal bd = new BigDecimal(value);
                        value = bd.setScale(3, RoundingMode.UP).toString();
                    } catch (Exception e) {
                    }
                }
                record[thisColumnIndex] = value;
            } else {
                if (name.equals("row")) {
                    if (minColumnCount > 0) {
                        rowReader.getRows(sheetName, currentRow, record.clone());
                        Arrays.fill(record, null);
                    }
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            lastContents += new String(ch, start, length);
        }
    }

    private int nameToColumn(String name) {
        int column = -1;
        for (int i = 0; i < name.length(); ++i) {
            int c = name.charAt(i);
            column = (column + 1) * 26 + c - 'A';
        }
        return column;
    }

    public static void main(String[] args) throws Exception {
        RowReader rowReader = new RowReader();
        BigExcelReader reader = new BigExcelReader("C:\\sandbox\\trunk\\public\\config\\Check.xlsx", 0);
        reader.setRowReader(rowReader);
        reader.process();
    }
}
