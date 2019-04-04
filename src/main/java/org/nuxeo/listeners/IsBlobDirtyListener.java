package org.nuxeo.listeners;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class IsBlobDirtyListener implements EventListener {
  
    private static final Log log = LogFactory.getLog(IsBlobDirtyListener.class);

    @Override
    public void handleEvent(Event event) {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
          return;
        }

        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        Boolean isDirty = (Boolean) docCtx.getProperty(CoreEventConstants.DOCUMENT_DIRTY);
        DocumentModel doc = docCtx.getSourceDocument();
//        log.warn(doc.getPathAsString() + " is dirty: " + isDirty + " " + doc.hasSchema("File") + " " + doc.getSchemas());
        if (doc.hasSchema("file")) {
            Property blobProp = doc.getProperty("file:content");
            if (blobProp != null && blobProp.isDirty()) {
                log.warn("file:content property is dirty for " + doc.getPathAsString());
            }
        }
    }
}
