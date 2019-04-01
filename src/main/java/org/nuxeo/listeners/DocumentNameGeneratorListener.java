package org.nuxeo.listeners;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.features.PlatformFunctions;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class DocumentNameGeneratorListener implements EventListener {

    protected static final String SEQUENCER_KEY = "SUPNXP-25839";

    protected static final String SYSTEM_USER = "system";

    protected static final Object DOC_TYPE = "CustomDocumentTypeHere";
    
    private static final Log log = LogFactory.getLog(DocumentNameGeneratorListener.class);

    @Override
    public void handleEvent(Event event) {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }
        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();
        if (!DOC_TYPE.equals(doc.getDocumentType())) {
            return;
        }
        if (doc.isProxy() || doc.isVersion()) {
            return;
        }
//        if (SYSTEM_USER.equals(ctx.getPrincipal().getName())) {
//            return;
//        }
//        if (doc.hasFacet("HiddenInNavigation")) {
//            return;
//        }
        String name = "document-" + new PlatformFunctions().getNextId(SEQUENCER_KEY);
        if (log.isDebugEnabled()) {
            log.debug("generated document name: " + name);
        }
        ctx.setProperty(CoreEventConstants.DESTINATION_NAME, name);
    }
}
