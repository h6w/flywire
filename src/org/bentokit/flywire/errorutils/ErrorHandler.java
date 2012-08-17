/*
    This file is part of Flywire.

    Flywire is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Flywire is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Flywire.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.bentokit.flywire.errorutils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

// ErrorHandler handles all the errors that occur in the program.
// This way we can block out all errors of a particular type,
// or pipe the errors to a logfile that can be read offsite.

public class ErrorHandler {
    static java.text.SimpleDateFormat df;

    static String logFilename = "error.log";

    public static boolean PRINT_MUST = true;
    public static boolean PRINT_INFO = true;
    public static boolean PRINT_MEM = true;
    public static boolean PRINT_ERROR = true;
    public static boolean PRINT_FATAL = true;
    public static boolean FILE_MUST = true;
    public static boolean FILE_INFO = true;
    public static boolean FILE_MEM = true;
    public static boolean FILE_ERROR = true;
    public static boolean FILE_FATAL = true;

    public static boolean PRINT_OFF = false;
    public static boolean FILE_OFF = false;

    static FileWriter fileStream;


    public static void initialise() {
        df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");

        //Open log file for writing
        if (!FILE_OFF && (FILE_MUST || FILE_INFO || FILE_ERROR || FILE_FATAL))
            openLogfile();
        else fileStream = null;
    }

    static void openLogfile() {
        try {
            fileStream = new FileWriter(logFilename,true);
        }
        catch (IOException e) {
            System.err.println("Could not open log file:"+e);
            System.exit(-1);
        }
    }

    static void closeLogfile() {
        try {
            fileStream.close();
        }
        catch (IOException e) {
            System.err.println("Could not close log file:"+e);
            System.exit(-1);
        }
        fileStream = null;
    }

    // MustPrints are not necessarily errors, but define
    // a special case where it's more than information, like
    // starting the program.
    public static void must(String s) {
        if (PRINT_MUST) printConsole("MUST :"+s);
        if (FILE_MUST) printFile("MUST :"+s);
    }

    public static void must(Exception e) {
        if (PRINT_MUST) printConsole("MUST :"+e.toString());
        if (FILE_MUST) printFile("MUST :"+e.toString());
    }

    // Information strings provide possibly superfluous
    // debugging information.
    public static void info(String s) {
        if (PRINT_INFO) printConsole("INFO :"+s);
        if (FILE_INFO) printFile("INFO :"+s);
    }

    public static void info(Exception e) {
        if (PRINT_INFO) printConsole("INFO :"+e.toString());
        if (FILE_INFO) printFile("INFO :"+e.toString());
    }

    // Information strings provide possibly superfluous
    // debugging information.
    public static void mem(String s) {
        if (PRINT_INFO) printConsole("MEM  :"+s);
        if (FILE_INFO) printFile("MEM  :"+s);
    }

    public static void mem(Exception e) {
        if (PRINT_INFO) printConsole("MEM  :"+e.toString());
        if (FILE_INFO) printFile("MEM  :"+e.toString());
    }

    // Errors mean the program went wrong somewhere, but
    // it was not serious enough to close the program.
    public static void error(String s) {
        if (PRINT_ERROR) printConsole("ERROR:"+s);
        if (FILE_ERROR) printFile("ERROR:"+s);
    }

    public static void error(Exception e) {
        if (PRINT_ERROR) printConsole("ERROR:"+e.toString());
        if (FILE_ERROR) printFile("ERROR:"+e.toString());
    }

    // Fatal errors means that we can't go on like this.
    // Make sure we report the error, then exit.
    public static void fatal(String s) {
        if (PRINT_FATAL) printConsole("FATAL:"+s);
        if (FILE_FATAL) printFile("FATAL:"+s);
    }

    public static void fatal(Exception e) {
        if (PRINT_FATAL) printConsole("FATAL:"+e.toString());
        if (FILE_FATAL) printFile("FATAL:"+e.toString());
    }

    //Private method for sending information
    static void printConsole(String s) {
        if (!PRINT_OFF) {
            Calendar calendar = Calendar.getInstance();
            System.err.println(df.format(calendar.getTime())+":"+s);
        }
    }

    static void printFile(String s) {
        if (fileStream == null && !FILE_OFF && (FILE_MUST || FILE_INFO || FILE_MEM || FILE_ERROR || FILE_FATAL))
            openLogfile();
        if (fileStream != null && (FILE_OFF || (!FILE_MUST && !FILE_INFO && !FILE_MEM && !FILE_ERROR && !FILE_FATAL)))
            closeLogfile();
        if (!FILE_OFF) {
            Calendar calendar = Calendar.getInstance();

            try {
                fileStream.write(df.format(calendar.getTime())+":"+s+"\n");
                fileStream.flush();
            }
            catch (IOException e) {
                System.err.println("Could not write to log file:"+e);
                System.exit(-1);
            }
        }
    }

}
