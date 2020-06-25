package com.mock.server.Server;

import com.mock.server.Query.Method;
import com.mock.server.URITree.DirName;
import com.mock.server.URITree.DirPattern;
import com.mock.server.URITree.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


@Component
public class Verifier {

    private static final Logger logger = LoggerFactory.getLogger(Verifier.class);

    /**
     * > The path must be relative
     * > All the directory names can be a regular expression
     * > Pattern matching needs pattern compilation and is very expensive, therefore compiled once and then matched at the time of responding
     * > All valid regular expressions does not contains a forward slash
     * > https://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html     *
     *
     * @param method               GET, POST...
     * @param path                 The string containing path, may be regex or simply a
     * @param queryParameters      Query parameters as an exact string
     * @param queryParametersRegex Query parameters as a regex
     * @return The Directory (pattern directory or exact name string directory list)
     * @throws IllegalArgumentException If path is invalid
     * @throws PatternSyntaxException   If regex in the path is incorrect
     */
    public static ArrayList <Directory> getPathList(
            Method method,
            String path,
            String queryParameters,
            String queryParametersRegex)
            throws IllegalArgumentException, PatternSyntaxException {

        if(path == null || path.length() == 0) throw new IllegalArgumentException("Path can not be a null or empty");
        if(path.charAt(0) != '/') throw new IllegalArgumentException("Path must be relative!");

        ArrayList <Directory> pathList = new ArrayList <>();
        pathList.add(new DirName(method.val));

        StringBuilder dirNameBuilder = new StringBuilder();

        for(int i = 1; i < path.length(); i++) { // first index will be /
            if(path.charAt(i) != '/') {
                dirNameBuilder.append(path.charAt(i));
            }else {
                String dir = dirNameBuilder.toString();
                addToPathList(dir, pathList);
                dirNameBuilder.setLength(0);
            }
        }

        if(dirNameBuilder.length() != 0) {
            String dir = dirNameBuilder.toString();
            addToPathList(dir, pathList);
        }

        if(queryParameters != null) {
            logger.info("Normal Query Parameters Found!");
            pathList.add(new DirName(queryParameters.substring(1)));
        }else if(queryParametersRegex != null) {
            logger.info("Regex Query Parameters Found!");
            String query = queryParametersRegex.substring(1);
            pathList.add(new DirPattern(query, Pattern.compile(query)));
        }

        StringBuilder stringPath = new StringBuilder();
        for(Directory directory : pathList) stringPath.append(" ").append(directory.getDirName());
        logger.info("Path List: " + stringPath.toString());

        return pathList;
    }

    /**
     * Helper function used by the getPathList function
     *
     * @param dir      the directory name (regex/exact string)
     * @param pathList add the directory to this list
     * @throws IllegalArgumentException Invalid Directory name
     * @throws PatternSyntaxException   Invalid Directory pattern
     */
    private static void addToPathList(String dir, ArrayList <Directory> pathList) throws IllegalStateException, PatternSyntaxException {
        if(dir.length() == 0) throw new IllegalStateException("Directory Name can not be empty!");
        boolean isDirString = true;
        for(int j = 0; j < dir.length(); j++) {
            if(!Character.isLetterOrDigit(dir.charAt(j)) && dir.charAt(j) != '-' && dir.charAt(j) != '_' && dir.charAt(j) != '.') {
                isDirString = false;
                break;
            }
        }
        if(isDirString) {    // is an exact directory name
            pathList.add(new DirName(dir));
        }else {              // must be a regex pattern
            logger.info(dir + " will be considered as a pattern");
            pathList.add(new DirPattern(dir, Pattern.compile(dir)));
        }
    }


    /**
     * Helper function for verification
     * This verification is also done in the slack bot
     */
    public static void verifyMethodAndQuery(Method method, String jsonBody, String queryParameters, String queryParametersRegex) {
        if(queryParameters != null && queryParametersRegex != null)
            throw new IllegalStateException("You can not set both query Parameters and QueryParameters in Regex at the same time");
        if(method == Method.GET || method == Method.HEAD || method == Method.OPTIONS || method == Method.TRACE) {
            if(jsonBody != null) {
                logger.info(method.val + " method should not have a request body. Request body will be ignored!");
            }
        }
    }

    /**
     * To convert a path to be matched to a list of strings
     *
     * @param uri    the path requested
     * @param method Method type
     * @return List of directories to traverse (Query Parameters might be added to this path by the caller)
     */
    public static ArrayList <String> getSimplePathList(String uri, String method) {
        ArrayList <String> res = new ArrayList <>();
        res.add(method);
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 1; i < uri.length(); i++) {
            if(uri.charAt(i) == '/') {
                res.add(stringBuilder.toString());
                stringBuilder.setLength(0);
            }else if(Character.isLetterOrDigit(uri.charAt(i))) {
                stringBuilder.append(uri.charAt(i));
            }else {
                throw new IllegalStateException("Path invalid!");
            }
        }
        if(stringBuilder.length() != 0) {
            res.add(stringBuilder.toString());
        }
        return res;
    }


}
