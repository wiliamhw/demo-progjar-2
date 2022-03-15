package com.serversocket;

import java.util.ArrayList;
import java.util.HashMap;

public class ListBuilder {
    private ArrayList<HashMap<String, String>> files;
    private String urn;
    private StringBuilder Html;

    static final String DEFAULT_LINK = "error.id";
    static final String[] SIZE_SYMBOL_ORDER = {"B", "KB", "MB", "GB"};

    public ListBuilder(ArrayList<HashMap<String, String>> files, String urn) {
        this.files = files;
        this.urn = "/" + urn;

        this.generateHtml();
    }

    private void generateHtml() {
        Html = new StringBuilder(String.format(
            "<html>\n" +
            "\n" +
            "<head>\n" +
            "   <title>Index of %s</title>\n" +
            "<head>\n" +
            "\n" +
            "<body>\n" +
            "   <h1>Index of %s</h1>\n" +
            "   <table>\n" +
            "       <tbody>\n"
        , this.urn, this.urn));

        Html.append(getTableRows());

        Html.append(
            "       </tbody>\n" +
            "   </table>\n" +
            "</body>\n" +
            "</html>\n"
        );
    }

    private String getTableRows() {
        StringBuilder tableRow = new StringBuilder(
            "           <tr>\n" +
            "               <th valign=\"top\"><img src=\"http://" + DEFAULT_LINK + "/list/blank.gif\" alt=\"[ICO]\"></th>\n" +
            "               <th>Name</th>\n" +
            "               <th style=\"padding: 0 10px;\">Last modified</th>\n" +
            "               <th>Size</th>\n" +
            "           </tr>\n" +
            "           <tr>\n" +
            "               <th colspan=\"5\"><hr></th>\n" +
            "           </tr>\n"
        );

        for (HashMap<String, String> file : this.files) {
            boolean isFile = file.get("type").equals("file");
            String alt = (isFile) ? "TXT" : "DIR";
            String icon = (isFile) ? "text.gif" : "folder.gif";
            String iconPath = String.format("http://%s/list/%s", DEFAULT_LINK, icon);

            // Get displayed size.
            String size = "-";
            if (!file.get("size").equals("0")) {
                size = getSize(file.get("size"));
            }

            tableRow.append(String.format(
                "           <tr>\n" +
                "               <td valign=\"top\"><img src=\"%s\" alt=\"[%s]\"></td>\n" +
                "               <td><a href=\"%s\">%s</a></td>\n" +
                "               <td style=\"padding: 0 10px;\">%s\t</td>\n" +
                "               <td align=\"right\">%s</td>\n" +
                "           </tr>\n"
            , iconPath, alt, file.get("path"), file.get("name"), file.get("lastModified"), size));
        }

        tableRow.append(
            "           <tr>\n" +
            "               <th colspan=\"5\"><hr></th>\n" +
            "           </tr>\n"
        );
        return tableRow.toString();
    }

    private String getSize(String sizeStr) {
        int symbolIdx = 0;
        int size = Integer.parseInt(sizeStr);

        while (symbolIdx < SIZE_SYMBOL_ORDER.length && ((size / 1024) > 0)) {
            size /= 1024;
            symbolIdx++;
        }
        return size + " " + SIZE_SYMBOL_ORDER[symbolIdx];
    }

    public String getHtml() {
        return this.Html.toString();
    }
}
