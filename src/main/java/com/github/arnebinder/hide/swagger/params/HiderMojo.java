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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

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
     * json file containing the input swagger code.
     * @parameter expression="${jsonfile}"
     *            default-value="target/doc/swagger-ui/swagger.json"
     */
    private File jsonfile;
    // @Parameter( property = "hideparams.jsonfile", defaultValue = "target/doc/swagger-ui/swagger.json" )

    /**
     * yaml file containing the output swagger code.
     * @parameter expression="${yamlfile}"
     *            default-value="target/doc/swagger-ui/swagger.yaml"
     */
    private File yamlfile;

    /**
     * name of the key of the element to hide.
     * @parameter expression="${excludeKey}"
     *            default-value="name"
     */
    private String excludeKey;

    /**
     * name of the value of the element to hide.
     * @parameter expression="${excludeValue}"
     *            default-value="HIDDEN"
     */
    private String excludeValue;

    /**
     * If true, the input json file will be modified.
     * In particular it will have the same semantics as the yaml output.
     * @parameter expression="${overwriteJSON}"
     *            default-value="false"
     */
    private Boolean overwriteJSON;

    /**
     * This file will be merged with the created yaml.
     * @parameter expression="${mergeYamlFile}"
     */
    private File mergeYamlFile;

    @SuppressWarnings("unchecked")
    public void execute()
            throws MojoExecutionException
    {

        getLog().info( "Hide swagger elements with "+excludeKey+" \""+excludeValue+"\"..." );
        getLog().info( "Load swagger JSON descriptions file: "+jsonfile);

        ObjectMapper m = new ObjectMapper();

        Map<String,Object> data;
        try {
            data = m.readValue(jsonfile, Map.class);
            invoke(data);
            if(overwriteJSON) {
                m.writeValue(jsonfile, data);
            }
        } catch (IOException e) {
            throw new MojoExecutionException( "Could not read file: " + jsonfile, e );
        }

        ObjectMapper ym = new ObjectMapper(new YAMLFactory());
        try {
            ym.writeValue(yamlfile, data);
        } catch (IOException e) {
            throw new MojoExecutionException( "Could not write file: " + yamlfile, e );
        }

        if(mergeYamlFile!=null){
            getLog().info( "Merge with yaml file: "+mergeYamlFile );
            JsonNode rootNode;
            JsonNode updateNode;
            try {
                rootNode = ym.readValue(yamlfile, JsonNode.class);
                updateNode = ym.readValue(mergeYamlFile, JsonNode.class);
            } catch (IOException e) {
                throw new MojoExecutionException( "Could not read file: " + mergeYamlFile, e );
            }
            JsonNode merged = merge(rootNode, updateNode);
            try {
                ym.writeValue(yamlfile, merged);
            } catch (IOException e) {
                throw new MojoExecutionException( "Could not write file: " + yamlfile, e );
            }
            getLog().info( "Merging succeeded." );
        }

        getLog().info( "Hiding succeeded." );
    }


    @SuppressWarnings("unchecked")
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


    public static JsonNode merge(JsonNode mainNode, JsonNode updateNode) throws MojoExecutionException {
        if(updateNode instanceof ArrayNode){
            if(!(mainNode instanceof ArrayNode)){
                // error
                throw new MojoExecutionException( "Could not merge nodes: " + mainNode.toString() + " is not an ArrayNode.");
            }else {
                Iterator<JsonNode> updateElements = updateNode.elements();
                while (updateElements.hasNext()) {
                    JsonNode updateElement = updateElements.next();
                    if (updateElement.has("name") && updateElement.get("name") != null && updateElement.get("name").isTextual()) {
                        String updateName = updateElement.get("name").asText();

                        JsonNode mNode = mainNode.findValue(updateName);
                        if (mNode == null) {
                            // add updateElement to mainNode
                            ((ArrayNode) mainNode).add(updateElement);
                        }
                        merge(mNode, updateElement);
                    }else{
                        throw new MojoExecutionException( "Could not find key \"name\" in ArrayNode update element: " + updateElement.toString());
                    }
                }
            }
        }else {
            Iterator<String> fieldNames = updateNode.fieldNames();
            while (fieldNames.hasNext()) {

                String fieldName = fieldNames.next();
                JsonNode jsonNode = mainNode.get(fieldName);
                // if field exists and is an embedded object
                if (jsonNode != null && jsonNode.isObject()) {
                    merge(jsonNode, updateNode.get(fieldName));
                } else {
                    if (mainNode instanceof ObjectNode) {
                        // Overwrite field
                        JsonNode value = updateNode.get(fieldName);
                        ((ObjectNode) mainNode).replace(fieldName, value);
                    }
                }

            }
        }
        return mainNode;
    }

}
