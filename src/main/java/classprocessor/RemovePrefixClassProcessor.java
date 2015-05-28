package classprocessor;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

/**
 * An subclass of {@link ClassProcessor}.
 * <p>
 * This class uses {@link RemovePrefixClassNameResolver} as the default implementation of
 * {@link ClassNameResolver}.
 *
 * @author Tsutomu YANO
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes({"shelfmap.classprocessor.annotation.GenerateClass"})
public class RemovePrefixClassProcessor extends ClassProcessor {

    public RemovePrefixClassProcessor() {
        super();
    }

    @Override
    protected ClassNameResolver getDefaultImplementationOfClassNameResolver() {
        return new RemovePrefixClassNameResolver();
    }
}
