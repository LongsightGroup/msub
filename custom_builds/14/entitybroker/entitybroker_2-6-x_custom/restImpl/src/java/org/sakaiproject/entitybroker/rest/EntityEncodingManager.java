/**
 * $Id$
 * $URL$
 * EntityEncodingManager.java - entity-broker - Jul 23, 2008 3:25:32 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.azeckoski.reflectutils.ConstructorUtils;
import org.azeckoski.reflectutils.ReflectUtils;
import org.azeckoski.reflectutils.StringUtils;
import org.azeckoski.reflectutils.map.ArrayOrderedMap;
import org.azeckoski.reflectutils.transcoders.HTMLTranscoder;
import org.azeckoski.reflectutils.transcoders.JSONTranscoder;
import org.azeckoski.reflectutils.transcoders.Transcoder;
import org.azeckoski.reflectutils.transcoders.XMLTranscoder;
import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.InputTranslatable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputFormattable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputSerializable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityEncodingException;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.FormatUnsupportedException;


/**
 * This handles the internal encoding (translation and formatting) of entity data,
 * this can be used by various parts of the EB system <br/>
 * this is for internal use only currently but may be exposed later
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityEncodingManager {

    public static final String ENTITY_REFERENCE = "entityReference";
    public static final String ENTITY_ID = "entityId";
    public static final String ENTITY_URL = "entityURL";
    public static final String ENTITY_TITLE = "entityTitle";
    public static final String ENTITY_PREFIX = "entityPrefix";
    public static final String COLLECTION = "_collection";

    private static Log log = LogFactory.getLog(EntityEncodingManager.class);

    public static final String[] HANDLED_INPUT_FORMATS = new String[] { Formats.XML, Formats.JSON, Formats.HTML };
    public static final String[] HANDLED_OUTPUT_FORMATS = new String[] { Formats.XML, Formats.JSON, Formats.HTML };


    protected EntityEncodingManager() { }

    public EntityEncodingManager(EntityProviderManager entityProviderManager,
            EntityBrokerManager entityBrokerManager) {
        super();
        this.entityProviderManager = entityProviderManager;
        this.entityBrokerManager = entityBrokerManager;
    }

    private EntityProviderManager entityProviderManager;
    public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
        this.entityProviderManager = entityProviderManager;
    }

    private EntityBrokerManager entityBrokerManager;
    public void setEntityBrokerManager(EntityBrokerManager entityBrokerManager) {
        this.entityBrokerManager = entityBrokerManager;
    }


    /**
     * Format and output an entity or collection included or referred to by this entity ref object
     * into output according to the format string provided,
     * Should take into account the reference when determining what the entities are
     * and how to encode them
     * (This is basically a copy of the code in EntityHandlerImpl with stuff removed)
     * 
     * @param ref a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments
     * @param format a string constant indicating the format (from {@link Formats}) 
     * for output, (example: {@link #XML})
     * @param entities (optional) a list of entities to create formatted output for,
     * if this is null then the entities should be retrieved based on the reference,
     * if this contains only a single item AND the ref refers to a single entity
     * then the entity should be extracted from the list and encoded without the indication
     * that it is a collection, for all other cases the encoding should include an indication that
     * this is a list of entities
     * @param outputStream the output stream to place the formatted data in,
     * should be UTF-8 encoded if there is char data
     * @throws FormatUnsupportedException if you do not handle this format type (passes control to the internal handlers)
     * @throws EntityEncodingException if you cannot encode the received data into an entity
     * @throws IllegalArgumentException if any of the arguments are invalid
     * @throws IllegalStateException for all other failures
     */
    public void formatAndOutputEntity(EntityReference ref, String format, List<EntityData> entities, OutputStream outputStream, Map<String, Object> params) {
        if (ref == null || format == null || outputStream == null) {
            throw new IllegalArgumentException("ref, format, and output cannot be null");
        }
        String prefix = ref.getPrefix();
        Outputable outputable = (Outputable) entityProviderManager.getProviderByPrefixAndCapability(prefix, Outputable.class);
        if (outputable != null) {
            String[] outputFormats = outputable.getHandledOutputFormats();
            // check if the output formats are allowed
            if (outputFormats == null || ReflectUtils.contains(outputFormats, format) ) {
                boolean handled = false;

                // if the user wants to serialize their objects specially then allow them to translate them
                OutputSerializable serializable = entityProviderManager.getProviderByPrefixAndCapability(prefix, OutputSerializable.class);
                if (serializable != null) {
                    if (entities == null) {
                        // these will be EntityData
                        entities = entityBrokerManager.getEntitiesData(ref, new Search(), params);
                    }
                    if (! entities.isEmpty()) {
                        // find the type of the objects this providers deals in
                        Object sample = entityBrokerManager.getSampleEntityObject(prefix, null);
                        Class<?> entityType = Object.class;
                        if (sample != null) {
                            entityType = sample.getClass();
                        }
                        // now translate the objects to serialize form
                        for (EntityData entityData : entities) {
                            Object entity = entityData.getData();
                            // only translate if the entity is set
                            if (entity != null) {
                                // only translate if the type matches
                                if (entityType.isAssignableFrom(entity.getClass())) {
                                    try {
                                        entity = serializable.makeSerializableObject(ref, entity);
                                        entityData.setData(entity);
                                    } catch (Exception e) {
                                        throw new RuntimeException("Failure while attempting to serialize the object from ("+entity+") for ref("+ref+"): " + e.getMessage(), e);
                                    }
                                }
                            }
                        }
                    }
                }

                /* try to use the provider formatter if one available,
                 * if it decided not to handle it or none is available then control passes to internal
                 */
                try {
                    OutputFormattable formattable = entityProviderManager.getProviderByPrefixAndCapability(prefix, OutputFormattable.class);
                    if (formattable != null) {
                        // use provider's formatter
                        formattable.formatOutput(ref, format, entities, params, outputStream);
                        handled = true;
                    }
                } catch (FormatUnsupportedException e) {
                    // provider decided not to handle this format
                    handled = false;
                }
                if (!handled) {
                    // handle internally or fail
                    internalOutputFormatter(ref, format, entities, params, outputStream, null);
                }
            } else {
                // format type not handled
                throw new FormatUnsupportedException("Outputable restriction for " 
                        + prefix + " blocked handling this format ("+format+")",
                        ref+"", format);
            }
        }
    }

    /**
     * Translates the input data stream in the supplied format into an entity object for this reference
     * (This is basically a copy of the code in EntityHandlerImpl with stuff removed)
     * 
     * @param ref a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments
     * @param format a string constant indicating the format (from {@link Formats}) 
     * of the input, (example: {@link #XML})
     * @param input a stream which contains the data to make up this entity,
     * you may assume this is UTF-8 encoded if you don't know anything else about it
     * @return an entity object of the type used for the given reference
     * @throws FormatUnsupportedException if you do not handle this format type (passes control to the internal handlers)
     * @throws EntityEncodingException if you cannot encode the received data into an entity
     * @throws IllegalArgumentException if any of the arguments are invalid
     * @throws IllegalStateException for all other failures
     */
    public Object translateInputToEntity(EntityReference ref, String format, InputStream inputStream, Map<String, Object> params) {
        if (ref == null || format == null || inputStream == null) {
            throw new IllegalArgumentException("ref, format, and inputStream cannot be null");
        }
        Object entity = null;
        String prefix = ref.getPrefix();
        Inputable inputable = (Inputable) entityProviderManager.getProviderByPrefixAndCapability(prefix, Inputable.class);
        if (inputable != null) {
            String[] inputFormats = inputable.getHandledInputFormats();
            if (inputFormats == null || ReflectUtils.contains(inputFormats, format) ) {
                boolean handled = false;
                /* try to use the provider translator if one available,
                 * if it decided not to handle it or none is available then control passes to internal
                 */
                try {
                    InputTranslatable translatable = (InputTranslatable) entityProviderManager.getProviderByPrefixAndCapability(prefix, InputTranslatable.class);
                    if (translatable != null) {
                        // use provider's translator
                        entity = translatable.translateFormattedData(ref, format, inputStream, params);
                        handled = true;
                    }
                } catch (FormatUnsupportedException e) {
                    // provider decided not to handle this format
                    handled = false;
                }
                if (!handled) {
                    // use internal translators or fail
                    entity = internalInputTranslator(ref, format, inputStream, null);
                }

                if (entity == null) {
                    // FAILURE input could not be translated into an entity object
                    throw new EntityEncodingException("Unable to translate entity ("+ref+") with format ("
                            +format+"), translated entity object was null", ref+"");
                }
            } else {
                // format type not handled
                throw new FormatUnsupportedException("Inputable restriction for " 
                        + prefix + " blocked handling this format ("+format+")",
                        ref+"", format);
            }
        }
        return entity;
    }

    /**
     * Will attempt to validate that string data is of a specific format
     * @param data a chunk of data to validate
     * @param format the format which the data is supposed encoded in
     * @return true if the data appears valid for the given format, false otherwise
     */
    public boolean validateFormat(String data, String format) {
        // note: this is a weak implementation for now -AZ
        boolean valid = false;
        if (data == null || format == null) {
            throw new IllegalArgumentException("Cannot validate format when the data ("+data+") OR the format ("+format+") are null");
        }
        data = data.trim();
        if (Formats.XML.equals(format)) {
            if (data.startsWith("<") && data.endsWith(">")) {
                valid = true;
            }
        } else if (Formats.JSON.equals(format)) {
            if (data.startsWith("{") && data.endsWith("}")) {
                valid = true;
            }
        } else if (Formats.HTML.equals(format)) {
            if (data.startsWith("<") && data.endsWith(">")) {
                valid = true;
            }
        } else {
            valid = true;
        }
        return valid;
    }


    // FUNCTIONAL CODE BELOW

    /**
     * Handled the internal encoding of data into an entity object
     * 
     * @param ref the entity reference
     * @param format the format which the input is encoded in
     * @param input the data being input
     * @return the entity object based on the data
     * @throws FormatUnsupportedException if you do not handle this format type (passes control to the internal handlers)
     * @throws EntityEncodingException if you cannot encode the received data into an entity
     * @throws IllegalArgumentException if any of the arguments are invalid
     * @throws IllegalStateException for all other failures
     */
    @SuppressWarnings("unchecked")
    public Object internalInputTranslator(EntityReference ref, String format, InputStream input, HttpServletRequest req) {
        Object entity = null;

        Inputable inputable = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), Inputable.class);
        if (inputable != null) {
            // get a the current entity object or a sample
            Object current = entityBrokerManager.getSampleEntityObject(ref.getPrefix(), ref.getId());
            if (current != null) {
                if (Formats.HTML.equals(format) || format == null || "".equals(format)) {
                    // html req handled specially
                    if (req != null) {
                        Map<String, String[]> params = req.getParameterMap();
                        if (params != null && params.size() > 0) {
                            entity = current;
                            try {
                                ReflectUtils.getInstance().populateFromParams(entity, params);
                            } catch (RuntimeException e) {
                                throw new EntityEncodingException("Unable to populate bean for ref ("+ref+") from request: " + e.getMessage(), ref+"", e);
                            }
                        } else {
                            // no request params, bad request
                            throw new EntityException("No request params for html input request (there must be at least one) for reference: " + ref, 
                                    ref.toString(), HttpServletResponse.SC_BAD_REQUEST);
                        }
                    }
                } else {
                    // all other formats
                    if (input == null) {
                        // no request params, bad request
                        throw new EntityException("No input for input translation (input cannot be null) for reference: " + ref, 
                                ref.toString(), HttpServletResponse.SC_BAD_REQUEST);
                    } else {
                        String data = StringUtils.makeStringFromInputStream(input);
                        Map<String, Object> decoded = null;
                        try {
                            decoded = decodeData(data, format);
                        } catch (IllegalArgumentException iae) {
                            throw new EntityEncodingException("No encoder available for the given format ("+format+"), ref=" + ref + ":" + iae.getMessage(), ref.toString(), iae);
                        } catch (UnsupportedOperationException uoe) {
                            throw new EntityEncodingException("Failure during internal input encoding of entity: " + ref + " to format ("+format+"):" + uoe.getMessage(), ref.toString(), uoe);
                        }
                        entity = current;
                        // handle the special case where the JSON was created by xstream or something else that puts the data inside an object with a "root"
                        if (decoded.size() == 1 && decoded.containsKey(ref.getPrefix())) {
                            Object o = decoded.get(ref.getPrefix());
                            if (o instanceof Map) {
                                decoded = (Map<String, Object>) o;
                            }
                        }
                        try {
                            ReflectUtils.getInstance().populate(entity, decoded);
                        } catch (RuntimeException e) {
                            throw new EntityEncodingException("Unable to populate bean for ref ("+ref+") from data: " + decoded + ":" + e.getMessage(), ref+"", e);
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("This entity ("+ref+") does not allow input translation");
        }
        if (entity == null) {
            throw new EntityException("Unable to encode entity from input for reference: " + ref, ref.toString(), HttpServletResponse.SC_BAD_REQUEST);
        }
        return entity;
    }


    /**
     * Format entities for output based on the reference into a format,
     * use the provided list or get the entities
     * 
     * @param ref the entity reference for this, 
     * if this is a reference to a collection then this will be rendered as a collection of entities,
     * if a reference to a single entity then only the matching one from the collection will be used
     * @param format the format to use for the output data
     * @param entities (optional) if this is null then the entities will be fetched
     * @param output the outputstream to place the encoded data into
     * @param view (optional) 
     * @throws FormatUnsupportedException if you do not handle this format type (passes control to the internal handlers)
     * @throws EntityEncodingException if you cannot encode the received data into an entity
     * @throws IllegalArgumentException if any of the arguments are invalid
     * @throws IllegalStateException for all other failures
     */
    public void internalOutputFormatter(EntityReference ref, String format, List<EntityData> entities, Map<String, Object> params, OutputStream output, EntityView view) {
        if (format == null) { format = Outputable.HTML; }

        // check the format to see if we can handle it
        if (! ReflectUtils.contains(HANDLED_OUTPUT_FORMATS, format)) {
            throw new FormatUnsupportedException("Internal output formatter cannot handle format ("+format+") for ref ("+ref+")", ref+"", format);
        }

        if (view == null) {
            view = entityBrokerManager.makeEntityView(ref, null, null);
        }

        // get the entities if not supplied
        if (entities == null) {
            // these will be EntityData
            entities = entityBrokerManager.getEntitiesData(ref, new Search(), params);
        }
        if (entities.isEmpty()) {
            // just log this for now
            log.info("No entities to format ("+format+") and output for ref (" + ref + ")");
        }

        String encoded = null;
        if (EntityView.VIEW_LIST.equals(view.getViewKey()) 
                || ref.getId() == null) {
            // encoding a collection of entities
            StringBuilder sb = new StringBuilder(40);

            // make header
            if (Formats.HTML.equals(format)) {
                sb.append("<h1>"+ref.getPrefix() + COLLECTION + "</h1>\n");
            } else if (Formats.JSON.equals(format)) {
                sb.append("{\""+ENTITY_PREFIX+"\": \""+ref.getPrefix() + "\", \"" + ref.getPrefix() + COLLECTION + "\": [\n");
            } else if (Formats.XML.equals(format)) {
                sb.append("<" + ref.getPrefix() + COLLECTION + " " + ENTITY_PREFIX + "=\"" + ref.getPrefix() + "\">\n");
            } else { // general case
                sb.append(ref.getPrefix() + COLLECTION + "\n");
            }

            // loop through and encode items
            int encodedEntities = 0;
            for (EntityData entity : entities) {
                try {
                    String encode = encodeEntity(ref.getPrefix(), format, entity);
                    if (encode.length() > 3) {
                        if (Formats.JSON.equals(format) 
                                && encodedEntities > 0) {
                            sb.append(",");
                        }
                        sb.append(encode);                     
                        encodedEntities++;
                    }
                } catch (RuntimeException e) {
                    throw new EntityEncodingException("Failure during internal output encoding of entity set on entity: " + ref, ref.toString(), e);
                }
            }

            // make footer
            if (Formats.HTML.equals(format)) {
                sb.append("\n<b>Collection size:</b> "+encodedEntities+"\n");
            } else if (Formats.JSON.equals(format)) {
                sb.append("\n]}");
            } else if (Formats.XML.equals(format)) {
                sb.append("</" + ref.getPrefix() + COLLECTION + ">");
            } else { // general case
                sb.append("\nSize: " + encodedEntities + "\n");
            }
            encoded = sb.toString();
        } else {
            // encoding a single entity
            EntityData toEncode = entities.get(0);
            if (toEncode == null) {
                throw new EntityEncodingException("Failed to encode data for entity (" + ref 
                        + "), entity object to encode could not be found (null object in list)", ref.toString());
            } else {
                try {
                    encoded = encodeEntity(ref.getPrefix(), format, toEncode);
                } catch (RuntimeException e) {
                    throw new EntityEncodingException("Failure during internal output encoding of entity: " + ref, ref.toString(), e);
                }
            }
        }
        // put the encoded data into the stream
        try {
            byte[] b = encoded.getBytes(Formats.UTF_8);
            output.write(b);
        } catch (UnsupportedEncodingException e) {
            throw new EntityEncodingException("Failed to encode UTF-8: " + ref, ref.toString(), e);
        } catch (IOException e) {
            throw new EntityEncodingException("Failed to encode into output stream: " + ref, ref.toString(), e);
        }
    }

    /**
     * Encodes entity data
     * @param prefix the entity prefix related to this data
     * @param format the format to encode the data into
     * @param entityData entity data to encode
     * @return the encoded entity or "" if encoding fails
     */
    public String encodeEntity(String prefix, String format, EntityData entityData) {
        if (prefix == null || format == null || entityData == null) {
            throw new IllegalArgumentException("prefix, format, and entity data to encode must not be null");
        }
        String encoded = "";
        if (Formats.HTML.equals(format)) {
            // special handling for HTML
            StringBuilder sb = new StringBuilder(200);
            sb.append("  <div style='padding-left:1em;'>\n");
            sb.append("    <div style='font-weight:bold;'>"+StringEscapeUtils.escapeHtml(entityData.getDisplayTitle())+"</div>\n");
            sb.append("    <table border='1'>\n");
            sb.append("      <caption style='font-weight:bold;'>Entity Data</caption>\n");
            if (! entityData.isDataOnly()) {
                sb.append("      <tr><td>entityReference</td><td>"+StringEscapeUtils.escapeHtml(entityData.getEntityReference())+"</td></tr>\n");
                sb.append("      <tr><td>entityURL</td><td>"+StringEscapeUtils.escapeHtml(entityData.getEntityURL())+"</td></tr>\n");
                if (entityData.getEntityRef() != null) {
                    sb.append("      <tr><td>entityPrefix</td><td>"+StringEscapeUtils.escapeHtml(entityData.getEntityRef().getPrefix())+"</td></tr>\n");
                    if (entityData.getEntityRef().getId() != null) {
                        sb.append("      <tr><td>entityID</td><td>"+StringEscapeUtils.escapeHtml(entityData.getEntityRef().getId())+"</td></tr>\n");
                    }
                }
            }
            if (entityData.getData() != null) {
                sb.append("      <tr><td>entity-type</td><td>"+entityData.getData().getClass().getName()+"</td></tr>\n");
                // dump entity data
                sb.append("      <tr><td colspan='2'>Data:<br/>\n");
                sb.append( encodeData(entityData.getData(), Formats.HTML, null, null) );
                sb.append("      </td></tr>\n");
            } else {
                sb.append("      <tr><td>entity-object</td><td><i>null</i></td></tr>\n");
            }
            sb.append("    </table>\n");
            Map<String, Object> props = entityData.getEntityProperties();
            if (!props.isEmpty()) {
                sb.append("    <table border='1'>\n");
                sb.append("      <caption style='font-weight:bold;'>Properties</caption>\n");
                for (Entry<String, Object> entry : props.entrySet()) {
                    sb.append("      <tr><td>"+StringEscapeUtils.escapeHtml(entry.getKey())+"</td><td>"+StringEscapeUtils.escapeHtml(entry.getValue().toString())+"</td></tr>\n");
                }
                sb.append("    </table>\n");
            }
            sb.append("  </div>\n");
            encoded = sb.toString();
        } else {
            // encode the entity itself
            Object toEncode = entityData; // default to encoding the entity data object
            Map<String, Object> entityProps = new ArrayOrderedMap<String, Object>();
            if (entityData.getData() != null) {
                if (entityData.isDataOnly()) {
                    toEncode = entityData.getData();
                    // no meta data except properties if there are any
                    entityProps.putAll( entityData.getEntityProperties() );
                } else {
                    if (ConstructorUtils.isClassBean(entityData.getData().getClass())) {
                        // encode the bean directly if it is one
                        toEncode = entityData.getData();
                        // add in the extra props
                        entityProps.put(ENTITY_REFERENCE, entityData.getEntityReference());
                        entityProps.put(ENTITY_URL, entityData.getEntityURL());
                        if (entityData.getEntityRef().getId() != null) {
                            entityProps.put(ENTITY_ID, entityData.getEntityRef().getId());
                        }
                        if (entityData.isDisplayTitleSet()) {
                            entityProps.put(ENTITY_TITLE, entityData.getDisplayTitle());
                        }
                    }
                }
            }
            // do the encoding
            try {
                encoded = encodeData(toEncode, format, prefix, entityProps);
            } catch (IllegalArgumentException e) {
                // no transcoder so just toString this and dump it out
                encoded = prefix + " : " + entityData;
            }
        }
        return encoded;
    }

    protected static final String DATA_KEY = Transcoder.DATA_KEY;

    private Map<String, Transcoder> transcoders;
    public void setTranscoders(Map<String, Transcoder> transcoders) {
        this.transcoders = transcoders;
    }
    /**
     * Override the transcoder used for a specific format
     * @param transcoder a transcoder implementation
     */
    public void setTranscoder(Transcoder transcoder) {
        if (transcoder == null) {
            throw new IllegalArgumentException("transcoder cannot be null");
        }
        if (transcoders == null) {
            getTranscoder(Formats.XML);
        }
        String format = transcoder.getHandledFormat();
        if (format != null && transcoder != null) {
            transcoders.put(format, transcoder);
        }
    }
    public Transcoder getTranscoder(String format) {
        if (transcoders == null) {
            transcoders = new HashMap<String, Transcoder>();
            JSONTranscoder jt = new JSONTranscoder(true, true, false);
            transcoders.put(jt.getHandledFormat(), jt);
            XMLTranscoder xt = new XMLTranscoder(true, true, false, false);
            transcoders.put(xt.getHandledFormat(), xt);
            HTMLTranscoder ht = new HTMLTranscoder();
            transcoders.put(ht.getHandledFormat(), ht);
        }
        Transcoder transcoder = transcoders.get(format);
        if (transcoder == null) {
            throw new IllegalArgumentException("Failed to find a transcoder for format, none exists, cannot encode or decode data for format: " + format);
        }
        return transcoder;
    }

    /**
     * Encode data into a given format, can handle any java object,
     * note that unsupported formats will result in an exception
     * 
     * @param data the data to encode (can be a POJO or Map or pretty much any java object)
     * @param format the format to use for output (from {@link Formats})
     * @param name (optional) the name to use for the encoded data (e.g. root node for XML)
     * @param properties (optional) extra properties to add into the encoding, ignored if encoded object is not a map or bean
     * @return the encoded string
     * @throws UnsupportedOperationException if the data cannot be encoded
     */
    public String encodeData(Object data, String format, String name, Map<String, Object> properties) {
        if (format == null) {
            format = Formats.XML;
        }
        String encoded = "";
        if (data != null) {
            Transcoder transcoder = getTranscoder(format);
            try {
                encoded = transcoder.encode(data, name, properties);
            } catch (RuntimeException e) {
                // convert failure to UOE
                throw new UnsupportedOperationException("Failure encoding data ("+data+") of type ("+data.getClass()+"): " + e.getMessage(), e);
            }
        }
        return encoded;
    }

    /**
     * Decode a string of a specified format into a java map <br/> 
     * Returned map can be fed into the {@link ReflectUtils#populate(Object, Map)} if you want to convert it
     * into a known object type <br/> 
     * Types are likely to require conversion as guesses are made about the right formats,
     * use of the {@link ReflectUtils#convert(Object, Class)} method is recommended
     * 
     * @param data encoded data
     * @param format the format of the encoded data (from {@link Formats})
     * @return a map containing all the data derived from the encoded data
     * @throws UnsupportedOperationException if the data cannot be decoded
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> decodeData(String data, String format) {
        if (format == null) {
            format = Formats.XML;
        }
        Map<String, Object> decoded = new ArrayOrderedMap<String, Object>();
        if (data != null && ! "".equals(data)) {
            Object decode = null;
            Transcoder transcoder = getTranscoder(format);
            try {
                decode = transcoder.decode(data);
                if (decode instanceof Map) {
                    decoded = (Map<String, Object>) decode;
                } else {
                    decoded.put(DATA_KEY, decode);
                }
            } catch (RuntimeException e) {
                // convert failure to UOE
                throw new UnsupportedOperationException("Failure decoding data ("+data+") for format ("+format+"): " + e.getMessage(), e);
            }
        }
        return decoded;
    }

    /**
     * Using GSON is hopeless:
     * http://code.google.com/p/google-gson/issues/detail?id=45
     * 
     * Gson gson = getGson();
     * encoded = gson.toJson(data, new TypeToken<Map<String, Object>>() {}.getType());
     * decoded = gson.fromJson(data, new TypeToken<Map<String, Object>>() {}.getType());
     */
    //    protected SoftReference<Gson> gsonCoder = null;
    //    /**
    //     * Get the gson encoder in an efficient way to avoid recreating it over and over and over again
    //     * @return the gson encoder
    //     */
    //    protected Gson getGson() {
    //        Gson gson = gsonCoder == null ? null : gsonCoder.get();
    //        if (gson == null) {
    //            gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
    //            gsonCoder = new SoftReference<Gson>(gson);
    //        }
    //        return gson;
    //    }

}

