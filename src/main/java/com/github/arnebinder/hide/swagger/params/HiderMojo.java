package com.github.arnebinder.hide.swagger.params;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Goal which touches a timestamp file.
 *
 * @goal hideparams
 *
 * @phase package
 */
@Mojo( name = "hideparams")
public class HiderMojo
        extends AbstractMojo
{

    /**
     * json file containing the swagger code.
     * @parameter expression="${jsonfile}"
     *            default-value="target/doc/swagger-ui/swagger.json"
     */
    private File jsonfile;
    // @Parameter( property = "hideparams.jsonfile", defaultValue = "target/doc/swagger-ui/swagger.json" )

    /**
     * value of the name of the element to hide.
     * @parameter expression="${hiddenNameValue}"
     *            default-value="HIDDEN"
     */
    private String hiddenNameValue;

    public void execute()
            throws MojoExecutionException
    {
        getLog().info( "Hide swagger parameters with name \"HIDDEN\"..." );
        getLog().info( "Load swagger JSON descriptions file: "+jsonfile);
        String content;
        try {
            Scanner scanner = new Scanner(jsonfile);
            content = scanner.useDelimiter("\\Z").next();
            scanner.close();
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException( "File not found: " + jsonfile, e );
        }



        JSONObject rootObject = new JSONObject(content);

        findHidden(rootObject);

        PrintWriter writer;
        String outfile = "test.json";
        try {
            writer = new PrintWriter(jsonfile, "UTF-8");
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException( "FileNotFoundException: " + outfile, e );
        } catch (UnsupportedEncodingException e) {
            throw new MojoExecutionException( "UnsupportedEncodingException: " + outfile, e );
        }
        rootObject.write(writer);
        writer.close();
        getLog().info( "Hiding succeeded." );
    }


    private boolean findHidden(JSONObject node){
        if(node.has("name") && node.get("name").toString().equals(hiddenNameValue)){
            return true;
        }else {
            for ( Iterator<String> it =  node.keys(); it.hasNext(); ){
                boolean handled = false;
                String key = it.next();
                try{
                    if (findHidden(node.getJSONObject(key))){
                        getLog().info( "remove: "+key+" -> "+ node.getJSONObject(key).toString());
                        node.remove(key);
                    }
                    handled = true;
                }catch (JSONException e){
                }
                if(!handled){
                    try{
                        findHidden(node.getJSONArray(key));
                    }catch (JSONException e){
                    }
                }
            }
            return false;
        }

    }

    private void findHidden(JSONArray nodes){
        for (int i = 0; i < nodes.length(); i++) {
            boolean handled = false;
            try{
                if (findHidden(nodes.getJSONObject(i))){
                    nodes.remove(i);
                }
                handled = true;
            }catch (JSONException e){
            }
            if(!handled){
                try{
                    findHidden(nodes.getJSONArray(i));
                }catch (JSONException e){
                }
            }
        }
    }
}
