package com.mock.server;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component
public class Verifier {

    private static final Logger logger = LoggerFactory.getLogger(Verifier.class);
    /*
     * The path must be relative
     * All the directory names can be a regular expression
     * Pattern matching needs pattern compilation and is very expensive, therefore compiled once and matched other times
     * All valid regular expressions does not contains a forward slash
     * https://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html
     */
    public  ArrayList <Directory> getPathList(
            Method method,
            String path,
            String queryParameters,
            String queryParametersRegex)
            throws IllegalStateException, PatternSyntaxException {

        if(path == null || path.length() == 0) throw new IllegalStateException("Path can not be a null or empty");

        ArrayList <Directory> pathList = new ArrayList <>();
        pathList.add(new DirName(method.val));

        StringBuilder dirNameBuilder = new StringBuilder();

        for(int i = 1; i < path.length(); i++) { // first index will be /
            if(path.charAt(i) != '/') {
                dirNameBuilder.append(path.charAt(i));
            } else {
                String dir = dirNameBuilder.toString();
                addToPathList(dir, pathList);
                dirNameBuilder.setLength(0);
            }
        }

        if(dirNameBuilder.length() != 0) {
            String dir = dirNameBuilder.toString();
            addToPathList(dir, pathList);
        }

        // if Query Request is a normal expression or a regex
        if(queryParameters!=null){
            logger.info("Normal Query Prameters Encountered!");
            pathList.add(new DirName(queryParameters.substring(1)));
        }else if(queryParametersRegex!=null){
            logger.info("Regex Query Parameters Encountered!");
            String query = queryParametersRegex.substring(1);
            // note name involves ?(diff it from a Directory!) but pattern does not!
            pathList.add(new DirPattern(query, Pattern.compile(query)));
        }

        StringBuilder stringPath= new StringBuilder();
        for(Directory directory : pathList) stringPath.append(" ").append(directory.getDirName());
        logger.info("Path List: "+stringPath.toString());

        return pathList;
    }


    private  void addToPathList(String dir, ArrayList <Directory> pathList) throws IllegalStateException, PatternSyntaxException {
        if(dir.length() == 0) throw new IllegalStateException("Directory Name can not be empty!");
        boolean isDirString = true;
        for(int j = 0; j < dir.length(); j++) {
            if(!Character.isLetterOrDigit(dir.charAt(j)) && dir.charAt(j)!='-' && dir.charAt(j)!='_') {
                isDirString = false;
                break;
            }
        }
        if(isDirString) {    // is a simple directory name
            pathList.add(new DirName(dir));
        } else {              // must be a regex pattern
            logger.info(dir+" will be considered as a pattern");
            pathList.add(new DirPattern(dir, Pattern.compile(dir)));
        }
    }


    /**
     * For methods not supporting particular members like request body for GET, the actual server will ignore them.
     */

    public  void verifyMethodAndQuery(Method method, String jsonBody, String queryParameters, String queryParametersRegex) {
        if(queryParameters!=null && queryParametersRegex!=null)
            throw new IllegalStateException("You can not set both query Parameters and QueryParameters in Regex at the same time");
        if(method == Method.GET) {
            if(jsonBody != null) {
                jsonBody = null;
                logger.info("GET method should not have a request body. Request body will be ignored!");
            }
        } else if(method == Method.HEAD) {
            if(jsonBody != null) {
                jsonBody = null;
                logger.info("HEAD method should not have a request body. Request body will be ignored!");
            }
        } else if(method == Method.OPTIONS) {
            if(jsonBody != null) {
                jsonBody = null;
                logger.info("OPTIONS method should not have a request body. Request body will be ignored!");
            }
        } else if(method == Method.TRACE) {
            if(jsonBody != null) {
                jsonBody = null;
                logger.info("TRACE method should not have a request body. Request body will be ignored!");
            }
        }
    }


    public ArrayList <String> getSimplePathList(String uri, String method){
        ArrayList<String> res = new ArrayList <>();
        res.add(method);
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=1;i<uri.length();i++){
            if(uri.charAt(i)=='/'){
                res.add(stringBuilder.toString());
                stringBuilder.setLength(0);
            }else if(Character.isLetterOrDigit(uri.charAt(i))){
                stringBuilder.append(uri.charAt(i));
            }else{
                throw new IllegalStateException("Path invalid!");
            }
        }
        if(stringBuilder.length()!=0){
            res.add(stringBuilder.toString());
        }
        return res;
    }




}
