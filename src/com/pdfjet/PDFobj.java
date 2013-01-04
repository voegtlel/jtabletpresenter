/**
 *  PDFobj.java
 *
Copyright (c) 2012, Innovatics Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and / or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.pdfjet;

import java.util.*;


/**
 *  Used to create Java or .NET objects that represent the objects in PDF document. 
 *  See the PDF specification for more information.
 *
 */
public class PDFobj {

    public static final String TYPE = "/Type";
    public static final String SUBTYPE = "/Subtype";
    public static final String FILTER = "/Filter";
    public static final String WIDTH = "/Width";
    public static final String HEIGHT = "/Height";
    public static final String COLORSPACE = "/ColorSpace";
    public static final String BITSPERCOMPONENT = "/BitsPerComponent";

    protected int offset;
    public int number;
    public List<String> dict;

    public byte[] stream;
    protected int stream_offset;

    public byte[] data;


    /**
     *  Used to create Java or .NET objects that represent the objects in PDF document. 
     *  See the PDF specification for more information.
     *  Also see Example_19.
     *
     *  @param offset the object offset in the offsets table.
     */
    public PDFobj(int offset) {
        this.offset = offset;
        this.dict = new ArrayList<String>();
    }


    /**
     *  Returns the parameter value given the specified key.
     *
     *  @param key the specified key.
     *
     *  @return the value.
     */
    public String getValue(String key) {
        for (int i = 0; i < dict.size(); i++) {
            String token = dict.get(i);
            if (token.equals(key)) {
                return dict.get(i + 1);
            }
        }
        return "";
    }


    /**
     *
     *
     *
     */
    protected int getLength(List<PDFobj> objects) {
        for (int i = 0; i < dict.size(); i++) {
            String token = dict.get(i);
            if (token.equals("/Length")) {
                int number = Integer.valueOf(dict.get(i + 1));
                if (dict.get(i + 2).equals("0") &&
                        dict.get(i + 3).equals("R")) {
                    return getLength(objects, number);
                }
                else {
                    return number;
                }
            }
        }
        return 0;
    }


    /**
     *
     *
     *
     */
    protected int getLength(List<PDFobj> objects, int number) {
        for (int i = 0; i < objects.size(); i++) {
            PDFobj obj = objects.get(i);
            int objNumber = Integer.valueOf(obj.dict.get(0));
            if (objNumber == number) {
                return Integer.valueOf(obj.dict.get(3));
            }
        }
        return 0;
    }


    /**
     *  Sets this PDF object's stream.
     *
     *
     */
    public void setStream(byte[] pdf, int length) {
        stream = new byte[length];
        System.arraycopy(pdf, this.stream_offset, stream, 0, length);
    }

}
