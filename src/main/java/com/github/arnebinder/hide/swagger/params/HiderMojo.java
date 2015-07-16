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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
//import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: arnebinder 16/07/2015
 */


/**
 * Goal which deletes all JSONObjects with key->value pair: name->hiddenNameValue
 * from a swagger description file.
 *
 * @goal hideparams
 *
 * @phase compile
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
     * json file containing the swagger code.
     * @parameter expression="${yamlfile}"
     *            default-value="target/doc/swagger-ui/swagger.yaml"
     */
    private File yamlfile;

    /**
     * value of the name of the element to hide.
     * @parameter expression="${excludeKey}"
     *            default-value="name"
     */
    private String excludeKey;

    /**
     * value of the name of the element to hide.
     * @parameter expression="${excludeValue}"
     *            default-value="HIDDEN"
     */
    private String excludeValue;

    public void execute()
            throws MojoExecutionException
    {

        getLog().info( "Hide swagger elements with "+excludeKey+" \""+excludeValue+"\"..." );
        getLog().info( "Load swagger JSON descriptions file: "+jsonfile);

        ObjectMapper m = new ObjectMapper();

        // can either use mapper.readTree(source), or mapper.readValue(source, JsonNode.class);
        JsonNode rootNode;
        try {
            rootNode = m.readTree(jsonfile);
        } catch (IOException e) {
            throw new MojoExecutionException( "Could not read file: " + jsonfile, e );
        }

        List<JsonNode> toDelete = rootNode.findParents(excludeKey);
        //getLog().info( "found hiddden: \n"+toDelete );
        for(JsonNode node: toDelete){
            if(node.get(excludeKey).isTextual()) {
                String value = node.get(excludeKey).asText();
                if (value.equals(excludeValue) && node instanceof ObjectNode) {
                    ObjectNode object = (ObjectNode) node;
                    object.removeAll();

                }
            }
        }

        Map<String,Object> data;
        try {
            data = m.readValue(jsonfile, Map.class);
            invoke(data);
            //m.writeValue(jsonfile, data);
        } catch (IOException e) {
            throw new MojoExecutionException( "Could not read file: " + jsonfile, e );
        }


        //getLog().info( "DATA:\n"+data.toString() );

        /*Object bean = null;
        try {
            bean = m.treeToValue(rootNode, Object.class);
        } catch (JsonProcessingException e) {
            throw new MojoExecutionException( "Could not parse json: " + jsonfile, e );
        }*/
        ObjectMapper ym = new ObjectMapper(new YAMLFactory());
        try {
            ym.writeValue(yamlfile, data);
        } catch (IOException e) {
            throw new MojoExecutionException( "Could not write file: " + yamlfile, e );
        }


        //////////////

        /*String content;
        try {
            Scanner scanner = new Scanner(jsonfile);
            content = scanner.useDelimiter("\\Z").next();
            scanner.close();
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException( "File not found: " + jsonfile, e );
        }
        JSONObject rootObject = new JSONObject(content);

        findHidden(rootObject);

        String prettyJSONString = rootObject.toString(2);
        PrintWriter jsonWriter;
        try {
            jsonWriter = new PrintWriter(jsonfile, "UTF-8");
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException( "FileNotFoundException: " + jsonfile, e );
        } catch (UnsupportedEncodingException e) {
            throw new MojoExecutionException( "UnsupportedEncodingException: " + jsonfile, e );
        }
        jsonWriter.write(prettyJSONString);
        //rootObject.write(jsonWriter);
        jsonWriter.close();

        Yaml yaml = new Yaml();
*/
        /*
        // mapping
        Map<String,Object> map = (Map<String, Object>) yaml.load(prettyJSONString);
        String output = yaml.dump(map);
        PrintWriter yamlWriter;
        try {
            yamlWriter = new PrintWriter(yamlfile, "UTF-8");
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException( "FileNotFoundException: " + yamlfile, e );
        } catch (UnsupportedEncodingException e) {
            throw new MojoExecutionException( "UnsupportedEncodingException: " + yamlfile, e );
        }
        yamlWriter.write(prettyJSONString);
        //rootObject.write(jsonWriter);
        yamlWriter.close();
        */

        getLog().info( "Hiding succeeded." );
    }


    private void invoke( Object object) {
        Iterator<String> it;
        if(object instanceof Map) {
            it = ((Map<String, Object>) object).keySet().iterator();
        }else if(object instanceof List) {
            it = ((List) object).iterator();
        }else{
            return;
        }

        while(it.hasNext()){
            Object itelem = it.next();
            Object element;
            if(object instanceof Map){
                element = ((Map<String, Object>) object).get(itelem);
            }else{
                element = itelem;
            }
            if(element instanceof Map){
                Map<String,Object> m = (Map<String,Object>) element;
                if(m.containsKey(excludeKey) && m.get(excludeKey).equals(excludeValue)){
                    it.remove();
                }else{
                    invoke(element);
                }
            }else if(element instanceof List){
                invoke(element);
            }
        }
    }


}
