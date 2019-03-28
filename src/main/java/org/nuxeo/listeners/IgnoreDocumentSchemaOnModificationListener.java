package org.nuxeo.listeners;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class IgnoreDocumentSchemaOnModificationListener implements EventListener {

    protected static final String DOC_TYPE = "SUPNXP-21143";

    protected static final String SCHEMA_NAME = "supnxp-21143_drc";
  
    private static final Log LOGGER = LogFactory.getLog(IgnoreDocumentSchemaOnModificationListener.class);

    @Override
    public void handleEvent(Event event) {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
          return;
        }
        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();
        // ignore documents of type other than 'SUPNXP-21143'
        if (!DOC_TYPE.equals(doc.getType())) {
            return;
        }
        LOGGER.debug("<handleEvent> " + doc.getPathAsString());
        doc.getDataModels().remove(SCHEMA_NAME);
        LOGGER.debug("<handleEvent> schema '" + SCHEMA_NAME + "' ignored");
    }
}
