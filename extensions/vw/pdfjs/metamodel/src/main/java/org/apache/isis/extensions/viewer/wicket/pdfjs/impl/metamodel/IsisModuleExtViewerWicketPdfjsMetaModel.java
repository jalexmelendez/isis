package org.apache.isis.extensions.viewer.wicket.pdfjs.impl.metamodel;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.viewer.wicket.pdfjs.impl.metamodel.facet.PdfJsViewerFacetFromAnnotationFactory;

@Configuration
@Import({
        // @Component's
        PdfJsViewerFacetFromAnnotationFactory.Register.class
})
public class IsisModuleExtViewerWicketPdfjsMetaModel {
}