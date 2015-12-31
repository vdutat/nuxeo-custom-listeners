/*
 *
 * Contributors:
 *     vdutat
 */

package org.nuxeo.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.InvalidChainException;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.api.model.DocumentPart;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;


/**
 * 
 */
public class DirtyPropListener implements EventListener {

    private static final Log LOGGER = LogFactory.getLog(DirtyPropListener.class);
    
    protected final List<String> docTypes = Arrays.asList(StringUtils.split(Framework.getProperty("nuxeo.listener.dirtyproperties.doctypes", ""), ","));
    
    public void handleEvent(Event event) throws ClientException {
        if (!DocumentEventTypes.BEFORE_DOC_UPDATE.equals(event.getName()) && !DocumentEventTypes.ABOUT_TO_CREATE.equals(event.getName())) {
            return;
        }
        if (event.getContext() instanceof DocumentEventContext) {
            DocumentEventContext context = (DocumentEventContext) event.getContext();
            DocumentModel doc = context.getSourceDocument();
            if (!docTypes.contains(doc.getType())) {
                return;
            }
            List<String> configDirtyPropNames = getDirtyPropNames(doc.getType());
            if (configDirtyPropNames.isEmpty()) {
                return;
            }
            List<String> lastDirtyFields = getDirtyPropertiesXPath(doc);
            if (LOGGER.isDebugEnabled()) {
                for (String xpath : lastDirtyFields) {
                    LOGGER.debug(xpath);
                }
            }
            Iterator<String> it = lastDirtyFields.iterator();
            while(it.hasNext()) {
                String propName = it.next();
                if (!configDirtyPropNames.contains(propName)) {
                    it.remove();
                }
            }
            if (!lastDirtyFields.isEmpty()) {
                String chainId = Framework.getProperty("nuxeo.listener.dirtyproperties. " + doc.getType() + ".chain");
                if (chainId != null) {
                    OperationContext ctx = new OperationContext();
                    ctx.put("dirtyFields", lastDirtyFields);
                    ctx.setInput(doc);
                    AutomationService as = Framework.getService(AutomationService.class);
                    try {
                        as.run(ctx, chainId);
                    } catch (InvalidChainException e) {
                        LOGGER.error(e, e);
                    } catch (OperationException e) {
                        LOGGER.error(e, e);
                    } catch (Exception e) {
                        LOGGER.error(e, e);
                   }
                } else {
                    LOGGER.warn("No automation chain configured for document type '" + doc.getType() + "' (configure 'nuxeo.listener.dirtyproperties. " + doc.getType() + ".chain' in 'nuxeo.conf' file)");
                }
            }
        }
    }    

    protected List<String> getDirtyPropertiesXPath(DocumentModel doc)
            throws ClientException {
        List<String> dirtyPropertiesName = new ArrayList<String>();
        DocumentPart[] docParts = doc.getParts();
        for (DocumentPart docPart : docParts) {
            Iterator<Property> dirtyChildrenIterator = docPart.getDirtyChildren();
            while (dirtyChildrenIterator.hasNext()) {
                Property property = dirtyChildrenIterator.next();
                if (!property.isContainer() && property.isDirty()) {
                    dirtyPropertiesName.add(docPart.getName() + ":"
                            + property.getField().getName().getLocalName());
                } else {
                    List<Property> dirtyProps = addChildrenDirtyProperties(
                            property, new ArrayList<Property>());
                    for (Property dirtyProperty : dirtyProps) {
                        dirtyPropertiesName.add(docPart.getName() + ":"
                                + dirtyProperty.getPath().substring(1));
                    }
                }
            }
        }
        return dirtyPropertiesName;
    }

    protected List<String> getDirtyPropNames(String docType) {
        String[] propNames = StringUtils.split(Framework.getProperty("nuxeo.listener.dirtyproperties. " + docType + ".metadata", ""), ",");
        return Arrays.asList(propNames);
    }
    
    private List<Property> addChildrenDirtyProperties(Property property,
            List<Property> dirtyProperties) {
        if (!property.isContainer() && property.isDirty()) {
            dirtyProperties.add(property);
            return dirtyProperties;
        } else {
            Iterator<Property> dirtyChildrenIterator = property.getDirtyChildren();
            while (dirtyChildrenIterator.hasNext()) {
                Property chilProperty = dirtyChildrenIterator.next();
                dirtyProperties = addChildrenDirtyProperties(chilProperty,
                        dirtyProperties);
            }
            return dirtyProperties;
        }
    }
}
